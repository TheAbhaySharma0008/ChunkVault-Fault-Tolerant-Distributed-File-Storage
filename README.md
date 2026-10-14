# ChunkVault — Phase 2

A fault-tolerant distributed file storage system built as an academic OS + DBMS project.

## Overview

ChunkVault is a distributed file storage system that breaks files into smaller chunks and distributes them across multiple storage nodes. This approach provides fault tolerance and reliability by ensuring that no single point of failure can compromise the entire storage system.

**Phase 2 Status:** Approximately 75% complete with persistent metadata, professional UI, and comprehensive file management.

## Implemented Features (Phase 2)

### Core Functionality
- File upload with drag-and-drop support
- Automatic file chunking (configurable chunk size)
- Round-robin chunk distribution across storage nodes
- File reconstruction and download
- File deletion with metadata cleanup
- MySQL database for persistent metadata storage

### Database Layer
- MySQL with JPA/Hibernate ORM
- Tables: `files`, `chunks`, `storage_nodes`, `contact_messages`
- Entity relationships with foreign keys
- Transactional data operations

### User Interface
- Professional landing page with product overview
- Dashboard with real-time statistics
- Files page with search, filtering, and management
- File details with chunk visualization
- Storage nodes monitoring page
- About Us page with project information
- Contact Us page with form submission
- Responsive design with Tailwind CSS

### API Endpoints
- `POST /api/files/upload` - Upload a file
- `GET /api/files` - List all files
- `GET /api/files/{fileId}/metadata` - Get file metadata
- `GET /api/files/{fileId}/download` - Download/reconstruct file
- `DELETE /api/files/{fileId}` - Delete a file
- `GET /api/nodes` - List storage nodes with status
- `GET /api/nodes/{nodeName}/health` - Get node health
- `POST /api/contact` - Submit contact form

### Storage Infrastructure
- Simulated local filesystem storage nodes (Node A, B, C)
- Round-robin chunk distribution
- Node health monitoring
- Storage usage tracking

## Not Yet Implemented (Future Work)

- Multi-machine distributed deployment
- Advanced replication strategies
- Automatic failover across physical machines
- SHA-256 integrity verification
- Authentication and authorization
- Encryption at rest
- Cloud object storage integration
- Advanced monitoring and alerting
- Containerized deployment (Docker/Kubernetes)
- Large-scale benchmarking

## Requirements

- Java 17+
- Maven 3.9+
- Node.js 18+ and npm
- MySQL 8.0+

## Database Setup

1. Install MySQL 8.0+ and start the MySQL service
2. Create a database (optional - the app will auto-create if configured):
   ```sql
   CREATE DATABASE chunkvault;
   ```
3. Update `backend/src/main/resources/application.properties` with your MySQL credentials:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/chunkvault
   spring.datasource.username=root
   spring.datasource.password=your_password
   ```

## Run Backend

powershell
cd backend
mvn spring-boot:run


Backend: `http://localhost:8080`

The backend will automatically create the database schema using Hibernate DDL.


## Testing

### Run Backend Tests

From the project root, run:

powershell
cd backend
mvn clean test



## Run Frontend

In a second terminal:

```powershell
cd frontend
npm install
npm run dev
```

Open the Vite URL, normally `http://localhost:5173`.

## Demo Workflow

1. Open the application at `http://localhost:5173`
2. Navigate to Dashboard or click "Get Started"
3. Upload a file using the drag-and-drop interface
4. View the chunk distribution and storage nodes used
5. Navigate to Files page to see all uploaded files
6. Click on a file to view detailed chunk visualization
7. Download the file to verify reconstruction
8. Delete the file and confirm cleanup
9. Check Storage Nodes page to monitor node status
10. Visit About and Contact pages for project information

## Architecture

- **Frontend:** React 18, TypeScript, Tailwind CSS, React Router
- **Backend:** Spring Boot 3.2, JPA/Hibernate, MySQL
- **Storage:** Local filesystem nodes (simulated distributed storage)
- **Chunk Size:** Configurable (default: 1MB)
- **Max File Size:** 100MB

## Project Structure

```
ChunkVault/
├── backend/
│   ├── src/main/java/com/chunkvault/
│   │   ├── entity/          # JPA entities
│   │   ├── repository/      # JPA repositories
│   │   ├── controller/      # REST controllers
│   │   ├── service/         # Business logic
│   │   ├── storage/         # Storage node implementations
│   │   ├── model/           # DTOs
│   │   ├── config/          # Configuration
│   │   └── exception/       # Exception handling
│   └── src/main/resources/
│       └── application.properties
├── frontend/
│   ├── src/
│   │   ├── components/      # Reusable components
│   │   ├── pages/           # Page components
│   │   ├── api.ts           # API client
│   │   └── types.ts         # TypeScript types
│   └── package.json
├── storage/
│   ├── nodeA/
│   ├── nodeB/
│   └── nodeC/
└── docs/
    └── ARCHITECTURE.md
```

## Known Limitations

- Storage nodes are local filesystem directories (not physical machines)
- No automatic replication or redundancy
- No authentication/authorization
- MySQL must be running locally
- Single-instance deployment only

## Phase 3 Roadmap

- Multi-node distributed deployment across physical machines
- Configurable replication factor for redundancy
- Automatic chunk recovery from replicas
- Authentication system with user accounts
- Advanced monitoring dashboard
- RESTful API documentation with Swagger
- Performance benchmarking and optimization

## Academic Context

This project demonstrates:
- **OS Concepts:** File systems, distributed storage, process management, fault tolerance
- **DBMS Concepts:** Relational database design, ORM, transactions, entity relationships
- **Full-Stack Development:** REST APIs, React frontend, database integration

## License

Academic project - Not for production use.
