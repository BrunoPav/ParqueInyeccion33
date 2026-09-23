package com.User.taller_mecanico_backend.seguridad;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SembradorUsuario implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String usuarioAdmin;
    private final String contrasenaAdmin;
    private final String usuarioDemo;
    private final String contrasenaDemo;

    public SembradorUsuario(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.usuario}") String usuarioAdmin,
            @Value("${app.admin.contrasena}") String contrasenaAdmin,
            @Value("${app.demo.usuario}") String usuarioDemo,
            @Value("${app.demo.contrasena}") String contrasenaDemo) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.usuarioAdmin = usuarioAdmin;
        this.contrasenaAdmin = contrasenaAdmin;
        this.usuarioDemo = usuarioDemo;
        this.contrasenaDemo = contrasenaDemo;
    }

    @Override
    public void run(String... args) {
        sembrar(usuarioAdmin, contrasenaAdmin, Rol.ADMIN);
        sembrar(usuarioDemo, contrasenaDemo, Rol.DEMO);
    }

    private void sembrar(String nombreUsuario, String contrasena, Rol rol) {
        if (usuarioRepository.existsByNombreUsuario(nombreUsuario)) {
            return;
        }
        usuarioRepository.save(
                new Usuario(nombreUsuario, passwordEncoder.encode(contrasena), rol));
    }
}
