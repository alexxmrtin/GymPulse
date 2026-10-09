package com.gympulse.backend.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CrearSerieDTO(
        @NotNull(message = "es obligatorio")
        Long entrenamientoId,

        @NotNull(message = "es obligatorio")
        Long ejercicioId,

        @NotNull(message = "es obligatorio")
        @Min(value = 1, message = "debe ser al menos 1")
        Integer repeticiones,

        @NotNull(message = "es obligatorio")
        @PositiveOrZero(message = "no puede ser negativo")
        Double peso) {
}
