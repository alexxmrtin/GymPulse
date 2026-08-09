package com.gympulse.backend.core.ManejoErrores;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RespuestaAccesoDenegado implements AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(RespuestaAccesoDenegado.class);

    private final ObjectMapper objectMapper;

    public RespuestaAccesoDenegado(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest peticion, HttpServletResponse respuesta,
            AccessDeniedException excepcion) throws IOException {

        log.warn("Acceso denegado a {}: {}", peticion.getRequestURI(), excepcion.getMessage());

        respuesta.setStatus(HttpStatus.FORBIDDEN.value());
        respuesta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        respuesta.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(respuesta.getWriter(), Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "codigo", 403,
                "mensaje", "No tienes permiso para acceder a este recurso"));
    }
}
