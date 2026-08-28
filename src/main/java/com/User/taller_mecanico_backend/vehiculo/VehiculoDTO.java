package com.User.taller_mecanico_backend.vehiculo;

public record VehiculoDTO(Long id, String marca, String modelo, Integer anio, String patente, Integer kilometraje, Long clienteId) {}