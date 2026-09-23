package com.User.taller_mecanico_backend.seguridad;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AutenticacionController {

    private final AuthenticationManager gestorDeAutenticacion;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public AutenticacionController(
            AuthenticationManager gestorDeAutenticacion,
            JwtService jwtService,
            UsuarioRepository usuarioRepository) {
        this.gestorDeAutenticacion = gestorDeAutenticacion;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/login")
    public TokenDTO login(@Valid @RequestBody CredencialesDTO credenciales) {
        gestorDeAutenticacion.authenticate(new UsernamePasswordAuthenticationToken(
                credenciales.nombreUsuario(), credenciales.contrasena()));

        Usuario usuario = usuarioRepository.findByNombreUsuario(credenciales.nombreUsuario())
                .orElseThrow(() -> new BadCredentialsException("Credenciales invalidas"));

        return new TokenDTO(
                jwtService.generarToken(usuario.getNombreUsuario(), usuario.getRol()),
                usuario.getNombreUsuario(),
                usuario.getRol());
    }
}
