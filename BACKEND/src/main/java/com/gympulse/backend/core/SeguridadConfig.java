package com.gympulse.backend.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.gympulse.backend.core.ManejoErrores.RespuestaAccesoDenegado;
import com.gympulse.backend.core.ManejoErrores.RespuestaNoAutenticado;

/*
 Registra en Spring el objeto que cifra contrasenas con BCrypt, para poder inyectarlo en UsuarioService: al registrar cifra la contrasena con encode(), y al hacer login compara la que llega con el hash guardado usando matches(), sin descifrar nada.
 Se hace con un @Bean porque BCryptPasswordEncoder es una clase de una libreria y no podemos anotarla como componente; el metodo devuelve la instancia y Spring se encarga de crearla una vez y repartirla a quien la pida.
*/

@Configuration
public class SeguridadConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //Define la cadena de filtros por la que pasa toda peticion antes de llegar al controlador: apaga lo que no aplica a una API (sesiones, CSRF, los logins que Spring monta solo) y decide que endpoints estan abiertos.
    @Bean
    public SecurityFilterChain cadenaDeFiltros(HttpSecurity http,
            RespuestaNoAutenticado respuestaNoAutenticado,
            RespuestaAccesoDenegado respuestaAccesoDenegado,
            @Value("${gympulse.https-obligatorio:false}") boolean httpsObligatorio) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(basic -> basic.disable())
                .formLogin(formulario -> formulario.disable())

                .authorizeHttpRequests(peticiones -> peticiones
                        .requestMatchers(HttpMethod.POST, "/usuario", "/usuario/login", "/usuario/login/google",
                                "/usuario/refresco", "/usuario/logout", "/usuario/verificacion/reenviar").permitAll()
                        .requestMatchers(HttpMethod.GET, "/usuario/verificar").permitAll()
                        .anyRequest().authenticated())

                .oauth2ResourceServer(servidor -> servidor
                        .jwt(Customizer.withDefaults())
                        .authenticationEntryPoint(respuestaNoAutenticado)
                        .accessDeniedHandler(respuestaAccesoDenegado))

                .exceptionHandling(errores -> errores
                        .authenticationEntryPoint(respuestaNoAutenticado)
                        .accessDeniedHandler(respuestaAccesoDenegado));

        if (httpsObligatorio) {
            http.redirectToHttps(Customizer.withDefaults());
        }

        return http.build();
    }
}
