import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, Image as ImageIcon, Loader2, AlertCircle } from 'lucide-react';

const HistoryPage = () => {
  const navigate = useNavigate();

  const [historyImages, setHistoryImages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const AWS_IP = import.meta.env.VITE_AWS_IP;
  const BASE_URL = `http://${AWS_IP}:8080`;

  useEffect(() => {
    const fetchHistory = async () => {
      try {
        setLoading(true);
        const response = await fetch(`${BASE_URL}/api/images/all`);
        
        if (!response.ok) {
            throw new Error("Impossible de récupérer l'historique local");
        }

        const data = await response.json();

        const validImages = data
            .reverse() 
            .slice(0, 5); 

        setHistoryImages(validImages);
      } catch (err) {
        console.error("Erreur historique:", err);
        setError("Erreur : Impossible de contacter le serveur.");
      } finally {
        setLoading(false);
      }
    };

    fetchHistory();
  }, []);

  return (
    <div className="min-h-screen flex flex-col bg-deep-black text-white p-6 md:p-12 overflow-y-auto">
      
      <header className="flex items-center justify-between mb-12 max-w-7xl mx-auto w-full border-b border-gray-800 pb-6">
        <button 
          onClick={() => navigate('/')} 
          className="flex items-center text-gray-400 hover:text-white transition-colors group px-4 py-2 rounded-full hover:bg-gray-900"
        >
          <ArrowLeft className="mr-2 group-hover:-translate-x-1 transition-transform" /> 
          RETOUR
        </button>
        
        <div className="text-right">
          <h1 className="text-3xl md:text-5xl font-tech font-bold text-transparent bg-clip-text bg-gradient-to-r from-neon-cyan to-neon-magenta">
            GALERIE LOCALE
          </h1>
          <p className="text-gray-500 text-sm mt-1">
            Les dernières captures (Stockage Disque)
          </p>
        </div>
      </header>

      <div className="flex-1 w-full max-w-7xl mx-auto">
        
        {loading ? (
            <div className="h-64 flex flex-col items-center justify-center text-neon-cyan animate-pulse">
                <Loader2 size={48} className="animate-spin mb-4"/>
                <p>Chargement des souvenirs...</p>
            </div>
        ) : error ? (
            <div className="h-64 flex flex-col items-center justify-center text-red-500 text-center">
                <AlertCircle size={48} className="mb-2"/>
                <p className="font-bold">{error}</p>
                <p className="text-xs text-gray-500 mt-2">Vérifiez que votre Backend Java tourne bien sur le port 8080.</p>
            </div>
        ) : historyImages.length === 0 ? (
            <div className="h-64 flex flex-col items-center justify-center text-gray-500 border-2 border-dashed border-gray-800 rounded-2xl">
                <ImageIcon size={48} className="mb-2 opacity-50"/>
                <p>Aucune image publique trouvée sur le disque.</p>
            </div>
        ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            
            {historyImages.map((img) => (
                <div 
                key={img.requestId} 
                className="group relative aspect-video bg-gray-900 rounded-2xl overflow-hidden border border-gray-800 hover:border-neon-cyan/50 transition-all duration-500 hover:shadow-[0_0_30px_rgba(0,240,255,0.15)]"
                >
                    <img 
                        src={`${BASE_URL}/api/images/${img.requestId}/view`}
                        alt={`Souvenir ${img.requestId}`}
                        className="w-full h-full object-cover transition-transform duration-700 group-hover:scale-105"
                        loading="lazy"
                        onError={(e) => {
                            e.target.onerror = null;
                            e.target.src = "https://via.placeholder.com/640x360?text=Fichier+Manquant"; 
                        }}
                    />
                
                    {/* Overlay d'information au survol */}
                    <div className="absolute inset-0 bg-gradient-to-t from-black/90 via-black/20 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300 flex flex-col justify-end p-6">
                        <div className="transform translate-y-4 group-hover:translate-y-0 transition-transform duration-300">
                            {/* 👇 Seul l'ID reste ici maintenant */}
                            <span className="text-neon-cyan font-tech text-sm block mb-1 break-all">
                                ID: {img.requestId ? img.requestId.split('-')[0] : 'Inconnu'}
                            </span>
                        </div>
                    </div>
                </div>
            ))}

            <div className="aspect-video flex flex-col items-center justify-center p-8 text-center border-2 border-dashed border-gray-800 rounded-2xl text-gray-600 bg-gray-900/30">
                <p className="max-w-xs text-sm">
                  Ceci est l'historique local de la borne. Seules les photos validées apparaissent ici.
                </p>
            </div>

            </div>
        )}
      </div>
    </div>
  );
};

export default HistoryPage;