import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { PhotoProvider } from './context/PhotoContext';

import CapturePage from './pages/CapturePage';
import AudioPage from './pages/AudioPage';
import AdminPage from './pages/AdminPage';
import HistoryPage from './pages/HistoryPage'; 
import EnregistrementPage from './pages/EnregistrementPage'; 

function App() {
  return (
    <PhotoProvider>
      <Router>
        <div className="...">
          <Routes>
            <Route path="/" element={<CapturePage />} />
            <Route path="/record" element={<AudioPage />} />
            <Route path="/admin" element={<AdminPage />} />
            <Route path="/history" element={<HistoryPage />} />
            <Route path="/result" element={<EnregistrementPage />} /> 
            
            
          </Routes>
        </div>
      </Router>
    </PhotoProvider>
  );
}
export default App;