package com.gympulse.backend.dtos;

import java.time.LocalDate;

public record CrearEntrenamientoDTO(
        String nombre,
        LocalDate fecha) {
}
