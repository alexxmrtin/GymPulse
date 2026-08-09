package com.gympulse.backend.core.ManejoErrores.exceptions;

public class CredencialesInvalidasException extends RuntimeException {
    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}
