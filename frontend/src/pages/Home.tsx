import { Link } from 'react-router-dom';

export default function Home() {
  return (
    <div className="min-h-screen">
      <section className="relative overflow-hidden bg-gradient-to-b from-slate-950 via-slate-900 to-slate-950">
        <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_top,_var(--tw-gradient-stops))] from-sky-900/20 via-slate-950 to-slate-950" />

        <div className="relative mx-auto max-w-7xl px-6 py-24 sm:py-32">
          <div className="mx-auto max-w-3xl text-center">
            <h1 className="text-5xl font-bold tracking-tight text-slate-100 sm:text-7xl">
              ChunkVault
            </h1>
            <p className="mt-6 text-xl leading-8 text-slate-400">
              Reliable distributed file storage, built for resilience.
            </p>
            <p className="mt-4 text-base text-slate-500">
              Upload your files, watch them get chunked and distributed across multiple storage nodes,
              and retrieve them anytime. Simple, fault-tolerant, and scalable.
            </p>
            <div className="mt-10 flex items-center justify-center gap-x-6">
              <Link
                to="/dashboard"
                className="rounded-lg bg-sky-500 px-6 py-3 text-base font-semibold text-slate-950 shadow-sm hover:bg-sky-400 transition-colors"
              >
                Get Started
              </Link>
              <Link
                to="/about"
                className="rounded-lg px-6 py-3 text-base font-semibold text-slate-300 shadow-sm hover:bg-slate-800 transition-colors"
              >
                Learn More
              </Link>
            </div>
          </div>
        </div>
      </section>

      <section className="mx-auto max-w-7xl px-6 py-16 sm:py-24">
        <div className="mx-auto max-w-2xl">
          <h2 className="text-3xl font-bold tracking-tight text-slate-100 sm:text-4xl">
            How It Works
          </h2>
          <p className="mt-4 text-lg text-slate-400">
            ChunkVault breaks your files into smaller chunks and distributes them across multiple
            storage nodes. This ensures fault tolerance and reliability.
          </p>
        </div>

        <div className="mx-auto mt-16 grid max-w-2xl grid-cols-1 gap-x-8 gap-y-12 sm:mt-20 lg:mx-0 lg:max-w-none lg:grid-cols-3">
          <div className="flex flex-col items-start">
            <div className="rounded-lg bg-sky-500/10 p-3">
              <svg className="h-6 w-6 text-sky-400" fill="none" viewBox="0 0 24 24" strokeWidth="1.5" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" d="M3 16.5v2.25A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75V16.5m-13.5-9L12 3m0 0l4.5 4.5M12 3v13.5" />
              </svg>
            </div>
            <h3 className="mt-4 text-lg font-semibold text-slate-100">Upload & Chunk</h3>
            <p className="mt-2 text-base text-slate-400">
              Upload any file and ChunkVault automatically splits it into smaller, manageable chunks.
            </p>
          </div>

          <div className="flex flex-col items-start">
            <div className="rounded-lg bg-sky-500/10 p-3">
              <svg className="h-6 w-6 text-sky-400" fill="none" viewBox="0 0 24 24" strokeWidth="1.5" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" d="M8.25 3v1.5M4.5 8.25H3m18 0h-1.5M4.5 12H3m18 0h-1.5m-15 3.75H3m18 0h-1.5M8.25 19.5V21M12 3v1.5m0 15V21m3.75-18v1.5m0 15V21m-9-1.5h10.5a2.25 2.25 0 002.25-2.25V6.75a2.25 2.25 0 00-2.25-2.25H6.75A2.25 2.25 0 004.5 6.75v10.5a2.25 2.25 0 002.25 2.25z" />
              </svg>
            </div>
            <h3 className="mt-4 text-lg font-semibold text-slate-100">Distribute</h3>
            <p className="mt-2 text-base text-slate-400">
              Chunks are distributed across multiple storage nodes using round-robin distribution.
            </p>
          </div>

          <div className="flex flex-col items-start">
            <div className="rounded-lg bg-sky-500/10 p-3">
              <svg className="h-6 w-6 text-sky-400" fill="none" viewBox="0 0 24 24" strokeWidth="1.5" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" d="M9 12.75L11.25 15 15 9.75M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <h3 className="mt-4 text-lg font-semibold text-slate-100">Retrieve & Reconstruct</h3>
            <p className="mt-2 text-base text-slate-400">
              Download your files anytime. Chunks are retrieved and reconstructed to the original file.
            </p>
          </div>
        </div>
      </section>

      <section className="border-t border-slate-800 bg-slate-950">
        <div className="mx-auto max-w-7xl px-6 py-12">
          <div className="grid grid-cols-1 gap-8 sm:grid-cols-3">
            <div>
              <h3 className="text-sm font-semibold text-slate-400">Fault Tolerance</h3>
              <p className="mt-2 text-sm text-slate-500">
                Distributed storage across multiple nodes reduces single-point failure risk.
              </p>
            </div>
            <div>
              <h3 className="text-sm font-semibold text-slate-400">Metadata Persistence</h3>
              <p className="mt-2 text-sm text-slate-500">
                All file and chunk metadata is stored in a MySQL database for reliability.
              </p>
            </div>
            <div>
              <h3 className="text-sm font-semibold text-slate-400">OS + DBMS Concepts</h3>
              <p className="mt-2 text-sm text-slate-500">
                Built using distributed storage principles and relational database management.
              </p>
            </div>
          </div>
          <div className="mt-8 border-t border-slate-800 pt-8 text-center text-sm text-slate-500">
            <p>ChunkVault Phase 2 — OS + DBMS Academic Project</p>
          </div>
        </div>
      </section>
    </div>
  );
}
