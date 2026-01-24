import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Upload, Trash2, ArrowLeft, Image as ImageIcon } from 'lucide-react';
import { usePhoto } from '../context/PhotoContext';
import NeonButton from '../components/ui/NeonButton';

const AdminPage = () => {
  const navigate = useNavigate();
  const { logo, saveLogo, removeLogo } = usePhoto();
  const [preview, setPreview] = useState(logo);

  // Fonction qui transforme le fichier en Base64
  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onloadend = () => {
        const base64String = reader.result;
        setPreview(base64String);
        saveLogo(base64String); // Sauvegarde immédiate
      };
      reader.readAsDataURL(file);
    }
  };

  const handleDelete = () => {
    removeLogo();
    setPreview(null);
  };

  return (
    <div className="min-h-screen bg-deep-black text-white p-8 flex flex-col items-center">
      
      {/* Header */}
      <div className="w-full max-w-4xl flex items-center justify-between mb-12 border-b border-gray-800 pb-4">
        <button onClick={() => navigate('/')} className="text-gray-400 hover:text-white flex items-center gap-2">
          <ArrowLeft /> Retour Borne
        </button>
        <h1 className="text-2xl font-tech text-neon-cyan tracking-widest">CONFIGURATION ADMIN</h1>
      </div>

      <div className="w-full max-w-md bg-gray-900/50 p-8 rounded-2xl border border-gray-700 backdrop-blur-sm">
        
        <h2 className="text-xl font-bold mb-6 flex items-center gap-2">
          <ImageIcon className="text-neon-magenta" />
          Logo de l'événement
        </h2>
        
        <p className="text-sm text-gray-400 mb-6">
          Ce logo s'affichera automatiquement en bas à droite de toutes les photos téléchargées par les invités.
          <br/><span className="text-xs opacity-50">(Format recommandé : PNG Transparent)</span>
        </p>

        {/* Zone de Prévisualisation */}
        <div className="relative h-40 w-full bg-black/50 rounded-lg border-2 border-dashed border-gray-700 flex items-center justify-center mb-6 overflow-hidden group">
          {preview ? (
            <>
              <img src={preview} alt="Logo Event" className="h-full object-contain p-4" />
              {/* Bouton supprimer au survol */}
              <button 
                onClick={handleDelete}
                className="absolute inset-0 bg-black/60 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity text-red-500 font-bold gap-2"
              >
                <Trash2 /> SUPPRIMER
              </button>
            </>
          ) : (
            <span className="text-gray-600 text-sm">Aucun logo configuré</span>
          )}
        </div>

        {/* Input Fichier Caché + Bouton Stylisé */}
        <div className="flex justify-center">
          <label className="cursor-pointer">
            <input 
              type="file" 
              accept="image/*" 
              className="hidden" 
              onChange={handleFileChange} 
            />
            <div className="px-6 py-3 bg-gray-800 hover:bg-gray-700 border border-neon-cyan text-neon-cyan rounded-lg transition-all flex items-center gap-2 shadow-[0_0_15px_rgba(0,240,255,0.2)] hover:shadow-[0_0_25px_rgba(0,240,255,0.4)]">
              <Upload size={20} />
              IMPORTER UN LOGO
            </div>
          </label>
        </div>

      </div>
    </div>
  );
};

export default AdminPage;