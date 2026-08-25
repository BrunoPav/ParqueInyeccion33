package com.User.taller_mecanico_backend.common;

public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String recurso, Long id) {
        super("%s con id %d no encontrado".formatted(recurso, id));
    }
}

