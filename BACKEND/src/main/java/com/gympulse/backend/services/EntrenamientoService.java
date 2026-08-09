package com.gympulse.backend.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gympulse.backend.core.ManejoErrores.exceptions.RecursoNoEncontradoException;
import com.gympulse.backend.dtos.CrearEntrenamientoDTO;
import com.gympulse.backend.dtos.EntrenamientoDTO;
import com.gympulse.backend.helpers.Mapeador;
import com.gympulse.backend.helpers.Validaciones;
import com.gympulse.backend.models.Entrenamiento;
import com.gympulse.backend.models.Usuario;
import com.gympulse.backend.repositories.EntrenamientoRepository;
import com.gympulse.backend.repositories.UsuarioRepository;

@Service
public class EntrenamientoService {

    private static final Logger log = LoggerFactory.getLogger(EntrenamientoService.class);

    private final EntrenamientoRepository entrenamientoRepository;
    private final UsuarioRepository usuarioRepository;

    public EntrenamientoService(EntrenamientoRepository entrenamientoRepository, UsuarioRepository usuarioRepository) {
        this.entrenamientoRepository = entrenamientoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public EntrenamientoDTO crear(CrearEntrenamientoDTO datos, Long usuarioId) {
        Validaciones.exigirValor(datos.fecha(), "fecha");

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        Entrenamiento entrenamiento = new Entrenamiento();
        entrenamiento.setUsuario(usuario);
        entrenamiento.setNombre(datos.nombre());
        entrenamiento.setFecha(datos.fecha());

        log.info("Creando entrenamiento para usuario id: {}", usuarioId);
        return Mapeador.aDTO(entrenamientoRepository.save(entrenamiento));
    }

    @Transactional(readOnly = true)
    public EntrenamientoDTO buscarPorId(Long id, Long usuarioId) {
        log.debug("Buscando entrenamiento con id: {}", id);
        return Mapeador.aDTO(obtenerPropio(id, usuarioId));
    }

    @Transactional(readOnly = true)
    public List<EntrenamientoDTO> buscarPropios(Long usuarioId) {
        log.debug("Buscando entrenamientos del usuario id: {}", usuarioId);
        return entrenamientoRepository.findByUsuarioIdOrderByFechaDesc(usuarioId)
                .stream().map(Mapeador::aDTO).toList();
    }

    @Transactional
    public void eliminar(Long id, Long usuarioId) {
        obtenerPropio(id, usuarioId);
        log.info("Eliminando entrenamiento con id: {}", id);
        entrenamientoRepository.deleteById(id);
    }

    /*
     Devuelve el entrenamiento solo si pertenece al usuario que hace la peticion.
     Un entrenamiento de otro se responde como 404 y no como 403 a proposito: un 403 confirmaria
     que ese id existe, y eso ya es informacion sobre datos ajenos.
    */
    @Transactional(readOnly = true)
    public Entrenamiento obtenerPropio(Long id, Long usuarioId) {
        Entrenamiento entrenamiento = entrenamientoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Entrenamiento no encontrado"));

        if (!entrenamiento.getUsuario().getId().equals(usuarioId)) {
            log.warn("El usuario {} ha intentado acceder al entrenamiento {}, que no es suyo", usuarioId, id);
            throw new RecursoNoEncontradoException("Entrenamiento no encontrado");
        }

        return entrenamiento;
    }
}
