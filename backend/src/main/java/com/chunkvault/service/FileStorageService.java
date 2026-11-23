package com.chunkvault.service;

import com.chunkvault.config.StorageProperties;
import com.chunkvault.exception.FileNotFoundException;
import com.chunkvault.exception.InvalidUploadException;
import com.chunkvault.model.*;
import com.chunkvault.storage.StorageNode;
import com.chunkvault.storage.StorageNodeRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    private final ChunkingService chunkingService;
    private final StorageNodeRouter router;
    private final MetadataStore metadataStore;
    private final StorageProperties props;

    public FileStorageService(ChunkingService chunkingService,
                              StorageNodeRouter router,
                              MetadataStore metadataStore,
                              StorageProperties props) {
        this.chunkingService = chunkingService;
        this.router = router;
        this.metadataStore = metadataStore;
        this.props = props;
    }

    public UploadResponse upload(MultipartFile file) {
        if (file == null) {
            throw new InvalidUploadException("Uploaded file is missing");
        }
        String originalName = sanitizeFilename(file.getOriginalFilename());

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new InvalidUploadException("Could not read uploaded file: " + e.getMessage());
        }

        String fileId = "F-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        String fileChecksum = ChecksumUtil.sha256Hex(bytes);

        List<byte[]> chunkBytes = chunkingService.split(bytes);
        int copies = Math.max(1, props.getReplicationFactor());

        List<ChunkMetadata> metas = new ArrayList<>();
        List<StorageNode> writtenNodes = new ArrayList<>();
        List<String> writtenChunkIds = new ArrayList<>();

        try {
            for (int i = 0; i < chunkBytes.size(); i++) {
                byte[] data = chunkBytes.get(i);
                String chunkId = fileId + "-C" + String.format("%04d", i);
                String checksum = ChecksumUtil.sha256Hex(data);

                List<StorageNode> targets = router.nodesForChunk(i, copies);
                StorageNode primary = targets.get(0);
                List<String> replicaNames = new ArrayList<>();

                primary.writeChunk(chunkId, data);
                writtenNodes.add(primary);
                writtenChunkIds.add(chunkId);

                for (int r = 1; r < targets.size(); r++) {
                    StorageNode replica = targets.get(r);
                    replica.writeChunk(chunkId, data);
                    writtenNodes.add(replica);
                    writtenChunkIds.add(chunkId);
                    replicaNames.add(replica.getName());
                }

                metas.add(new ChunkMetadata(chunkId, fileId, i, primary.getName(),
                        data.length, checksum, replicaNames));
            }

            FileMetadata metadata = new FileMetadata(fileId, originalName, bytes.length, metas);
            metadata.setChecksum(fileChecksum);
            metadata.setContentType(file.getContentType());
            metadataStore.save(metadata);
        } catch (RuntimeException e) {
            cleanupWrittenChunks(writtenNodes, writtenChunkIds);
            throw e;
        }

        List<ChunkInfo> chunkInfos = new ArrayList<>();
        for (ChunkMetadata m : metas) {
            chunkInfos.add(new ChunkInfo(m.getChunkId(), m.getSequenceNumber(), m.getStorageNode(),
                    m.getSize(), m.getReplicas()));
        }

        return new UploadResponse(
                fileId,
                originalName,
                bytes.length,
                metas.size(),
                chunkingService.chunkSize(),
                chunkInfos
        );
    }

    public FileMetadata getMetadata(String fileId) {
        FileMetadata meta = metadataStore.get(fileId);
        if (meta == null) {
            throw new FileNotFoundException("No file found with id: " + fileId);
        }
        return meta;
    }

    public byte[] reconstruct(String fileId) {
        FileMetadata meta = getMetadata(fileId);

        List<ChunkMetadata> ordered = new ArrayList<>(meta.getChunks());
        ordered.sort(Comparator.comparingInt(ChunkMetadata::getSequenceNumber));

        ByteArrayOutputStream out = new ByteArrayOutputStream((int) Math.min(meta.getFileSize(), Integer.MAX_VALUE));

        for (ChunkMetadata cm : ordered) {
            byte[] data = readChunkWithFailover(cm);
            out.writeBytes(data);
        }

        byte[] result = out.toByteArray();
        if (result.length != meta.getFileSize()) {
            throw new IllegalStateException(
                    "Reconstructed size mismatch for " + fileId +
                            " expected=" + meta.getFileSize() + " actual=" + result.length);
        }
        if (meta.getChecksum() != null && !ChecksumUtil.matches(meta.getChecksum(), result)) {
            throw new IllegalStateException(
                    "Reconstructed file checksum mismatch for " + fileId);
        }
        return result;
    }

    /**
     * Reads a chunk trying the primary node first, then each replica.
     * A copy is accepted only if its SHA-256 checksum matches the metadata.
     */
    private byte[] readChunkWithFailover(ChunkMetadata cm) {
        List<String> candidates = new ArrayList<>();
        candidates.add(cm.getStorageNode());
        candidates.addAll(cm.getReplicas());

        List<String> failures = new ArrayList<>();
        for (String nodeName : candidates) {
            StorageNode node;
            try {
                node = router.nodeByName(nodeName);
            } catch (IllegalArgumentException e) {
                failures.add(nodeName + " (unknown node)");
                continue;
            }
            try {
                byte[] data = node.readChunk(cm.getChunkId());
                if (!ChecksumUtil.matches(cm.getChecksum(), data)) {
                    failures.add(nodeName + " (checksum mismatch)");
                    continue;
                }
                if (data.length != cm.getSize()) {
                    failures.add(nodeName + " (size mismatch)");
                    continue;
                }
                return data;
            } catch (RuntimeException e) {
                failures.add(nodeName + " (" + e.getMessage() + ")");
            }
        }
        throw new com.chunkvault.exception.ChunkMissingException(
                "No readable copy of chunk " + cm.getChunkId() + " for file " + cm.getFileId()
                        + ". Tried: " + String.join(", ", failures));
    }

    public void delete(String fileId) {
        FileMetadata meta = getMetadata(fileId);

        for (ChunkMetadata cm : meta.getChunks()) {
            List<String> targets = new ArrayList<>();
            targets.add(cm.getStorageNode());
            targets.addAll(cm.getReplicas());
            for (String nodeName : targets) {
                try {
                    router.nodeByName(nodeName).deleteChunk(cm.getChunkId());
                } catch (RuntimeException e) {
                    log.warn("Failed to delete chunk {} on {}: {}", cm.getChunkId(), nodeName, e.getMessage());
                }
            }
        }

        metadataStore.delete(fileId);
    }

    public List<FileMetadata> getAllFiles() {
        return metadataStore.getAll();
    }

    private void cleanupWrittenChunks(List<StorageNode> nodes, List<String> chunkIds) {
        for (int i = 0; i < nodes.size(); i++) {
            try {
                nodes.get(i).deleteChunk(chunkIds.get(i));
            } catch (RuntimeException e) {
                log.warn("Cleanup failed for chunk {} on {}: {}",
                        chunkIds.get(i), nodes.get(i).getName(), e.getMessage());
            }
        }
    }

    private String sanitizeFilename(String name) {
        if (name == null || name.isBlank()) {
            return "unnamed.bin";
        }
        String cleaned = name.replace('\\', '/');
        int slash = cleaned.lastIndexOf('/');
        if (slash >= 0) {
            cleaned = cleaned.substring(slash + 1);
        }
        cleaned = cleaned.replaceAll("[\\x00-\\x1F\\x7F]", "").trim();
        if (cleaned.isBlank() || cleaned.equals(".") || cleaned.equals("..")) {
            return "unnamed.bin";
        }
        if (cleaned.length() > 255) {
            cleaned = cleaned.substring(cleaned.length() - 255);
        }
        return cleaned;
    }
}
