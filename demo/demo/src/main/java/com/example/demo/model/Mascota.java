package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "mascotas")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Mascota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String especie;         // PERRO, GATO, AVE, OTRO

    private String raza;

    private String color;

    private String tamano;          // PEQUENO, MEDIANO, GRANDE

    @Column(length = 500)
    private String descripcion;

    private String fotoUrl;

    private Double latitud;

    private Double longitud;

    private String contacto;        // Teléfono o email del dueño/reportante

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoMascota estado;   // PERDIDA, ENCONTRADA, REUNIDA

    @Column(nullable = false)
    private LocalDateTime fechaReporte;

    @PrePersist
    public void prePersist() {
        if (this.fechaReporte == null) {
            this.fechaReporte = LocalDateTime.now();
        }
    }
}