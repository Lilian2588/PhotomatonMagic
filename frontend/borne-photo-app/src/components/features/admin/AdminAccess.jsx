import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { Settings, Lock, X, ChevronRight, AlertTriangle } from 'lucide-react';

const AdminAccess = () => {
  const navigate = useNavigate();
  
  // --- ÉTATS ---
  const [isOpen, setIsOpen] = useState(false); // Est-ce que le popup est ouvert ?
  const [code, setCode] = useState("");        // Ce que l'utilisateur tape
  const [error, setError] = useState(false);   // Code faux ?
  
  const inputRef = useRef(null);

  // Focus automatique sur le champ quand on ouvre le popup
  useEffect(() => {
    if (isOpen && inputRef.current) {
      inputRef.current.focus();
    }
  }, [isOpen]);

  // --- LOGIQUE ---
  const handleOpen = () => {
    setIsOpen(true);
    setCode("");
    setError(false);
  };

  const handleClose = () => {
    setIsOpen(false);
    setCode("");
    setError(false);
  };

  const handleSubmit = (e) => {
    e.preventDefault(); // Empêche le rechargement de page
    
    if (code === "1234") {
      // SUCCÈS
      setIsOpen(false);
      navigate('/admin');
    } else {
      // ERREUR
      setError(true);
      setCode(""); // On vide le champ
      
      // Petit effet visuel : on remet l'erreur à false après l'animation
      setTimeout(() => setError(false), 2000);
    }
  };

  return (
    <>
      {/* 1. LE BOUTON DÉCLENCHEUR (Celui en haut à gauche) */}
      <button 
        onClick={handleOpen}
        className="absolute top-6 left-6 z-50 flex items-center gap-3 text-white/40 hover:text-neon-cyan transition-all duration-300 group"
        title="Accès Administrateur"
      >
        <Settings size={24} className="group-hover:rotate-90 transition-transform duration-500" />
        <span className="text-xs font-mono font-bold tracking-widest uppercase hidden md:inline opacity-70 group-hover:opacity-100">
          Admin
        </span>
      </button>

      {/* 2. LE POP-UP (MODALE) */}
      {isOpen && (
        <div className="fixed inset-0 z-[9999] flex items-center justify-center bg-black/80 backdrop-blur-md animate-fade-in">
          
          {/* La Boîte (Card) */}
          <div className="w-full max-w-sm bg-gray-900 border border-gray-700 shadow-[0_0_50px_rgba(0,0,0,0.8)] rounded-2xl p-6 relative animate-scale-up">
            
            {/* Bouton Fermer (X) */}
            <button 
              onClick={handleClose}
              className="absolute top-4 right-4 text-gray-500 hover:text-white transition-colors"
            >
              <X size={20} />
            </button>

            {/* En-tête */}
            <div className="flex flex-col items-center mb-6">
              <div className={`p-3 rounded-full mb-3 transition-colors duration-300 ${error ? 'bg-red-500/20 text-red-500' : 'bg-neon-cyan/10 text-neon-cyan'}`}>
                {error ? <AlertTriangle size={32} /> : <Lock size={32} />}
              </div>
              <h2 className="text-xl font-tech text-white tracking-wider">
                SÉCURITÉ
              </h2>
              <p className="text-xs text-gray-400 font-mono mt-1">
                Authentification requise
              </p>
            </div>

            {/* Formulaire */}
            <form onSubmit={handleSubmit} className="space-y-4">
              
              {/* Champ Code */}
              <div className="relative">
                <input
                  ref={inputRef}
                  type="password"
                  value={code}
                  onChange={(e) => {
                    setCode(e.target.value);
                    if(error) setError(false); // Enlève l'erreur dès qu'on retape
                  }}
                  placeholder="Code d'accès"
                  className={`
                    w-full bg-black/50 border-2 text-center text-white text-lg tracking-[0.5em] py-3 rounded-lg outline-none transition-all
                    placeholder:text-gray-700 placeholder:tracking-normal placeholder:text-sm
                    ${error 
                      ? 'border-red-500 shadow-[0_0_20px_rgba(239,68,68,0.3)] animate-shake' 
                      : 'border-gray-700 focus:border-neon-cyan focus:shadow-[0_0_15px_rgba(0,240,255,0.2)]'}
                  `}
                />
              </div>

              {/* Message d'erreur */}
              {error && (
                <p className="text-red-500 text-xs text-center font-mono animate-pulse">
                  Accès refusé. Code incorrect.
                </p>
              )}

              {/* Bouton Valider */}
              <button
                type="submit"
                className="w-full bg-white text-black font-bold py-3 rounded-lg hover:bg-neon-cyan hover:text-black transition-all duration-300 flex items-center justify-center gap-2 group"
              >
                DÉVERROUILLER
                <ChevronRight size={16} className="group-hover:translate-x-1 transition-transform" />
              </button>
            </form>

          </div>
        </div>
      )}
    </>
  );
};

export default AdminAccess;