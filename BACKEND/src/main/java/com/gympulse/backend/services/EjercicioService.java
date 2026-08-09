package com.gympulse.backend.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.gympulse.backend.core.ManejoErrores.exceptions.RecursoNoEncontradoException;
import com.gympulse.backend.dtos.CrearEjercicioDTO;
import com.gympulse.backend.dtos.EjercicioDTO;
import com.gympulse.backend.helpers.Mapeador;
import com.gympulse.backend.helpers.Validaciones;
import com.gympulse.backend.models.Ejercicio;
import com.gympulse.backend.repositories.EjercicioRepository;

@Service
public class EjercicioService {

    private static final Logger log = LoggerFactory.getLogger(EjercicioService.class);

    private final EjercicioRepository ejercicioRepository;

    public EjercicioService(EjercicioRepository ejercicioRepository) {
        this.ejercicioRepository = ejercicioRepository;
    }

    public EjercicioDTO crear(CrearEjercicioDTO datos) {
        Validaciones.exigirTexto(datos.nombre(), "nombre");

        Ejercicio ejercicio = new Ejercicio();
        ejercicio.setNombre(datos.nombre());
        ejercicio.setDescripcion(datos.descripcion());
        ejercicio.setGrupoMuscular(datos.grupoMuscular());

        log.info("Creando ejercicio: {}", datos.nombre());
        return Mapeador.aDTO(ejercicioRepository.save(ejercicio));
    }

    public List<EjercicioDTO> buscarTodos() {
        log.debug("Buscando todos los ejercicios");
        return ejercicioRepository.findAll().stream().map(Mapeador::aDTO).toList();
    }

    public EjercicioDTO buscarPorId(Long id) {
        log.debug("Buscando ejercicio con id: {}", id);
        Ejercicio ejercicio = ejercicioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ejercicio no encontrado"));
        return Mapeador.aDTO(ejercicio);
    }

    public List<EjercicioDTO> buscarPorGrupoMuscular(String grupoMuscular) {
        log.debug("Buscando ejercicios del grupo muscular: {}", grupoMuscular);
        return ejercicioRepository.findByGrupoMuscular(grupoMuscular).stream().map(Mapeador::aDTO).toList();
    }

    public List<EjercicioDTO> buscarPorNombre(String nombre) {
        log.debug("Buscando ejercicios por nombre: {}", nombre);
        return ejercicioRepository.findByNombreContainingIgnoreCase(nombre).stream().map(Mapeador::aDTO).toList();
    }

    public void eliminar(Long id) {
        if (!ejercicioRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Ejercicio no encontrado");
        }
        log.info("Eliminando ejercicio con id: {}", id);
        ejercicioRepository.deleteById(id);
    }
}
