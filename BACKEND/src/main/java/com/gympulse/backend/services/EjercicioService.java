package com.gympulse.backend.services;

import com.gympulse.backend.models.Ejercicio;
import com.gympulse.backend.repositories.EjercicioRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class EjercicioService {

    private final EjercicioRepository ejercicioRepository;

    public EjercicioService(EjercicioRepository ejercicioRepository) {
        this.ejercicioRepository = ejercicioRepository;
    }

    public Ejercicio crear(Ejercicio ejercicio) {
        return ejercicioRepository.save(ejercicio);
    }

    public List<Ejercicio> buscarTodos() {
        return ejercicioRepository.findAll();
    }

    public Optional<Ejercicio> buscarPorId(Long id) {
        return ejercicioRepository.findById(id);
    }

    public List<Ejercicio> buscarPorGrupoMuscular(String grupoMuscular) {
        return ejercicioRepository.findByGrupoMuscular(grupoMuscular);
    }

    public List<Ejercicio> buscarPorNombre(String nombre) {
        return ejercicioRepository.findByNombreContainingIgnoreCase(nombre);
    }

    public void eliminar(Long id) {
        ejercicioRepository.deleteById(id);
    }
}
