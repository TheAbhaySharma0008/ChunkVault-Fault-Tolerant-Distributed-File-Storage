package com.chunkvault;

import com.chunkvault.model.FileMetadata;
import com.chunkvault.model.UploadResponse;
import com.chunkvault.service.FileStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;

import java.nio.charset.StandardCharsets;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "chunkvault.chunk-size-bytes=1024",
        "chunkvault.storage-root=target/test-storage",
        "chunkvault.nodes=nodeA,nodeB,nodeC"
})
class UploadDownloadIntegrationTest {

    @Autowired
    private FileStorageService service;

    @Test
    void uploadThenDownloadIsByteIdentical_smallFile() {
        byte[] original = "Hello ChunkVault!".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", "text/plain", original);

        UploadResponse response = service.upload(file);
        assertNotNull(response.getFileId());
        assertEquals(1, response.getChunkCount());
        assertEquals(1, response.getChunks().size());

        byte[] reconstructed = service.reconstruct(response.getFileId());
        assertArrayEquals(original, reconstructed);
    }

    @Test
    void uploadThenDownloadIsByteIdentical_multiChunk() {
        byte[] original = new byte[5000];
        new Random(42).nextBytes(original);
        MockMultipartFile file = new MockMultipartFile("file", "random.bin", "application/octet-stream", original);

        UploadResponse response = service.upload(file);
        assertEquals(5, response.getChunkCount());
        assertEquals(5000, response.getFileSize());

        // Verify round-robin distribution
        assertEquals("nodeA", response.getChunks().get(0).getStorageNode());
        assertEquals("nodeB", response.getChunks().get(1).getStorageNode());
        assertEquals("nodeC", response.getChunks().get(2).getStorageNode());
        assertEquals("nodeA", response.getChunks().get(3).getStorageNode());
        assertEquals("nodeB", response.getChunks().get(4).getStorageNode());

        // Sequence numbers must be preserved
        for (int i = 0; i < response.getChunks().size(); i++) {
            assertEquals(i, response.getChunks().get(i).getSequenceNumber());
        }

        byte[] reconstructed = service.reconstruct(response.getFileId());
        assertArrayEquals(original, reconstructed);
    }

    @Test
    void largeFileRoundTrip() {
        byte[] original = new byte[123456];
        new Random(7).nextBytes(original);
        MockMultipartFile file = new MockMultipartFile("file", "big.bin", "application/octet-stream", original);

        UploadResponse response = service.upload(file);
        FileMetadata meta = service.getMetadata(response.getFileId());
        assertEquals(original.length, meta.getFileSize());

        byte[] reconstructed = service.reconstruct(response.getFileId());
        assertArrayEquals(original, reconstructed);
    }

    @Test
    void unknownFileIdThrows() {
        assertThrows(RuntimeException.class, () -> service.reconstruct("F-DOES-NOT-EXIST"));
    }
}
