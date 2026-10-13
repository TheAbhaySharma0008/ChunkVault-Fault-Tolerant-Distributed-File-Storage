package com.chunkvault.service;

import com.chunkvault.config.StorageProperties;
import com.chunkvault.exception.FileNotFoundException;
import com.chunkvault.exception.InvalidUploadException;
import com.chunkvault.model.*;
import com.chunkvault.storage.StorageNode;
import com.chunkvault.storage.StorageNodeRouter;
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
        if (file == null || file.isEmpty()) {
            throw new InvalidUploadException("Uploaded file is empty or missing");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            originalName = "unnamed.bin";
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new InvalidUploadException("Could not read uploaded file: " + e.getMessage());
        }

        String fileId = "F-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();

        List<byte[]> chunkBytes = chunkingService.split(bytes);

        List<ChunkMetadata> metas = new ArrayList<>();
        for (int i = 0; i < chunkBytes.size(); i++) {
            byte[] data = chunkBytes.get(i);
            StorageNode node = router.nodeForSequence(i);
            String chunkId = fileId + "-C" + String.format("%04d", i);

            node.writeChunk(chunkId, data);

            metas.add(new ChunkMetadata(chunkId, fileId, i, node.getName(), data.length));
        }

        FileMetadata metadata = new FileMetadata(fileId, originalName, bytes.length, metas);
        metadataStore.save(metadata);

        List<ChunkInfo> chunkInfos = new ArrayList<>();
        for (ChunkMetadata m : metas) {
            chunkInfos.add(new ChunkInfo(m.getChunkId(), m.getSequenceNumber(), m.getStorageNode(), m.getSize()));
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
            StorageNode node = router.nodeByName(cm.getStorageNode());
            byte[] data = node.readChunk(cm.getChunkId());
            out.writeBytes(data);
        }

        byte[] result = out.toByteArray();
        if (result.length != meta.getFileSize()) {
            throw new IllegalStateException(
                    "Reconstructed size mismatch for " + fileId +
                            " expected=" + meta.getFileSize() + " actual=" + result.length);
        }
        return result;
    }

    public void delete(String fileId) {
        FileMetadata meta = getMetadata(fileId);

        for (ChunkMetadata cm : meta.getChunks()) {
            StorageNode node = router.nodeByName(cm.getStorageNode());
            node.deleteChunk(cm.getChunkId());
        }

        metadataStore.delete(fileId);
    }

    public List<FileMetadata> getAllFiles() {
        return metadataStore.getAll();
    }
}
