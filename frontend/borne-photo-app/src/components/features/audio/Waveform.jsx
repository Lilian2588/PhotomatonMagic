import React, { useEffect, useState, useRef } from 'react';

const Waveform = ({ isActive, isPlaying, audioUrl, onEnded }) => {
  const TOTAL_BARS = 40;
  const [bars, setBars] = useState(new Array(TOTAL_BARS).fill(5));
  
  const audioContextRef = useRef(null);
  const analyserRef = useRef(null);
  const sourceRef = useRef(null);
  const animationRef = useRef(null);

  // --- LOGIQUE D'ANIMATION (SCROLL) ---
  const updateBars = (newVolume) => {
    setBars(prevBars => {
      const newBars = [...prevBars.slice(1), newVolume];
      return newBars;
    });
  };

  const runAnimationLoop = () => {
    if (!analyserRef.current) return;

    const bufferLength = analyserRef.current.frequencyBinCount;
    const dataArray = new Uint8Array(bufferLength);
    analyserRef.current.getByteFrequencyData(dataArray);

    // Calcul du volume moyen
    const average = dataArray.reduce((a, b) => a + b) / dataArray.length;
    
    // Amplification pour le visuel
    const volume = Math.min(100, Math.max(5, (average / 255) * 100 * 2.5)); // x2.5 pour bien voir les mouvements

    updateBars(volume);

    // Vitesse de défilement (50ms)
    setTimeout(() => {
      animationRef.current = requestAnimationFrame(runAnimationLoop);
    }, 50);
  };

  // --- GESTION AUDIO ---
  useEffect(() => {
    // Fonction de nettoyage
    const stopAudio = () => {
      if (animationRef.current) cancelAnimationFrame(animationRef.current);
      
      if (sourceRef.current) {
        try { sourceRef.current.stop(); } catch (e) {} // Pour AudioBufferSourceNode
        try { sourceRef.current.disconnect(); } catch (e) {}
      }
      
      if (audioContextRef.current && audioContextRef.current.state !== 'closed') {
        audioContextRef.current.close();
        audioContextRef.current = null;
      }
    };

    const initContext = () => {
      const AudioContext = window.AudioContext || window.webkitAudioContext;
      const ctx = new AudioContext();
      const analyser = ctx.createAnalyser();
      analyser.fftSize = 256;
      audioContextRef.current = ctx;
      analyserRef.current = analyser;
      return { ctx, analyser };
    };

    const startProcess = async () => {
      stopAudio(); // Reset complet avant de démarrer

      if (isActive) {
        // ==============================
        // 1. CAS ENREGISTREMENT (MICRO)
        // ==============================
        try {
          const { ctx, analyser } = initContext();
          const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
          const source = ctx.createMediaStreamSource(stream);
          source.connect(analyser);
          sourceRef.current = source; // On stocke pour pouvoir déconnecter plus tard
          
          runAnimationLoop();
        } catch (err) {
          console.error("Erreur Micro:", err);
        }

      } else if (isPlaying && audioUrl) {
        // ==============================
        // 2. CAS LECTURE (FICHIER)
        // ==============================
        try {
          const { ctx, analyser } = initContext();

          // A. On récupère le fichier brut (Blob)
          const response = await fetch(audioUrl);
          const arrayBuffer = await response.arrayBuffer();

          // B. On le décode en AudioBuffer (Méthode Pro)
          const audioBuffer = await ctx.decodeAudioData(arrayBuffer);

          // C. On prépare la source de lecture
          const source = ctx.createBufferSource();
          source.buffer = audioBuffer;

          // D. On connecte tout : Source -> Analyser -> Haut-parleurs
          source.connect(analyser);
          analyser.connect(ctx.destination); // IMPORTANT : Pour entendre le son

          // E. Gestion de la fin
          source.onended = () => {
            if (onEnded) onEnded();
          };

          // F. Lecture !
          source.start(0);
          sourceRef.current = source; // On stocke pour pouvoir faire source.stop()

          runAnimationLoop();

        } catch (err) {
          console.error("Erreur Lecture:", err);
          if (onEnded) onEnded();
        }
      }
    };

    // Déclencheur
    if (isActive || isPlaying) {
      startProcess();
    } else {
      stopAudio();
      setBars(new Array(TOTAL_BARS).fill(5)); // Reset visuel
    }

    // Cleanup au démontage du composant
    return () => stopAudio();
  }, [isActive, isPlaying, audioUrl]); 

  return (
    <div className="flex items-center justify-end gap-[2px] h-full w-full max-w-md px-4 overflow-hidden mask-linear-fade">
      {bars.map((height, index) => (
        <div
          key={index}
          style={{
            height: `${height}%`,
            opacity: 0.5 + (index / TOTAL_BARS) * 0.5, 
          }}
          className={`
            w-2 rounded-full transition-all duration-100 ease-out
            ${index > TOTAL_BARS - 5 ? 'bg-white shadow-[0_0_10px_white]' : 'bg-neon-cyan'} 
          `}
        />
      ))}
    </div>
  );
};

export default Waveform;