package com.chunkvault.storage;

public interface StorageNode {
    String getName();
    void writeChunk(String chunkId, byte[] data);
    byte[] readChunk(String chunkId);
    boolean exists(String chunkId);
    void deleteChunk(String chunkId);
}
