package com.User.taller_mecanico_backend.seguridad;

import jakarta.validation.constraints.NotBlank;

public record CredencialesDTO(
        @NotBlank(message = " es obligatorio") String nombreUsuario,
        @NotBlank(message = " es obligatorio") String contrasena) {
}
