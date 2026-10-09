package com.gympulse.backend.services;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gympulse.backend.core.ManejoErrores.exceptions.CredencialesInvalidasException;
import com.gympulse.backend.helpers.TokenAleatorio;
import com.gympulse.backend.models.TokenRefresco;
import com.gympulse.backend.models.Usuario;
import com.gympulse.backend.repositories.TokenRefrescoRepository;

@Service
public class TokenRefrescoService {

    private static final Logger log = LoggerFactory.getLogger(TokenRefrescoService.class);
    private static final String SESION_CADUCADA = "La sesion ha caducado, vuelve a iniciar sesion";

    public record Rotacion(Usuario usuario, String nuevoToken) {
    }

    private final TokenRefrescoRepository repositorio;
    private final long duracionDias;

    public TokenRefrescoService(TokenRefrescoRepository repositorio,
            @Value("${gympulse.jwt.refresco-dias}") long duracionDias) {
        this.repositorio = repositorio;
        this.duracionDias = duracionDias;
    }

    @Transactional
    public String emitir(Usuario usuario) {
        Instant ahora = Instant.now();
        repositorio.borrarCaducadosDe(usuario.getId(), ahora);

        String token = TokenAleatorio.nuevo();

        TokenRefresco registro = new TokenRefresco();
        registro.setUsuario(usuario);
        registro.setHash(TokenAleatorio.hash(token));
        registro.setCreadoEn(ahora);
        registro.setExpiraEn(ahora.plus(duracionDias, ChronoUnit.DAYS));
        repositorio.save(registro);

        return token;
    }

    @Transactional(noRollbackFor = CredencialesInvalidasException.class)
    public Rotacion rotar(String token) {
        if (token == null || token.isBlank()) {
            throw new CredencialesInvalidasException(SESION_CADUCADA);
        }

        TokenRefresco registro = repositorio.findByHash(TokenAleatorio.hash(token))
                .orElseThrow(() -> new CredencialesInvalidasException(SESION_CADUCADA));
        Usuario usuario = registro.getUsuario();
        Instant ahora = Instant.now();

        if (registro.getRevocadoEn() != null) {
            int revocados = repositorio.revocarTodosDe(usuario.getId(), ahora);
            log.warn("Reutilizacion de un token de refresco ya usado del usuario {}: se revocan {} sesiones",
                    usuario.getId(), revocados);
            throw new CredencialesInvalidasException(SESION_CADUCADA);
        }
        if (registro.getExpiraEn().isBefore(ahora)) {
            throw new CredencialesInvalidasException(SESION_CADUCADA);
        }

        registro.setRevocadoEn(ahora);
        log.debug("Rotando token de refresco del usuario {}", usuario.getId());
        return new Rotacion(usuario, emitir(usuario));
    }

    @Transactional
    public void revocar(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        repositorio.findByHash(TokenAleatorio.hash(token)).ifPresent(registro -> {
            if (registro.getRevocadoEn() == null) {
                registro.setRevocadoEn(Instant.now());
                log.info("Sesion cerrada del usuario {}", registro.getUsuario().getId());
            }
        });
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void revocarTodosDe(Long usuarioId) {
        repositorio.revocarTodosDe(usuarioId, Instant.now());
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void borrarTodosDe(Long usuarioId) {
        repositorio.borrarTodosDe(usuarioId);
    }

    public long duracionEnSegundos() {
        return duracionDias * 24 * 60 * 60;
    }
}
