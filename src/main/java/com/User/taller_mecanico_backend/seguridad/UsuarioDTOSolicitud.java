package com.User.taller_mecanico_backend.seguridad;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioDTOSolicitud(
        @NotBlank(message = " es obligatorio") @Size(message = " no puede exceder los 60 caracteres", max = 60) String nombreUsuario,
        @NotBlank(message = " es obligatorio") @Size(message = " minimo 8 caracteres", min = 8) String contrasena) 
{}


