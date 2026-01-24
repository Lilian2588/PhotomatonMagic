import React, { useRef, useState, useCallback } from 'react';
import Webcam from 'react-webcam';
import { useNavigate } from 'react-router-dom';
import { Camera } from 'lucide-react';

// Imports internes
import { usePhoto } from '../context/PhotoContext';
import NeonButton from '../components/ui/NeonButton';
import CustomCheckbox from '../components/ui/Checkbox';
import AdminAccess from '../components/features/admin/AdminAccess';
import HistoryButton from '../components/features/history/HistoryButton';

const CapturePage = () => {
  const webcamRef = useRef(null);
  const navigate = useNavigate();
  const { setPhoto } = usePhoto();

  // --- ÉTATS ---
  const [countdown, setCountdown] = useState(null);
  const [flash, setFlash] = useState(false);
  const [isChecked, setIsChecked] = useState(false); // Par défaut non coché

  // --- CONFIGURATION BASSE QUALITÉ (Optimisation) ---
  const videoConstraints = {
    width: 640,
    height: 480,
    facingMode: "user"
  };

  // --- LOGIQUE DE CAPTURE ---
  const capture = useCallback(() => {
    setCountdown(3);
    
    let count = 3;
    const timer = setInterval(() => {
      count -= 1;
      if (count > 0) {
        setCountdown(count);
      } else {
        clearInterval(timer);
        setCountdown(null);
        setFlash(true);

        const imageSrc = webcamRef.current.getScreenshot();
        
        setPhoto(imageSrc);
        
        setTimeout(() => {
          setFlash(false);
          navigate('/record');
        }, 300);
      }
    }, 1000);
  }, [webcamRef, navigate, setPhoto]);

  return (
    <div className="relative h-screen w-full bg-deep-black overflow-hidden flex flex-col items-center justify-center">
      
      {/* --- FLASH BLANC --- */}
      <div 
        className={`absolute inset-0 bg-white z-50 pointer-events-none transition-opacity duration-300 ${flash ? 'opacity-100' : 'opacity-0'}`} 
      />

      {/* --- HEADER --- */}
      <div className="absolute top-6 w-full px-8 flex justify-between items-center z-30">
        <div className="flex items-center gap-2">
          <div className="w-3 h-3 bg-red-500 rounded-full animate-pulse shadow-[0_0_10px_red]"></div>
          <span className="text-white/80 font-mono text-sm tracking-widest">LIVE FEED</span>
        </div>
        
        <div className="flex gap-4">
          <HistoryButton />
          <AdminAccess />
        </div>
      </div>

      {/* --- CADRE CAMÉRA --- */}
      <div className="relative z-10 p-1 rounded-2xl bg-gradient-to-b from-neon-cyan via-purple-500 to-neon-magenta shadow-[0_0_40px_rgba(0,240,255,0.3)]">
        <div className="relative rounded-xl overflow-hidden bg-black w-[90vw] max-w-2xl aspect-video border-4 border-black">
          
          <Webcam
            audio={false}
            ref={webcamRef}
            screenshotFormat="image/jpeg"
            screenshotQuality={0.5}
            videoConstraints={videoConstraints}
            className="w-full h-full object-cover transform scale-x-[-1]"
            mirrored={true}
          />

          {countdown && (
            <div className="absolute inset-0 flex items-center justify-center bg-black/40 backdrop-blur-sm z-30">
              <span className="text-9xl font-bold text-white font-tech animate-ping">
                {countdown}
              </span>
            </div>
          )}

          <div className="absolute inset-0 border border-white/10 pointer-events-none">
            <div className="absolute top-4 left-4 w-8 h-8 border-t-2 border-l-2 border-neon-cyan"></div>
            <div className="absolute top-4 right-4 w-8 h-8 border-t-2 border-r-2 border-neon-cyan"></div>
            <div className="absolute bottom-4 left-4 w-8 h-8 border-b-2 border-l-2 border-neon-cyan"></div>
            <div className="absolute bottom-4 right-4 w-8 h-8 border-b-2 border-r-2 border-neon-cyan"></div>
          </div>

        </div>
      </div>

      {/* --- CONTRÔLES --- */}
      <div className="mt-8 flex flex-col items-center gap-6 z-20">
      
        

        {/* BOUTON PHOTO */}
        <NeonButton 
          onClick={!countdown ? capture : null}
          text={
            <div className="h-16 w-16 rounded-full border-4 border-white flex items-center justify-center group-hover:bg-white transition-all">
              <Camera size={32} className="text-white group-hover:text-black transition-colors" />
            </div>
          }
          variant="ghost"
          glowColor="rgba(255,255,255,0.8)"
          className="transform transition-all duration-300 hover:scale-110"
        />
        
      </div>

    </div>
  );
};

export default CapturePage;