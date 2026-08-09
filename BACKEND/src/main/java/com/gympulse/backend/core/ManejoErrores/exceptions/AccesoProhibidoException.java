package com.gympulse.backend.core.ManejoErrores.exceptions;

public class AccesoProhibidoException extends RuntimeException {
    public AccesoProhibidoException(String mensaje) {
        super(mensaje);
    }
}
