package com.gympulse.backend.dtos;

public record SerieDTO(
        Long id,
        Long entrenamientoId,
        Long ejercicioId,
        String ejercicioNombre,
        Integer repeticiones,
        Double peso) {
}
