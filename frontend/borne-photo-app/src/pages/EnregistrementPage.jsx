import React, { useEffect, useRef, useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import QRCode from 'react-qr-code';
import { usePhoto } from '../context/PhotoContext';
import { Save, Printer, LogOut, ArrowRight, Wand2, Download, QrCode } from 'lucide-react';

const EnregistrementPage = () => {
  const { photo, transformedPhoto, clearPhoto } = usePhoto();
  const navigate = useNavigate();

  const savedLogo = localStorage.getItem('logo_admin') || null;
  
  // 1. On récupère les infos
  const location = useLocation();
  const { requestId } = location.state || {}; 

  const canvasRef = useRef(null);
  const [isDownloading, setIsDownloading] = useState(false);


  const AWS_IP = import.meta.env.VITE_AWS_IP;
  const BASE_URL = `http://${AWS_IP}:8080`;

  // 3. URL DIRECTE
  // On pointe directement vers le endpoint de l'image
  const QR_CODE_URL = `${BASE_URL}/api/images/${requestId}/view`;

  // Sécurité : Si pas de photo, retour au début
  useEffect(() => {
    if (!photo) navigate('/');
  }, [photo, navigate]);

  // ---------------------------------------------------------
  // PRÉPARATION DU FICHIER FINAL (Canvas Invisible)
  // ---------------------------------------------------------
  useEffect(() => {
    const prepareFinalImage = async () => {
      const imageToSave = transformedPhoto || photo;

      if (imageToSave && canvasRef.current) {
        const canvas = canvasRef.current;
        const ctx = canvas.getContext('2d');
        
        const mainImg = new Image();
        mainImg.crossOrigin = "anonymous";
        mainImg.src = imageToSave;

        await new Promise(resolve => { mainImg.onload = resolve; });

        canvas.width = mainImg.width;
        canvas.height = mainImg.height;
        ctx.drawImage(mainImg, 0, 0);

        // Charger et dessiner le Logo (S'il existe)
        if (savedLogo) {
            const logoImg = new Image();
            logoImg.crossOrigin = "anonymous";
            logoImg.src = savedLogo;
            
            try {
                await new Promise((resolve, reject) => { 
                    logoImg.onload = resolve; 
                    logoImg.onerror = reject;
                });

                // --- CONFIGURATION TAILLE LOGO ---
                // Le logo prendra 20% de la largeur de l'image
                const logoWidth = canvas.width * 0.20; 
                // On garde les proportions
                const scale = logoWidth / logoImg.width;
                const logoHeight = logoImg.height * scale;

                // Position : En bas à droite (avec une marge de 30px)
                const margin = 30;
                const x = canvas.width - logoWidth - margin;
                const y = canvas.height - logoHeight - margin;

                // Dessiner le logo
                ctx.drawImage(logoImg, x, y, logoWidth, logoHeight);
            } catch (e) {
                console.warn("Impossible de charger le logo pour le téléchargement", e);
            }
        }
      }
    };

    prepareFinalImage();
  }, [photo, transformedPhoto]);

  // ---------------------------------------------------------
  // TÉLÉCHARGEMENT LOCAL
  // ---------------------------------------------------------
  const handleDownload = async () => {
    if (!transformedPhoto) return;
    setIsDownloading(true);
    
    const canvas = canvasRef.current;
    
    canvas.toBlob((blob) => {
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `mon-souvenir-ia-${Date.now()}.png`; 
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(url);
      setIsDownloading(false);
    }, 'image/png');
  };

  // ---------------------------------------------------------
  // ACTIONS ANNEXES
  // ---------------------------------------------------------
  const handleFinish = () => {
    clearPhoto();
    navigate('/');
  };

  const handlePrint = () => {
    window.print();
  };

  return (
    <div className="min-h-screen bg-deep-black text-white p-6 flex flex-col overflow-auto pb-10">
      
      {/* STYLE IMPRESSION */}
      <style>{`
        @media print {
          body * { visibility: hidden; }
          #printable-area, #printable-area * { visibility: visible; }
          #printable-area {
            position: fixed; left: 0; top: 0; width: 100%; height: 100%;
            display: flex; align-items: center; justify-content: center;
            background: white; z-index: 9999;
          }
          #printable-img { max-width: 100%; max-height: 100%; object-fit: contain; }
        }
      `}</style>

      {/* --- HEADER --- */}
      <header className="w-full flex justify-between items-center mb-8 border-b border-gray-800 pb-4 print:hidden">
        <div>
            <h1 className="text-3xl font-tech text-neon-cyan">RÉSULTAT</h1>
            <div className="text-gray-400 text-sm">Votre transformation IA est prête.</div>
        </div>
      </header>

      {/* --- CONTENEUR PRINCIPAL --- */}
      <div className="flex flex-col lg:flex-row gap-8 flex-1 max-w-7xl mx-auto w-full">
        
        {/* --- COLONNE GAUCHE : IMAGES ET BOUTONS --- */}
        <div className="flex-1 flex flex-col gap-8">
            
            {/* ZONE IMAGES AVANT / APRÈS */}
            <div className="flex flex-col md:flex-row gap-6 items-center justify-center">
                {/* 1. PHOTO ORIGINALE */}
                <div className="relative w-full md:w-1/2 aspect-[4/3] bg-gray-900 rounded-2xl overflow-hidden border border-gray-700 print:hidden opacity-80 hover:opacity-100 transition-opacity">
                    {photo && (
                    <img 
                        src={photo} 
                        alt="Originale Webcam" 
                        className="w-full h-full object-cover grayscale hover:grayscale-0 transition-all duration-500"
                    />
                    )}
                    <div className="absolute top-4 left-4 bg-black/60 px-3 py-1 rounded-full text-xs font-bold text-white border border-white/20">
                        ORIGINALE
                    </div>
                </div>

                {/* FLÈCHE */}
                <div className="hidden md:flex text-neon-magenta animate-pulse print:hidden">
                    <ArrowRight size={32} />
                </div>

                {/* 2. PHOTO TRANSFORMÉE */}
                <div id="printable-area" className="relative w-full md:w-1/2 aspect-[4/3] bg-gray-900 rounded-2xl overflow-hidden border-2 border-neon-cyan shadow-[0_0_30px_rgba(0,240,255,0.3)]">
                    {transformedPhoto ? (
                        <img 
                            id="printable-img"
                            src={transformedPhoto} 
                            alt="Résultat IA" 
                            className="w-full h-full object-cover"
                        />
                    ) : (
                        <div className="w-full h-full flex flex-col items-center justify-center text-neon-cyan animate-pulse">
                            <Wand2 size={48} className="mb-4 animate-spin-slow" />
                            <p>Chargement de l'image...</p>
                        </div>
                    )}
                    <div className="absolute top-4 right-4 bg-neon-cyan/90 text-black px-3 py-1 rounded-full text-xs font-bold shadow-lg print:hidden">
                        RÉSULTAT FINAL
                    </div>
                    {savedLogo && transformedPhoto && (
                        <img 
                            src={savedLogo} 
                            alt="Logo Event" 
                            className="absolute bottom-4 right-4 w-1/5 max-h-24 object-contain drop-shadow-lg z-10"
                        />
                    )}
                </div>
            </div>

            {/* BARRE D'ACTIONS */}
            <div className="flex flex-wrap justify-center gap-4 mt-4 print:hidden">
                <button 
                onClick={handleFinish}
                className="flex items-center gap-2 px-6 py-3 rounded-full border border-red-500/50 text-red-400 hover:bg-red-500 hover:text-white transition-all order-3 lg:order-1"
                >
                <LogOut size={20} />
                <span>TERMINER</span>
                </button>

                <button 
                onClick={handlePrint}
                disabled={!transformedPhoto}
                className={`flex items-center gap-2 px-8 py-3 rounded-full font-bold transition-all border border-neon-magenta text-neon-magenta hover:bg-neon-magenta hover:text-white order-2 ${!transformedPhoto && "opacity-50 cursor-not-allowed"}`}
                >
                <Printer size={20} />
                <span>IMPRIMER</span>
                </button>

                <button 
                    onClick={handleDownload}
                    disabled={!transformedPhoto || isDownloading}
                    className={`flex items-center gap-3 px-8 py-3 rounded-full font-bold text-black transition-all transform hover:scale-105 shadow-[0_0_20px_rgba(0,240,255,0.4)] order-1 lg:order-3 ${
                        !transformedPhoto || isDownloading 
                            ? "bg-gray-600 cursor-not-allowed opacity-50" 
                            : "bg-neon-cyan hover:bg-white"
                    }`}
                >
                    {isDownloading ? "Téléchargement..." : (
                        <>
                        <Download size={20} />
                        <span>TÉLÉCHARGER</span>
                        </>
                    )}
                </button>
            </div>
        </div>

        {/* --- COLONNE DROITE : SIDEBAR QR CODE --- */}
        <div className="lg:w-1/3 print:hidden flex flex-col gap-6 animate-in fade-in slide-in-from-right-8 duration-700">
            
            <div className="bg-gray-900/80 border border-neon-cyan/30 rounded-3xl p-6 backdrop-blur-md flex flex-col items-center text-center shadow-[0_0_30px_rgba(0,240,255,0.1)]">
               <div className="mb-4 flex items-center gap-2 text-neon-cyan">
                    <QrCode size={24} />
                    <h3 className="text-lg font-bold font-tech">PARTAGE MOBILE</h3>
               </div>
               
               <div className="bg-white p-4 rounded-2xl shadow-inner mb-4">
                  <QRCode 
                    value={QR_CODE_URL}
                    size={160}
                    bgColor="#FFFFFF"
                    fgColor="#000000"
                    level="M"
                  />
               </div>
               
               <p className="text-gray-300 text-sm mb-2">
                Scannez pour ouvrir la photo sur votre mobile.
               </p>
               <p className="text-xs text-gray-500 font-mono bg-black/40 p-2 rounded w-full break-all">
                 {/* Affiche seulement le début et la fin de l'URL pour faire propre si c'est trop long */}
                 ID: {requestId ? requestId.split('-')[0] : '...'}
               </p>
            </div>

            <div className="bg-gray-900/50 border border-gray-800 rounded-3xl p-6 text-sm text-gray-400">
                <h4 className="text-white font-bold mb-2 flex items-center gap-2">
                    <Save size={16} className="text-neon-magenta"/>
                    Options de sauvegarde
                </h4>
                <ul className="list-disc list-inside space-y-1">
                    <li>Utilisez le bouton <b>Télécharger</b> pour enregistrer l'image sur cet appareil.</li>
                    <li>Utilisez <b>Imprimer</b> pour une sortie papier.</li>
                </ul>
            </div>

        </div>

      </div>

      <canvas ref={canvasRef} className="hidden" />

    </div>
  );
};

export default EnregistrementPage;