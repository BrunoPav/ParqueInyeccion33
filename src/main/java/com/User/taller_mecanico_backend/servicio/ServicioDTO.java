package com.User.taller_mecanico_backend.servicio;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ServicioDTO(Long id,LocalDate fecha,String descripcion,BigDecimal precio,Long vehiculoId) {}
