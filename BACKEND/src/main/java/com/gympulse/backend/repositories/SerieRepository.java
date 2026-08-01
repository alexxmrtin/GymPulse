package com.gympulse.backend.repositories;

import com.gympulse.backend.models.Serie;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SerieRepository extends JpaRepository<Serie, Long> {

    // SELECT * FROM series WHERE entrenamiento_id = ?
    List<Serie> findByEntrenamientoId(Long entrenamientoId);

    // SELECT * FROM series WHERE ejercicio_id = ?
    List<Serie> findByEjercicioId(Long ejercicioId);
}
