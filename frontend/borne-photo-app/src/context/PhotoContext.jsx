import React, { createContext, useState, useContext } from 'react';

const PhotoContext = createContext();

export const usePhoto = () => useContext(PhotoContext);

export const PhotoProvider = ({ children }) => {
  // 1. La photo prise par la webcam (NE DOIT JAMAIS CHANGER après la capture)
  const [photo, setPhoto] = useState(null);
  
  // 2. La photo reçue de l'IA (Celle qu'on affiche à droite)
  const [transformedPhoto, setTransformedPhoto] = useState(null);
  
  // 3. Permission utilisateur
  const [permission, setPermission] = useState(false);

  // Fonction pour tout nettoyer (bouton "Terminer la session")
  const clearPhoto = () => {
    setPhoto(null);
    setTransformedPhoto(null);
    setPermission(false);
  };

  return (
    <PhotoContext.Provider value={{ 
      photo, 
      setPhoto, 
      transformedPhoto, 
      setTransformedPhoto, // <-- On exporte bien cette fonction pour l'utiliser ailleurs
      permission, 
      setPermission,
      clearPhoto 
    }}>
      {children}
    </PhotoContext.Provider>
  );
};