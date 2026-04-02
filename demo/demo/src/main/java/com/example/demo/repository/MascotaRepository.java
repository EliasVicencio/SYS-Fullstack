package com.example.demo.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.EstadoMascota;
import com.example.demo.model.Mascota;

import java.util.List;

@Repository
public interface MascotaRepository extends JpaRepository<Mascota, Long> {

    // Buscar por estado: PERDIDA, ENCONTRADA, REUNIDA
    List<Mascota> findByEstado(EstadoMascota estado);

    // Buscar por especie (PERRO, GATO, etc.)
    List<Mascota> findByEspecie(String especie);

    // Buscar por color y especie (útil para el motor de coincidencias)
    List<Mascota> findByColorAndEspecie(String color, String especie);
}