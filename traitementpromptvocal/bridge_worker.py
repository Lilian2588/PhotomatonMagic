import os
import pika
import requests
import json
import tempfile

# Import des services de traitement
from services.emotion_detection import EmotionDetector
from services.transcription_whisper import WhisperTranscriber

# --- CONFIGURATION ---
# RabbitMQ (Local ou CloudAMQP)
#RABBITMQ_HOST = 'localhost'
RABBITMQ_HOST = os.getenv('RABBITMQ_HOST', 'localhost')
QUEUE_NAME_REQUEST = 'audio_request_queue'
QUEUE_NAME_RESPONSE = 'audio_response_queue'

# API Spring (Pour récupérer le fichier audio via l'ID)
#URL_CACHE = "http://localhost:8083/cache/file"
DEFAULT_URL = "http://host.docker.internal:8083/cache/file" 
URL_CACHE = os.getenv('CACHE_URL', DEFAULT_URL)

# Chargement des modèles
emo_detector = EmotionDetector()
transcriber = WhisperTranscriber()

# --- FONCTIONS PRINCIPALES ---

def dl_audio_from_cache(request_id):
    """Appel api pour récupérer le binaire .wav grâce à l'ID"""
    url = f"{URL_CACHE}/{request_id}/audio" #?deleteAfterRead=true pour supprimer après lecture
    print(f" [⬇️] Téléchargement audio depuis Spring : {url}")
    try:
        resp = requests.get(url, timeout=30)
        if resp.status_code == 200:
            return resp.content # Les bytes du fichier
        else:
            print(f" [❌] Erreur Spring: {resp.status_code}")
            return None
    except Exception as e:
        print(f" [❌] Erreur connexion Spring: {e}")
        return None

def on_request(ch, method, props, body):
    request_id = body.decode('utf-8')
    print(f"\n [📥] Reçu demande ID : {request_id}")

    # Je renomme la variable car si tu utilises 'copyfileobj', c'est un flux (stream), pas un chemin
    audio_bytes = dl_audio_from_cache(request_id)
    # Clause de garde (Guard Clause) : on gère l'erreur tout de suite pour éviter d'imbriquer tout le code
    if not audio_bytes:
        response_data = {"emotion": "error", "transcription": "Inconnu"}
        # ... penser à publier la réponse ici ...
        return

    tmp_path = None
    
    try:
        # Utilisation de tempfile
        # delete=False : On demande à Python de ne pas le supprimer tout de suite à la fermeture, 
        # car tes modèles IA (librosa/whisper) vont devoir ouvrir ce fichier via son chemin.
        with tempfile.NamedTemporaryFile(suffix=".wav", delete=False) as tmp_file:
            tmp_file.write(audio_bytes)  
            tmp_path = tmp_file.name     

        # 3. Traitement (Le fichier est physiquement sur le disque et fermé, prêt à être lu)
        emotion = emo_detector.process_emotions(tmp_path)
        transcription = transcriber.process_transcription(tmp_path)
        response_data = {
            "requestId": request_id,
            "emotion": emotion,
            "transcription": transcription
        }
        print(" [✅] Traitement terminé.")

    except Exception as e:
        print(f" [❌] Erreur traitement: {e}")
        response_data = {"emotion": "error", "transcription": str(e)}

    finally:
        # 4. Nettoyage ultra simple
        # On a juste besoin de supprimer le fichier. Pas besoin de gérer le dossier parent.
        if tmp_path and os.path.exists(tmp_path):
            os.remove(tmp_path)

    print(f" [📤] Envoi ASYNCHRONE vers 'audio_response_queue'...")
    # Envoi de la réponse via RabbitMQ
    ch.basic_publish(
        exchange='',
        routing_key=QUEUE_NAME_RESPONSE, 
        properties=pika.BasicProperties(
            correlation_id=props.correlation_id,
            content_type='application/json',
            content_encoding='utf-8'
        ),
        body=json.dumps(response_data, ensure_ascii=False).encode('utf-8')
    )

    # On dit à RabbitMQ "le boulot est fini, tu peux supprimer la demande"
    ch.basic_ack(delivery_tag=method.delivery_tag)

def start():
    # --- LANCEMENT ---
    # Configuration avec heartbeat désactivé pour tolérer les longs traitements
    params = pika.ConnectionParameters(
        host=RABBITMQ_HOST,
        heartbeat=0,
        blocked_connection_timeout=300 
    )
    try:
        connection = pika.BlockingConnection(params)
        channel = connection.channel()
        
        # Déclaration des deux queues pour être sûr qu'elles existent
        channel.queue_declare(queue=QUEUE_NAME_REQUEST, durable=True)
        channel.queue_declare(queue=QUEUE_NAME_RESPONSE, durable=True) 
        
        channel.basic_qos(prefetch_count=1)
        channel.basic_consume(queue=QUEUE_NAME_REQUEST, on_message_callback=on_request)

        print(f" [🎧] En attente de messages sur '{QUEUE_NAME_REQUEST}' (Heartbeat: OFF)...")
        channel.start_consuming()
        
    except pika.exceptions.AMQPConnectionError as e:
        print(f" [❌] Impossible de se connecter à RabbitMQ : {e}")
        print("      Vérifie que le hostname est bon et que RabbitMQ est démarré.")

if __name__ == "__main__":
    start()
    