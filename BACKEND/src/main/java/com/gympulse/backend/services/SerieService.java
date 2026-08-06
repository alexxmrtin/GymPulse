package com.gympulse.backend.services;

import com.gympulse.backend.models.Serie;
import com.gympulse.backend.repositories.SerieRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SerieService {

    private static final Logger log = LoggerFactory.getLogger(SerieService.class);

    private final SerieRepository serieRepository;

    public SerieService(SerieRepository serieRepository) {
        this.serieRepository = serieRepository;
    }

    public Serie crear(Serie serie) {
        log.info("Creando serie para entrenamiento id: {}", serie.getEntrenamiento().getId());
        return serieRepository.save(serie);
    }

    public List<Serie> buscarPorEntrenamiento(Long entrenamientoId) {
        log.debug("Buscando series del entrenamiento id: {}", entrenamientoId);
        return serieRepository.findByEntrenamientoId(entrenamientoId);
    }

    public List<Serie> buscarPorEjercicio(Long ejercicioId) {
        log.debug("Buscando series del ejercicio id: {}", ejercicioId);
        return serieRepository.findByEjercicioId(ejercicioId);
    }

    public void eliminar(Long id) {
        log.info("Eliminando serie con id: {}", id);
        serieRepository.deleteById(id);
    }
}
