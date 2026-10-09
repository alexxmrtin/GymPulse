package com.gympulse.backend.core.ManejoErrores.exceptions;

public class DemasiadosIntentosException extends RuntimeException {

    private final long segundosRestantes;

    public DemasiadosIntentosException(long segundosRestantes) {
        super("Demasiados intentos fallidos. Vuelve a intentarlo en "
                + Math.max(1, (segundosRestantes + 59) / 60) + " minutos");
        this.segundosRestantes = segundosRestantes;
    }

    public long getSegundosRestantes() {
        return segundosRestantes;
    }
}
