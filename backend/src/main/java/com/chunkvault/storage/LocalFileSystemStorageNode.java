package com.chunkvault.storage;

import com.chunkvault.exception.ChunkMissingException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LocalFileSystemStorageNode implements StorageNode {

    private final String name;
    private final Path rootDir;

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
    public void writeChunk(String chunkId, byte[] data) {
        Path file = rootDir.resolve(chunkId + ".chunk");
        try {
            Files.write(file, data);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write chunk " + chunkId + " on " + name, e);
        }
    }

    @Override
    public byte[] readChunk(String chunkId) {
        Path file = rootDir.resolve(chunkId + ".chunk");
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
        return Files.exists(rootDir.resolve(chunkId + ".chunk"));
    }

    @Override
    public void deleteChunk(String chunkId) {
        try {
            Files.deleteIfExists(rootDir.resolve(chunkId + ".chunk"));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to delete chunk " + chunkId + " on " + name, e);
        }
    }
}
