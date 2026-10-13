import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Navigation from './components/Navigation';
import Home from './pages/Home';
import About from './pages/About';
import Contact from './pages/Contact';
import Dashboard from './pages/Dashboard';
import Files from './pages/Files';
import FileDetails from './pages/FileDetails';
import StorageNodes from './pages/StorageNodes';
import Upload from './pages/Upload';

export default function App() {
  return (
    <Router>
      <div className="min-h-screen bg-slate-950">
        <Navigation />
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/about" element={<About />} />
          <Route path="/contact" element={<Contact />} />
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/files" element={<Files />} />
          <Route path="/files/:fileId" element={<FileDetails />} />
          <Route path="/nodes" element={<StorageNodes />} />
          <Route path="/upload" element={<Upload />} />
        </Routes>
      </div>
    </Router>
  );
}
