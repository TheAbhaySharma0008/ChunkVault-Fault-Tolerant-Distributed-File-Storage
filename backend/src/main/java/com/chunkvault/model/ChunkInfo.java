package com.chunkvault.model;

import java.util.ArrayList;
import java.util.List;

public class ChunkInfo {
    private String chunkId;
    private int sequenceNumber;
    private String storageNode;
    private long size;
    private List<String> replicas = new ArrayList<>();

    public ChunkInfo() {}

    public ChunkInfo(String chunkId, int sequenceNumber, String storageNode, long size) {
        this(chunkId, sequenceNumber, storageNode, size, new ArrayList<>());
    }

    public ChunkInfo(String chunkId, int sequenceNumber, String storageNode,
                     long size, List<String> replicas) {
        this.chunkId = chunkId;
        this.sequenceNumber = sequenceNumber;
        this.storageNode = storageNode;
        this.size = size;
        this.replicas = replicas == null ? new ArrayList<>() : replicas;
    }

    public String getChunkId() { return chunkId; }
    public void setChunkId(String chunkId) { this.chunkId = chunkId; }

    public int getSequenceNumber() { return sequenceNumber; }
    public void setSequenceNumber(int sequenceNumber) { this.sequenceNumber = sequenceNumber; }

    public String getStorageNode() { return storageNode; }
    public void setStorageNode(String storageNode) { this.storageNode = storageNode; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }

    public List<String> getReplicas() { return replicas; }
    public void setReplicas(List<String> replicas) { this.replicas = replicas; }
}
