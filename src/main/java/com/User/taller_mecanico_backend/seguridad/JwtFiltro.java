package com.User.taller_mecanico_backend.seguridad;

import java.io.IOException;

import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFiltro extends OncePerRequestFilter {

    private static final String CABECERA = "Authorization";
    private static final String PREFIJO = "Bearer ";

    private final JwtService jwtService;
    private final DetallesUsuarioService detallesUsuarioService;

    public JwtFiltro(JwtService jwtService, DetallesUsuarioService detallesUsuarioService) {
        this.jwtService = jwtService;
        this.detallesUsuarioService = detallesUsuarioService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest peticion,
            @NonNull HttpServletResponse respuesta,
            @NonNull FilterChain cadena) throws ServletException, IOException {

        String token = extraerToken(peticion);

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            String nombreUsuario = jwtService.nombreUsuarioSiEsValido(token);

            if (nombreUsuario != null) {
                UserDetails detalles = detallesUsuarioService.loadUserByUsername(nombreUsuario);

                var autenticacion = new UsernamePasswordAuthenticationToken(
                        detalles, null, detalles.getAuthorities());
                autenticacion.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(peticion));

                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            }
        }

        cadena.doFilter(peticion, respuesta);
    }

    private String extraerToken(HttpServletRequest peticion) {
        String cabecera = peticion.getHeader(CABECERA);
        return (cabecera != null && cabecera.startsWith(PREFIJO))
                ? cabecera.substring(PREFIJO.length())
                : null;
    }
}
