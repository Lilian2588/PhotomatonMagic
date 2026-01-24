import React, { useState, useEffect } from 'react';
import { Sparkles, Hammer, AlertTriangle } from 'lucide-react';

// --- IMPORTS DES IMAGES ---
import tristanImg from '../../assets/tristan-taupe.png';
import laetitiaImg from '../../assets/laetitia-taupe.png';
import cedricImg from '../../assets/cedric-taupe.png';
import lilianImg from '../../assets/lilian-taupe.png';

import tristanAngry from '../../assets/tristan-taupe-enerve.png';
import laetitiaAngry from '../../assets/laetitia-taupe-enerve.png';
import cedricAngry from '../../assets/cedric-taupe-enerve.png';
import lilianAngry from '../../assets/lilian-taupe-enerve.png';

import goldMole from '../../assets/taupe-dor.png';

const NORMAL_MOLES = [tristanImg, laetitiaImg, cedricImg, lilianImg];
const ANGRY_MOLES = [tristanAngry, laetitiaAngry, cedricAngry, lilianAngry];

// --- COMPOSANT PROGRESSION (Version XL avec 5 étapes) ---
const ProgressTimeline = ({ stage }) => {
    const steps = [
        { id: 1, label: "Envoi" },
        { id: 2, label: "Cache" },
        { id: 3, label: "Analyse" },
        { id: 4, label: "Prompt" },
        { id: 5, label: "Génération" },
    ];

    // Calcul du pourcentage (0%, 25%, 50%, 75%, 100%)
    const progressWidth = ((stage - 1) / (steps.length - 1)) * 100;

    return (
        <div className="w-full max-w-2xl mt-8 mb-2 px-6">
            <div className="relative">
                {/* Ligne de fond (Plus épaisse) */}
                <div className="absolute top-1/2 left-0 w-full h-3 bg-gray-700 -translate-y-1/2 rounded-full"></div>
                
                {/* Ligne de progression (Plus épaisse et brillante) */}
                <div 
                    className="absolute top-1/2 left-0 h-3 bg-neon-cyan -translate-y-1/2 rounded-full transition-all duration-700 ease-out shadow-[0_0_15px_#00f0ff]"
                    style={{ width: `${progressWidth}%` }}
                ></div>

                {/* Les Points (Checkpoints plus gros) */}
                <div className="relative flex justify-between w-full">
                    {steps.map((step) => {
                        const isCompleted = stage >= step.id;
                        const isCurrent = stage === step.id;

                        return (
                            <div key={step.id} className="flex flex-col items-center gap-3">
                                {/* Le Point */}
                                <div className={`
                                    w-6 h-6 rounded-full border-4 z-10 transition-all duration-500 flex items-center justify-center
                                    ${isCompleted ? 'bg-neon-cyan border-neon-cyan shadow-[0_0_20px_#00f0ff]' : 'bg-gray-900 border-gray-600'}
                                    ${isCurrent ? 'scale-125 ring-4 ring-neon-cyan/30' : ''}
                                `}>
                                    {isCurrent && <div className="w-full h-full rounded-full bg-white animate-ping absolute"></div>}
                                </div>
                                {/* Le Label */}
                                <span className={`text-[10px] md:text-xs font-bold font-mono tracking-widest uppercase transition-colors duration-300 ${isCompleted ? 'text-neon-cyan drop-shadow-[0_0_5px_rgba(0,240,255,0.8)]' : 'text-gray-500'}`}>
                                    {step.label}
                                </span>
                            </div>
                        );
                    })}
                </div>
            </div>
        </div>
    );
};

const WhackAMoleLoading = ({ message, stage = 1 }) => {
  const [score, setScore] = useState(0);
  const [activeHole, setActiveHole] = useState(null); 
  const [activeMoleImg, setActiveMoleImg] = useState(null);
  const [moleType, setMoleType] = useState('normal'); 
  const [isBonked, setIsBonked] = useState(false);
  const [hitFeedback, setHitFeedback] = useState("");

  useEffect(() => {
    const interval = setInterval(() => {
      const randomHole = Math.floor(Math.random() * 9);
      const randChance = Math.random(); 
      let type = 'normal';
      let img = null;

      if (randChance > 0.95) { 
        type = 'gold';
        img = goldMole;
      } else if (randChance > 0.70) { 
        type = 'angry';
        img = ANGRY_MOLES[Math.floor(Math.random() * ANGRY_MOLES.length)];
      } else {
        type = 'normal';
        img = NORMAL_MOLES[Math.floor(Math.random() * NORMAL_MOLES.length)];
      }

      setIsBonked(false);
      setHitFeedback("");
      setActiveHole(randomHole);
      setMoleType(type);
      setActiveMoleImg(img);
    }, 750); // Un peu plus rapide

    return () => clearInterval(interval);
  }, []);

  const handleWhack = (index) => {
    if (index === activeHole && !isBonked) {
      setIsBonked(true);
      if (moleType === 'normal') {
        setScore(s => s + 10);
        setHitFeedback("POW! (+10)");
      } else if (moleType === 'angry') {
        setScore(s => Math.max(0, s - 20));
        setHitFeedback("AÏE! (-20)");
        if (navigator.vibrate) navigator.vibrate(200);
      } else if (moleType === 'gold') {
        setScore(s => s + 100);
        setHitFeedback("JACKPOT! (+100)");
        if (navigator.vibrate) navigator.vibrate([100, 50, 100]);
      }
    }
  };

  const getScoreColor = () => {
      if (hitFeedback.includes("AÏE")) return "text-red-500";
      if (hitFeedback.includes("JACKPOT")) return "text-yellow-400";
      return "text-neon-cyan";
  };

  return (
    <div className="fixed inset-0 z-50 flex flex-col items-center justify-center bg-black/95 backdrop-blur-md transition-opacity duration-500 py-4">
      
      {/* Header (Titre uniquement, le message a bougé) */}
      <div className="text-center mb-4 space-y-1 animate-fade-in-down px-4">
        <h2 className="text-2xl md:text-3xl font-tech font-bold text-white tracking-widest flex flex-col md:flex-row items-center justify-center gap-2 md:gap-3">
          <span className="flex items-center gap-2"><Hammer className="text-neon-magenta animate-bounce" /> TAPE SUR LES TAUPES</span>
          <span className="text-xs md:text-sm text-red-400 font-mono bg-red-900/30 px-2 py-1 rounded border border-red-500/50">
             (Évite les énervées !)
          </span>
        </h2>
      </div>

      {/* Zone de Jeu (Légèrement réduite pour laisser place à la barre) */}
      <div className="relative bg-gray-800 p-4 rounded-3xl border-4 border-gray-700 shadow-[0_0_30px_rgba(0,255,255,0.2)]">
        
        <div className="absolute -top-4 -right-4 bg-gradient-to-r from-neon-magenta to-purple-600 text-white font-bold px-4 py-1 rounded-full shadow-lg border-2 border-white transform rotate-6 z-20 text-lg">
          SCORE: {score}
        </div>

        {/* Grille un peu plus compacte (gap-3 au lieu de gap-6) */}
        <div className="grid grid-cols-3 gap-3">
          {[...Array(9)].map((_, index) => (
            <div 
              key={index}
              onClick={() => handleWhack(index)} 
              // Taille réduite : w-24 h-24 au lieu de w-32
              className="w-24 h-24 md:w-28 md:h-28 bg-gray-900 rounded-full border-b-8 border-gray-950 shadow-inner relative overflow-hidden cursor-pointer active:scale-95 transition-transform select-none -webkit-tap-highlight-color-transparent"
            >
              <div className="absolute bottom-0 w-full h-8 bg-black/40 rounded-b-full"></div>

              {activeHole === index && (
                <div className={`absolute bottom-0 left-1/2 -translate-x-1/2 w-20 md:w-24 transition-all duration-100 
                    ${isBonked ? 'scale-90 translate-y-8 opacity-60' : 'animate-pop-up'} 
                `}>
                  <img 
                    src={activeMoleImg} 
                    alt="Taupe" 
                    className={`w-full h-full object-contain drop-shadow-2xl pointer-events-none 
                        ${isBonked && moleType === 'angry' ? 'grayscale opacity-50' : ''}
                        ${moleType === 'gold' ? 'drop-shadow-[0_0_15px_rgba(255,215,0,0.8)]' : ''}
                    `}
                  />
                  {isBonked && (
                    <div className={`absolute top-0 left-1/2 -translate-x-1/2 -translate-y-10 font-black text-lg md:text-xl italic drop-shadow-md whitespace-nowrap animate-ping ${getScoreColor()}`}>
                      {hitFeedback}
                    </div>
                  )}
                </div>
              )}
            </div>
          ))}
        </div>
      </div>

      {/* --- BARRE DE PROGRESSION --- */}
      <ProgressTimeline stage={stage} />

      {/* --- MESSAGE DÉTAILLÉ (SOUS LA BARRE) --- */}
      <div className="mt-2 h-6 text-center">
          <p className="text-neon-cyan animate-pulse font-mono text-sm uppercase tracking-wide">
              {message}
          </p>
      </div>

      {/* --- PRELOADER CACHE (Pour éviter les requêtes 304 à répétition) --- */}
      <div className="fixed top-0 left-0 w-0 h-0 overflow-hidden opacity-0 pointer-events-none">
          {NORMAL_MOLES.map((src, i) => <img key={`preload-norm-${i}`} src={src} alt="" />)}
          {ANGRY_MOLES.map((src, i) => <img key={`preload-angry-${i}`} src={src} alt="" />)}
          <img src={goldMole} alt="" />
      </div>

    </div>
  );
};

export default WhackAMoleLoading;