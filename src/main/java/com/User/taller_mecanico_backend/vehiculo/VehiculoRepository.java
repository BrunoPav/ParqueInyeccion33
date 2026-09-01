package com.User.taller_mecanico_backend.vehiculo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VehiculoRepository  extends JpaRepository<Vehiculo, Long> {

    Optional<Vehiculo> findByPatente(String patente);

    List<Vehiculo> findByCliente_Id(Long clienteId);

    boolean existsByPatente(String patente);

    boolean existsByPatenteAndIdNot(String patente, Long id);

    List<Vehiculo> findByCliente_ActivoTrue();

}
