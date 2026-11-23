export interface ChunkInfo {
  chunkId: string;
  sequenceNumber: number;
  storageNode: string;
  size: number;
  replicas?: string[];
}

export interface UploadResponse {
  fileId: string;
  fileName: string;
  fileSize: number;
  chunkCount: number;
  chunkSizeBytes: number;
  chunks: ChunkInfo[];
}

export interface FileMetadata {
  fileId: string;
  fileName: string;
  fileSize: number;
  chunks: ChunkInfo[];
  checksum?: string;
  contentType?: string;
  uploadTimestamp?: string;
  status?: string;
}

export interface NodeInfo {
  nodeName: string;
  status: string;
  storagePath: string;
  chunkCount: number;
  storageUsed: number;
  health: string;
}

export interface ContactFormData {
  name: string;
  email: string;
  subject: string;
  message: string;
}
