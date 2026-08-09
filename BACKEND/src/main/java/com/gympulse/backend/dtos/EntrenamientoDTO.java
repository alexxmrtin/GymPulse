package com.gympulse.backend.dtos;

import java.time.LocalDate;
import java.util.List;

public record EntrenamientoDTO(
        Long id,
        Long usuarioId,
        String nombre,
        LocalDate fecha,
        List<SerieDTO> series) {
}
