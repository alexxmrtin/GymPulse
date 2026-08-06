package com.gympulse.backend.services;

import com.gympulse.backend.models.Ejercicio;
import com.gympulse.backend.repositories.EjercicioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class EjercicioService {

    private static final Logger log = LoggerFactory.getLogger(EjercicioService.class);

    private final EjercicioRepository ejercicioRepository;

    public EjercicioService(EjercicioRepository ejercicioRepository) {
        this.ejercicioRepository = ejercicioRepository;
    }

    public Ejercicio crear(Ejercicio ejercicio) {
        log.info("Creando ejercicio: {}", ejercicio.getNombre());
        return ejercicioRepository.save(ejercicio);
    }

    public List<Ejercicio> buscarTodos() {
        log.debug("Buscando todos los ejercicios");
        return ejercicioRepository.findAll();
    }

    public Optional<Ejercicio> buscarPorId(Long id) {
        log.debug("Buscando ejercicio con id: {}", id);
        return ejercicioRepository.findById(id);
    }

    public List<Ejercicio> buscarPorGrupoMuscular(String grupoMuscular) {
        log.debug("Buscando ejercicios del grupo muscular: {}", grupoMuscular);
        return ejercicioRepository.findByGrupoMuscular(grupoMuscular);
    }

    public List<Ejercicio> buscarPorNombre(String nombre) {
        log.debug("Buscando ejercicios por nombre: {}", nombre);
        return ejercicioRepository.findByNombreContainingIgnoreCase(nombre);
    }

    public void eliminar(Long id) {
        log.info("Eliminando ejercicio con id: {}", id);
        ejercicioRepository.deleteById(id);
    }
}
