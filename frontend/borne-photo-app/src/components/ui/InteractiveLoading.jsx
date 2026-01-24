import React, { useState, useEffect, useCallback } from 'react';
import { Sparkles } from 'lucide-react';

const InteractiveLoading = ({ message = "Connexion..." }) => {
  const [score, setScore] = useState(0);
  const [nodes, setNodes] = useState([]);

  // --- JEU (Code abrégé car identique) ---
  const createNode = useCallback(() => {
    return {
      id: Date.now() + Math.random(),
      variant: Math.random() > 0.5 ? 'cyan' : 'magenta',
      size: Math.floor(Math.random() * 30) + 30,
      left: Math.floor(Math.random() * 80) + 10, 
      top: Math.floor(Math.random() * 60) + 20, 
    };
  }, []);

  useEffect(() => {
    const interval = setInterval(() => {
      setNodes((prev) => (prev.length >= 15 ? prev : [...prev, createNode()]));
    }, 800);
    return () => clearInterval(interval);
  }, [createNode]);

  const handlePop = (id) => {
    setScore((s) => s + 100); 
    setNodes((prev) => prev.filter((n) => n.id !== id));
  };

  return (
    <div className="fixed inset-0 z-50 bg-deep-black/95 cursor-crosshair overflow-hidden animate-fade-in">
      
      {/* HEADER */}
      <div className="absolute top-0 w-full p-8 text-center z-20 pointer-events-none">
        <h2 className="text-3xl font-tech text-white animate-pulse flex items-center justify-center gap-3">
          <Sparkles className="text-neon-cyan" /> TRAITEMENT EN COURS...
        </h2>
        <div className="mt-4 font-mono text-2xl">SCORE: <span className="text-neon-magenta">{score}</span></div>
      </div>
      
      {/* JEU */}
      <div className="absolute inset-0 z-10">
        {nodes.map((node) => (
          <button
            key={node.id}
            onClick={() => handlePop(node.id)}
            style={{ left: `${node.left}%`, top: `${node.top}%`, width: `${node.size}px`, height: `${node.size}px` }}
            className={`absolute rounded-full animate-bounce-slow active:scale-150 transition-transform ${node.variant === 'cyan' ? 'bg-neon-cyan shadow-[0_0_20px_cyan]' : 'bg-neon-magenta shadow-[0_0_20px_magenta]'}`}
          >
             <div className="absolute inset-[35%] bg-white rounded-full opacity-70"></div>
          </button>
        ))}
      </div>
      
      {/* FOOTER : MESSAGE DYNAMIQUE */}
      <div className="absolute bottom-10 w-full flex flex-col items-center pointer-events-none z-30 gap-4">
         <div className="bg-black/80 backdrop-blur border border-white/10 px-6 py-3 rounded-full">
            <p className="text-neon-cyan font-mono text-lg font-bold animate-pulse flex items-center gap-2">
               <span className="text-neon-magenta">{">"}</span> {message}
            </p>
         </div>
         <div className="w-8 h-8 border-2 border-t-neon-cyan border-b-neon-cyan border-r-transparent border-l-transparent rounded-full animate-spin"></div>
      </div>
    </div>
  );
};

export default InteractiveLoading;