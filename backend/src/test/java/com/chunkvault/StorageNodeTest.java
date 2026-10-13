package com.chunkvault;

import com.chunkvault.exception.ChunkMissingException;
import com.chunkvault.storage.LocalFileSystemStorageNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class StorageNodeTest {

    @TempDir
    Path tmp;

    @Test
    void writesAndReadsChunkRoundTrip() {
        LocalFileSystemStorageNode node = new LocalFileSystemStorageNode("nodeA", tmp);
        byte[] data = "chunk-bytes".getBytes(StandardCharsets.UTF_8);
        node.writeChunk("C0001", data);

        assertTrue(node.exists("C0001"));
        assertArrayEquals(data, node.readChunk("C0001"));
    }

    @Test
    void missingChunkThrows() {
        LocalFileSystemStorageNode node = new LocalFileSystemStorageNode("nodeA", tmp);
        assertThrows(ChunkMissingException.class, () -> node.readChunk("nope"));
    }

    @Test
    void deleteRemovesChunk() {
        LocalFileSystemStorageNode node = new LocalFileSystemStorageNode("nodeA", tmp);
        node.writeChunk("C1", new byte[]{1, 2, 3});
        node.deleteChunk("C1");
        assertFalse(node.exists("C1"));
    }
}
