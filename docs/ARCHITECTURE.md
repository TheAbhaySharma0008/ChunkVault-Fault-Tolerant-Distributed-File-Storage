# ChunkVault Architecture (Final)

## System Overview

```
User → React Frontend → Spring Boot REST API → Chunking + Replication → Local Storage Nodes
                          ↓                            ↓
                       MySQL (metadata)        {node}/{chunkId}.chunk files
                          ↓
        Download: metadata → ordered chunks → primary/replica failover → SHA-256 verify → reconstruct
```

## Layers

### 1. Frontend (React 18 + TypeScript + Vite + Tailwind)

Pages: Home, About, Contact, Dashboard, Files, File Details (chunk + replica visualization), Storage Nodes (health + simulated-failure button), Upload (drag-and-drop, result summary with chunk distribution table).

`src/api.ts` wraps all REST calls; `src/types.ts` mirrors backend DTOs (`ChunkInfo.replicas`, `FileMetadata.checksum/uploadTimestamp/status`).

### 2. Backend (Spring Boot 3.2, Java 17)

- `FileController` — upload/download/list/metadata/delete under `/api/files`
- `NodeController` — node listing, health, and `PUT /{node}/availability` outage simulation
- `ContactController` — contact form persistence
- `FileStorageService` — orchestrates chunking, replicated writes, failover reads, cleanup
- `ChunkingService` — fixed-size splitter (empty file → single empty chunk)
- `MetadataStore` — JPA-backed persistence (files + chunks in one transaction via `CascadeType.ALL`)
- `ChecksumUtil` — SHA-256 hex helpers
- `StorageNodeRouter` — round-robin primary selection + distinct replica selection, skipping unavailable nodes
- `LocalFileSystemStorageNode` — `.chunk` file IO, availability flag, chunk-id sanitization (path-traversal guard)

### 3. Database (MySQL 8, `ddl-auto=update`)

- **files**: id, file_id (unique), original_filename, file_size, content_type, checksum, total_chunks, status, upload_timestamp
- **chunks**: id, chunk_id (unique), file_id (FK), sequence_number, chunk_size, storage_node (primary), checksum, replica_nodes (CSV), status, created_at
- **contact_messages**: id, name, email, subject, message, submitted_at
- **storage_nodes**: table exists; live node stats are computed from the filesystem instead

### 4. Storage Nodes

Three simulated nodes (`storage/nodeA|B|C`), each a directory of `{chunkId}.chunk` files. Chunk ids are server-generated (`F-<id>-C<seq>`), so filenames cannot escape the node directory; `LocalFileSystemStorageNode` additionally rejects ids containing `/`, `\`, or `..`.

## Data Flows

### Upload
1. Multipart POST → `FileStorageService.upload`
2. Filename sanitized (basename only, control chars stripped, ≤255 chars); empty files allowed
3. `ChunkingService.split` → fixed-size chunks
4. File-level SHA-256 computed
5. Per chunk: `router.nodesForChunk(seq, replicationFactor)` returns primary + distinct replicas (unavailable nodes skipped); every copy written; per-chunk SHA-256 recorded
6. Any failure → all already-written copies deleted (compensation), error returned
7. `MetadataStore.save` persists file + chunk rows (checksum, replica_nodes) in one transaction

### Download / Reconstruction
1. `GET /{fileId}/download` → metadata + chunks sorted by `sequence_number`
2. Per chunk, `readChunkWithFailover` tries primary then each replica:
   - node unknown / unavailable / missing file / IO error → next candidate
   - SHA-256 mismatch or size mismatch → next candidate (corruption tolerated)
3. All candidates exhausted → `ChunkMissingException` → HTTP 503 with per-node failure reasons
4. Reassembled bytes verified against stored file size **and** file-level SHA-256

### Delete
- Every chunk copy (primary + all replicas) deleted best-effort per node; metadata rows removed transactionally afterwards.

### Node Failure Simulation
- `PUT /api/nodes/{name}/availability?available=false` flips an in-memory flag. New uploads skip the node; reads on it throw `NodeUnavailableException` (HTTP 503 if reached directly) and failover engages. Restarting the backend resets all nodes to available.

## Replication

- `chunkvault.replication-factor` = total copies per chunk (default **2** → 1 primary + 1 replica), clamped to `[1, configured node count]` and to currently available nodes at write time.
- Placement: primary = round-robin over available nodes; replicas = subsequent distinct nodes in the ring — a replica never shares its primary's node (when ≥2 nodes are up).
- Metadata: `chunks.storage_node` = primary, `chunks.replica_nodes` = CSV of replica node names.

## Integrity

- Per-chunk SHA-256 stored at upload; verified on every read candidate.
- Whole-file SHA-256 stored on `files.checksum`; verified after reconstruction (legacy rows without checksums skip verification).

## Configuration

```properties
server.port=8080
spring.servlet.multipart.max-file-size=100MB
spring.datasource.url=jdbc:mysql://localhost:3306/chunkvault?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=${DB_PASSWORD:...}
spring.jpa.hibernate.ddl-auto=update
chunkvault.chunk-size-bytes=1048576
chunkvault.storage-root=../storage
chunkvault.nodes=nodeA,nodeB,nodeC
chunkvault.replication-factor=2
```

## API Endpoints

- `POST /api/files/upload` — multipart `file`; returns fileId, size, chunkCount, chunkSizeBytes, chunks[]{chunkId, sequenceNumber, storageNode, size, replicas[]}
- `GET /api/files` — all files with chunks, checksum, uploadTimestamp, status
- `GET /api/files/{fileId}/metadata`
- `GET /api/files/{fileId}/download` — attachment download
- `DELETE /api/files/{fileId}`
- `GET /api/nodes`, `GET /api/nodes/{name}/health`
- `PUT /api/nodes/{name}/availability?available={bool}`
- `POST /api/contact`

## Error Handling

`GlobalExceptionHandler` maps: FileNotFound → 404, InvalidUpload → 400, NodeUnavailable → 503, ChunkMissing (all copies failed) → 503 with per-node reasons, DataIntegrityViolation → 409, IllegalArgument → 400, IllegalState → 500, generic → 500. Responses are JSON `{timestamp, status, error, message}`.

## Testing

- `ChunkingServiceTest` (5), `StorageNodeTest` (3), `FileStorageServiceFaultToleranceTest` (10, no DB), `UploadDownloadIntegrationTest` (4, Spring Boot + MySQL) — **22 tests, all passing** via `cd backend && mvn test`.
- Frontend verified with `npm run build` (tsc + vite).
- Live smoke test performed: upload → identical download (SHA-256 match) → node failure → failover download → all-nodes-down → 503 → delete → all copies removed.

## Known Limitations

- Simulated nodes share one filesystem; availability flags are in-memory.
- Whole file buffered in memory on upload/download (bounded by 100 MB upload cap).
- `storage_nodes` table unused; node stats computed per request.
- No auth; CORS `*`; DB password has a local-dev default (use `DB_PASSWORD` env var).
- Replica repair (re-creating a lost copy while the node is back) is not implemented — recovery is read-time failover only.
