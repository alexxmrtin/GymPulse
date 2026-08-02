package com.gympulse.backend.services;

import com.gympulse.backend.models.Entrenamiento;
import com.gympulse.backend.repositories.EntrenamientoRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class EntrenamientoService {

    private final EntrenamientoRepository entrenamientoRepository;

    public EntrenamientoService(EntrenamientoRepository entrenamientoRepository) {
        this.entrenamientoRepository = entrenamientoRepository;
    }

    public Entrenamiento crear(Entrenamiento entrenamiento) {
        return entrenamientoRepository.save(entrenamiento);
    }

    public Optional<Entrenamiento> buscarPorId(Long id) {
        return entrenamientoRepository.findById(id);
    }

    // Todos los entrenamientos de un usuario, ordenados del mas reciente al mas antiguo
    public List<Entrenamiento> buscarPorUsuario(Long usuarioId) {
        return entrenamientoRepository.findByUsuarioIdOrderByFechaDesc(usuarioId);
    }

    public void eliminar(Long id) {
        entrenamientoRepository.deleteById(id);
    }
}
