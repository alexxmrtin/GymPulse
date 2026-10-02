package com.gympulse.backend.core.ManejoErrores;

import java.time.LocalDateTime;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import com.gympulse.backend.core.ManejoErrores.exceptions.AccesoProhibidoException;
import com.gympulse.backend.core.ManejoErrores.exceptions.ConflictoException;
import com.gympulse.backend.core.ManejoErrores.exceptions.CredencialesInvalidasException;
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

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<Map<String, Object>> manejarCredencialesInvalidas(CredencialesInvalidasException ex) {
        log.warn("Credenciales invalidas: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(respuesta(401, ex.getMessage()));
    }

    @ExceptionHandler(AccesoProhibidoException.class)
    public ResponseEntity<Map<String, Object>> manejarAccesoProhibido(AccesoProhibidoException ex) {
        log.warn("Acceso prohibido: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(respuesta(403, ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> manejarIntegridad(DataIntegrityViolationException ex) {
        log.warn("Violacion de integridad en la BD: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(respuesta(409, "La operacion entra en conflicto con datos ya existentes"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> manejarArgumentoInvalido(IllegalArgumentException ex) {
        log.warn("Argumento invalido: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(respuesta(400, ex.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> manejarArchivoDemasiadoGrande(MaxUploadSizeExceededException ex) {
        log.warn("Archivo demasiado grande: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONTENT_TOO_LARGE)
                .body(respuesta(413, "La imagen no puede superar los 5 MB"));
    }

    @ExceptionHandler({ MultipartException.class, MissingServletRequestPartException.class })
    public ResponseEntity<Map<String, Object>> manejarSubidaInvalida(Exception ex) {
        log.warn("Subida de archivo invalida: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(respuesta(400, "Envia la imagen como multipart/form-data en el campo 'foto'"));
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
