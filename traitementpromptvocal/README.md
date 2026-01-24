# 🎙️ Audio Emotion & Transcription Service

Service de traitement audio combinant **détection d'émotions** et **transcription ASR (Automatic Speech Recognition)** en français.

## 🚀 Installation rapide pour le service individuel

```bash
pip install -r requirement.txt
```

## 📦 Structure du Projet
```
traitementpromptvocal/
├── services                                               # Différents services du traitement
├── bridge_worker.py                                       # Pont entre Spring Orchestrator et Modèles de traitements
├── contributors.md                                        # Informations des contributeurs
├── README.md                                              # Ce fichier
├── Dockerfile                                             # Dockerfile qui conteneurise le service et permet d'automatiser le run
└── requirements.txt                                       # Fichier contenant toutes les dépendances                                               
```

## 🎯 Utilisation

## Commandes de Base

### Lancer le consommateur 

```bash
python bridge_worker.py
```

### Modules Individuels

```bash
# Détection d'émotions uniquement
python services/emotion_detection.py audio_folder/

# Transcription uniquement
python services/transcription_whisper.py audio_folder/
```

## 🔧 Configuration Requise

- **Python** : 3.8 ou supérieur
- **GPU** : NVIDIA avec CUDA 12.1 (recommandé mais optionnel)
- **RAM** : 8 Go minimum (16 Go recommandé)
- **Espace disque** : 5 Go pour les modèles

## 🤖 Modèles Utilisés

- **Émotions** : `ONNX w2v2-L-robust-12.` url : https://zenodo.org/record/6221127/files/w2v2-L-robust-12.6bc4a7fd-1.1.0.zip
- **Transcription** : `bofenghuang/whisper-small-v3-french`

Les modèles sont téléchargés automatiquement au premier lancement.

## 📈 Performance

- **GPU NVIDIA** : ~2-5 fichiers/seconde
- **CPU** : ~0.3-1 fichier/seconde à vérifier

### Le modèle de transcription ne se télécharge pas 
Vérifiez votre connexion internet et l'accès à `huggingface.co`.