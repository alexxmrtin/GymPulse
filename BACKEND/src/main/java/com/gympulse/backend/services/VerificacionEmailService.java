package com.gympulse.backend.services;

import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gympulse.backend.helpers.TokenAleatorio;
import com.gympulse.backend.models.Usuario;
import com.gympulse.backend.repositories.UsuarioRepository;

@Service
public class VerificacionEmailService {

    private static final Logger log = LoggerFactory.getLogger(VerificacionEmailService.class);
    private static final Duration ESPERA_ENTRE_ENVIOS = Duration.ofMinutes(1);

    private final UsuarioRepository usuarioRepository;
    private final CorreoService correoService;
    private final boolean obligatoria;
    private final Duration validez;
    private final String urlPublica;

    public VerificacionEmailService(UsuarioRepository usuarioRepository, CorreoService correoService,
            @Value("${gympulse.registro.verificar-email}") boolean obligatoria,
            @Value("${gympulse.registro.verificacion-horas}") long horas,
            @Value("${gympulse.url-publica}") String urlPublica) {
        this.usuarioRepository = usuarioRepository;
        this.correoService = correoService;
        this.obligatoria = obligatoria;
        this.validez = Duration.ofHours(horas);
        this.urlPublica = urlPublica.endsWith("/") ? urlPublica.substring(0, urlPublica.length() - 1) : urlPublica;
    }

    public void iniciar(Usuario usuario) {
        if (!obligatoria) {
            usuario.setEmailVerificadoEn(Instant.now());
            return;
        }
        emitirYEnviar(usuario);
    }

    public void avisarRegistroDuplicado(String email) {
        if (obligatoria) {
            correoService.avisarRegistroDuplicado(email);
        }
    }

    @Transactional
    public boolean verificar(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        Usuario usuario = usuarioRepository.findByVerificacionHash(TokenAleatorio.hash(token)).orElse(null);
        if (usuario == null || usuario.getVerificacionExpiraEn().isBefore(Instant.now())) {
            return false;
        }

        usuario.setEmailVerificadoEn(Instant.now());
        usuario.setVerificacionHash(null);
        usuario.setVerificacionExpiraEn(null);
        log.info("Email verificado del usuario {}", usuario.getId());
        return true;
    }

    @Transactional
    public void reenviar(String email) {
        usuarioRepository.findByEmail(email)
                .filter(usuario -> usuario.getEmailVerificadoEn() == null)
                .filter(usuario -> usuario.getVerificacionEnviadaEn() == null
                        || usuario.getVerificacionEnviadaEn().plus(ESPERA_ENTRE_ENVIOS).isBefore(Instant.now()))
                .ifPresent(this::emitirYEnviar);
    }

    private void emitirYEnviar(Usuario usuario) {
        Instant ahora = Instant.now();
        String token = TokenAleatorio.nuevo();
        usuario.setVerificacionHash(TokenAleatorio.hash(token));
        usuario.setVerificacionExpiraEn(ahora.plus(validez));
        usuario.setVerificacionEnviadaEn(ahora);
        correoService.enviarVerificacion(usuario.getEmail(), urlPublica + "/usuario/verificar?token=" + token);
    }
}
