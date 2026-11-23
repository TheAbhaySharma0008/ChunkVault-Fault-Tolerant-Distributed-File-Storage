package com.chunkvault.model;

import java.util.ArrayList;
import java.util.List;

public class ChunkMetadata {
    private String chunkId;
    private String fileId;
    private int sequenceNumber;
    private String storageNode;
    private long size;
    private String checksum;
    private List<String> replicas = new ArrayList<>();

    public ChunkMetadata() {}

    public ChunkMetadata(String chunkId, String fileId, int sequenceNumber, String storageNode, long size) {
        this(chunkId, fileId, sequenceNumber, storageNode, size, null, new ArrayList<>());
    }

    public ChunkMetadata(String chunkId, String fileId, int sequenceNumber, String storageNode,
                         long size, String checksum, List<String> replicas) {
        this.chunkId = chunkId;
        this.fileId = fileId;
        this.sequenceNumber = sequenceNumber;
        this.storageNode = storageNode;
        this.size = size;
        this.checksum = checksum;
        this.replicas = replicas == null ? new ArrayList<>() : replicas;
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

    public String getChecksum() { return checksum; }
    public void setChecksum(String checksum) { this.checksum = checksum; }

    public List<String> getReplicas() { return replicas; }
    public void setReplicas(List<String> replicas) { this.replicas = replicas; }
}
