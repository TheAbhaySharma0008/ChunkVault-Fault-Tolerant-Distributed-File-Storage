package com.chunkvault.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "files")
public class FileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String fileId;

    @Column(nullable = false)
    private String originalFilename;

    @Column(nullable = false)
    private Long fileSize;

    @Column
    private String contentType;

    @Column(nullable = false)
    private Integer totalChunks;

    @Column(nullable = false)
    private String status = "ACTIVE";

    @Column(nullable = false)
    private LocalDateTime uploadTimestamp;

    @OneToMany(mappedBy = "file", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChunkEntity> chunks = new ArrayList<>();

    public FileEntity() {}

    public FileEntity(String fileId, String originalFilename, Long fileSize, 
                      String contentType, Integer totalChunks) {
        this.fileId = fileId;
        this.originalFilename = originalFilename;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.totalChunks = totalChunks;
        this.uploadTimestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFileId() { return fileId; }
    public void setFileId(String fileId) { this.fileId = fileId; }

    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Integer getTotalChunks() { return totalChunks; }
    public void setTotalChunks(Integer totalChunks) { this.totalChunks = totalChunks; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getUploadTimestamp() { return uploadTimestamp; }
    public void setUploadTimestamp(LocalDateTime uploadTimestamp) { this.uploadTimestamp = uploadTimestamp; }

    public List<ChunkEntity> getChunks() { return chunks; }
    public void setChunks(List<ChunkEntity> chunks) { this.chunks = chunks; }
}
