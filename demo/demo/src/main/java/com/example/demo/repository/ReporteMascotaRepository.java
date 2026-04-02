package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.ReporteMascota;
import com.example.demo.model.TipoReporte;

import java.util.List;

@Repository
public interface ReporteMascotaRepository extends JpaRepository<ReporteMascota, Long> {

    // Todos los reportes asociados a una mascota
    List<ReporteMascota> findByMascotaId(Long mascotaId);

    // Filtrar por tipo: PERDIDA o ENCONTRADA
    List<ReporteMascota> findByTipo(TipoReporte tipo);

    // Reportes de una mascota filtrados por tipo
    List<ReporteMascota> findByMascotaIdAndTipo(Long mascotaId, TipoReporte tipo);
}