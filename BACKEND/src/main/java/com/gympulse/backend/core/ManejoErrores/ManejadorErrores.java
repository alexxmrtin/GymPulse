package com.gympulse.backend.core.ManejoErrores;

import java.time.LocalDateTime;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.gympulse.backend.core.ManejoErrores.exceptions.ConflictoException;
import com.gympulse.backend.core.ManejoErrores.exceptions.RecursoNoEncontradoException;

/*
 @RestControllerAdvice convierte esta clase en un interceptor global de excepciones.
 Cualquier excepcion que se lance desde un servicio o controlador llega aqui
 antes de salir como respuesta HTTP. Sin esto, Spring devuelve un 500 generico
 con un stacktrace que no le dice nada util al cliente.

 Cada metodo anotado con @ExceptionHandler captura un tipo de excepcion concreto
 y devuelve la respuesta HTTP apropiada.
*/
@RestControllerAdvice
public class ManejadorErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorErrores.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> manejarNoEncontrado(RecursoNoEncontradoException ex) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(respuesta(404, ex.getMessage()));
    }

    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<Map<String, Object>> manejarConflicto(ConflictoException ex) {
        log.warn("Conflicto: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(respuesta(409, ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> manejarArgumentoInvalido(IllegalArgumentException ex) {
        log.warn("Argumento invalido: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(respuesta(400, ex.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> manejarErrorGenerico(RuntimeException ex) {
        log.error("Error inesperado: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(respuesta(500, "Error interno del servidor"));
    }

    
    //Construye el cuerpo JSON de la respuesta de error.Todos los errores tienen la misma estructura para que el cliente pueda procesarlos de forma uniforme.
    private Map<String, Object> respuesta(int codigo, String mensaje) {
        return Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "codigo", codigo,
                "mensaje", mensaje
        );
    }
}
