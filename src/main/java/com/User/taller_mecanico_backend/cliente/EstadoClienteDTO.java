package com.User.taller_mecanico_backend.cliente;

import jakarta.validation.constraints.NotNull;
public record EstadoClienteDTO(@NotNull(message = " es obligatorio") Boolean activo) {}
