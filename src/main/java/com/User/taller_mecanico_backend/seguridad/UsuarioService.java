package com.User.taller_mecanico_backend.seguridad;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.User.taller_mecanico_backend.common.RecursoExistente;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UsuarioDTORespuesta crearUsuario(UsuarioDTOSolicitud dto) {
        if (usuarioRepository.existsByNombreUsuario(dto.nombreUsuario())) {
            throw new RecursoExistente("Usuario", "nombre de usuario", dto.nombreUsuario());
        }
        Usuario usuario = new Usuario(dto.nombreUsuario(), passwordEncoder.encode(dto.contrasena()));
        Usuario guardado = usuarioRepository.save(usuario);
        return new UsuarioDTORespuesta(guardado.getNombreUsuario());
    }


}
