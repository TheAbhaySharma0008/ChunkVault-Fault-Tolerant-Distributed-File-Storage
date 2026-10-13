package com.chunkvault.model;

import java.util.List;

public class UploadResponse {
    private String fileId;
    private String fileName;
    private long fileSize;
    private int chunkCount;
    private long chunkSizeBytes;
    private List<ChunkInfo> chunks;

    public UploadResponse() {}

    public UploadResponse(String fileId, String fileName, long fileSize, int chunkCount,
                          long chunkSizeBytes, List<ChunkInfo> chunks) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.chunkCount = chunkCount;
        this.chunkSizeBytes = chunkSizeBytes;
        this.chunks = chunks;
    }

    public String getFileId() { return fileId; }
    public void setFileId(String fileId) { this.fileId = fileId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public int getChunkCount() { return chunkCount; }
    public void setChunkCount(int chunkCount) { this.chunkCount = chunkCount; }

    public long getChunkSizeBytes() { return chunkSizeBytes; }
    public void setChunkSizeBytes(long chunkSizeBytes) { this.chunkSizeBytes = chunkSizeBytes; }

    public List<ChunkInfo> getChunks() { return chunks; }
    public void setChunks(List<ChunkInfo> chunks) { this.chunks = chunks; }
}
