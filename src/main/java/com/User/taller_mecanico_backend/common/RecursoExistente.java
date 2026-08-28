package com.User.taller_mecanico_backend.common;

public class RecursoExistente extends RuntimeException {
    
    public RecursoExistente(String recurso, Long id) {
        super("%s con id %d ya existe".formatted(recurso, id));
    }

    public RecursoExistente(String recurso, String criterio, String valor) {
        super("%s con %s %s ya existe".formatted(recurso, criterio, valor));
    }
    
}
