package com.chunkvault.storage;

import com.chunkvault.exception.ChunkMissingException;
import com.chunkvault.exception.NodeUnavailableException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LocalFileSystemStorageNode implements StorageNode {

    private final String name;
    private final Path rootDir;
    private volatile boolean available = true;

    public LocalFileSystemStorageNode(String name, Path rootDir) {
        this.name = name;
        this.rootDir = rootDir;
        try {
            Files.createDirectories(rootDir);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create storage node directory: " + rootDir, e);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public void setAvailable(boolean available) {
        this.available = available;
    }

    private void requireAvailable() {
        if (!available) {
            throw new NodeUnavailableException("Storage node " + name + " is currently unavailable");
        }
    }

    private Path chunkPath(String chunkId) {
        if (chunkId == null || chunkId.isBlank()
                || chunkId.contains("..") || chunkId.contains("/") || chunkId.contains("\\")) {
            throw new IllegalArgumentException("Invalid chunk id: " + chunkId);
        }
        return rootDir.resolve(chunkId + ".chunk");
    }

    @Override
    public void writeChunk(String chunkId, byte[] data) {
        requireAvailable();
        Path file = chunkPath(chunkId);
        try {
            Files.write(file, data);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write chunk " + chunkId + " on " + name, e);
        }
    }

    @Override
    public byte[] readChunk(String chunkId) {
        requireAvailable();
        Path file = chunkPath(chunkId);
        if (!Files.exists(file)) {
            throw new ChunkMissingException("Chunk " + chunkId + " missing on " + name);
        }
        try {
            return Files.readAllBytes(file);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read chunk " + chunkId + " on " + name, e);
        }
    }

    @Override
    public boolean exists(String chunkId) {
        try {
            return available && Files.exists(chunkPath(chunkId));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public void deleteChunk(String chunkId) {
        try {
            Files.deleteIfExists(chunkPath(chunkId));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to delete chunk " + chunkId + " on " + name, e);
        }
    }
}
