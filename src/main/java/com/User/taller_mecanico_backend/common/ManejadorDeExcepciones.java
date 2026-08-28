package com.User.taller_mecanico_backend.common;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ManejadorDeExcepciones {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorRespuesta> noEncontradoException(RecursoNoEncontradoException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorRespuesta(404, ex.getMessage(), Instant.now()));
    }

    @ExceptionHandler(RecursoExistente.class)
    public ResponseEntity<ErrorRespuesta> existenteException(RecursoExistente ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorRespuesta(409, ex.getMessage(), Instant.now()));
    }

}