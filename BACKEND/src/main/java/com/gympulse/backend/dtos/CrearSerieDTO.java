package com.gympulse.backend.dtos;

public record CrearSerieDTO(
        Long entrenamientoId,
        Long ejercicioId,
        Integer repeticiones,
        Double peso) {
}
