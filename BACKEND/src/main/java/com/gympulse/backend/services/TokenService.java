package com.gympulse.backend.services;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.gympulse.backend.models.Usuario;

@Service
public class TokenService {

    private static final Logger log = LoggerFactory.getLogger(TokenService.class);
    private static final String EMISOR = "gympulse";

    private final JwtEncoder jwtEncoder;
    private final long duracionMinutos;

    public TokenService(JwtEncoder jwtEncoder, @Value("${gympulse.jwt.duracion-minutos}") long duracionMinutos) {
        this.jwtEncoder = jwtEncoder;
        this.duracionMinutos = duracionMinutos;
    }

    public String generar(Usuario usuario) {
        Instant ahora = Instant.now();

        JwtClaimsSet contenido = JwtClaimsSet.builder()
                .issuer(EMISOR)
                .issuedAt(ahora)
                .expiresAt(ahora.plus(duracionMinutos, ChronoUnit.MINUTES))
                .subject(usuario.getId().toString())
                .claim("username", usuario.getUsername())
                .build();

        JwsHeader cabecera = JwsHeader.with(MacAlgorithm.HS256).build();

        log.debug("Emitiendo token para el usuario {}", usuario.getId());
        return jwtEncoder.encode(JwtEncoderParameters.from(cabecera, contenido)).getTokenValue();
    }

    public long duracionEnSegundos() {
        return duracionMinutos * 60;
    }
}
