package com.gympulse.backend.helpers;

import java.util.Optional;

public final class TipoImagen {

    private TipoImagen() {
    }

    public static Optional<String> detectar(byte[] datos) {
        if (empiezaPor(datos, 0, 0xFF, 0xD8, 0xFF)) {
            return Optional.of("image/jpeg");
        }
        if (empiezaPor(datos, 0, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)) {
            return Optional.of("image/png");
        }
        if (empiezaPor(datos, 0, 'R', 'I', 'F', 'F') && empiezaPor(datos, 8, 'W', 'E', 'B', 'P')) {
            return Optional.of("image/webp");
        }
        return Optional.empty();
    }

    private static boolean empiezaPor(byte[] datos, int desde, int... firma) {
        if (datos.length < desde + firma.length) {
            return false;
        }
        for (int i = 0; i < firma.length; i++) {
            if ((datos[desde + i] & 0xFF) != firma[i]) {
                return false;
            }
        }
        return true;
    }
}
