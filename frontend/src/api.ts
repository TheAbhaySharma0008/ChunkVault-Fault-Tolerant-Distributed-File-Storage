import type { UploadResponse, FileMetadata, NodeInfo, ContactFormData } from './types';

export async function uploadFile(file: File): Promise<UploadResponse> {
  const formData = new FormData();
  formData.append('file', file);

  const response = await fetch('/api/files/upload', {
    method: 'POST',
    body: formData,
  });

  if (!response.ok) {
    let message = `Upload failed (${response.status})`;
    try {
      const body = await response.json();
      if (body?.message) message = body.message;
    } catch {
      // Keep the default message.
    }
    throw new Error(message);
  }

  return response.json() as Promise<UploadResponse>;
}

export function downloadUrl(fileId: string): string {
  return `/api/files/${encodeURIComponent(fileId)}/download`;
}

export async function listFiles(): Promise<FileMetadata[]> {
  const response = await fetch('/api/files');
  if (!response.ok) {
    throw new Error(`Failed to fetch files (${response.status})`);
  }
  return response.json() as Promise<FileMetadata[]>;
}

export async function getFileMetadata(fileId: string): Promise<FileMetadata> {
  const response = await fetch(`/api/files/${encodeURIComponent(fileId)}/metadata`);
  if (!response.ok) {
    throw new Error(`Failed to fetch file metadata (${response.status})`);
  }
  return response.json() as Promise<FileMetadata>;
}

export async function deleteFile(fileId: string): Promise<void> {
  const response = await fetch(`/api/files/${encodeURIComponent(fileId)}`, {
    method: 'DELETE',
  });
  if (!response.ok) {
    throw new Error(`Failed to delete file (${response.status})`);
  }
}

export async function listNodes(): Promise<NodeInfo[]> {
  const response = await fetch('/api/nodes');
  if (!response.ok) {
    throw new Error(`Failed to fetch nodes (${response.status})`);
  }
  return response.json() as Promise<NodeInfo[]>;
}

export async function submitContactForm(data: ContactFormData): Promise<{ message: string }> {
  const response = await fetch('/api/contact', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(data),
  });
  if (!response.ok) {
    throw new Error(`Failed to submit contact form (${response.status})`);
  }
  return response.json() as Promise<{ message: string }>;
}
