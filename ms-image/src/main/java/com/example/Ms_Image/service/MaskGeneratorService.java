package com.example.Ms_Image.service;

import com.example.Ms_Image.DTO.FaceAnalysisResult;
import org.springframework.stereotype.Service;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.Base64;
import java.util.List;

@Service
public class MaskGeneratorService {

    /**
     * Conteneur pour transporter les 3 masques en mémoire sous forme de String Base64.
     */
    public static class MaskSet {
        private final String headMask, bodyMask, backgroundMask;

        public MaskSet(String h, String b, String bg) {
            this.headMask = h;
            this.bodyMask = b;
            this.backgroundMask = bg;
        }

        public String getHeadMask() { return headMask; }
        public String getBodyMask() { return bodyMask; }
        public String getBackgroundMask() { return backgroundMask; }
    }

    /**
     * Génère les masques Head, Body et Background sans enregistrement disque.
     */
    public MaskSet generateMasks(String imageBase64, FaceAnalysisResult analysis) throws Exception {
        // Décodage de l'image source
        String base64Data = imageBase64.contains(",") ? imageBase64.split(",")[1] : imageBase64;
        byte[] bytes = Base64.getDecoder().decode(base64Data);

        BufferedImage img;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            img = ImageIO.read(bais);
        }

        int w = img.getWidth();
        int h = img.getHeight();

        // 1. Génération des masques Head et Body (Blanc sur fond Noir)
        BufferedImage headM = drawContour(analysis.getZones().getHead().getContour(), w, h, 5);
        BufferedImage bodyM = drawContour(analysis.getZones().getBody().getContour(), w, h, 8);

        // 2. Génération du masque Background (Blanc partout, Noir sur la silhouette)
        BufferedImage bgM = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D gBg = bgM.createGraphics();

        gBg.setColor(Color.WHITE);
        gBg.fillRect(0, 0, w, h);

        gBg.setColor(Color.BLACK);
        fillZone(gBg, analysis.getZones().getHead().getContour());
        fillZone(gBg, analysis.getZones().getBody().getContour());
        gBg.dispose();

        // Flou pour une transition naturelle sur le décor
        bgM = applyBlur(bgM, 12);

        // Conversion directe des BufferedImages en Base64
        return new MaskSet(toB64(headM), toB64(bodyM), toB64(bgM));
    }

    private BufferedImage drawContour(List<List<Integer>> contour, int w, int h, int expand) {
        BufferedImage mask = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = mask.createGraphics();

        // Fond Noir (zone protégée)
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, w, h);

        // Zone Blanche (zone à modifier)
        g.setColor(Color.WHITE);
        fillZone(g, contour);

        // Expansion du masque si nécessaire pour éviter les bordures nettes
        if (expand > 0) {
            g.setStroke(new BasicStroke(expand * 2.0f));
            drawOutline(g, contour);
        }

        g.dispose();
        return applyBlur(mask, expand > 0 ? expand * 2 : 5);
    }

    private void fillZone(Graphics2D g, List<List<Integer>> contour) {
        if (contour == null || contour.isEmpty()) return;
        int[] x = contour.stream().mapToInt(p -> p.get(0)).toArray();
        int[] y = contour.stream().mapToInt(p -> p.get(1)).toArray();
        g.fillPolygon(x, y, contour.size());
    }

    private void drawOutline(Graphics2D g, List<List<Integer>> contour) {
        if (contour == null || contour.isEmpty()) return;
        int[] x = contour.stream().mapToInt(p -> p.get(0)).toArray();
        int[] y = contour.stream().mapToInt(p -> p.get(1)).toArray();
        g.drawPolygon(x, y, contour.size());
    }

    private BufferedImage applyBlur(BufferedImage img, int radius) {
        if (radius <= 0) radius = 1;
        int size = radius * 2 + 1;
        float weight = 1.0f / (size * size);
        float[] data = new float[size * size];
        java.util.Arrays.fill(data, weight);

        Kernel kernel = new Kernel(size, size, data);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        return op.filter(img, null);
    }

    private String toB64(BufferedImage img) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(img, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
        }
    }
}