package com.gympulse.backend.services;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.gympulse.backend.core.ManejoErrores.exceptions.DemasiadosIntentosException;
import com.gympulse.backend.helpers.Privacidad;

@Service
public class LimiteIntentosService {

    private static final Logger log = LoggerFactory.getLogger(LimiteIntentosService.class);
    private static final int TAMANO_MAXIMO_ANTES_DE_LIMPIAR = 10_000;
    private static final String PREFIJO_EMAIL = "email:";

    private record Contador(int fallos, Instant primerFallo, Instant bloqueadoHasta) {
    }

    private final ConcurrentHashMap<String, Contador> contadores = new ConcurrentHashMap<>();
    private final Clock reloj;
    private final int maxFallosEmail;
    private final int maxFallosIp;
    private final Duration ventana;
    private final Duration bloqueo;

    @Autowired
    public LimiteIntentosService(
            @Value("${gympulse.login.max-fallos-email}") int maxFallosEmail,
            @Value("${gympulse.login.max-fallos-ip}") int maxFallosIp,
            @Value("${gympulse.login.ventana-minutos}") long ventanaMinutos,
            @Value("${gympulse.login.bloqueo-minutos}") long bloqueoMinutos) {
        this(Clock.systemUTC(), maxFallosEmail, maxFallosIp, ventanaMinutos, bloqueoMinutos);
    }

    LimiteIntentosService(Clock reloj, int maxFallosEmail, int maxFallosIp, long ventanaMinutos, long bloqueoMinutos) {
        this.reloj = reloj;
        this.maxFallosEmail = maxFallosEmail;
        this.maxFallosIp = maxFallosIp;
        this.ventana = Duration.ofMinutes(ventanaMinutos);
        this.bloqueo = Duration.ofMinutes(bloqueoMinutos);
    }

    public void comprobar(String email, String ip) {
        Instant ahora = reloj.instant();
        long restante = Math.max(segundosBloqueado(claveEmail(email), ahora), segundosBloqueado(claveIp(ip), ahora));
        if (restante > 0) {
            throw new DemasiadosIntentosException(restante);
        }
    }

    public void registrarFallo(String email, String ip) {
        Instant ahora = reloj.instant();
        sumar(claveEmail(email), maxFallosEmail, ahora);
        sumar(claveIp(ip), maxFallosIp, ahora);
        limpiarSiHaceFalta(ahora);
    }

    public void registrarExito(String email) {
        contadores.remove(claveEmail(email));
    }

    private void sumar(String clave, int maximo, Instant ahora) {
        contadores.compute(clave, (k, actual) -> {
            Contador vigente = (actual == null || caducado(actual, ahora)) ? new Contador(0, ahora, null) : actual;
            int fallos = vigente.fallos() + 1;
            if (fallos >= maximo) {
                log.warn("Login bloqueado {} minutos tras {} fallos: {}", bloqueo.toMinutes(), fallos, paraLog(k));
                return new Contador(0, ahora, ahora.plus(bloqueo));
            }
            return new Contador(fallos, vigente.primerFallo(), vigente.bloqueadoHasta());
        });
    }

    private long segundosBloqueado(String clave, Instant ahora) {
        Contador contador = contadores.get(clave);
        if (contador == null || contador.bloqueadoHasta() == null || !contador.bloqueadoHasta().isAfter(ahora)) {
            return 0;
        }
        return Duration.between(ahora, contador.bloqueadoHasta()).toSeconds() + 1;
    }

    private boolean caducado(Contador contador, Instant ahora) {
        boolean bloqueoTerminado = contador.bloqueadoHasta() == null || !contador.bloqueadoHasta().isAfter(ahora);
        return bloqueoTerminado && contador.primerFallo().plus(ventana).isBefore(ahora);
    }

    private void limpiarSiHaceFalta(Instant ahora) {
        if (contadores.size() > TAMANO_MAXIMO_ANTES_DE_LIMPIAR) {
            contadores.entrySet().removeIf(entrada -> caducado(entrada.getValue(), ahora));
        }
    }

    private static String paraLog(String clave) {
        return clave.startsWith(PREFIJO_EMAIL) ? PREFIJO_EMAIL + Privacidad.enmascararEmail(clave.substring(PREFIJO_EMAIL.length())) : clave;
    }

    private static String claveEmail(String email) {
        return PREFIJO_EMAIL + email.trim().toLowerCase(Locale.ROOT);
    }

    private static String claveIp(String ip) {
        return "ip:" + ip;
    }
}
