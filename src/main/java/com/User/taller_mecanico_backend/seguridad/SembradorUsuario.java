package com.User.taller_mecanico_backend.seguridad;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SembradorUsuario implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String nombreUsuario;
    private final String contrasena;

    public SembradorUsuario(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.usuario}") String nombreUsuario,
            @Value("${app.admin.contrasena}") String contrasena) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.nombreUsuario = nombreUsuario;
        this.contrasena = contrasena;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.existsByNombreUsuario(nombreUsuario)) {
            return;
        }
        usuarioRepository.save(new Usuario(nombreUsuario, passwordEncoder.encode(contrasena)));
    }
}
