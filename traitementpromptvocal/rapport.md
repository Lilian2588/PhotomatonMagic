# Rapport technique — Traitement Prompt Vocal

Date: 2026-01-20

Fichiers documentés
- [bridge_worker.py](bridge_worker.py)
- [services/emotion_detection.py](services/emotion_detection.py)
- [services/transcription_whisper.py](services/transcription_whisper.py)

---

**Résumé exécutif**

Ce projet implémente un worker asynchrone de traitement audio. Le worker consomme des identifiants d'audio depuis une queue RabbitMQ, récupère le binaire audio via une API cache HTTP (service Spring), exécute deux traitements ML (détection d'émotions via un modèle ONNX et transcription via Whisper/Hugging Face), puis publie les résultats structurés en JSON sur une queue de réponse. La logique favorise la simplicité et la robustesse : chargement persistant des modèles en mémoire, traitement séquentiel d'un message à la fois, et nettoyage sécurisé des fichiers temporaires.

**But global et interaction des modules**

- Objectif principal : fournir un pipeline automatisé et réutilisable pour transformer un audio (identifié via ID) en métadonnées exploitables : `emotion` et `transcription`.
- Orchestrateur : [bridge_worker.py](bridge_worker.py)
  - Consomme la queue `audio_request_queue`, récupère l'audio depuis `CACHE_URL/{request_id}/audio`, écrit temporairement le binaire pour compatibilité avec les librairies, appelle les services ML, publie sur `audio_response_queue` et ack le message.
- Services ML :
  - [services/emotion_detection.py](services/emotion_detection.py) — `EmotionDetector` : modèle ONNX (via `audonnx`) retournant un vecteur [Arousal, Dominance, Valence] mappé vers une étiquette émotionnelle.
  - [services/transcription_whisper.py](services/transcription_whisper.py) — `WhisperTranscriber` : modèle Hugging Face Whisper (pipeline `automatic-speech-recognition`) optimisé pour le français.

---

**Architecture technique — Flux de données**

1. Réception d'un message RabbitMQ (body = `request_id` en UTF-8).
2. Appel HTTP GET vers `CACHE_URL/{request_id}/audio` (timeout 30s) pour récupérer `bytes`.
3. Écriture dans un fichier temporaire `.wav` (tempfile.NamedTemporaryFile(delete=False)).
4. Traitements ML synchrones :
   - `EmotionDetector.process_emotions(tmp_path)`
   - `WhisperTranscriber.process_transcription(tmp_path)`
5. Construction du payload JSON :
   {
     "requestId": "...",
     "emotion": "...",
     "transcription": "..."
   }
6. Publication du JSON sur `audio_response_queue` et ack du message.
7. Suppression du fichier temporaire.

Raisons des choix techniques
- Écriture sur disque : compatibilité maximale (librosa, audonnx, pipelines HF) sans avoir à adapter chaque API pour lire depuis des flux en mémoire.
- Chargement global des modèles : réduit la latence d'inférence après warmup (les objets modèles sont instanciés une fois au démarrage).
- Consommation séquentielle (`prefetch_count=1`) : simplicité et garantie que chaque message est traité correctement sans sur-allocation de ressources.
- Heartbeat AMQP désactivé (`heartbeat=0`) : tolère des inférences longues sans faire tomber la connexion.

---

**Guide d'installation & dépendances**

Prérequis système
- Python 3.10+ (recommandé)
- 8+ GB RAM minimum (plus pour modèles large)
- GPU NVIDIA + CUDA (optionnel mais fortement recommandé pour modèles Whisper large)
- ffmpeg installé sur le système
- RabbitMQ accessible depuis l'hôte (ou container)
- Service Spring (cache) accessible à l'URL configurée

Variables d'environnement
- `RABBITMQ_HOST` — hôte RabbitMQ (défaut `localhost`)
- `CACHE_URL` — URL de l'API cache (défaut `http://host.docker.internal:8083/cache/file`)

Installation rapide

```bash
python -m venv .venv
# Windows
.venv\Scripts\activate
# Linux/macOS
source .venv/bin/activate
python -m pip install --upgrade pip
pip install -r requirements.txt
# Installer ffmpeg via le package manager (ex: apt, choco)
```

Dépendances (extrait de `requirements.txt`)
- torch>=2.0.0
- transformers>=4.35.0
- librosa>=0.10.0
- pandas>=2.0.0
- tqdm>=4.65.0
- soundfile, numpy, audeer, audonnx
- pika (RabbitMQ client)
- requests
- huggingface-hub, accelerate

Exécution
- Lancer le worker:

```bash
python bridge_worker.py
```

Assurez-vous que RabbitMQ et l'API cache sont joignables.

---

**Référence API détaillée**

Fichier: [bridge_worker.py](bridge_worker.py)

- `dl_audio_from_cache(request_id: str) -> Optional[bytes]`
  - Paramètres:
    - `request_id` (str)
  - Description: effectue un GET HTTP vers `{CACHE_URL}/{request_id}/audio`. Retourne les bytes si 200, sinon `None`.
  - Exceptions: les erreurs réseau sont interceptées ; la fonction retourne `None` et logge l'erreur.

- `on_request(ch, method, props, body) -> None`
  - Paramètres:
    - `ch` : canal Pika
    - `method` : objet méthode AMQP (utilisé pour `delivery_tag`)
    - `props` : `BasicProperties` (utilisé pour `correlation_id`)
    - `body` : bytes contenant `request_id` encodé en UTF-8
  - Description: flux principal de traitement d'un message : récupération audio, écriture temporaire, appels ML, publication de la réponse JSON et ack du message.
  - Comportement d'erreur: exceptions interceptées et converties en `response_data` avec `emotion: "error"` et `transcription` contenant l'erreur. Remarque: si `dl_audio_from_cache` retourne `None`, la fonction actuelle retourne sans publier de réponse — voir suggestions d'amélioration.

- `start() -> None`
  - Description: initialise la connexion Pika (`heartbeat=0`, `blocked_connection_timeout=300`), déclare les queues, configure `basic_qos(prefetch_count=1)`, et démarre `start_consuming()`.
  - Exceptions: attrape `pika.exceptions.AMQPConnectionError` pour afficher des messages d'erreur lisibles.


Fichier: [services/emotion_detection.py](services/emotion_detection.py)

- Classe `EmotionDetector`:
  - `__init__(self) -> None` : configure `model_root`, `cache_root`, `model_url` et charge le modèle via `_load_model()`. Initialise le mapping prototype d'émotions.
  - `_load_model(self) -> None` : télécharge et extrait le modèle si nécessaire, charge via `audonnx.load`.
  - `predict_emotion(self, audio_path: str) -> str` :
    - Charge le fichier audio à 16kHz via `librosa`, trim, normalise, exécute le modèle ONNX, calcule le vecteur émotionnel (A,D,V) et retourne l'étiquette prototype la plus proche par distance euclidienne.
    - En cas d'erreur, logge et retourne "neutre".
  - `process_emotions(self, audio_path: str) -> Union[pd.DataFrame, str]` :
    - Si `audio_path` est un fichier, retourne la prédiction (str). Si c'est un répertoire, parcourt les `.wav` et retourne un `DataFrame`.
    - Lève `FileNotFoundError` si le chemin n'existe pas.

- Fonction globale `process_emotions(audio_path: str) -> pd.DataFrame` : wrapper simple instanciant `EmotionDetector`.


Fichier: [services/transcription_whisper.py](services/transcription_whisper.py)

- Classe `WhisperTranscriber`:
  - `__init__(self, model_name: str = "bofenghuang/whisper-small-cv11-french")` : détecte si CUDA est disponible, choisit `dtype`, charge `AutoModelForSpeechSeq2Seq` et `AutoProcessor`, crée un `pipeline("automatic-speech-recognition")` configuré.
  - `transcribe_file(self, audio_path: str, sr: int = 16000) -> Optional[str]` : charge avec `librosa.load`, appelle `self.pipe(...)` puis retourne `result["text"].strip()` ou `None` si erreur.
  - `process_transcription(self, audio_path: str, output_csv: str = "resultats_transcriptions.csv") -> Union[pd.DataFrame, str]` : si fichier unique retourne la transcription ; si répertoire, traite tous les `.wav`, sauvegarde un CSV et retourne un `DataFrame`.
  - Fonction globale `process_transcription(audio_dir: str)` : wrapper instanciant `WhisperTranscriber`.

---

**Exemples d'utilisation**

- Lancer le worker (dev):

```bash
python bridge_worker.py
```

- Tester la transcription sur un fichier:

```python
from services.transcription_whisper import WhisperTranscriber
transcriber = WhisperTranscriber()
print(transcriber.transcribe_file("examples/test.wav"))
```

- Tester la détection d'émotion sur un fichier:

```python
from services.emotion_detection import EmotionDetector
detector = EmotionDetector()
print(detector.predict_emotion("examples/test.wav"))
```

- Tester le traitement complet (simuler en publiant un message RabbitMQ contenant un `request_id` correspondant à un audio accessible via l'API cache).

---

**Gestion des erreurs et points critiques**

Points de défaillance observés
- `dl_audio_from_cache` peut échouer → retourne `None`. Actuellement, `on_request` retourne sans publier de réponse quand `audio_bytes` est falsy ; risque de laisser le producteur sans retour.
- Chargement des modèles (audonnx / HF) peut échouer pour des raisons réseau, mémoire ou format de fichier.
- E/S disque pour fichiers temporaires : dépend du système (permissions, espace) et ajoute latence.
- Mémoire GPU et CPU : instanciation simultanée de modèles lourds peut provoquer OOM si plusieurs instances sont lancées.

Recommandations pour la robustesse
- Toujours publier un message d'erreur sur `audio_response_queue` même quand l'audio est introuvable.
- Remplacer `print` par `logging` structuré (niveau INFO/ERROR/DEBUG) et éventuellement exporter vers un backend (ELK, Grafana).
- Ajouter retry avec backoff sur `dl_audio_from_cache` pour transient failures.
- Valider la taille minimale / format du binaire audio avant traitement.
- Gérer proprement les exceptions lors du chargement des modèles et ajouter un état `ready`/`health` pour savoir si le worker peut traiter.
- Ajout d'un gestionnaire pour signaux Unix/Windows (arrêt gracieux, fermeture connexion RabbitMQ).

---

**Performance & Optimisations**

Optimisations immédiates
1. Lire l'audio depuis la mémoire (io.BytesIO + soundfile) au lieu d'écrire sur disque. Exemple :

```python
import io
import soundfile as sf
from pathlib import Path

audio_bytes = dl_audio_from_cache(request_id)
with io.BytesIO(audio_bytes) as bio:
    audio, sr = sf.read(bio, dtype='float32')
# convertir si nécessaire puis passer à la partie ML
```

2. Lancer plusieurs instances du worker (chaque instance `prefetch_count=1`) pour scaler horizontalement.
3. Faire du batching si possible pour Whisper si la latence et la nature des requêtes le permettent.
4. Warm-up CUDA (exécuter une inférence dummy après chargement) pour réduire la latence initiale.
5. Ajouter monitoring (Prometheus + exposer métriques) pour latence, erreurs et taille des messages.

Considérations mémoire
- Charger `whisper-large` nécessite beaucoup de VRAM ; utiliser `float16` sur GPU et modèles plus petits en fallback pour CPU.

---

**Checklist d'améliorations immédiates (pratique)**

- [ ] Toujours publier une réponse d'erreur quand audio introuvable.
- [ ] Remplacer `print` par `logging` structuré.
- [ ] Passer à lecture en mémoire pour éviter E/S disque.
- [ ] Ajouter retries/backoff pour `dl_audio_from_cache`.
- [ ] Ajouter healthcheck / readiness pour le service.
- [ ] Documenter clairement les variables d'environnement et les prérequis système.

---

**Propositions d'évolution**

- Remplacer la consommation synchrone par une architecture asynchrone (`aiohttp`, `aio-pika`) pour améliorer l'évolutivité.
- Déployer les modèles dans des microservices dédiés (ex: une API d'inférence pour Whisper, une pour ONNX) et appeler via HTTP internes, facilitant le scaling et la gestion mémoire.
- Ajouter contrôle de version des modèles et feature flags pour basculer entre `small` / `large` selon la charge.

---

Si vous le souhaitez, je peux appliquer automatiquement :
- patch minimal pour que `on_request` publie un message d'erreur lorsque l'audio est introuvable ; ou
- patch pour lire l'audio depuis mémoire (`io.BytesIO`) au lieu d'utiliser un fichier temporaire ; ou
- intégrer `logging` structuré et un petit README de déploiement Docker.

---

Fin du rapport.
