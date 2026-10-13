package com.chunkvault.model;

public class ChunkMetadata {
    private String chunkId;
    private String fileId;
    private int sequenceNumber;
    private String storageNode;
    private long size;

    public ChunkMetadata() {}

    public ChunkMetadata(String chunkId, String fileId, int sequenceNumber, String storageNode, long size) {
        this.chunkId = chunkId;
        this.fileId = fileId;
        this.sequenceNumber = sequenceNumber;
        this.storageNode = storageNode;
        this.size = size;
    }

    public String getChunkId() { return chunkId; }
    public void setChunkId(String chunkId) { this.chunkId = chunkId; }

    public String getFileId() { return fileId; }
    public void setFileId(String fileId) { this.fileId = fileId; }

    public int getSequenceNumber() { return sequenceNumber; }
    public void setSequenceNumber(int sequenceNumber) { this.sequenceNumber = sequenceNumber; }

    public String getStorageNode() { return storageNode; }
    public void setStorageNode(String storageNode) { this.storageNode = storageNode; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }
}
