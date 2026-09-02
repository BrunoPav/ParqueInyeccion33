package com.User.taller_mecanico_backend.servicio;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ServicioDTO(
    Long id,
    @NotNull(message = " es obligatorio") @PastOrPresent(message = " no puede ser una fecha futura") LocalDate fecha,
    @NotBlank(message = " es obligatorio") @Size(message = " no puede exceder los 2000 caracteres", max = 2000) String descripcion,
    @NotNull(message = " es obligatorio") @PositiveOrZero(message = " no puede ser negativo") BigDecimal precio,
    @NotNull(message = " es obligatorio") Long vehiculoId) {}
