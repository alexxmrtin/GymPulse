package com.gympulse.backend.services;

import com.gympulse.backend.models.Serie;
import com.gympulse.backend.repositories.SerieRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SerieService {

    private final SerieRepository serieRepository;

    public SerieService(SerieRepository serieRepository) {
        this.serieRepository = serieRepository;
    }

    public Serie crear(Serie serie) {
        return serieRepository.save(serie);
    }

    public List<Serie> buscarPorEntrenamiento(Long entrenamientoId) {
        return serieRepository.findByEntrenamientoId(entrenamientoId);
    }

    public List<Serie> buscarPorEjercicio(Long ejercicioId) {
        return serieRepository.findByEjercicioId(ejercicioId);
    }

    public void eliminar(Long id) {
        serieRepository.deleteById(id);
    }
}
