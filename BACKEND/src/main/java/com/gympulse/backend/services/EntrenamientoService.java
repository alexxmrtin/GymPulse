package com.gympulse.backend.services;

import com.gympulse.backend.models.Entrenamiento;
import com.gympulse.backend.repositories.EntrenamientoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class EntrenamientoService {

    private static final Logger log = LoggerFactory.getLogger(EntrenamientoService.class);

    private final EntrenamientoRepository entrenamientoRepository;

    public EntrenamientoService(EntrenamientoRepository entrenamientoRepository) {
        this.entrenamientoRepository = entrenamientoRepository;
    }

    public Entrenamiento crear(Entrenamiento entrenamiento) {
        log.info("Creando entrenamiento para usuario id: {}", entrenamiento.getUsuario().getId());
        return entrenamientoRepository.save(entrenamiento);
    }

    public Optional<Entrenamiento> buscarPorId(Long id) {
        log.debug("Buscando entrenamiento con id: {}", id);
        return entrenamientoRepository.findById(id);
    }

    public List<Entrenamiento> buscarPorUsuario(Long usuarioId) {
        log.debug("Buscando entrenamientos del usuario id: {}", usuarioId);
        return entrenamientoRepository.findByUsuarioIdOrderByFechaDesc(usuarioId);
    }

    public void eliminar(Long id) {
        log.info("Eliminando entrenamiento con id: {}", id);
        entrenamientoRepository.deleteById(id);
    }
}
