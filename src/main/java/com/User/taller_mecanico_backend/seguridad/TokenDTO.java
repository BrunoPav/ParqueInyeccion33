package com.User.taller_mecanico_backend.seguridad;

public record TokenDTO(String token, String tipo, String nombreUsuario, String rol) {

    public TokenDTO(String token, String nombreUsuario, Rol rol) {
        this(token, "Bearer", nombreUsuario, rol.name());
    }
}
