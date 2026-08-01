package com.gympulse.backend.repositories;

import com.gympulse.backend.models.Ejercicio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EjercicioRepository extends JpaRepository<Ejercicio, Long> {

    // SELECT * FROM ejercicios WHERE grupo_muscular = ?
    List<Ejercicio> findByGrupoMuscular(String grupoMuscular);

    // SELECT * FROM ejercicios WHERE nombre LIKE %?%
    List<Ejercicio> findByNombreContainingIgnoreCase(String nombre);
}
