# ChunkVault — Fault-Tolerant Distributed File Storage

An academic distributed file storage system demonstrating OS concepts (chunking, distributed storage, fault tolerance) and DBMS concepts (JPA/Hibernate, MySQL persistence, transactions).

## Overview

ChunkVault splits uploaded files into fixed-size chunks, distributes them across simulated storage nodes with replication, persists all metadata in MySQL, and reconstructs files on download with SHA-256 integrity verification and automatic failover to replica copies.

## Features

### Implemented and Tested
- File upload via React UI (drag-and-drop or file picker) with loading/error feedback
- Fixed-size chunking (default 1 MiB, configurable via `chunkvault.chunk-size-bytes`)
- Round-robin primary placement across storage nodes
- **Replication**: each chunk is copied to `replication-factor` distinct nodes (default 2; replica never shares the primary's node)
- **SHA-256 checksums** for the whole file and every chunk, verified on reconstruction
- **Failover recovery**: download tries primary, then replicas; corrupted/missing/unavailable copies are skipped automatically
- Empty-file, small-file, exact-chunk-size and multi-chunk edge cases handled
- Upload compensation: failed uploads delete already-written chunk copies (no orphans)
- File listing, metadata view, chunk visualization, deletion (removes all copies + metadata)
- Simulated node outage via `PUT /api/nodes/{node}/availability?available=false` (no data deleted) + UI button on Storage Nodes page
- MySQL persistence (`files`, `chunks`, `contact_messages` tables); `storage_nodes` table exists but live stats are computed from disk
- Contact form, dashboard statistics, storage-node monitoring

### Partially Implemented / Known Limitations
- Storage nodes are local filesystem directories, not physical machines
- Node availability flags are in-memory (reset on backend restart)
- `storage_nodes` DB table is not kept in sync with live node stats (stats are computed on read)
- No authentication/authorization; CORS is open (`*`) for the academic demo
- Files are buffered in memory during upload/reconstruction (fine for the 100 MB limit)
- Replication factor is capped by the number of configured nodes

### Future Work
- Multi-machine deployment, automatic replica repair, authentication, encryption at rest, Swagger docs, Docker/Kubernetes

## Tech Stack

- **Backend:** Java 17+, Spring Boot 3.2, Spring Data JPA, Hibernate, Maven
- **Database:** MySQL 8.x, database `chunkvault`
- **Frontend:** React 18, TypeScript, Vite, Tailwind CSS, React Router
- **Storage:** local filesystem nodes `storage/nodeA`, `nodeB`, `nodeC`

## Prerequisites

- Java 17+ and Maven 3.9+
- Node.js 18+ and npm
- MySQL 8.x running on `localhost:3306`

## Setup

### Database

```sql
CREATE DATABASE chunkvault;
```

Hibernate (`ddl-auto=update`) creates/updates the schema automatically. The database password is **not** stored in the repo — provide it via the `DB_PASSWORD` environment variable or a local `.env` file:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/chunkvault?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=${DB_PASSWORD}
```

Option A — environment variable (PowerShell): `$env:DB_PASSWORD="your_password"` then `cd backend; mvn spring-boot:run`

Option B — `.env` file (gitignored): copy `.env.example` to `.env` in the project root and set `DB_PASSWORD=...`. The backend loads it automatically; real environment variables take precedence.

### Backend

```powershell
cd backend
mvn spring-boot:run
```

Runs on `http://localhost:8080`.

### Frontend

```powershell
cd frontend
npm install
npm run dev
```

Runs on `http://localhost:5173` (proxies `/api` to `:8080`). Production build: `npm run build`.

## Configuration (`application.properties`)

```properties
chunkvault.chunk-size-bytes=1048576   # 1 MiB
chunkvault.storage-root=../storage    # relative to backend working dir
chunkvault.nodes=nodeA,nodeB,nodeC
chunkvault.replication-factor=2       # total copies per chunk (primary + replicas)
```

## API Reference

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/files/upload` | Multipart upload (`file` field); returns fileId, chunk count, per-chunk node + replicas |
| GET | `/api/files` | List all files with chunk metadata |
| GET | `/api/files/{fileId}/metadata` | File metadata incl. SHA-256 checksum, timestamp, status |
| GET | `/api/files/{fileId}/download` | Reconstruct and download (fails over to replicas) |
| DELETE | `/api/files/{fileId}` | Delete all chunk copies and metadata |
| GET | `/api/nodes` | Node status, health, chunk counts, storage used |
| GET | `/api/nodes/{nodeName}/health` | Single-node health |
| PUT | `/api/nodes/{nodeName}/availability?available=false` | Simulate node outage/recovery |
| POST | `/api/contact` | Submit contact message |

## Metadata Schema

- **files**: `id`, `file_id` (unique), `original_filename`, `file_size`, `content_type`, `checksum` (SHA-256), `total_chunks`, `status`, `upload_timestamp`
- **chunks**: `id`, `chunk_id` (unique), `file_id` (FK → files), `sequence_number`, `chunk_size`, `storage_node` (primary), `checksum`, `replica_nodes` (comma-separated), `status`, `created_at`
- **contact_messages**: `id`, `name`, `email`, `subject`, `message`, `submitted_at`
- **storage_nodes**: defined but currently unused by the API (live stats come from the filesystem)

Chunk files on disk: `{storage-root}/{nodeName}/{chunkId}.chunk`, e.g. `storage/nodeA/F-1A2B3C4D5E6F-C0000.chunk`.

## Verification Queries

```sql
-- File records
SELECT file_id, original_filename, file_size, total_chunks, status FROM files;

-- Chunk records with replica locations
SELECT chunk_id, sequence_number, storage_node, replica_nodes, checksum FROM chunks ORDER BY file_id, sequence_number;

-- Orphaned chunk rows (file deleted but chunks remain)
SELECT c.* FROM chunks c LEFT JOIN files f ON c.file_id = f.id WHERE f.id IS NULL;
```

## Testing

```powershell
cd backend
mvn test
```

Test classes:
- `ChunkingServiceTest` — chunk splitting, ordering, empty file
- `StorageNodeTest` — node read/write/delete, missing chunk
- `FileStorageServiceFaultToleranceTest` — replication placement, failover on missing/corrupted/down primary, total-outage error, upload cleanup, delete cleanup, replication factor 1 (no DB required)
- `UploadDownloadIntegrationTest` — Spring Boot + MySQL upload/download round-trip

Frontend: `cd frontend && npm run build`.

## Demo Workflow

1. `cd backend && mvn spring-boot:run`, then `cd frontend && npm run dev`
2. Open `http://localhost:5173`, upload a file on the Upload page
3. Files page → Details shows chunk-to-node distribution and replicas
4. Download the file — bytes are identical to the original (SHA-256 verified server-side)
5. Storage Nodes page → "Simulate Node Failure" on a node → download again: reconstruction fails over to replicas transparently
6. Take all nodes down → download returns `503` with the list of failed copies
7. Restore nodes, delete the file → all `.chunk` files removed from every node directory

## Project Structure

```
ChunkVault/
├── backend/
│   └── src/main/java/com/chunkvault/
│       ├── entity/          # JPA entities (File, Chunk, StorageNode, ContactMessage)
│       ├── repository/      # Spring Data JPA repositories
│       ├── controller/      # REST controllers
│       ├── service/         # FileStorageService, ChunkingService, MetadataStore, ChecksumUtil
│       ├── storage/         # StorageNode interface, LocalFileSystemStorageNode, StorageNodeRouter
│       ├── model/           # DTOs
│       ├── config/          # StorageProperties
│       └── exception/       # Global exception handler + typed exceptions
├── frontend/src/            # React pages, api client, types
├── storage/node{A,B,C}/     # simulated storage nodes
└── docs/ARCHITECTURE.md
```

## Academic Context

- **OS concepts:** file chunking, distributed/replicated storage, fault detection and recovery, simulated node failure
- **DBMS concepts:** relational schema, FK relationships, JPA/Hibernate ORM, transactions, integrity constraints
- **Full-stack:** REST API + React/TypeScript frontend + MySQL persistence

## License

Academic project — not for production use.
