package com.example.gestionacademica.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para toda la API.
 * Intercepta errores y los convierte en respuestas HTTP estructuradas.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja estudiantes inexistentes.
     * Retorna HTTP 404 cuando un recurso de estudiante no existe.
     */
    @ExceptionHandler(EstudianteNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEstudianteNotFoundException(
            EstudianteNotFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage());
    }

    /**
     * Maneja errores de autorización.
     * Retorna HTTP 403 para accesos autenticados no permitidos.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(
            AccessDeniedException ex) {
        return buildError(HttpStatus.FORBIDDEN, "Prohibido", ex.getMessage());
    }

    /**
     * Maneja identificadores con tipos inválidos en path/query params.
     * Retorna HTTP 400 para solicitudes mal formadas.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex) {
        return buildError(HttpStatus.BAD_REQUEST, "Solicitud inválida", "Identificador inválido");
    }

    /**
     * Maneja errores de logica de negocio (RuntimeException).
     * Retorna HTTP 400 con el mensaje del error.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException ex) {

        return buildError(HttpStatus.BAD_REQUEST, "Error de negocio", ex.getMessage());
    }

    /**
     * Maneja errores de validación de campos (@Valid).
     * Retorna HTTP 400 con detalle de cada campo inválido.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex) {

        Map<String, String> erroresCampos = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fieldError ->
            erroresCampos.put(fieldError.getField(), fieldError.getDefaultMessage())
        );

        Map<String, Object> error = baseError(HttpStatus.BAD_REQUEST, "Error de validación");
        error.put("campos", erroresCampos);

        return ResponseEntity.badRequest().body(error);
    }

    private ResponseEntity<Map<String, Object>> buildError(HttpStatus status, String error, String message) {
        Map<String, Object> body = baseError(status, error);
        body.put("mensaje", message);
        return ResponseEntity.status(status).body(body);
    }

    private Map<String, Object> baseError(HttpStatus status, String error) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", error);
        return body;
    }
}