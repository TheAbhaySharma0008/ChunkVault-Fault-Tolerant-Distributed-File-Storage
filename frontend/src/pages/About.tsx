export default function About() {
  return (
    <div className="min-h-screen">
      <div className="mx-auto max-w-7xl px-6 py-12">
        <div className="mx-auto max-w-3xl">
          <h1 className="text-4xl font-bold tracking-tight text-slate-100">About ChunkVault</h1>
          <p className="mt-4 text-lg text-slate-400">
            A fault-tolerant distributed file storage system built as an academic project.
          </p>
        </div>

        <div className="mx-auto mt-12 max-w-3xl space-y-12">
          <section>
            <h2 className="text-2xl font-semibold text-slate-100">What is ChunkVault?</h2>
            <p className="mt-4 text-base text-slate-400 leading-relaxed">
              ChunkVault is a distributed file storage system that breaks files into smaller chunks and
              distributes them across multiple storage nodes. This approach provides fault tolerance
              and reliability by ensuring that no single point of failure can compromise the entire
              storage system.
            </p>
            <p className="mt-2 text-base text-slate-400 leading-relaxed">
              The project demonstrates key concepts in operating systems (distributed storage, file
              chunking, node management) and database management systems (metadata persistence,
              relational data modeling, transactional operations).
            </p>
          </section>

          <section>
            <h2 className="text-2xl font-semibold text-slate-100">Why We Built It</h2>
            <p className="mt-4 text-base text-slate-400 leading-relaxed">
              This project was developed as part of an academic curriculum to understand and implement
              distributed storage principles. It explores how large files can be efficiently stored,
              managed, and retrieved across multiple storage locations while maintaining data integrity
              and availability.
            </p>
          </section>

          <section>
            <h2 className="text-2xl font-semibold text-slate-100">Distributed Storage Concept</h2>
            <p className="mt-4 text-base text-slate-400 leading-relaxed">
              Distributed storage systems store data across multiple physical or logical storage nodes.
              ChunkVault implements this by:
            </p>
            <ul className="mt-3 space-y-2 text-base text-slate-400 list-disc list-inside">
              <li>Splitting files into fixed-size chunks</li>
              <li>Distributing chunks across multiple storage nodes using round-robin</li>
              <li>Storing metadata to track chunk locations</li>
              <li>Reconstructing files by retrieving and reassembling chunks</li>
            </ul>
          </section>

          <section>
            <h2 className="text-2xl font-semibold text-slate-100">OS Concepts Used</h2>
            <ul className="mt-4 space-y-2 text-base text-slate-400 list-disc list-inside">
              <li>File systems and file I/O operations</li>
              <li>Process management and concurrent operations</li>
              <li>Distributed system architecture</li>
              <li>Storage node management and routing</li>
              <li>Fault tolerance and error handling</li>
            </ul>
          </section>

          <section>
            <h2 className="text-2xl font-semibold text-slate-100">DBMS Concepts Used</h2>
            <ul className="mt-4 space-y-2 text-base text-slate-400 list-disc list-inside">
              <li>Relational database design with MySQL</li>
              <li>Entity-relationship modeling (Files, Chunks, Storage Nodes)</li>
              <li>JPA/Hibernate ORM for object-relational mapping</li>
              <li>Transaction management for data consistency</li>
              <li>Query optimization with indexes and foreign keys</li>
            </ul>
          </section>

          <section>
            <h2 className="text-2xl font-semibold text-slate-100">Technology Stack</h2>
            <div className="mt-4 grid grid-cols-1 gap-4 sm:grid-cols-2">
              <div className="rounded-lg border border-slate-800 bg-slate-900/50 p-4">
                <h3 className="font-semibold text-slate-100">Backend</h3>
                <p className="mt-2 text-sm text-slate-400">Java 17, Spring Boot 3.2, JPA/Hibernate, MySQL</p>
              </div>
              <div className="rounded-lg border border-slate-800 bg-slate-900/50 p-4">
                <h3 className="font-semibold text-slate-100">Frontend</h3>
                <p className="mt-2 text-sm text-slate-400">React 18, TypeScript, Tailwind CSS, Vite</p>
              </div>
            </div>
          </section>

          <section>
            <h2 className="text-2xl font-semibold text-slate-100">Current Phase</h2>
            <p className="mt-4 text-base text-slate-400 leading-relaxed">
              ChunkVault is currently in Phase 2 of development. This phase includes persistent
              metadata storage using MySQL, comprehensive file management features, node monitoring,
              and a professional user interface. Future phases will explore advanced replication
              strategies, automatic failover, and cloud deployment options.
            </p>
          </section>
        </div>
      </div>
    </div>
  );
}
