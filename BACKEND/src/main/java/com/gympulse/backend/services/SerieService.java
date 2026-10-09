package com.gympulse.backend.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gympulse.backend.core.ManejoErrores.exceptions.RecursoNoEncontradoException;
import com.gympulse.backend.dtos.CrearSerieDTO;
import com.gympulse.backend.dtos.SerieDTO;
import com.gympulse.backend.helpers.Mapeador;
import com.gympulse.backend.models.Ejercicio;
import com.gympulse.backend.models.Entrenamiento;
import com.gympulse.backend.models.Serie;
import com.gympulse.backend.repositories.SerieRepository;

@Service
public class SerieService {

    private static final Logger log = LoggerFactory.getLogger(SerieService.class);

    private final SerieRepository serieRepository;
    private final EjercicioService ejercicioService;
    private final EntrenamientoService entrenamientoService;

    public SerieService(SerieRepository serieRepository,
            EjercicioService ejercicioService,
            EntrenamientoService entrenamientoService) {
        this.serieRepository = serieRepository;
        this.ejercicioService = ejercicioService;
        this.entrenamientoService = entrenamientoService;
    }

    @Transactional
    public SerieDTO crear(CrearSerieDTO datos, Long usuarioId) {

        Entrenamiento entrenamiento = entrenamientoService.obtenerPropio(datos.entrenamientoId(), usuarioId);

        Ejercicio ejercicio = ejercicioService.obtenerVisible(datos.ejercicioId(), usuarioId);

        Serie serie = new Serie();
        serie.setEntrenamiento(entrenamiento);
        serie.setEjercicio(ejercicio);
        serie.setRepeticiones(datos.repeticiones());
        serie.setPeso(datos.peso());

        log.info("Creando serie para entrenamiento id: {}", datos.entrenamientoId());
        return Mapeador.aDTO(serieRepository.save(serie));
    }

    @Transactional(readOnly = true)
    public List<SerieDTO> buscarPorEntrenamiento(Long entrenamientoId, Long usuarioId) {
        log.debug("Buscando series del entrenamiento id: {}", entrenamientoId);
        entrenamientoService.obtenerPropio(entrenamientoId, usuarioId);
        return serieRepository.findByEntrenamientoId(entrenamientoId).stream().map(Mapeador::aDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<SerieDTO> buscarPorEjercicio(Long ejercicioId, Long usuarioId) {
        log.debug("Buscando series del ejercicio id: {} del usuario {}", ejercicioId, usuarioId);
        return serieRepository.findByEjercicioIdAndEntrenamientoUsuarioId(ejercicioId, usuarioId)
                .stream().map(Mapeador::aDTO).toList();
    }

    @Transactional
    public void eliminar(Long id, Long usuarioId) {
        Serie serie = serieRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Serie no encontrada"));

        entrenamientoService.obtenerPropio(serie.getEntrenamiento().getId(), usuarioId);

        log.info("Eliminando serie con id: {}", id);
        serieRepository.deleteById(id);
    }
}
