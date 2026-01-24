"""
Module de transcription audio avec Whisper optimisé pour le français.
Utilise bofenghuang/whisper-large-v3-french/bofenghuang/whisper-small-cv11-french avec support GPU/CPU automatique mais lent.

"""

import torch
import librosa
import pandas as pd
from pathlib import Path
from transformers import AutoModelForSpeechSeq2Seq, AutoProcessor, pipeline
from tqdm import tqdm
from typing import Optional


class WhisperTranscriber:
    """Transcripteur audio basé sur Whisper optimisé pour le français."""
    
    def __init__(self, model_name: str = "bofenghuang/whisper-small-cv11-french"): #bofenghuang/whisper-large-v3-french -> plus robuste
        """
        Initialise le transcripteur Whisper.
        
        Args:
            model_name: Nom du modèle Hugging Face à utiliser
        """
        self.model_name = model_name
        if not torch.cuda.is_available():
            self.device = "cpu" 
            self.dtype = torch.float32
        else :
            self.dtype = torch.float16 
        
        print(f"🔧 Initialisation Whisper sur {self.device.upper()} (dtype: {self.dtype})")
        
        # Chargement du modèle et processeur
        self.model = AutoModelForSpeechSeq2Seq.from_pretrained(
            model_name,
            dtype=self.dtype,  # Utilisation de 'dtype' au lieu de 'torch_dtype'
            low_cpu_mem_usage=True,
            use_safetensors=True
        )
        self.model.to(self.device)
        
        self.processor = AutoProcessor.from_pretrained(model_name)
        
        # Pipeline de transcription optimisé
        self.pipe = pipeline(
            "automatic-speech-recognition",
            model=self.model,
            tokenizer=self.processor.tokenizer,
            feature_extractor=self.processor.feature_extractor,
            max_new_tokens=128,
            chunk_length_s=30,
            batch_size=8 if self.device == "cuda" else 1,
            dtype=self.dtype,  # Utilisation de 'dtype' au lieu de 'torch_dtype'
            device=self.device,
        )
        
        print(f"Modèle Whisper chargé : {model_name}")
    
    def transcribe_file(self, audio_path: str, sr: int = 16000) -> Optional[str]:
        """
        Transcrit un fichier audio unique.
        
        Args:
            audio_path: Chemin vers le fichier .wav
            sr: Taux d'échantillonnage cible (16kHz par défaut)
        
        Returns:
            Transcription textuelle ou None en cas d'erreur
        """
        try:
            # Chargement audio avec librosa
            audio, _ = librosa.load(audio_path, sr=sr, mono=True)
            
            # Transcription via pipeline
            result = self.pipe(audio, generate_kwargs={"language": "french"})
            
            return result["text"].strip()
        
        except Exception as e:
            print(f"❌ Erreur lors de la transcription de {audio_path}: {e}")
            return None
    
    def process_transcription(self, audio_path: str, output_csv: str = "resultats_transcriptions.csv") -> pd.DataFrame:
        """
        Traite tous les fichiers .wav d'un répertoire et génère un CSV.
        
        Args:
            audio_path: Chemin du répertoire contenant les .wav
            output_csv: Nom du fichier CSV de sortie
        
        Returns:
            DataFrame avec colonnes [fichier, transcription]
        """
        audio_path = Path(audio_path)

        # Cas fichier unique
        if audio_path.is_file():
            print("\nTraitement du fichier pour transcription...", end="")

            transcription = self.transcribe_file(str(audio_path))  
            print("Terminé")
            return transcription
            
        elif not audio_path.exists():
            raise FileNotFoundError(f"Le répertoire {audio_path} n'existe pas")
        # Cas répertoire
        else : 
            # Récupération des fichiers .wav
            wav_files = sorted(audio_path.glob("*.wav"))
            
            if not wav_files:
                print(f"⚠️ Aucun fichier .wav trouvé dans {audio_path}")
                return pd.DataFrame(columns=["fichier", "transcription"])
            
            print(f"\n🎙️ Traitement de {len(wav_files)} fichiers audio...", end = "")
            
            results = []
            
            # Traitement avec barre de progression
            for wav_file in tqdm(wav_files, desc="Transcription Whisper"):
                transcription = self.transcribe_file(str(wav_file))
                
                results.append({
                    "fichier": wav_file.name,
                    "transcription": transcription if transcription else "[ERREUR]"
                })
            
            # Création DataFrame
            df = pd.DataFrame(results)
            
            # Sauvegarde CSV
            output_path = audio_path / output_csv
            df.to_csv(output_path, index=False, encoding="utf-8")
            
            print(f"\n✅ Transcriptions sauvegardées : {output_path}")
            print(f"📊 {len(df)} fichiers traités, {df['transcription'].str.contains('[ERREUR]').sum()} erreurs")
            
            return df


def process_transcription(audio_dir: str) -> pd.DataFrame:
    """
    Fonction standalone pour traiter un répertoire audio.
    
    Args:
        audio_dir: Chemin du répertoire contenant les .wav
    
    Returns:
        DataFrame avec les transcriptions
    """
    transcriber = WhisperTranscriber()
    return transcriber.process_transcription(audio_dir)


if __name__ == "__main__":
    import sys
    
    if len(sys.argv) < 2:
        print("Usage: python transcription_whisper.py <audio_directory>")
        sys.exit(1)
    
    audio_directory = sys.argv[1]
    print(process_transcription(audio_directory))