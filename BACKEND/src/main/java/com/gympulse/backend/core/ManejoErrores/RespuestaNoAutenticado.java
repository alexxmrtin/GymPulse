package com.gympulse.backend.core.ManejoErrores;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/*
 Los fallos de autenticacion ocurren en un filtro, antes de que la peticion llegue a ningun controlador, asi que ManejadorErrores no puede capturarlos: cuando salta, todavia no hay controlador al que aconsejar.
 Esta clase es el equivalente para esa etapa: escribe a mano el mismo JSON de error que devuelve el resto de la API para que el cliente no tenga que distinguir dos formatos.
*/
@Component
public class RespuestaNoAutenticado implements AuthenticationEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(RespuestaNoAutenticado.class);

    private final ObjectMapper objectMapper;

    public RespuestaNoAutenticado(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest peticion, HttpServletResponse respuesta,
            AuthenticationException excepcion) throws IOException {

        log.warn("Peticion sin token valido a {}: {}", peticion.getRequestURI(), excepcion.getMessage());

        respuesta.setStatus(HttpStatus.UNAUTHORIZED.value());
        respuesta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        respuesta.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(respuesta.getWriter(), Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "codigo", 401,
                "mensaje", "Token ausente, caducado o invalido"));
    }
}
