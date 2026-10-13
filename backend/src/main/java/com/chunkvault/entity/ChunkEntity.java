package com.chunkvault.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chunks")
public class ChunkEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String chunkId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private FileEntity file;

    @Column(nullable = false)
    private Integer sequenceNumber;

    @Column(nullable = false)
    private Long chunkSize;

    @Column(nullable = false)
    private String storageNode;

    @Column(nullable = false)
    private String status = "STORED";

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public ChunkEntity() {}

    public ChunkEntity(String chunkId, FileEntity file, Integer sequenceNumber, 
                      Long chunkSize, String storageNode) {
        this.chunkId = chunkId;
        this.file = file;
        this.sequenceNumber = sequenceNumber;
        this.chunkSize = chunkSize;
        this.storageNode = storageNode;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getChunkId() { return chunkId; }
    public void setChunkId(String chunkId) { this.chunkId = chunkId; }

    public FileEntity getFile() { return file; }
    public void setFile(FileEntity file) { this.file = file; }

    public Integer getSequenceNumber() { return sequenceNumber; }
    public void setSequenceNumber(Integer sequenceNumber) { this.sequenceNumber = sequenceNumber; }

    public Long getChunkSize() { return chunkSize; }
    public void setChunkSize(Long chunkSize) { this.chunkSize = chunkSize; }

    public String getStorageNode() { return storageNode; }
    public void setStorageNode(String storageNode) { this.storageNode = storageNode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
