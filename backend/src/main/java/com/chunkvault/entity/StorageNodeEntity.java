package com.chunkvault.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "storage_nodes")
public class StorageNodeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String nodeName;

    @Column(nullable = false)
    private String status = "ACTIVE";

    @Column(nullable = false)
    private String storagePath;

    @Column
    private Long chunkCount = 0L;

    @Column
    private Long storageUsed = 0L;

    @Column
    private LocalDateTime lastHealthCheck;

    public StorageNodeEntity() {}

    public StorageNodeEntity(String nodeName, String storagePath) {
        this.nodeName = nodeName;
        this.storagePath = storagePath;
        this.lastHealthCheck = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNodeName() { return nodeName; }
    public void setNodeName(String nodeName) { this.nodeName = nodeName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }

    public Long getChunkCount() { return chunkCount; }
    public void setChunkCount(Long chunkCount) { this.chunkCount = chunkCount; }

    public Long getStorageUsed() { return storageUsed; }
    public void setStorageUsed(Long storageUsed) { this.storageUsed = storageUsed; }

    public LocalDateTime getLastHealthCheck() { return lastHealthCheck; }
    public void setLastHealthCheck(LocalDateTime lastHealthCheck) { this.lastHealthCheck = lastHealthCheck; }
}
