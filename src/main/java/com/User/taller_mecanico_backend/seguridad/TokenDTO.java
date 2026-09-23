package com.User.taller_mecanico_backend.seguridad;

public record TokenDTO(String token, String tipo, String nombreUsuario) {

    public TokenDTO(String token, String nombreUsuario) {
        this(token, "Bearer", nombreUsuario);
    }
}
