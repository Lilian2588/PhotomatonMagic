import React, { useEffect, useState, useRef } from 'react';
import { useReactMediaRecorder } from "react-media-recorder";
import { useNavigate } from 'react-router-dom';
import { Pause, Square, Trash2, Send, Mic, AlertCircle, ArrowLeft } from 'lucide-react';

import { usePhoto } from '../context/PhotoContext';
import { sendAvatarData } from '../services/avatarService';

import NeonButton from '../components/ui/NeonButton';
import Waveform from '../components/features/audio/Waveform';
import WhackAMoleLoading from '../components/ui/WhackAMoleLoading';

const AudioPage = () => {
  const navigate = useNavigate();
  const { photo, setPhoto, permission, setTransformedPhoto } = usePhoto(); 
  
  const [isPlaying, setIsPlaying] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [timer, setTimer] = useState(0); 
  
  const [loadingMessage, setLoadingMessage] = useState("Préparation...");
  const [progressStage, setProgressStage] = useState(1);
  const [hasError, setHasError] = useState(false);

  const eventSourceRef = useRef(null);
  const backendRequestIdRef = useRef(null);
  const backupTimerRef = useRef(null);

  const TUNNEL_URL = ""; 

  const { status, startRecording, stopRecording, mediaBlobUrl, clearBlobUrl } =
    useReactMediaRecorder({ audio: true, mediaRecorderOptions: { mimeType: "audio/webm" } });

  const isRecording = status === "recording";

  useEffect(() => {
    if (!photo) navigate('/');
    return () => {
      if (eventSourceRef.current) eventSourceRef.current.close();
      if (backupTimerRef.current) clearTimeout(backupTimerRef.current);
    };
  }, [photo, navigate]);

  useEffect(() => {
    let interval = null;
    if (isRecording) {
      interval = setInterval(() => setTimer((prev) => prev + 1), 1000);
    }
    return () => clearInterval(interval);
  }, [isRecording]);

  const formatTime = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins < 10 ? '0' : ''}${mins}:${secs < 10 ? '0' : ''}${secs}`;
  };

  const handleSend = async () => {
    if (!mediaBlobUrl) return;

    setIsUploading(true);
    setIsPlaying(false); 
    setHasError(false);
    setLoadingMessage("Envoi des données au serveur...");
    setProgressStage(1);

    const handleSuccess = (finalImageUrl, requestId) => {
        if (eventSourceRef.current) {
            eventSourceRef.current.close();
            eventSourceRef.current = null;
        }
        if (backupTimerRef.current) clearTimeout(backupTimerRef.current);
        
        setTransformedPhoto(finalImageUrl);

        navigate('/result', { 
            state: { 
                requestId: requestId || backendRequestIdRef.current,
                tunnelUrl: TUNNEL_URL 
            } 
        });
    };

    backupTimerRef.current = setTimeout(() => {
        console.warn("⚠️ Délai d'attente dépassé (Timeout)");
        if (eventSourceRef.current) eventSourceRef.current.close();
        setHasError(true);
        setIsUploading(false);
    }, 120000); 

    try {
        const notificationUrl = "/api/notifications/stream";
        const es = new EventSource(notificationUrl);
        eventSourceRef.current = es;
        
es.onmessage = (event) => {
            try {
                const notif = JSON.parse(event.data);
                
                const currentId = backendRequestIdRef.current;
                const isMatching = currentId && notif.requestId === currentId;
                const isLikelyOurs = !currentId && isUploading; 

                if (isMatching || isLikelyOurs) {
                    
                    // 1. FIN (SUCCÈS)
                    if (notif.status === "COMPLETED" || notif.status === "SUCCESS") {
                        // On force un petit délai pour être sûr qu'on a vu l'étape 5
                        setProgressStage(5); 
                        setLoadingMessage("Finalisation...");
                        
                        let finalUrl = notif.imageUrl;
                        if (finalUrl && !finalUrl.startsWith("data:image")) {
                            finalUrl = `data:image/png;base64,${finalUrl}`;
                        }
                        
                        setTimeout(() => {
                            handleSuccess(finalUrl, notif.requestId);
                        }, 800);

                    // 2. ERREUR
                    } else if (notif.status === "FAILED" || notif.status === "ERROR") {
                        console.error("Erreur Backend:", notif.message);
                        setHasError(true);
                        setIsUploading(false);
                        if (backupTimerRef.current) clearTimeout(backupTimerRef.current);
                        es.close();

                    // 3. PROGRESSION (Correction ici !)
                    } else {
                        // On utilise le STATUS technique (plus fiable que le texte)
                        switch (notif.status) {
                            case "UPLOAD_START":
                                setLoadingMessage(notif.message || "Envoi en cours...");
                                setProgressStage(1);
                                break;
                                
                            case "IMAGE_CACHED":
                                // Le switch détectera ce code même si le message ne contient pas "cache"
                                setLoadingMessage(notif.message || "Mise en cache...");
                                setProgressStage(2);
                                break;
                                
                            case "FACE_ANALYZED":
                            case "VOICE_ANALYZED":
                                setLoadingMessage(notif.message || "Analyse terminée...");
                                setProgressStage(3);
                                break;
                                
                            case "PROMPT_CREATED":
                                setLoadingMessage(notif.message || "Construction du prompt...");
                                setProgressStage(4);
                                break;
                                
                            case "IMAGE_GEN_START":
                                // ⚠️ ASTUCE : On retarde l'affichage de l'étape 5 pour laisser 
                                // le temps de voir l'étape 4 (Prompt) qui est trop rapide.
                                    setLoadingMessage(notif.message || "Génération de l'image...");
                                    setProgressStage(5);
                                break;
                                
                            default:
                                // Fallback (ancienne méthode)
                                if (notif.message) setLoadingMessage(notif.message);
                                break;
                        }
                    }
                }
            } catch (err) { console.log("SSE Ignored"); }
        };
        es.onerror = (err) => {
            console.log("SSE Reconnecting...");
        };

    } catch (e) { console.error("SSE Error", e); }

    try {
      const responseText = await sendAvatarData(mediaBlobUrl, photo, permission, null);
      const match = responseText.match(/requestId=([a-f0-9\-]+)/);
      if (match && match[1]) {
          backendRequestIdRef.current = match[1];
      }
    } catch (error) {
      console.error("Erreur POST:", error);
      setHasError(true);
      setIsUploading(false);
      if (backupTimerRef.current) clearTimeout(backupTimerRef.current);
    }
  };

  const togglePlay = () => { if (mediaBlobUrl) setIsPlaying(!isPlaying); };
  const handleDelete = () => { clearBlobUrl(); setIsPlaying(false); setTimer(0); };

  const getInstructionText = () => {
      if (isRecording) return "🎙️ Je vous écoute...";
      if (mediaBlobUrl) return "Audio capturé !";
      return "Dites ce que vous voulez...";
  };

  if (hasError) {
      return (
        <div className="flex flex-col items-center justify-center min-h-screen w-full bg-deep-black text-white gap-6">
            <AlertCircle size={64} className="text-red-500 animate-pulse" />
            <h2 className="text-2xl font-bold">Oups, le traitement a échoué.</h2>
            <p className="text-gray-400">Le serveur met trop de temps à répondre ou une erreur est survenue.</p>
            <div className="flex gap-4">
                <button 
                    onClick={() => window.location.reload()} 
                    className="px-6 py-3 rounded-full border border-gray-600 hover:bg-gray-800 transition-colors"
                >
                    Abandonner
                </button>
                <NeonButton 
                    text="RÉESSAYER L'ENVOI" 
                    onClick={handleSend} 
                    variant="primary"
                />
            </div>
        </div>
      );
  }

  return (
    <div className="flex flex-col items-center justify-center min-h-screen w-full px-4 gap-6 py-8 relative overflow-hidden">
      
      {/* BOUTON RETOUR */}
      <button 
        onClick={() => navigate('/')} 
        className="absolute top-6 left-6 z-50 flex items-center gap-2 text-gray-400 hover:text-neon-cyan transition-colors group"
      >
        <div className="p-2 rounded-full border border-gray-600 group-hover:border-neon-cyan transition-all">
            <ArrowLeft size={20} />
        </div>
        <span className="font-tech text-sm tracking-widest hidden md:block">REFAIRE PHOTO</span>
      </button>

      <div className="relative group animate-fade-in-up z-10 mt-8 md:mt-0">
         <div className="absolute -inset-1 bg-gradient-to-r from-neon-cyan to-neon-magenta rounded-lg blur opacity-40"></div>
         {photo && <img src={photo} alt="Capture" className="relative w-32 h-24 md:w-48 md:h-36 object-cover rounded-lg border border-white/10 shadow-2xl transform scale-x-[-1]" />}
      </div>

      <div className="text-center space-y-1 z-10">
        <h2 className="text-2xl font-tech font-bold text-white">PERSONNALISATION</h2>
        <p className="text-gray-400 text-sm">Dites à l'IA comment vous transformer !</p>
      </div>

      {/* 👇 MODIFICATION ICI : TEXTE DE L'EXEMPLE */}
      <div className="flex flex-col items-center gap-2 animate-fade-in z-10 w-full max-w-lg">
          <span className="text-xs text-neon-cyan uppercase tracking-widest opacity-80">Exemple :</span>
          
          <div className="px-6 py-4 rounded-xl border border-neon-cyan/30 bg-gray-900/60 backdrop-blur-sm w-full text-center shadow-[0_0_15px_rgba(0,240,255,0.1)]">
              <p className="text-gray-300 italic font-medium leading-relaxed">
                "J'aimerais être une Princesse avec une robe rose et une couronne dans un château dans un style anime"
              </p>
          </div>
      </div>

      <div className="w-full max-w-lg transition-all duration-500 z-10 flex flex-col items-center mt-4">
        <div className="text-center mb-3 h-12 flex flex-col justify-center">
           <div className="font-mono text-neon-cyan text-lg font-bold animate-pulse">{isRecording ? formatTime(timer) : getInstructionText()}</div>
        </div>
        <div className="w-full h-24 flex items-center justify-end bg-gray-900/80 rounded-xl border border-gray-700 backdrop-blur-md p-4 shadow-inner overflow-hidden mb-4">
          <Waveform isActive={isRecording} isPlaying={isPlaying} audioUrl={mediaBlobUrl} onEnded={() => setIsPlaying(false)} />
        </div>
      </div>

      <div className="flex items-center gap-6 mt-2 z-10">
        {!mediaBlobUrl ? (
          isRecording ? (
            <NeonButton text={<Square size={32} fill="currentColor" />} onClick={stopRecording} variant="danger" className="w-24 h-24 flex items-center justify-center rounded-full !px-0"/>
          ) : (
            <button onClick={startRecording} className="w-24 h-24 flex items-center justify-center rounded-full border-2 border-neon-cyan text-neon-cyan bg-gray-900 hover:scale-105 hover:shadow-[0_0_20px_rgba(0,255,255,0.4)] transition-all duration-300">
                <Mic size={32}/>
            </button>
          )
        ) : (
          <>
            <button onClick={handleDelete} className="p-4 rounded-full bg-gray-800 text-gray-400 hover:text-red-500"><Trash2 size={24}/></button>
            <NeonButton text={isPlaying ? <Pause size={24} fill="currentColor"/> : "ÉCOUTER"} onClick={togglePlay} variant="ghost" className="min-w-[140px] flex justify-center"/>
            <NeonButton text={<Send size={24} className="ml-2"/>} onClick={handleSend} variant="primary" className="flex items-center"/>
          </>
        )}
      </div>

      {isUploading && <WhackAMoleLoading message={loadingMessage} stage={progressStage} />}

    </div>
  );
};

export default AudioPage;