package com.chunkvault.model;

import java.util.List;

public class FileMetadata {
    private String fileId;
    private String fileName;
    private long fileSize;
    private List<ChunkMetadata> chunks;

    public FileMetadata() {}

    public FileMetadata(String fileId, String fileName, long fileSize, List<ChunkMetadata> chunks) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.chunks = chunks;
    }

    public String getFileId() { return fileId; }
    public void setFileId(String fileId) { this.fileId = fileId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public List<ChunkMetadata> getChunks() { return chunks; }
    public void setChunks(List<ChunkMetadata> chunks) { this.chunks = chunks; }
}
