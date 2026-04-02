package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import com.example.demo.model.EstadoMascota;
import com.example.demo.model.Mascota;
import com.example.demo.repository.MascotaRepository;

import java.util.List;

@RestController
@RequestMapping("/api/mascotas")
@CrossOrigin(origins = "*")
public class MascotaController {

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    // GET /api/mascotas — Listar todas
    @GetMapping
    public List<Mascota> listarTodas() {
        return mascotaRepository.findAll();
    }

    // GET /api/mascotas/{id} — Obtener una
    @GetMapping("/{id}")
    public ResponseEntity<Mascota> obtenerPorId(@PathVariable Long id) {
        return mascotaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/mascotas/estado/{estado} — Filtrar por estado
    @GetMapping("/estado/{estado}")
    public List<Mascota> listarPorEstado(@PathVariable EstadoMascota estado) {
        return mascotaRepository.findByEstado(estado);
    }

    // POST /api/mascotas — Registrar nueva mascota y publicar en Kafka
    @PostMapping
    public Mascota registrar(@RequestBody Mascota mascota) {
        Mascota guardada = mascotaRepository.save(mascota);

        // Publicar evento en Kafka para el motor de coincidencias
        String mensaje = String.format(
            "{\"id\":%d,\"nombre\":\"%s\",\"estado\":\"%s\",\"especie\":\"%s\",\"color\":\"%s\"}",
            guardada.getId(), guardada.getNombre(),
            guardada.getEstado(), guardada.getEspecie(), guardada.getColor()
        );
        kafkaTemplate.send("mascotas-reportadas", mensaje);

        return guardada;
    }

    // PUT /api/mascotas/{id} — Actualizar
    @PutMapping("/{id}")
    public ResponseEntity<Mascota> actualizar(@PathVariable Long id, @RequestBody Mascota datos) {
        return mascotaRepository.findById(id).map(mascota -> {
            mascota.setNombre(datos.getNombre());
            mascota.setEspecie(datos.getEspecie());
            mascota.setRaza(datos.getRaza());
            mascota.setColor(datos.getColor());
            mascota.setTamano(datos.getTamano());
            mascota.setDescripcion(datos.getDescripcion());
            mascota.setFotoUrl(datos.getFotoUrl());
            mascota.setLatitud(datos.getLatitud());
            mascota.setLongitud(datos.getLongitud());
            mascota.setContacto(datos.getContacto());
            mascota.setEstado(datos.getEstado());
            return ResponseEntity.ok(mascotaRepository.save(mascota));
        }).orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/mascotas/{id} — Eliminar
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!mascotaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        mascotaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}