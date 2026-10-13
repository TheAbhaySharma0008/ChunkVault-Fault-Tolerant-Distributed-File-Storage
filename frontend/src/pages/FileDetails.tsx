import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getFileMetadata, downloadUrl, deleteFile } from '../api';
import type { FileMetadata } from '../types';

function formatBytes(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(2)} MB`;
}

export default function FileDetails() {
  const { fileId } = useParams<{ fileId: string }>();
  const [file, setFile] = useState<FileMetadata | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    async function loadFile() {
      if (!fileId) return;
      try {
        const data = await getFileMetadata(fileId);
        setFile(data);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to load file details');
      } finally {
        setLoading(false);
      }
    }
    loadFile();
  }, [fileId]);

  const handleDelete = async () => {
    if (!file || !fileId) return;
    if (!confirm(`Are you sure you want to delete "${file.fileName}"? This action cannot be undone.`)) {
      return;
    }

    try {
      await deleteFile(fileId);
      window.location.href = '/files';
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to delete file');
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-slate-400">Loading file details...</div>
      </div>
    );
  }

  if (error || !file) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="rounded-lg border border-red-800 bg-red-950/40 p-6 text-red-300">
          {error || 'File not found'}
        </div>
      </div>
    );
  }

  const sortedChunks = [...file.chunks].sort((a, b) => a.sequenceNumber - b.sequenceNumber);

  return (
    <div className="min-h-screen">
      <div className="mx-auto max-w-7xl px-6 py-12">
        <div className="mb-6">
          <Link to="/files" className="text-sm text-sky-400 hover:text-sky-300">
            ← Back to Files
          </Link>
        </div>

        <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
          <div className="lg:col-span-2 space-y-6">
            <div className="rounded-2xl border border-slate-800 bg-slate-900/50 p-6">
              <div className="flex items-start justify-between">
                <div className="flex-1">
                  <h1 className="text-2xl font-bold text-slate-100">{file.fileName}</h1>
                  <p className="mt-2 text-sm font-mono text-slate-500">{file.fileId}</p>
                </div>
                <div className="flex gap-2">
                  <a
                    href={downloadUrl(file.fileId)}
                    className="inline-flex items-center justify-center rounded-lg bg-emerald-500 px-4 py-2 text-sm font-semibold text-slate-950 hover:bg-emerald-400 transition-colors"
                  >
                    Download
                  </a>
                  <button
                    onClick={handleDelete}
                    className="inline-flex items-center justify-center rounded-lg bg-red-500/10 px-4 py-2 text-sm font-semibold text-red-400 hover:bg-red-500/20 transition-colors"
                  >
                    Delete
                  </button>
                </div>
              </div>

              <div className="mt-6 grid grid-cols-2 gap-4 sm:grid-cols-4">
                <div className="rounded-lg bg-slate-950 p-4">
                  <p className="text-xs uppercase text-slate-500">File Size</p>
                  <p className="mt-1 text-lg font-semibold text-slate-100">{formatBytes(file.fileSize)}</p>
                </div>
                <div className="rounded-lg bg-slate-950 p-4">
                  <p className="text-xs uppercase text-slate-500">Total Chunks</p>
                  <p className="mt-1 text-lg font-semibold text-slate-100">{file.chunks.length}</p>
                </div>
                <div className="rounded-lg bg-slate-950 p-4">
                  <p className="text-xs uppercase text-slate-500">Status</p>
                  <p className="mt-1 text-lg font-semibold text-emerald-400">{file.status || 'ACTIVE'}</p>
                </div>
                <div className="rounded-lg bg-slate-950 p-4">
                  <p className="text-xs uppercase text-slate-500">Uploaded</p>
                  <p className="mt-1 text-lg font-semibold text-slate-100">
                    {file.uploadTimestamp ? new Date(file.uploadTimestamp).toLocaleDateString() : 'N/A'}
                  </p>
                </div>
              </div>
            </div>

            <div className="rounded-2xl border border-slate-800 bg-slate-900/50 p-6">
              <h2 className="text-xl font-semibold text-slate-100">Chunk Distribution</h2>
              <p className="mt-2 text-sm text-slate-400">
                This file is split into {file.chunks.length} chunks distributed across storage nodes.
              </p>

              <div className="mt-6 rounded-lg border border-slate-800 bg-slate-950 p-4 font-mono text-sm">
                <div className="text-slate-400">
                  <div className="mb-2">{file.fileName}</div>
                  <div className="pl-4 space-y-1">
                    {sortedChunks.map((chunk) => (
                      <div key={chunk.chunkId} className="flex items-center gap-2">
                        <span className="text-slate-500">├──</span>
                        <span className="text-sky-400">Chunk {String(chunk.sequenceNumber).padStart(4, '0')}</span>
                        <span className="text-slate-500">→</span>
                        <span className="rounded-full bg-sky-950 px-2 py-0.5 text-xs text-sky-300">
                          {chunk.storageNode}
                        </span>
                        <span className="text-slate-500">({formatBytes(chunk.size)})</span>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div className="space-y-6">
            <div className="rounded-2xl border border-slate-800 bg-slate-900/50 p-6">
              <h2 className="text-lg font-semibold text-slate-100">Chunk Breakdown</h2>
              <div className="mt-4 space-y-3">
                {sortedChunks.map((chunk) => (
                  <div
                    key={chunk.chunkId}
                    className="rounded-lg border border-slate-800 bg-slate-950/50 p-3"
                  >
                    <div className="flex items-center justify-between mb-2">
                      <span className="text-sm font-medium text-slate-100">#{chunk.sequenceNumber}</span>
                      <span className="text-xs text-slate-500">{formatBytes(chunk.size)}</span>
                    </div>
                    <div className="text-xs font-mono text-slate-500 break-all">{chunk.chunkId}</div>
                    <div className="mt-2 flex items-center gap-2">
                      <span className="rounded-full bg-sky-950 px-2 py-0.5 text-xs text-sky-300">
                        {chunk.storageNode}
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            <div className="rounded-2xl border border-slate-800 bg-slate-900/50 p-6">
              <h2 className="text-lg font-semibold text-slate-100">Storage Summary</h2>
              <div className="mt-4 space-y-3">
                {Array.from(new Set(sortedChunks.map(c => c.storageNode))).map((nodeName) => {
                  const nodeChunks = sortedChunks.filter(c => c.storageNode === nodeName);
                  const nodeSize = nodeChunks.reduce((sum, c) => sum + c.size, 0);
                  return (
                    <div key={nodeName} className="flex items-center justify-between">
                      <span className="text-sm text-slate-300">{nodeName}</span>
                      <div className="text-right">
                        <span className="text-sm font-medium text-slate-100">{nodeChunks.length} chunks</span>
                        <span className="mx-2 text-slate-600">•</span>
                        <span className="text-sm text-slate-400">{formatBytes(nodeSize)}</span>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
