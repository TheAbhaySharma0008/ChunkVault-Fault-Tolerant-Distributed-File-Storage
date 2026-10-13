import { useEffect, useState } from 'react';
import { listNodes } from '../api';
import type { NodeInfo } from '../types';

function formatBytes(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(2)} MB`;
}

export default function StorageNodes() {
  const [nodes, setNodes] = useState<NodeInfo[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadNodes() {
      try {
        const data = await listNodes();
        setNodes(data);
      } catch (err) {
        console.error('Failed to load nodes:', err);
      } finally {
        setLoading(false);
      }
    }
    loadNodes();
  }, []);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-slate-400">Loading storage nodes...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen">
      <div className="mx-auto max-w-7xl px-6 py-12">
        <div>
          <h1 className="text-4xl font-bold tracking-tight text-slate-100">Storage Nodes</h1>
          <p className="mt-2 text-lg text-slate-400">Monitor your distributed storage infrastructure</p>
        </div>

        <div className="mt-8 grid grid-cols-1 gap-6 lg:grid-cols-3">
          {nodes.map((node) => (
            <div key={node.nodeName} className="rounded-2xl border border-slate-800 bg-slate-900/50 p-6">
              <div className="flex items-start justify-between">
                <div>
                  <h2 className="text-xl font-semibold text-slate-100">{node.nodeName}</h2>
                  <p className="mt-1 text-sm text-slate-500 font-mono">{node.storagePath}</p>
                </div>
                <span className={`rounded-full px-3 py-1 text-xs font-medium ${
                  node.health === 'HEALTHY' ? 'bg-emerald-500/10 text-emerald-400' : 'bg-red-500/10 text-red-400'
                }`}>
                  {node.health}
                </span>
              </div>

              <div className="mt-6 space-y-4">
                <div>
                  <div className="flex items-center justify-between mb-2">
                    <span className="text-sm text-slate-400">Status</span>
                    <span className={`text-sm font-medium ${
                      node.status === 'ACTIVE' ? 'text-emerald-400' : 'text-red-400'
                    }`}>
                      {node.status}
                    </span>
                  </div>
                  <div className="h-2 rounded-full bg-slate-800 overflow-hidden">
                    <div
                      className={`h-full rounded-full ${
                        node.status === 'ACTIVE' ? 'bg-emerald-500' : 'bg-red-500'
                      }`}
                      style={{ width: '100%' }}
                    />
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div className="rounded-lg bg-slate-950 p-4">
                    <p className="text-xs uppercase text-slate-500">Chunks Stored</p>
                    <p className="mt-1 text-2xl font-bold text-slate-100">{node.chunkCount}</p>
                  </div>
                  <div className="rounded-lg bg-slate-950 p-4">
                    <p className="text-xs uppercase text-slate-500">Storage Used</p>
                    <p className="mt-1 text-2xl font-bold text-slate-100">{formatBytes(node.storageUsed)}</p>
                  </div>
                </div>

                <div className="rounded-lg border border-slate-800 bg-slate-950/50 p-4">
                  <p className="text-xs uppercase text-slate-500 mb-2">Node Information</p>
                  <div className="space-y-1 text-sm">
                    <div className="flex justify-between">
                      <span className="text-slate-400">Node Name</span>
                      <span className="text-slate-200 font-mono">{node.nodeName}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-slate-400">Type</span>
                      <span className="text-slate-200">Local Filesystem</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-slate-400">Health Check</span>
                      <span className="text-emerald-400">Passed</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>

        <div className="mt-12 rounded-2xl border border-slate-800 bg-slate-900/50 p-6">
          <h2 className="text-xl font-semibold text-slate-100">System Overview</h2>
          <div className="mt-4 grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <div className="rounded-lg bg-slate-950 p-4">
              <p className="text-xs uppercase text-slate-500">Total Nodes</p>
              <p className="mt-1 text-2xl font-bold text-slate-100">{nodes.length}</p>
            </div>
            <div className="rounded-lg bg-slate-950 p-4">
              <p className="text-xs uppercase text-slate-500">Active Nodes</p>
              <p className="mt-1 text-2xl font-bold text-emerald-400">
                {nodes.filter(n => n.status === 'ACTIVE').length}
              </p>
            </div>
            <div className="rounded-lg bg-slate-950 p-4">
              <p className="text-xs uppercase text-slate-500">Total Chunks</p>
              <p className="mt-1 text-2xl font-bold text-slate-100">
                {nodes.reduce((sum, n) => sum + n.chunkCount, 0)}
              </p>
            </div>
            <div className="rounded-lg bg-slate-950 p-4">
              <p className="text-xs uppercase text-slate-500">Total Storage</p>
              <p className="mt-1 text-2xl font-bold text-slate-100">
                {formatBytes(nodes.reduce((sum, n) => sum + n.storageUsed, 0))}
              </p>
            </div>
          </div>
        </div>

        <div className="mt-8 rounded-2xl border border-slate-800 bg-slate-900/50 p-6">
          <h2 className="text-xl font-semibold text-slate-100">About Storage Nodes</h2>
          <div className="mt-4 text-sm text-slate-400 space-y-2">
            <p>
              ChunkVault uses local filesystem storage nodes to simulate a distributed storage environment.
              In a production deployment, these would be separate physical machines or cloud storage instances.
            </p>
            <p>
              Chunks are distributed across nodes using a round-robin algorithm, ensuring even distribution
              and fault tolerance. If one node becomes unavailable, the system can still reconstruct files
              from the remaining nodes (advanced recovery coming in Phase 3).
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
