package com.example.demo.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.example.demo.model.ReporteMascota;
import com.example.demo.model.TipoReporte;
import com.example.demo.repository.ReporteMascotaRepository;

import java.time.LocalDateTime;

@Component
public class ControladorKafka {

    @Autowired
    private ReporteMascotaRepository reporteRepository;

    /**
     * Escucha el tópico "mascotas-reportadas".
     * Cuando llega un evento de mascota PERDIDA o ENCONTRADA,
     * se genera automáticamente un ReporteMascota de seguimiento.
     */
    @KafkaListener(topics = "mascotas-reportadas", groupId = "sanos-y-salvos-group")
    public void procesarReporteMascota(String mensaje) {
        System.out.println("[Kafka] Mensaje recibido: " + mensaje);

        try {
            // Parseo básico del JSON (sin dependencia extra)
            Long mascotaId = extraerLong(mensaje, "id");
            String estadoStr = extraerString(mensaje, "estado");

            if (mascotaId == null || estadoStr == null) {
                System.out.println("[Kafka] Mensaje incompleto, descartando.");
                return;
            }

            // Solo crear reporte automático para PERDIDA o ENCONTRADA
            TipoReporte tipo;
            try {
                tipo = TipoReporte.valueOf(estadoStr);
            } catch (IllegalArgumentException e) {
                System.out.println("[Kafka] Estado no relevante: " + estadoStr);
                return;
            }

            ReporteMascota reporte = new ReporteMascota();
            reporte.setMascotaId(mascotaId);
            reporte.setTipo(tipo);
            reporte.setDescripcion("Reporte automático generado por el sistema.");
            reporte.setFecha(LocalDateTime.now());
            reporte.setContacto("sistema@sanoysalvos.cl");

            reporteRepository.save(reporte);
            System.out.println("[Kafka] ReporteMascota automático creado para mascotaId=" + mascotaId);

        } catch (Exception e) {
            System.err.println("[Kafka] Error procesando mensaje: " + e.getMessage());
        }
    }

    // ---- Helpers de parseo ----

    private Long extraerLong(String json, String clave) {
        try {
            String patron = "\"" + clave + "\":";
            int inicio = json.indexOf(patron) + patron.length();
            int fin = json.indexOf(",", inicio);
            if (fin == -1) fin = json.indexOf("}", inicio);
            return Long.parseLong(json.substring(inicio, fin).trim());
        } catch (Exception e) { return null; }
    }

    private String extraerString(String json, String clave) {
        try {
            String patron = "\"" + clave + "\":\"";
            int inicio = json.indexOf(patron) + patron.length();
            int fin = json.indexOf("\"", inicio);
            return json.substring(inicio, fin).trim();
        } catch (Exception e) { return null; }
    }
}