import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { listFiles, deleteFile, downloadUrl } from '../api';
import type { FileMetadata } from '../types';

function formatBytes(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(2)} MB`;
}

export default function Files() {
  const [files, setFiles] = useState<FileMetadata[]>([]);
  const [filteredFiles, setFilteredFiles] = useState<FileMetadata[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    async function loadFiles() {
      try {
        const data = await listFiles();
        setFiles(data);
        setFilteredFiles(data);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to load files');
      } finally {
        setLoading(false);
      }
    }
    loadFiles();
  }, []);

  useEffect(() => {
    if (searchQuery.trim() === '') {
      setFilteredFiles(files);
    } else {
      const query = searchQuery.toLowerCase();
      setFilteredFiles(
        files.filter(f => f.fileName.toLowerCase().includes(query) || f.fileId.toLowerCase().includes(query))
      );
    }
  }, [searchQuery, files]);

  const handleDelete = async (fileId: string, fileName: string) => {
    if (!confirm(`Are you sure you want to delete "${fileName}"? This action cannot be undone.`)) {
      return;
    }

    try {
      await deleteFile(fileId);
      setFiles(files.filter(f => f.fileId !== fileId));
      setFilteredFiles(filteredFiles.filter(f => f.fileId !== fileId));
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to delete file');
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-slate-400">Loading files...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen">
      <div className="mx-auto max-w-7xl px-6 py-12">
        <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h1 className="text-4xl font-bold tracking-tight text-slate-100">My Files</h1>
            <p className="mt-2 text-lg text-slate-400">Manage your uploaded files</p>
          </div>
          <Link
            to="/upload"
            className="inline-flex items-center justify-center rounded-lg bg-sky-500 px-4 py-2 text-sm font-semibold text-slate-950 hover:bg-sky-400 transition-colors"
          >
            Upload New File
          </Link>
        </div>

        <div className="mt-8">
          <div className="rounded-2xl border border-slate-800 bg-slate-900/50 p-4">
            <input
              type="text"
              placeholder="Search files by name or ID..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full rounded-lg border border-slate-700 bg-slate-950 px-4 py-2 text-sm text-slate-100 placeholder-slate-500 focus:border-sky-500 focus:outline-none focus:ring-1 focus:ring-sky-500"
            />
          </div>
        </div>

        {error && (
          <div className="mt-4 rounded-lg border border-red-800 bg-red-950/40 p-3 text-sm text-red-300">
            {error}
          </div>
        )}

        <div className="mt-6">
          {filteredFiles.length === 0 ? (
            <div className="rounded-2xl border border-slate-800 bg-slate-900/50 p-12 text-center">
              <svg className="mx-auto h-12 w-12 text-slate-600" fill="none" viewBox="0 0 24 24" strokeWidth="1.5" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m2.25 0H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z" />
              </svg>
              <h3 className="mt-4 text-lg font-semibold text-slate-100">No files found</h3>
              <p className="mt-2 text-slate-400">
                {files.length === 0 ? 'Upload your first file to get started.' : 'Try a different search term.'}
              </p>
              {files.length === 0 && (
                <Link
                  to="/upload"
                  className="mt-4 inline-flex items-center justify-center rounded-lg bg-sky-500 px-4 py-2 text-sm font-semibold text-slate-950 hover:bg-sky-400 transition-colors"
                >
                  Upload File
                </Link>
              )}
            </div>
          ) : (
            <div className="rounded-2xl border border-slate-800 bg-slate-900/50 overflow-hidden">
              <div className="overflow-x-auto">
                <table className="w-full text-left text-sm">
                  <thead className="border-b border-slate-800 bg-slate-950/50 text-slate-400">
                    <tr>
                      <th className="px-6 py-4 font-medium">File Name</th>
                      <th className="px-6 py-4 font-medium">Size</th>
                      <th className="px-6 py-4 font-medium">Chunks</th>
                      <th className="px-6 py-4 font-medium">Uploaded</th>
                      <th className="px-6 py-4 font-medium">Status</th>
                      <th className="px-6 py-4 font-medium text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredFiles.map((file) => (
                      <tr key={file.fileId} className="border-b border-slate-800 hover:bg-slate-800/30 transition-colors">
                        <td className="px-6 py-4">
                          <Link
                            to={`/files/${file.fileId}`}
                            className="font-medium text-slate-100 hover:text-sky-400 transition-colors"
                          >
                            {file.fileName}
                          </Link>
                          <p className="mt-1 text-xs text-slate-500 font-mono">{file.fileId}</p>
                        </td>
                        <td className="px-6 py-4 text-slate-300">{formatBytes(file.fileSize)}</td>
                        <td className="px-6 py-4 text-slate-300">{file.chunks.length}</td>
                        <td className="px-6 py-4 text-slate-300">
                          {file.uploadTimestamp ? new Date(file.uploadTimestamp).toLocaleDateString() : 'N/A'}
                        </td>
                        <td className="px-6 py-4">
                          <span className="rounded-full bg-emerald-500/10 px-2 py-1 text-xs font-medium text-emerald-400">
                            {file.status || 'ACTIVE'}
                          </span>
                        </td>
                        <td className="px-6 py-4 text-right">
                          <div className="flex items-center justify-end gap-2">
                            <a
                              href={downloadUrl(file.fileId)}
                              className="rounded-lg bg-slate-800 px-3 py-1.5 text-xs font-medium text-slate-300 hover:bg-slate-700 transition-colors"
                              title="Download"
                            >
                              Download
                            </a>
                            <Link
                              to={`/files/${file.fileId}`}
                              className="rounded-lg bg-slate-800 px-3 py-1.5 text-xs font-medium text-slate-300 hover:bg-slate-700 transition-colors"
                              title="View Details"
                            >
                              Details
                            </Link>
                            <button
                              onClick={() => handleDelete(file.fileId, file.fileName)}
                              className="rounded-lg bg-red-500/10 px-3 py-1.5 text-xs font-medium text-red-400 hover:bg-red-500/20 transition-colors"
                              title="Delete"
                            >
                              Delete
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
