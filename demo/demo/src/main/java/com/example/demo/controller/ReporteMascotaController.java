package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.model.ReporteMascota;
import com.example.demo.model.TipoReporte;
import com.example.demo.repository.ReporteMascotaRepository;

import java.util.List;

@RestController
@RequestMapping("/api/reportes")
@CrossOrigin(origins = "*")
public class ReporteMascotaController {

    @Autowired
    private ReporteMascotaRepository reporteRepository;

    // GET /api/reportes — Todos los reportes
    @GetMapping
    public List<ReporteMascota> listarTodos() {
        return reporteRepository.findAll();
    }

    // GET /api/reportes/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ReporteMascota> obtenerPorId(@PathVariable Long id) {
        return reporteRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/reportes/mascota/{mascotaId}
    @GetMapping("/mascota/{mascotaId}")
    public List<ReporteMascota> porMascota(@PathVariable Long mascotaId) {
        return reporteRepository.findByMascotaId(mascotaId);
    }

    // GET /api/reportes/tipo/{tipo}
    @GetMapping("/tipo/{tipo}")
    public List<ReporteMascota> porTipo(@PathVariable TipoReporte tipo) {
        return reporteRepository.findByTipo(tipo);
    }

    // POST /api/reportes — Crear reporte
    @PostMapping
    public ReporteMascota crear(@RequestBody ReporteMascota reporte) {
        return reporteRepository.save(reporte);
    }

    // PUT /api/reportes/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ReporteMascota> actualizar(@PathVariable Long id,
                                                      @RequestBody ReporteMascota datos) {
        return reporteRepository.findById(id).map(reporte -> {
            reporte.setMascotaId(datos.getMascotaId());
            reporte.setTipo(datos.getTipo());
            reporte.setDescripcion(datos.getDescripcion());
            reporte.setUbicacion(datos.getUbicacion());
            reporte.setLatitud(datos.getLatitud());
            reporte.setLongitud(datos.getLongitud());
            reporte.setContacto(datos.getContacto());
            return ResponseEntity.ok(reporteRepository.save(reporte));
        }).orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/reportes/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!reporteRepository.existsById(id)) return ResponseEntity.notFound().build();
        reporteRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}