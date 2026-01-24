package com.example.Ms_Image.DTO;

import java.util.List;
import java.util.Map;

public class FaceAnalysisResult {
    private Zones zones;
    private Metadata metadata; // Ajouté pour correspondre au JSON

    public Zones getZones() { return zones; }
    public void setZones(Zones zones) { this.zones = zones; }

    public Metadata getMetadata() { return metadata; }
    public void setMetadata(Metadata metadata) { this.metadata = metadata; }

    public static class Zones {
        private ZoneData head;
        private ZoneData body;

        public ZoneData getHead() { return head; }
        public void setHead(ZoneData head) { this.head = head; }
        public ZoneData getBody() { return body; }
        public void setBody(ZoneData body) { this.body = body; }
    }

    public static class ZoneData {
        private List<List<Integer>> contour;

        public List<List<Integer>> getContour() { return contour; }
        public void setContour(List<List<Integer>> contour) { this.contour = contour; }
    }

    public static class Metadata {
        private Map<String, Integer> face_bbox; // Contient x, y, w, h

        public Map<String, Integer> getFace_bbox() { return face_bbox; }
        public void setFace_bbox(Map<String, Integer> face_bbox) { this.face_bbox = face_bbox; }
    }
}