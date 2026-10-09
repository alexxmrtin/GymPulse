package com.gympulse.backend.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


//Implementamos Cors por estandarizacion a la hora de desarrollar una API, aunque la aplicacion esta destinada a ser una app movil, en un futuro se puede agregar un frontend web

/*
 @Configuration le dice a Spring que esta clase contiene configuracion del framework,
 no logica de negocio. Spring la procesa al arrancar antes que los controladores.

 WebMvcConfigurer es una interfaz de Spring MVC con metodos para personalizar
 el comportamiento del servidor web. Solo sobreescribimos el que nos interesa.
*/
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final String[] origenesPermitidos;

    public CorsConfig(@Value("${gympulse.cors.origenes:*}") String[] origenesPermitidos) {
        this.origenesPermitidos = origenesPermitidos;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry
            .addMapping("/**")                          // aplica a todos los endpoints
            .allowedOrigins(origenesPermitidos)         // "*" en desarrollo; en prod, gympulse.cors.origenes
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("*");                       // cualquier cabecera
    }
}
