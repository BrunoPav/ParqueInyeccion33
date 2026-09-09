package com.User.taller_mecanico_backend.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ConfiguracionCors implements WebMvcConfigurer {

    private final String[] origenesPermitidos;

    public ConfiguracionCors(@Value("${app.cors.origenes}") String[] origenesPermitidos) {
        this.origenesPermitidos = origenesPermitidos;
    }

    @Override
    public void addCorsMappings(CorsRegistry registro) {
        // allowedOriginPatterns y no allowedOrigins: en desarrollo el frontend
        // arranca en un puerto distinto cada vez, y solo la version con patrones
        // acepta el comodin del puerto.
        registro.addMapping("/api/**")
                .allowedOriginPatterns(origenesPermitidos)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE");
    }
}
