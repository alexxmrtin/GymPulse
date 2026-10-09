package com.gympulse.backend.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.gympulse.backend.helpers.Privacidad;

@Service
public class CorreoService {

    private static final Logger log = LoggerFactory.getLogger(CorreoService.class);

    private final ObjectProvider<JavaMailSender> enviador;
    private final String remitente;

    public CorreoService(ObjectProvider<JavaMailSender> enviador,
            @Value("${gympulse.correo.remitente:no-responder@gympulse.local}") String remitente) {
        this.enviador = enviador;
        this.remitente = remitente;
    }

    @Async
    public void enviarVerificacion(String email, String enlace) {
        enviar(email, "Verifica tu email en GymPulse", """
                Hola,

                Para activar tu cuenta de GymPulse abre este enlace:

                %s

                Caduca en 24 horas. Si no te has registrado tú, ignora este correo.
                """.formatted(enlace), enlace);
    }

    @Async
    public void avisarRegistroDuplicado(String email) {
        enviar(email, "Intento de registro en GymPulse", """
                Hola,

                Alguien ha intentado crear una cuenta de GymPulse con este email, pero ya tienes una.
                Si fuiste tú, inicia sesión con tu contraseña. Si no, puedes ignorar este correo.
                """, null);
    }

    private void enviar(String para, String asunto, String texto, String enlace) {
        JavaMailSender servidor = enviador.getIfAvailable();
        if (servidor == null) {
            log.info("[CORREO sin SMTP] Para {} - {}{}", Privacidad.enmascararEmail(para), asunto,
                    enlace == null ? "" : " - enlace: " + enlace);
            return;
        }

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(para);
        mensaje.setSubject(asunto);
        mensaje.setText(texto);
        try {
            servidor.send(mensaje);
            log.info("Correo '{}' enviado a {}", asunto, Privacidad.enmascararEmail(para));
        } catch (RuntimeException e) {
            log.error("No se pudo enviar el correo '{}' a {}: {}", asunto, Privacidad.enmascararEmail(para), e.getMessage());
        }
    }
}
