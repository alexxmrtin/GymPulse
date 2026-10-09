package com.gympulse.backend.services;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import com.gympulse.backend.core.ManejoErrores.exceptions.CredencialesInvalidasException;

@Component
public class VerificadorTokenGoogle {

    private static final Logger log = LoggerFactory.getLogger(VerificadorTokenGoogle.class);
    private static final String CLAVES_PUBLICAS_GOOGLE = "https://www.googleapis.com/oauth2/v3/certs";
    private static final Set<String> EMISORES_GOOGLE = Set.of("accounts.google.com", "https://accounts.google.com");
    private static final String TOKEN_NO_VALIDO = "No se ha podido verificar la cuenta de Google";

    public record IdentidadGoogle(String id, String email, String nombre) {
    }

    private final NimbusJwtDecoder decodificador;

    @Autowired
    public VerificadorTokenGoogle(@Value("${gympulse.google.client-id}") String clientId) {
        this(NimbusJwtDecoder.withJwkSetUri(CLAVES_PUBLICAS_GOOGLE).build(), clientId);
    }

    VerificadorTokenGoogle(NimbusJwtDecoder decodificador, String clientId) {
        OAuth2TokenValidator<Jwt> validador = new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(),
                new JwtClaimValidator<String>(JwtClaimNames.ISS, EMISORES_GOOGLE::contains),
                new JwtClaimValidator<List<String>>(JwtClaimNames.AUD, audiencia -> audiencia != null && audiencia.contains(clientId)));
        decodificador.setJwtValidator(validador);
        this.decodificador = decodificador;
    }

    public IdentidadGoogle verificar(String idToken) {
        Jwt token;
        try {
            token = decodificador.decode(idToken);
        } catch (JwtException e) {
            log.warn("Token de Google rechazado: {}", e.getMessage());
            throw new CredencialesInvalidasException(TOKEN_NO_VALIDO);
        }

        Object emailVerificado = token.getClaim("email_verified");
        if (!Boolean.TRUE.equals(emailVerificado) && !"true".equals(emailVerificado)) {
            log.warn("Token de Google con el email sin verificar");
            throw new CredencialesInvalidasException(TOKEN_NO_VALIDO);
        }

        return new IdentidadGoogle(token.getSubject(), token.getClaimAsString("email"), token.getClaimAsString("name"));
    }
}
