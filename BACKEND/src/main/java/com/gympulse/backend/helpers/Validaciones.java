package com.gympulse.backend.helpers;

/*
 Comprueba que los campos obligatorios de un DTO llegan con valor antes de tocar la BD, para no depender de que reviente la restriccion NOT NULL de la tabla.
 Lanza IllegalArgumentException, que ManejadorErrores traduce a un 400 diciendo que campo falta, en vez del 500 generico que devolveria la excepcion de la base de datos.
*/
public final class Validaciones {

    private Validaciones() {
    }

    public static void exigirTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo '" + campo + "' es obligatorio");
        }
    }

    public static void exigirValor(Object valor, String campo) {
        if (valor == null) {
            throw new IllegalArgumentException("El campo '" + campo + "' es obligatorio");
        }
    }
}
