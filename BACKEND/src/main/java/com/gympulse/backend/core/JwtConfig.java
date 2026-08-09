package com.gympulse.backend.core;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;

@Configuration
public class JwtConfig {

    private static final int LONGITUD_MINIMA = 32;

    @Bean
    public SecretKey claveJwt(@Value("${gympulse.jwt.secreto}") String secreto) {
        if (secreto.length() < LONGITUD_MINIMA) {
            throw new IllegalStateException(
                    "gympulse.jwt.secreto debe tener al menos " + LONGITUD_MINIMA + " caracteres");
        }
        return new SecretKeySpec(secreto.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey claveJwt) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(claveJwt));
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey claveJwt) {
        return NimbusJwtDecoder
                .withSecretKey(claveJwt)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }
}
