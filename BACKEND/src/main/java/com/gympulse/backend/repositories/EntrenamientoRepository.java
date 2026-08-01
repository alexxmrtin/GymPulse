package com.gympulse.backend.repositories;

import com.gympulse.backend.models.Entrenamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EntrenamientoRepository extends JpaRepository<Entrenamiento, Long> {

    // SELECT * FROM entrenamientos WHERE usuario_id = ?
    List<Entrenamiento> findByUsuarioId(Long usuarioId);

    // SELECT * FROM entrenamientos WHERE usuario_id = ? ORDER BY fecha DESC
    List<Entrenamiento> findByUsuarioIdOrderByFechaDesc(Long usuarioId);
}
