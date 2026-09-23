package com.User.taller_mecanico_backend.common;

import java.time.Instant;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

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

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorRespuesta> credencialesInvalidas(AuthenticationException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorRespuesta(401, "Credenciales invalidas", Instant.now()));
    }

    @ExceptionHandler (MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuesta> validacionFallida (MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors()
        .stream()
        .map(error -> error.getField() + ":" + error.getDefaultMessage())
        .collect(Collectors.joining("; "));
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorRespuesta(400, mensaje, Instant.now()));
    }
}