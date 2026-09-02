package com.User.taller_mecanico_backend.vehiculo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record VehiculoDTO(
                Long id,
                @NotBlank(message = " es obligatorio") @Size(message = " no puede exceder los 50 caracteres", max = 50) String marca,
                @NotBlank(message = " es obligatorio") @Size(message = " no puede exceder los 50 caracteres", max = 50) String modelo,
                @NotNull(message = " es obligatorio") @Min(message = " no puede ser menor que 1900", value = 1900) @Max(message = " no puede ser mayor que 2100", value = 2100) Integer anio,
                @NotBlank(message = " es obligatorio") @Size(message = " no puede exceder los 10 caracteres", max = 10) String patente,
                @NotNull(message = " es obligatorio") @PositiveOrZero(message = " no puede ser negativo") Integer kilometraje,
                @NotNull(message = " es obligatorio") Long clienteId
) {}