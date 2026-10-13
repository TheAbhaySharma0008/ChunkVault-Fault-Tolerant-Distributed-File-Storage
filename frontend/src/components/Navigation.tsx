import { Link, useLocation } from 'react-router-dom';

export default function Navigation() {
  const location = useLocation();

  const isActive = (path: string) => location.pathname === path;

  return (
    <nav className="border-b border-slate-800 bg-slate-950/50 backdrop-blur-sm sticky top-0 z-50">
      <div className="mx-auto max-w-7xl px-6">
        <div className="flex items-center justify-between h-16">
          <Link to="/" className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-sky-500 to-sky-600 flex items-center justify-center">
              <span className="text-slate-950 font-bold text-sm">CV</span>
            </div>
            <span className="text-xl font-bold text-slate-100">ChunkVault</span>
          </Link>

          <div className="flex items-center gap-1">
            <Link
              to="/"
              className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                isActive('/')
                  ? 'bg-sky-500/10 text-sky-400'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800'
              }`}
            >
              Home
            </Link>
            <Link
              to="/about"
              className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                isActive('/about')
                  ? 'bg-sky-500/10 text-sky-400'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800'
              }`}
            >
              About
            </Link>
            <Link
              to="/dashboard"
              className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                isActive('/dashboard') || location.pathname.startsWith('/files') || location.pathname.startsWith('/upload')
                  ? 'bg-sky-500/10 text-sky-400'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800'
              }`}
            >
              Dashboard
            </Link>
            <Link
              to="/nodes"
              className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                isActive('/nodes')
                  ? 'bg-sky-500/10 text-sky-400'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800'
              }`}
            >
              Storage
            </Link>
            <Link
              to="/contact"
              className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                isActive('/contact')
                  ? 'bg-sky-500/10 text-sky-400'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800'
              }`}
            >
              Contact
            </Link>
          </div>
        </div>
      </div>
    </nav>
  );
}
