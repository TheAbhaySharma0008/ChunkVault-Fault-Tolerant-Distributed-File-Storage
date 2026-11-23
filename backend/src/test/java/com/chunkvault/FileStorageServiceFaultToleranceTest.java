package com.chunkvault;

import com.chunkvault.config.StorageProperties;
import com.chunkvault.exception.ChunkMissingException;
import com.chunkvault.model.FileMetadata;
import com.chunkvault.model.UploadResponse;
import com.chunkvault.service.ChunkingService;
import com.chunkvault.service.FileStorageService;
import com.chunkvault.service.MetadataStore;
import com.chunkvault.storage.StorageNodeRouter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for replication, checksum failover and cleanup logic.
 * Uses an in-memory MetadataStore subclass so no database or Mockito is required.
 */
class FileStorageServiceFaultToleranceTest {

    @TempDir
    Path tmp;

    private StorageNodeRouter router;
    private FileStorageService service;
    private final Map<String, FileMetadata> db = new HashMap<>();
    private boolean failSave;

    private MetadataStore inMemoryStore() {
        return new MetadataStore(null, null) {
            @Override
            public void save(FileMetadata metadata) {
                if (failSave) {
                    throw new RuntimeException("db down");
                }
                db.put(metadata.getFileId(), metadata);
            }

            @Override
            public FileMetadata get(String fileId) {
                return db.get(fileId);
            }

            @Override
            public void delete(String fileId) {
                db.remove(fileId);
            }

            @Override
            public List<FileMetadata> getAll() {
                return new ArrayList<>(db.values());
            }
        };
    }

    @BeforeEach
    void setUp() {
        db.clear();
        failSave = false;
        StorageProperties props = new StorageProperties();
        props.setChunkSizeBytes(4);
        props.setStorageRoot(tmp.toString());
        props.setNodes(List.of("nodeA", "nodeB", "nodeC"));
        props.setReplicationFactor(2);

        router = new StorageNodeRouter(props);
        router.init();

        service = new FileStorageService(new ChunkingService(props), router, inMemoryStore(), props);
    }

    private MockMultipartFile file(String name, byte[] data) {
        return new MockMultipartFile("file", name, "application/octet-stream", data);
    }

    private Path chunkFile(String node, String chunkId) {
        return tmp.resolve(node).resolve(chunkId + ".chunk");
    }

    @Test
    void uploadReplicatesEachChunkToADifferentNode() {
        byte[] data = new byte[20]; // 5 chunks of 4 bytes
        new Random(1).nextBytes(data);

        UploadResponse res = service.upload(file("r.bin", data));
        assertEquals(5, res.getChunkCount());

        FileMetadata meta = db.get(res.getFileId());
        for (var cm : meta.getChunks()) {
            assertEquals(1, cm.getReplicas().size());
            assertNotEquals(cm.getStorageNode(), cm.getReplicas().get(0));
            assertNotNull(cm.getChecksum());
            assertEquals(64, cm.getChecksum().length());
            assertTrue(Files.exists(chunkFile(cm.getReplicas().get(0), cm.getChunkId())));
        }
        assertNotNull(meta.getChecksum());
        assertArrayEquals(data, service.reconstruct(res.getFileId()));
    }

    @Test
    void reconstructFailsOverWhenPrimaryChunkMissing() throws IOException {
        byte[] data = new byte[12];
        new Random(2).nextBytes(data);
        UploadResponse res = service.upload(file("f.bin", data));

        FileMetadata meta = db.get(res.getFileId());
        var first = meta.getChunks().get(0);
        Files.delete(chunkFile(first.getStorageNode(), first.getChunkId()));

        assertArrayEquals(data, service.reconstruct(res.getFileId()));
    }

    @Test
    void reconstructFailsOverWhenPrimaryChunkCorrupted() throws IOException {
        byte[] data = new byte[12];
        new Random(3).nextBytes(data);
        UploadResponse res = service.upload(file("c.bin", data));

        FileMetadata meta = db.get(res.getFileId());
        var first = meta.getChunks().get(0);
        Files.write(chunkFile(first.getStorageNode(), first.getChunkId()), new byte[]{9, 9, 9, 9});

        assertArrayEquals(data, service.reconstruct(res.getFileId()));
    }

    @Test
    void reconstructFailsOverWhenPrimaryNodeDown() {
        byte[] data = new byte[12];
        new Random(4).nextBytes(data);
        UploadResponse res = service.upload(file("d.bin", data));

        FileMetadata meta = db.get(res.getFileId());
        var first = meta.getChunks().get(0);
        router.nodeByName(first.getStorageNode()).setAvailable(false);

        assertArrayEquals(data, service.reconstruct(res.getFileId()));
    }

    @Test
    void reconstructThrowsWhenAllCopiesUnavailable() {
        byte[] data = new byte[8];
        new Random(5).nextBytes(data);
        UploadResponse res = service.upload(file("a.bin", data));

        FileMetadata meta = db.get(res.getFileId());
        for (var cm : meta.getChunks()) {
            router.nodeByName(cm.getStorageNode()).setAvailable(false);
            for (String r : cm.getReplicas()) {
                router.nodeByName(r).setAvailable(false);
            }
        }

        assertThrows(ChunkMissingException.class, () -> service.reconstruct(res.getFileId()));
    }

    @Test
    void newUploadsSkipUnavailableNodes() {
        router.nodeByName("nodeB").setAvailable(false);
        byte[] data = new byte[12]; // 3 chunks
        new Random(6).nextBytes(data);

        UploadResponse res = service.upload(file("s.bin", data));
        FileMetadata meta = db.get(res.getFileId());
        for (var cm : meta.getChunks()) {
            assertNotEquals("nodeB", cm.getStorageNode());
            assertFalse(cm.getReplicas().contains("nodeB"));
        }
        assertArrayEquals(data, service.reconstruct(res.getFileId()));
    }

    @Test
    void deleteRemovesPrimaryAndReplicaFiles() {
        byte[] data = new byte[12];
        new Random(7).nextBytes(data);
        UploadResponse res = service.upload(file("del.bin", data));
        FileMetadata meta = db.get(res.getFileId());

        service.delete(res.getFileId());

        assertFalse(db.containsKey(res.getFileId()));
        for (var cm : meta.getChunks()) {
            assertFalse(Files.exists(chunkFile(cm.getStorageNode(), cm.getChunkId())));
            for (String r : cm.getReplicas()) {
                assertFalse(Files.exists(chunkFile(r, cm.getChunkId())));
            }
        }
    }

    @Test
    void emptyFileUploadsAndDownloads() {
        UploadResponse res = service.upload(file("empty.bin", new byte[0]));
        assertEquals(0, res.getFileSize());
        assertEquals(1, res.getChunkCount());
        assertArrayEquals(new byte[0], service.reconstruct(res.getFileId()));
    }

    @Test
    void metadataFailureCleansUpWrittenChunks() {
        failSave = true;
        byte[] data = new byte[12];

        assertThrows(RuntimeException.class, () -> service.upload(file("x.bin", data)));

        try (var walk = Files.walk(tmp)) {
            long leftovers = walk.filter(Files::isRegularFile).count();
            assertEquals(0, leftovers);
        } catch (IOException e) {
            fail(e);
        }
    }

    @Test
    void replicationFactorOneDisablesReplicas() {
        StorageProperties props = new StorageProperties();
        props.setChunkSizeBytes(4);
        props.setStorageRoot(tmp.resolve("single").toString());
        props.setNodes(List.of("nodeA", "nodeB", "nodeC"));
        props.setReplicationFactor(1);
        StorageNodeRouter r2 = new StorageNodeRouter(props);
        r2.init();
        FileStorageService s2 = new FileStorageService(new ChunkingService(props), r2, inMemoryStore(), props);

        UploadResponse res = s2.upload(file("one.bin", new byte[8]));
        FileMetadata meta = db.get(res.getFileId());
        for (var cm : meta.getChunks()) {
            assertTrue(cm.getReplicas().isEmpty());
        }
    }
}
