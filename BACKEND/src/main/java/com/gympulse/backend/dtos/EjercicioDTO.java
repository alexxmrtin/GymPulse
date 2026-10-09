package com.gympulse.backend.dtos;

public record EjercicioDTO(
        Long id,
        String nombre,
        String descripcion,
        String grupoMuscular,
        boolean predefinido) {
}
