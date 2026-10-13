package com.chunkvault;

import com.chunkvault.config.StorageProperties;
import com.chunkvault.service.ChunkingService;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChunkingServiceTest {

    private ChunkingService serviceWithChunkSize(long size) {
        StorageProperties p = new StorageProperties();
        p.setChunkSizeBytes(size);
        return new ChunkingService(p);
    }

    @Test
    void splitsFileIntoExpectedNumberOfChunks() {
        ChunkingService s = serviceWithChunkSize(4);
        byte[] data = "abcdefghij".getBytes(StandardCharsets.UTF_8); // 10 bytes
        List<byte[]> chunks = s.split(data);
        assertEquals(3, chunks.size());
        assertEquals(4, chunks.get(0).length);
        assertEquals(4, chunks.get(1).length);
        assertEquals(2, chunks.get(2).length);
    }

    @Test
    void preservesByteOrderWhenReassembled() {
        ChunkingService s = serviceWithChunkSize(3);
        byte[] data = "HelloWorld".getBytes(StandardCharsets.UTF_8);
        List<byte[]> chunks = s.split(data);

        StringBuilder sb = new StringBuilder();
        for (byte[] c : chunks) sb.append(new String(c, StandardCharsets.UTF_8));
        assertEquals("HelloWorld", sb.toString());
    }

    @Test
    void emptyFileProducesSingleEmptyChunk() {
        ChunkingService s = serviceWithChunkSize(1024);
        List<byte[]> chunks = s.split(new byte[0]);
        assertEquals(1, chunks.size());
        assertEquals(0, chunks.get(0).length);
    }

    @Test
    void singleByteFileProducesSingleChunk() {
        ChunkingService s = serviceWithChunkSize(1024);
        List<byte[]> chunks = s.split(new byte[]{42});
        assertEquals(1, chunks.size());
        assertEquals(1, chunks.get(0).length);
        assertEquals(42, chunks.get(0)[0]);
    }

    @Test
    void exactlyOneChunkFile() {
        ChunkingService s = serviceWithChunkSize(5);
        byte[] data = "12345".getBytes(StandardCharsets.UTF_8);
        List<byte[]> chunks = s.split(data);
        assertEquals(1, chunks.size());
    }
}
