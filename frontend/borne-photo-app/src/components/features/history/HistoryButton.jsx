import React from 'react';
import { useNavigate } from 'react-router-dom';
import { History } from 'lucide-react';

const HistoryButton = () => {
  const navigate = useNavigate();

  return (
    <button 
      onClick={() => navigate('/history')}
      className="absolute top-6 right-6 z-50 flex items-center gap-3 text-neon-magenta transition-all duration-300 group hover:brightness-125"
      title="Galerie des souvenirs"
    >
      {/* Le texte */}
      <span className="text-xs font-mono font-bold tracking-widest uppercase hidden md:inline">
        Galerie
      </span>

      {/* L'icône grossit toujours légèrement au survol */}
      <History size={24} className="group-hover:scale-110 transition-transform duration-300" />
    </button>
  );
};

export default HistoryButton;