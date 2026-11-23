package com.chunkvault.service;

import com.chunkvault.entity.ChunkEntity;
import com.chunkvault.entity.FileEntity;
import com.chunkvault.model.ChunkMetadata;
import com.chunkvault.model.FileMetadata;
import com.chunkvault.repository.ChunkRepository;
import com.chunkvault.repository.FileRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Database-backed metadata store for Phase 2.
 * Replaces Phase 1 in-memory implementation with MySQL/JPA.
 */
@Service
public class MetadataStore {

    private final FileRepository fileRepository;
    private final ChunkRepository chunkRepository;

    public MetadataStore(FileRepository fileRepository, ChunkRepository chunkRepository) {
        this.fileRepository = fileRepository;
        this.chunkRepository = chunkRepository;
    }

    @Transactional
    public void save(FileMetadata metadata) {
        FileEntity fileEntity = fileRepository.findByFileId(metadata.getFileId())
                .orElse(new FileEntity(
                        metadata.getFileId(),
                        metadata.getFileName(),
                        metadata.getFileSize(),
                        null,
                        metadata.getChunks().size()
                ));

        fileEntity.setOriginalFilename(metadata.getFileName());
        fileEntity.setFileSize(metadata.getFileSize());
        fileEntity.setTotalChunks(metadata.getChunks().size());
        fileEntity.setChecksum(metadata.getChecksum());
        if (metadata.getContentType() != null) {
            fileEntity.setContentType(metadata.getContentType());
        }

        fileEntity.getChunks().clear();
        for (ChunkMetadata cm : metadata.getChunks()) {
            String replicaNodes = cm.getReplicas() == null || cm.getReplicas().isEmpty()
                    ? null
                    : String.join(",", cm.getReplicas());
            ChunkEntity chunkEntity = new ChunkEntity(
                    cm.getChunkId(),
                    fileEntity,
                    cm.getSequenceNumber(),
                    cm.getSize(),
                    cm.getStorageNode(),
                    cm.getChecksum(),
                    replicaNodes
            );
            fileEntity.getChunks().add(chunkEntity);
        }

        fileRepository.save(fileEntity);
    }

    @Transactional
    public FileMetadata get(String fileId) {
        return fileRepository.findByFileId(fileId)
                .map(this::toFileMetadata)
                .orElse(null);
    }

    public boolean contains(String fileId) {
        return fileRepository.existsByFileId(fileId);
    }

    @Transactional
    public void delete(String fileId) {
        chunkRepository.deleteByFileFileId(fileId);
        fileRepository.deleteByFileId(fileId);
    }

    @Transactional
    public List<FileMetadata> getAll() {
        return fileRepository.findAll().stream()
                .map(this::toFileMetadata)
                .collect(Collectors.toList());
    }

    private FileMetadata toFileMetadata(FileEntity entity) {
        List<ChunkMetadata> chunkMetas = entity.getChunks().stream()
                .map(this::toChunkMetadata)
                .collect(Collectors.toList());

        FileMetadata meta = new FileMetadata(
                entity.getFileId(),
                entity.getOriginalFilename(),
                entity.getFileSize(),
                chunkMetas
        );
        meta.setChecksum(entity.getChecksum());
        meta.setContentType(entity.getContentType());
        meta.setUploadTimestamp(entity.getUploadTimestamp());
        meta.setStatus(entity.getStatus());
        return meta;
    }

    private ChunkMetadata toChunkMetadata(ChunkEntity entity) {
        List<String> replicas = entity.getReplicaNodes() == null || entity.getReplicaNodes().isBlank()
                ? new ArrayList<>()
                : List.of(entity.getReplicaNodes().split(","));
        return new ChunkMetadata(
                entity.getChunkId(),
                entity.getFile().getFileId(),
                entity.getSequenceNumber(),
                entity.getStorageNode(),
                entity.getChunkSize(),
                entity.getChecksum(),
                replicas
        );
    }
}
