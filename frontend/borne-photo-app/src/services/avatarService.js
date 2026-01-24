/**
 * Fonction utilitaire pour compresser l'image
 * Réduit la taille à max 1024px et convertit en JPEG qualité 0.7
 */
const compressImage = (base64Str, maxWidth = 1024, quality = 0.7) => {
    return new Promise((resolve) => {
        // Si l'image n'a pas de header, on l'ajoute temporairement pour que le navigateur la lise
        const imgSrc = base64Str.startsWith('data:image') 
            ? base64Str 
            : `data:image/png;base64,${base64Str}`;

        const img = new Image();
        img.src = imgSrc;

        img.onload = () => {
            const canvas = document.createElement('canvas');
            let width = img.width;
            let height = img.height;

            // Calcul du ratio pour redimensionner si nécessaire
            if (width > maxWidth) {
                height = Math.round((height * maxWidth) / width);
                width = maxWidth;
            }

            canvas.width = width;
            canvas.height = height;

            const ctx = canvas.getContext('2d');
            ctx.drawImage(img, 0, 0, width, height);

            // Conversion en JPEG compressé (ce qui réduit drastiquement la taille)
            // On récupère le résultat et on enlève le header "data:image/jpeg;base64,"
            const newDataUrl = canvas.toDataURL('image/jpeg', quality);
            const cleanData = newDataUrl.split(',')[1];
            
            console.log("📉 Compression :", Math.round(base64Str.length / 1024) + "ko -> " + Math.round(cleanData.length / 1024) + "ko");
            
            resolve(cleanData);
        };
        
        img.onerror = (err) => {
            console.error("Erreur compression image", err);
            // En cas d'erreur, on renvoie l'original (tant pis pour la taille)
            resolve(base64Str.includes(',') ? base64Str.split(',')[1] : base64Str);
        };
    });
};

/**
 * Service gérant l'envoi des données vers le Monolithe
 */
export const sendAvatarData = async (audioBlobUrl, imageBase64, permission, style = null) => {
    
    // 1. Conversion de l'Audio (Blob URL -> Base64 pur)
    const audioBlob = await fetch(audioBlobUrl).then(r => r.blob());
    
    const audioBase64 = await new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onloadend = () => {
            if (!reader.result) {
                reject("Échec de la conversion audio");
                return;
            }
            const base64String = reader.result.toString();
            resolve(base64String.includes(',') ? base64String.split(',')[1] : base64String);
        };
        reader.onerror = reject;
        reader.readAsDataURL(audioBlob);
    });

    // 2. COMPRESSION DE L'IMAGE (C'est l'étape qui manquait !) 📸
    // On attend que la compression soit finie avant de continuer
    const compressedImage = await compressImage(imageBase64);

    // 3. Construction du Payload
    const payload = {
        audioBase64: audioBase64,
        imageBase64: compressedImage, // On envoie la version légère
        userPermission: permission,
        style: style 
    };

    console.log("📤 Envoi payload vers Monolithe...");

    // 4. Appel HTTP POST
    // Utilisation d'un chemin relatif pour passer par Nginx
    const response = await fetch("/avatar/process", {
        method: "POST",
        headers: { 
            "Content-Type": "application/json" 
        },
        body: JSON.stringify(payload)
    });

    if (!response.ok) {
        // Si c'est une 413, on le verra ici
        if (response.status === 413) {
            throw new Error("L'image est toujours trop lourde pour le serveur (413).");
        }
        const errorText = await response.text();
        throw new Error(`Erreur Backend (${response.status}): ${errorText}`);
    }

    return await response.text();
};