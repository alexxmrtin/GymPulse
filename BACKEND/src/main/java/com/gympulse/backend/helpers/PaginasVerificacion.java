package com.gympulse.backend.helpers;

public final class PaginasVerificacion {

    private PaginasVerificacion() {
    }

    public static final String EXITO = pagina("Email verificado",
            "Tu cuenta de GymPulse ya está activa. Vuelve a la app e inicia sesión.");

    public static final String ERROR = pagina("Enlace no válido",
            "El enlace ha caducado o ya se ha usado. Desde la app puedes pedir que te enviemos otro.");

    private static String pagina(String titulo, String texto) {
        return """
                <!doctype html>
                <html lang="es">
                <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>GymPulse · %1$s</title>
                <style>
                  body { margin: 0; min-height: 100vh; display: grid; place-items: center; background: #000;
                         color: #fff; font-family: system-ui, sans-serif; padding: 24px; box-sizing: border-box; }
                  main { max-width: 420px; text-align: center; }
                  h1 { color: #D0BCFF; font-size: 1.6rem; }
                  p { color: #999; line-height: 1.5; }
                </style>
                </head>
                <body><main><h1>%1$s</h1><p>%2$s</p></main></body>
                </html>
                """.formatted(titulo, texto);
    }
}
