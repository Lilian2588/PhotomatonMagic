# --- BUILD  ---
FROM node:18-alpine as build-stage

WORKDIR /app

# On copie juste les fichiers de dépendances d'abord (pour le cache Docker)
COPY package*.json ./

# Installation des dépendances
RUN npm install

# On copie tout le reste du code
COPY . .

# On lance la compilation (crée le dossier /dist)
RUN npm run build

# --- ÉTAPE 2 : RUN (On sert le site avec Nginx) ---
FROM nginx:alpine as production-stage

# On copie le dossier 'dist' généré à l'étape 1 vers le dossier de Nginx
# ATTENTION : Avec Vite c'est souvent 'dist', avec Create-React-App c'est 'build'
COPY --from=build-stage /app/dist /usr/share/nginx/html

# On copie notre configuration Nginx personnalisée
COPY nginx.conf /etc/nginx/conf.d/default.conf

# On expose le port 80 (standard pour le web)
EXPOSE 80

# On lance Nginx
CMD ["nginx", "-g", "daemon off;"]