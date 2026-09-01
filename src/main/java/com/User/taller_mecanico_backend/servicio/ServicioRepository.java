package com.User.taller_mecanico_backend.servicio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicioRepository extends JpaRepository<Servicio, Long> {
    
    List<Servicio> findByVehiculo_IdOrderByFechaDesc(Long vehiculoId);
    
}