package com.gympulse.backend.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gympulse.backend.core.ManejoErrores.exceptions.AccesoProhibidoException;
import com.gympulse.backend.core.ManejoErrores.exceptions.ConflictoException;
import com.gympulse.backend.core.ManejoErrores.exceptions.RecursoNoEncontradoException;
import com.gympulse.backend.dtos.CrearEjercicioDTO;
import com.gympulse.backend.dtos.EjercicioDTO;
import com.gympulse.backend.helpers.Mapeador;
import com.gympulse.backend.models.Ejercicio;
import com.gympulse.backend.models.Usuario;
import com.gympulse.backend.repositories.EjercicioRepository;
import com.gympulse.backend.repositories.SerieRepository;
import com.gympulse.backend.repositories.UsuarioRepository;

@Service
public class EjercicioService {

    private static final Logger log = LoggerFactory.getLogger(EjercicioService.class);

    private final EjercicioRepository ejercicioRepository;
    private final UsuarioRepository usuarioRepository;
    private final SerieRepository serieRepository;

    public EjercicioService(EjercicioRepository ejercicioRepository, UsuarioRepository usuarioRepository,
            SerieRepository serieRepository) {
        this.ejercicioRepository = ejercicioRepository;
        this.usuarioRepository = usuarioRepository;
        this.serieRepository = serieRepository;
    }

    @Transactional
    public EjercicioDTO crear(CrearEjercicioDTO datos, Long usuarioId) {
        String nombre = datos.nombre().trim();

        if (ejercicioRepository.existeVisible(usuarioId, nombre)) {
            throw new ConflictoException("Ya existe un ejercicio llamado '" + nombre + "'");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        Ejercicio ejercicio = new Ejercicio();
        ejercicio.setNombre(nombre);
        ejercicio.setDescripcion(datos.descripcion());
        ejercicio.setGrupoMuscular(datos.grupoMuscular());
        ejercicio.setUsuario(usuario);

        log.info("El usuario {} crea el ejercicio personalizado '{}'", usuarioId, nombre);
        return Mapeador.aDTO(ejercicioRepository.save(ejercicio));
    }

    @Transactional(readOnly = true)
    public List<EjercicioDTO> buscar(Long usuarioId, String nombre, String grupoMuscular) {
        List<Ejercicio> ejercicios;
        if (nombre != null) {
            ejercicios = ejercicioRepository.visiblesParaPorNombre(usuarioId, nombre);
        } else if (grupoMuscular != null) {
            ejercicios = ejercicioRepository.visiblesParaPorGrupo(usuarioId, grupoMuscular);
        } else {
            ejercicios = ejercicioRepository.visiblesPara(usuarioId);
        }
        return ejercicios.stream().map(Mapeador::aDTO).toList();
    }

    @Transactional(readOnly = true)
    public EjercicioDTO buscarPorId(Long id, Long usuarioId) {
        return Mapeador.aDTO(obtenerVisible(id, usuarioId));
    }

    @Transactional
    public void eliminar(Long id, Long usuarioId) {
        Ejercicio ejercicio = obtenerVisible(id, usuarioId);

        if (ejercicio.esPredefinido()) {
            throw new AccesoProhibidoException("Los ejercicios predefinidos no se pueden borrar");
        }
        if (serieRepository.existsByEjercicioId(id)) {
            throw new ConflictoException("Este ejercicio tiene series registradas y no se puede borrar");
        }

        log.info("El usuario {} borra su ejercicio {}", usuarioId, id);
        ejercicioRepository.delete(ejercicio);
    }

    public Ejercicio obtenerVisible(Long id, Long usuarioId) {
        Ejercicio ejercicio = ejercicioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ejercicio no encontrado"));

        if (!ejercicio.esPredefinido() && !ejercicio.getUsuario().getId().equals(usuarioId)) {
            log.warn("El usuario {} ha intentado acceder al ejercicio {}, que es de otro usuario", usuarioId, id);
            throw new RecursoNoEncontradoException("Ejercicio no encontrado");
        }
        return ejercicio;
    }
}
