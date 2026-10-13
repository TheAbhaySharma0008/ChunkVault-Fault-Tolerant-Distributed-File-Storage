import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { uploadFile } from '../api';
import type { UploadResponse } from '../types';

function formatBytes(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(2)} MB`;
}

export default function Upload() {
  const navigate = useNavigate();
  const [file, setFile] = useState<File | null>(null);
  const [dragActive, setDragActive] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [result, setResult] = useState<UploadResponse | null>(null);
  const [error, setError] = useState('');

  const handleDrag = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === 'dragenter' || e.type === 'dragover') {
      setDragActive(true);
    } else if (e.type === 'dragleave') {
      setDragActive(false);
    }
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);

    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      setFile(e.dataTransfer.files[0]);
    }
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      setFile(e.target.files[0]);
    }
  };

  const handleUpload = async () => {
    if (!file) return;

    setUploading(true);
    setError('');
    setResult(null);

    try {
      const response = await uploadFile(file);
      setResult(response);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Upload failed');
    } finally {
      setUploading(false);
    }
  };

  const handleViewFile = () => {
    if (result) {
      navigate(`/files/${result.fileId}`);
    }
  };

  const handleReset = () => {
    setFile(null);
    setResult(null);
    setError('');
  };

  return (
    <div className="min-h-screen">
      <div className="mx-auto max-w-7xl px-6 py-12">
        <div className="mb-6">
          <h1 className="text-4xl font-bold tracking-tight text-slate-100">Upload File</h1>
          <p className="mt-2 text-lg text-slate-400">Upload a file to be chunked and distributed across storage nodes</p>
        </div>

        {!result ? (
          <div className="max-w-2xl">
            <div
              className={`rounded-2xl border-2 border-dashed p-12 text-center transition-colors ${
                dragActive
                  ? 'border-sky-500 bg-sky-500/5'
                  : 'border-slate-700 bg-slate-900/50'
              }`}
              onDragEnter={handleDrag}
              onDragLeave={handleDrag}
              onDragOver={handleDrag}
              onDrop={handleDrop}
            >
              <input
                type="file"
                id="file-upload"
                className="hidden"
                onChange={handleFileChange}
                disabled={uploading}
              />
              <label htmlFor="file-upload" className="cursor-pointer">
                <div className="mx-auto mb-4 h-16 w-16 rounded-full bg-slate-800 flex items-center justify-center">
                  <svg className="h-8 w-8 text-slate-400" fill="none" viewBox="0 0 24 24" strokeWidth="1.5" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M3 16.5v2.25A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75V16.5m-13.5-9L12 3m0 0l4.5 4.5M12 3v13.5" />
                  </svg>
                </div>
                <p className="text-lg font-medium text-slate-100">
                  {file ? file.name : 'Drop your file here or click to browse'}
                </p>
                <p className="mt-2 text-sm text-slate-500">
                  {file ? `${formatBytes(file.size)}` : 'Maximum file size: 100 MB'}
                </p>
              </label>
            </div>

            {file && (
              <div className="mt-6 rounded-2xl border border-slate-800 bg-slate-900/50 p-6">
                <div className="flex items-center justify-between">
                  <div className="flex-1 min-w-0">
                    <p className="text-lg font-medium text-slate-100 truncate">{file.name}</p>
                    <p className="mt-1 text-sm text-slate-500">{formatBytes(file.size)} • {file.type || 'Unknown type'}</p>
                  </div>
                  <button
                    onClick={handleReset}
                    className="ml-4 rounded-lg border border-slate-700 px-4 py-2 text-sm font-medium text-slate-300 hover:bg-slate-800 transition-colors"
                  >
                    Change
                  </button>
                </div>

                <button
                  onClick={handleUpload}
                  disabled={uploading}
                  className="mt-6 w-full rounded-lg bg-sky-500 px-4 py-3 text-base font-semibold text-slate-950 hover:bg-sky-400 disabled:cursor-not-allowed disabled:opacity-40 transition-colors"
                >
                  {uploading ? 'Uploading...' : 'Upload File'}
                </button>
              </div>
            )}

            {error && (
              <div className="mt-4 rounded-lg border border-red-800 bg-red-950/40 p-4 text-sm text-red-300">
                {error}
              </div>
            )}
          </div>
        ) : (
          <div className="max-w-2xl space-y-6">
            <div className="rounded-2xl border border-emerald-800 bg-emerald-950/40 p-6">
              <div className="flex items-center gap-3">
                <div className="rounded-full bg-emerald-500/10 p-2">
                  <svg className="h-6 w-6 text-emerald-400" fill="none" viewBox="0 0 24 24" strokeWidth="1.5" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M9 12.75L11.25 15 15 9.75M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
                <div>
                  <h2 className="text-lg font-semibold text-slate-100">Upload Successful</h2>
                  <p className="text-sm text-slate-400">Your file has been chunked and distributed</p>
                </div>
              </div>
            </div>

            <div className="rounded-2xl border border-slate-800 bg-slate-900/50 p-6">
              <h3 className="text-lg font-semibold text-slate-100">{result.fileName}</h3>
              <p className="mt-1 text-sm font-mono text-slate-500">{result.fileId}</p>

              <div className="mt-6 grid grid-cols-3 gap-4">
                <div className="rounded-lg bg-slate-950 p-4 text-center">
                  <p className="text-xs uppercase text-slate-500">File Size</p>
                  <p className="mt-1 text-lg font-semibold text-slate-100">{formatBytes(result.fileSize)}</p>
                </div>
                <div className="rounded-lg bg-slate-950 p-4 text-center">
                  <p className="text-xs uppercase text-slate-500">Chunks</p>
                  <p className="mt-1 text-lg font-semibold text-slate-100">{result.chunkCount}</p>
                </div>
                <div className="rounded-lg bg-slate-950 p-4 text-center">
                  <p className="text-xs uppercase text-slate-500">Chunk Size</p>
                  <p className="mt-1 text-lg font-semibold text-slate-100">{formatBytes(result.chunkSizeBytes)}</p>
                </div>
              </div>

              <div className="mt-6 flex gap-3">
                <button
                  onClick={handleViewFile}
                  className="flex-1 rounded-lg bg-sky-500 px-4 py-2.5 text-sm font-semibold text-slate-950 hover:bg-sky-400 transition-colors"
                >
                  View File Details
                </button>
                <button
                  onClick={handleReset}
                  className="flex-1 rounded-lg border border-slate-700 px-4 py-2.5 text-sm font-semibold text-slate-300 hover:bg-slate-800 transition-colors"
                >
                  Upload Another
                </button>
              </div>
            </div>

            <div className="rounded-2xl border border-slate-800 bg-slate-900/50 p-6">
              <h3 className="text-lg font-semibold text-slate-100">Chunk Distribution</h3>
              <div className="mt-4 max-h-48 overflow-y-auto">
                <table className="w-full text-left text-sm">
                  <thead className="border-b border-slate-800 text-slate-400">
                    <tr>
                      <th className="px-3 py-2">Sequence</th>
                      <th className="px-3 py-2">Chunk ID</th>
                      <th className="px-3 py-2">Node</th>
                      <th className="px-3 py-2">Size</th>
                    </tr>
                  </thead>
                  <tbody>
                    {result.chunks.map((chunk) => (
                      <tr key={chunk.chunkId} className="border-b border-slate-800">
                        <td className="px-3 py-2">{chunk.sequenceNumber}</td>
                        <td className="px-3 py-2 font-mono text-xs">{chunk.chunkId}</td>
                        <td className="px-3 py-2">
                          <span className="rounded-full bg-sky-950 px-2 py-0.5 text-xs text-sky-300">
                            {chunk.storageNode}
                          </span>
                        </td>
                        <td className="px-3 py-2">{formatBytes(chunk.size)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
