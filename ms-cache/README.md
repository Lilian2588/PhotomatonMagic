# Cache Media Microservice

Microservice Spring Boot pour stocker/récupérer des médias (image + audio) en Base64 via Redis.

## Prérequis

- Java 17+
- Maven 3.6+
- Docker (pour Redis)
- Redis sur `localhost:6379`

## Quick Start (Windows)

## Lancer Redis manuellement

### Avec Docker (recommandé)
```bash
docker run -d --name ms-cache-redis -p 6379:6379 redis:latest
```

### Avec Redis local
```bash
redis-server
```

## Lancer l'application manuellement

```bash
# Depuis la racine du projet
mvnw spring-boot:run

# Ou avec Maven installé
mvn spring-boot:run
```

Ou compiler puis exécuter :

```bash
mvn clean package
java -jar target/ms-cache-1.0.0.jar
```

## API Endpoints

**URL de base** : `http://localhost:8083`

### 1. Upload des médias (endpoint principal pour integration)

```bash
POST /cache/media/upload
Content-Type: application/json

{
  "requestId": "req123",
  "imageB64": "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==",
  "audioB64": "UklGRiQAAABXQVZFZm10IBAAAAABAAEAQB8AAEAfAAABAAgAZGF0YQAAAAA=",
  "imageName": "test.png"
}
```

**Réponse:**
```json
{
  "imageUrl": "http://localhost:8083/cache/file/req123/image",
  "audioUrl": "http://localhost:8083/cache/file/req123/audio",
  "status": "success"
}
```

### 2. Récupérer l'image

```bash
GET /cache/file/{requestId}/image
```
Retourne les bytes de l'image avec Content-Type: image/png

### 3. Récupérer l'audio

```bash
GET /cache/file/{requestId}/audio
```
Retourne les bytes de l'audio avec Content-Type: audio/wav

### 4. Sauvegarder des médias (legacy endpoint)

```bash
POST /cache/media
Content-Type: application/json

{
  "requestId": "req123",
  "imageB64": "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==",
  "audioB64": "UklGRiQAAABXQVZFZm10IBAAAAABAAEAQB8AAEAfAAABAAgAZGF0YQAAAAA=",
  "imageName": "test.png"
}
```

### 5. Mettre à jour le nom de l'image

```bash
POST /cache/media/{requestId}
Content-Type: application/json

{
  "newImageName": "nouveau-nom.png"
}
```

### 6. Récupérer les médias (format JSON)

```bash
# Sans suppression
GET /cache/media/{requestId}

# Avec suppression après lecture
GET /cache/media/{requestId}?deleteAfterRead=true
```

## Structure du projet

```
ms-cache/
├── src/main/java/com/example/ms_cache/
│   ├── MsCacheApplication.java
│   ├── config/
│   │   └── RedisConfig.java
│   ├── controller/
│   │   ├── CacheMediaController.java
│   │   └── FileRetrievalController.java (NEW)
│   ├── service/
│   │   └── CacheMediaService.java
│   ├── dto/
│   │   ├── MediaCacheRequest.java
│   │   ├── MediaCacheResponse.java
│   │   ├── CacheUploadResponseDTO.java (NEW)
│   │   └── UpdateImageNameRequest.java
│   └── exception/
│       ├── BadRequestException.java
│       ├── MediaNotFoundException.java
│       └── GlobalExceptionHandler.java
├── src/main/resources/
│   └── application.properties
├── quick-start-all.bat (NEW - Démarrage automatique)
├── start-redis.bat (NEW - Démarre Redis)
├── start-cache-service.bat (NEW - Démarre le service)
└── test-cache-api.bat (NEW - Tests API)
```

## Clés Redis utilisées

- `cache:media:{requestId}:image` → bytes de l'image
- `cache:media:{requestId}:audio` → bytes de l'audio
- `cache:media:{requestId}:meta` → hash contenant imageName et createdAt

## Notes importantes

- Le service tourne sur le port **8083** (modifié depuis 8080)
- L'endpoint principal pour l'intégration est **POST /cache/upload**
- Les URLs retournées permettent d'accéder directement aux fichiers
- Les fichiers restent en cache Redis jusqu'à suppression explicite
