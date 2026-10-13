import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { listFiles, listNodes } from '../api';
import type { FileMetadata, NodeInfo } from '../types';

function formatBytes(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(2)} MB`;
}

export default function Dashboard() {
  const [files, setFiles] = useState<FileMetadata[]>([]);
  const [nodes, setNodes] = useState<NodeInfo[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadData() {
      try {
        const [filesData, nodesData] = await Promise.all([listFiles(), listNodes()]);
        setFiles(filesData);
        setNodes(nodesData);
      } catch (err) {
        console.error('Failed to load dashboard data:', err);
      } finally {
        setLoading(false);
      }
    }
    loadData();
  }, []);

  const totalFiles = files.length;
  const totalChunks = files.reduce((sum, f) => sum + f.chunks.length, 0);
  const totalStorage = files.reduce((sum, f) => sum + f.fileSize, 0);
  const activeNodes = nodes.filter(n => n.status === 'ACTIVE').length;

  const recentFiles = [...files].sort((a, b) => {
    const dateA = a.uploadTimestamp ? new Date(a.uploadTimestamp).getTime() : 0;
    const dateB = b.uploadTimestamp ? new Date(b.uploadTimestamp).getTime() : 0;
    return dateB - dateA;
  }).slice(0, 5);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-slate-400">Loading dashboard...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen">
      <div className="mx-auto max-w-7xl px-6 py-12">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-4xl font-bold tracking-tight text-slate-100">Dashboard</h1>
            <p className="mt-2 text-lg text-slate-400">Overview of your storage system</p>
          </div>
          <Link
            to="/upload"
            className="rounded-lg bg-sky-500 px-4 py-2 text-sm font-semibold text-slate-950 hover:bg-sky-400 transition-colors"
          >
            Upload File
          </Link>
        </div>

        <div className="mt-8 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
          <div className="rounded-2xl border border-slate-800 bg-slate-900/50 p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-slate-400">Total Files</p>
                <p className="mt-2 text-3xl font-bold text-slate-100">{totalFiles}</p>
              </div>
              <div className="rounded-lg bg-sky-500/10 p-3">
                <svg className="h-6 w-6 text-sky-400" fill="none" viewBox="0 0 24 24" strokeWidth="1.5" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m2.25 0H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z" />
                </svg>
              </div>
            </div>
          </div>

          <div className="rounded-2xl border border-slate-800 bg-slate-900/50 p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-slate-400">Storage Used</p>
                <p className="mt-2 text-3xl font-bold text-slate-100">{formatBytes(totalStorage)}</p>
              </div>
              <div className="rounded-lg bg-emerald-500/10 p-3">
                <svg className="h-6 w-6 text-emerald-400" fill="none" viewBox="0 0 24 24" strokeWidth="1.5" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M20.25 6.375c0 2.278-3.694 4.125-8.25 4.125S3.75 8.653 3.75 6.375m16.5 0c0-2.278-3.694-4.125-8.25-4.125S3.75 4.097 3.75 6.375m16.5 0v11.25c0 2.278-3.694 4.125-8.25 4.125s-8.25-1.847-8.25-4.125V6.375m16.5 0v3.75m-16.5-3.75v3.75m16.5 0v3.75C20.25 16.153 16.556 18 12 18s-8.25-1.847-8.25-4.125v-3.75m16.5 0c0 2.278-3.694 4.125-8.25 4.125s-8.25-1.847-8.25-4.125" />
                </svg>
              </div>
            </div>
          </div>

          <div className="rounded-2xl border border-slate-800 bg-slate-900/50 p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-slate-400">Total Chunks</p>
                <p className="mt-2 text-3xl font-bold text-slate-100">{totalChunks}</p>
              </div>
              <div className="rounded-lg bg-purple-500/10 p-3">
                <svg className="h-6 w-6 text-purple-400" fill="none" viewBox="0 0 24 24" strokeWidth="1.5" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M3.75 6A2.25 2.25 0 016 3.75h2.25A2.25 2.25 0 0110.5 6v2.25a2.25 2.25 0 01-2.25 2.25H6a2.25 2.25 0 01-2.25-2.25V6zM3.75 15.75A2.25 2.25 0 016 13.5h2.25a2.25 2.25 0 012.25 2.25V18a2.25 2.25 0 01-2.25 2.25H6A2.25 2.25 0 013.75 18v-2.25zM13.5 6a2.25 2.25 0 012.25-2.25H18A2.25 2.25 0 0120.25 6v2.25A2.25 2.25 0 0118 10.5h-2.25a2.25 2.25 0 01-2.25-2.25V6zM13.5 15.75a2.25 2.25 0 012.25-2.25H18a2.25 2.25 0 012.25 2.25V18A2.25 2.25 0 0118 20.25h-2.25A2.25 2.25 0 0113.5 18v-2.25z" />
                </svg>
              </div>
            </div>
          </div>

          <div className="rounded-2xl border border-slate-800 bg-slate-900/50 p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-slate-400">Active Nodes</p>
                <p className="mt-2 text-3xl font-bold text-slate-100">{activeNodes} / {nodes.length}</p>
              </div>
              <div className="rounded-lg bg-amber-500/10 p-3">
                <svg className="h-6 w-6 text-amber-400" fill="none" viewBox="0 0 24 24" strokeWidth="1.5" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M5.25 14.25h13.5m-13.5 0a3 3 0 01-3-3m3 3a3 3 0 013 3h13.5a3 3 0 003-3m-16.5-3a3 3 0 013-3h13.5a3 3 0 013 3m-16.5 0v1.5a1.5 1.5 0 001.5 1.5h13.5a1.5 1.5 0 001.5-1.5V12" />
                </svg>
              </div>
            </div>
          </div>
        </div>

        <div className="mt-8 grid grid-cols-1 gap-6 lg:grid-cols-2">
          <div className="rounded-2xl border border-slate-800 bg-slate-900/50 p-6">
            <div className="flex items-center justify-between">
              <h2 className="text-xl font-semibold text-slate-100">Recent Uploads</h2>
              <Link to="/files" className="text-sm text-sky-400 hover:text-sky-300">
                View All
              </Link>
            </div>
            <div className="mt-4 space-y-3">
              {recentFiles.length === 0 ? (
                <p className="text-sm text-slate-500">No files uploaded yet</p>
              ) : (
                recentFiles.map((file) => (
                  <Link
                    key={file.fileId}
                    to={`/files/${file.fileId}`}
                    className="flex items-center justify-between rounded-lg border border-slate-800 bg-slate-950/50 p-3 hover:border-slate-700 transition-colors"
                  >
                    <div className="flex-1 min-w-0">
                      <p className="text-sm font-medium text-slate-100 truncate">{file.fileName}</p>
                      <p className="text-xs text-slate-500">{formatBytes(file.fileSize)} • {file.chunks.length} chunks</p>
                    </div>
                    <svg className="ml-2 h-4 w-4 text-slate-500" fill="none" viewBox="0 0 24 24" strokeWidth="2" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" d="M8.25 4.5l7.5 7.5-7.5 7.5" />
                    </svg>
                  </Link>
                ))
              )}
            </div>
          </div>

          <div className="rounded-2xl border border-slate-800 bg-slate-900/50 p-6">
            <div className="flex items-center justify-between">
              <h2 className="text-xl font-semibold text-slate-100">Storage Nodes</h2>
              <Link to="/nodes" className="text-sm text-sky-400 hover:text-sky-300">
                View All
              </Link>
            </div>
            <div className="mt-4 space-y-3">
              {nodes.map((node) => (
                <div
                  key={node.nodeName}
                  className="flex items-center justify-between rounded-lg border border-slate-800 bg-slate-950/50 p-3"
                >
                  <div>
                    <p className="text-sm font-medium text-slate-100">{node.nodeName}</p>
                    <p className="text-xs text-slate-500">{node.chunkCount} chunks • {formatBytes(node.storageUsed)}</p>
                  </div>
                  <span className={`rounded-full px-2 py-1 text-xs font-medium ${
                    node.health === 'HEALTHY' ? 'bg-emerald-500/10 text-emerald-400' : 'bg-red-500/10 text-red-400'
                  }`}>
                    {node.health}
                  </span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
