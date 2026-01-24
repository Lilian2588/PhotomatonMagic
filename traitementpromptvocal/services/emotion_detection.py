"""
    Détecteur d'émotions basé sur le modèle ONNX w2v2-L-robust-12.
    Transforme les dimensions (Valence, Arousal, Dominance) en catégories
"""

import pandas as pd
import numpy as np
import librosa
import audonnx
import audeer
import os
from pathlib import Path
from tqdm import tqdm



class EmotionDetector:
    
    def __init__(self):
        # Configuration des chemins
        self.model_root = 'model_onnx'
        self.cache_root = 'cache_onnx'
        self.model_url = 'https://zenodo.org/record/6221127/files/w2v2-L-robust-12.6bc4a7fd-1.1.0.zip'
        
        # Téléchargement et Chargement du modèle ONNX
        self._load_model()
        
        # Définition des "Prototypes" émotionnels (Mapping Dimensionnel)
        # Valeurs approximatives (0 à 1) : [Arousal (Intensité), Dominance, Valence (Positivité)]
        self.emotion_prototypes = {
            "énervé":     np.array([0.80, 0.80, 0.20]), # Intense, Dominant, Négatif
            "joyeux":       np.array([0.75, 0.75, 0.85]), # Intense, Dominant, Positif
            "triste":  np.array([0.20, 0.20, 0.20]), # Faible, Soumis, Négatif
            "effrayé":       np.array([0.75, 0.20, 0.20]), # Intense, Soumis, Négatif
            "degouté":     np.array([0.40, 0.40, 0.10]), # Moyen, Moyen, Très Négatif
            "ennuyé":      np.array([0.10, 0.30, 0.40]), # Très faible, Moyen, Neutre/Négatif
            "neutre":     np.array([0.45, 0.50, 0.50]), # Tout au milieu
            "surpris":   np.array([0.85, 0.50, 0.60])  # Très intense, Neutre, Positif
        }
        print("Moteur Emotion ONNX prêt.")

    def _load_model(self):
        """
        Télécharge et initialise le modèle ONNX si nécessaire
        """

        if not os.path.exists(self.model_root):
            print("⬇️ Téléchargement du modèle ONNX...")
            dst_path = os.path.join(self.model_root, 'model.zip')
            audeer.mkdir(self.model_root)
            
            if not os.path.exists(dst_path):
                audeer.download_url(self.model_url, dst_path, verbose=True)
            
            audeer.extract_archive(dst_path, self.model_root, verbose=True)
        
        # Chargement via audonnx (wrapper pratique pour l'audio)
        print("🔧 Chargement du modèle en mémoire...")
        self.model = audonnx.load(self.model_root)

    def predict_emotion(self, audio_path: str):
        """
        Analyse un fichier audio et retourne l'émotion la plus proche.
        """
        try:
            # Chargement et Prétraitement Audio
            # Le modèle attend du 16kHz
            speech, sr = librosa.load(audio_path, sr=16000)
            
            # Nettoyage silence et normalisation
            speech, _ = librosa.effects.trim(speech)
            if len(speech) == 0: 
                return "neutre"
            
            # Normalisation impérative pour ce modèle
            speech = speech / np.max(np.abs(speech))
            
            # Inférence ONNX
            # Le modèle attend un signal brut et retourne 'logits' : [Arousal, Dominance, Valence]
            output = self.model(speech, sr)
            
            # On récupère les 3 valeurs (Moyenne sur la durée du fichier si nécessaire)
            # La sortie est souvent [[A, D, V]]
            logits = output['logits']

            # Si le modèle retourne plusieurs frames, on fait la moyenne
            if logits.ndim > 1:
                current_emotion_vector = np.mean(logits, axis=0)
            else:
                current_emotion_vector = logits

            # Mapping : Trouver l'émotion la plus proche (Distance Euclidienne)
            best_emotion = "neutre"
            min_dist = float('inf')
            
            # On compare le vecteur obtenu avec nos prototypes
            for emotion_name, prototype_vector in self.emotion_prototypes.items():
                dist = np.linalg.norm(current_emotion_vector - prototype_vector)
                if dist < min_dist:
                    min_dist = dist
                    best_emotion = emotion_name
        
            return best_emotion
        
        except Exception as e:
            print(f"❌ Erreur sur {audio_path}: {e}")
            return "neutre"
        

        
    def process_emotions(self, audio_path: str) -> pd.DataFrame:
        """
        Traite tous les .wav d'un répertoire et retourne un DataFrame.
        
        Args:
            audio_path: Chemin du répertoire contenant les .wav ou du fichier .wav
        
        Returns:
            DataFrame avec colonnes [fichier, emotion_predite] ou simple émotion si fichier unique
        """
        audio_path = Path(audio_path)
        if audio_path.is_file():
            # Si c'est un fichier unique, on traite juste celui-ci
            print("\nTraitement du fichier pour détection d'émotion...",end ="")
            prediction = self.predict_emotion(str(audio_path))
            results = [{
                "fichier": audio_path.name,
                "emotion_predite": prediction
            }]
            print("Terminé")
            return prediction
        
        elif not audio_path.exists():
            raise FileNotFoundError(f"Le répertoire {audio_path} n'existe pas")
        else: 
            # Récupération fichiers .wav
            wav_files = sorted(audio_path.glob("*.wav"))
            
            if not wav_files:
                print(f"⚠️  Aucun fichier .wav trouvé dans {audio_path}")
                return pd.DataFrame(columns=["fichier", "emotion_predite"])
            
            print(f"\n😊 Traitement de {len(wav_files)} fichiers pour détection d'émotions...", end="")
            
            results = []
            
            # Traitement avec barre de progression
            for wav_file in tqdm(wav_files, desc="Détection émotions"):
                prediction = self.predict_emotion(str(wav_file))
                
                results.append({
                    "fichier": wav_file.name,
                    "emotion_predite": prediction
                })
            
            # Création DataFrame
            df = pd.DataFrame(results)
            
            # Statistiques
            emotion_counts = df["emotion_predite"].value_counts()
            print(f"\n📊 Statistiques des émotions détectées :")
            for emotion, count in emotion_counts.items():
                print(f"   {emotion}: {count} ({count/len(df)*100:.1f}%)")

            return df
    
        
def process_emotions(audio_path: str) -> pd.DataFrame:
    """
    Fonction standalone pour détecter les émotions d'un répertoire.
        
    Args:
    audio_path: Chemin du répertoire contenant les .wav ou du fichier .wav
        
    Returns:
    DataFrame avec émotions prédites
    """
    detector = EmotionDetector()
    return detector.process_emotions(audio_path)


if __name__ == "__main__":
    import sys
    
    if len(sys.argv) < 2:
        print("Usage: python emotion_detection.py <audio_path>")
        sys.exit(1)

    audio_directory = sys.argv[1]
    print(process_emotions(sys.argv[1]))
    print("Traitement terminé")

    
