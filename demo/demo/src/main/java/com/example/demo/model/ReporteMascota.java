package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reportes_mascota")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ReporteMascota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long mascotaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoReporte tipo;       // PERDIDA, ENCONTRADA

    @Column(length = 500)
    private String descripcion;

    private String ubicacion;       // Dirección textual

    private Double latitud;

    private Double longitud;

    @Column(nullable = false)
    private LocalDateTime fecha;

    private String contacto;        // Quién hace el reporte

    @PrePersist
    public void prePersist() {
        if (this.fecha == null) {
            this.fecha = LocalDateTime.now();
        }
    }
}