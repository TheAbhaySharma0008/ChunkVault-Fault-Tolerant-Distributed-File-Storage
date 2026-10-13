# ChunkVault Phase 2 Architecture

## System Overview

User → React Frontend → Spring Boot REST API → File Processing → MySQL Metadata → Simulated Storage Nodes → Retrieval → Reconstruction → User

## Architecture Layers

### 1. Frontend Layer (React)
- **Framework:** React 18 with TypeScript
- **Routing:** React Router for SPA navigation
- **Styling:** Tailwind CSS for responsive design
- **State Management:** React hooks (useState, useEffect)
- **API Communication:** Fetch API with TypeScript types

**Pages:**
- Home: Landing page with product overview
- About: Project information and concepts
- Contact: Contact form for user feedback
- Dashboard: Statistics and quick actions
- Files: File management with search and delete
- File Details: Chunk visualization and metadata
- Storage Nodes: Node monitoring and health status
- Upload: Drag-and-drop file upload interface

### 2. Backend Layer (Spring Boot)
- **Framework:** Spring Boot 3.2 with Java 17
- **API:** RESTful endpoints with CORS support
- **ORM:** JPA/Hibernate for database operations
- **Validation:** Input validation and error handling

**Controllers:**
- `FileController`: File upload, download, listing, deletion
- `NodeController`: Storage node status and health
- `ContactController`: Contact form submission

**Services:**
- `FileStorageService`: File upload, chunking, reconstruction, deletion
- `ChunkingService`: File splitting into fixed-size chunks
- `MetadataStore`: Database-backed metadata persistence

### 3. Database Layer (MySQL)
- **Database:** MySQL 8.0
- **ORM:** Hibernate with JPA annotations
- **Schema:** Auto-generated via DDL update

**Tables:**

**files**
- `id` (PK, auto-increment)
- `file_id` (unique, indexed)
- `original_filename`
- `file_size`
- `content_type`
- `total_chunks`
- `status` (ACTIVE, DELETED)
- `upload_timestamp`

**chunks**
- `id` (PK, auto-increment)
- `chunk_id` (unique, indexed)
- `file_id` (FK → files.id)
- `sequence_number`
- `chunk_size`
- `storage_node`
- `status` (STORED, MISSING)
- `created_at`

**storage_nodes**
- `id` (PK, auto-increment)
- `node_name` (unique)
- `status`
- `storage_path`
- `chunk_count`
- `storage_used`
- `last_health_check`

**contact_messages**
- `id` (PK, auto-increment)
- `name`
- `email`
- `subject`
- `message`
- `submitted_at`

### 4. Storage Layer (Local Filesystem)
- **Implementation:** LocalFileSystemStorageNode
- **Nodes:** Simulated distributed nodes (nodeA, nodeB, nodeC)
- **Path:** Configurable storage root directory

**Chunk Storage:**
- Format: `{chunkId}.chunk`
- Location: `{storageRoot}/{nodeName}/{chunkId}.chunk`
- Example: `storage/nodeA/F-ABC123-C0000.chunk`

## Data Flow

### Upload Flow
1. User selects file via drag-and-drop or file picker
2. Frontend sends POST to `/api/files/upload` with multipart form data
3. Backend validates file (size, type, content)
4. `ChunkingService` splits file into fixed-size chunks (default: 1MB)
5. `StorageNodeRouter` assigns each chunk to a node using round-robin
6. Chunks are written to respective node directories
7. Metadata is persisted to MySQL (files and chunks tables)
8. Response with file ID, chunk distribution, and metadata returned to frontend

### Download Flow
1. User requests download via `/api/files/{fileId}/download`
2. Backend retrieves file metadata from MySQL
3. Chunk metadata is sorted by sequence number
4. Each chunk is read from its assigned storage node
5. Chunks are reassembled in sequence order
6. Reconstructed file is returned as binary stream with proper headers

### Delete Flow
1. User requests delete via DELETE `/api/files/{fileId}`
2. Backend retrieves file metadata from MySQL
3. All chunk files are deleted from storage nodes
4. Chunk records are deleted from chunks table
5. File record is deleted from files table
6. Transaction ensures data consistency

## Chunk Distribution Algorithm

**Round-Robin Strategy:**
```
chunk 0 → nodeA
chunk 1 → nodeB
chunk 2 → nodeC
chunk 3 → nodeA
chunk 4 → nodeB
...
```

Formula: `nodeIndex = sequenceNumber % totalNodes`

This ensures even distribution across all available nodes.

## Fault Tolerance (Phase 2 Foundation)

### Current Implementation
- **Node Health Monitoring:** Basic health check endpoint
- **Chunk Status Tracking:** Status field in chunks table
- **Error Handling:** Comprehensive exception handling for missing chunks
- **Transaction Safety:** Database operations are transactional

### Limitations
- No automatic replication (single copy per chunk)
- No automatic recovery from missing chunks
- No redundancy across physical machines
- No automatic failover

## Configuration

**application.properties:**
```properties
# Server
server.port=8080

# File Upload
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=100MB

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/chunkvault
spring.datasource.username=root
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# ChunkVault
chunkvault.chunk-size-bytes=1048576
chunkvault.storage-root=../storage
chunkvault.nodes=nodeA,nodeB,nodeC
```

## API Endpoints

### Files
- `POST /api/files/upload` - Upload file
- `GET /api/files` - List all files
- `GET /api/files/{fileId}/metadata` - Get file metadata
- `GET /api/files/{fileId}/download` - Download file
- `DELETE /api/files/{fileId}` - Delete file

### Nodes
- `GET /api/nodes` - List all nodes with status
- `GET /api/nodes/{nodeName}/health` - Get node health

### Contact
- `POST /api/contact` - Submit contact form

## Phase 2 Improvements Over Phase 1

1. **Persistent Metadata:** MySQL database replaces in-memory storage
2. **Professional UI:** Complete navigation and polished pages
3. **File Management:** Full CRUD operations for files
4. **Node Monitoring:** Dedicated page for storage node status
5. **Error Handling:** Comprehensive exception handling
6. **Contact Form:** User feedback collection
7. **Dashboard:** Real-time statistics and overview
8. **Search & Filter:** Enhanced file discovery

## Phase 3 Roadmap

1. **Multi-Machine Deployment:** Physical node distribution
2. **Replication:** Configurable replication factor
3. **Automatic Recovery:** Chunk reconstruction from replicas
4. **Authentication:** User accounts and permissions
5. **Integrity Verification:** SHA-256 checksums
6. **Advanced Monitoring:** Real-time alerts and metrics
7. **Cloud Integration:** S3 or object storage backend
8. **Containerization:** Docker and Kubernetes deployment
