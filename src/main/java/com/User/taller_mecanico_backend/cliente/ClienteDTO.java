package com.User.taller_mecanico_backend.cliente;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClienteDTO(
    Long id, 
    @NotBlank(message = " es obligatorio") @Size(message = " no puede exceder los 100 caracteres", max = 100) String nombre, 
    @Size(message = " no puede exceder los 100 caracteres", max = 100) String contacto, 
    boolean activo) {

    }

