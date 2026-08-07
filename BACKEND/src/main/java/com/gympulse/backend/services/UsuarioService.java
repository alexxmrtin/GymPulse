package com.gympulse.backend.services;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.gympulse.backend.core.ManejoErrores.exceptions.ConflictoException;
import com.gympulse.backend.core.ManejoErrores.exceptions.RecursoNoEncontradoException;
import com.gympulse.backend.models.Usuario;
import com.gympulse.backend.repositories.UsuarioRepository;

/*
 @Service le dice a Spring que esta clase contiene logica de negocio.
 Spring la registra y la tiene disponible para inyectarla donde se necesite.

 La capa de servicio es el intermediario entre el controlador y el repositorio:
   Controlador → Servicio → Repositorio → BD

 Aqui van las reglas del negocio: validaciones, comprobaciones, transformaciones.
 El repositorio solo sabe guardar y buscar; el servicio decide SI se guarda y como.
*/
@Service
public class UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario registrar(Usuario usuario) {
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            log.warn("Intento de registro con email ya existente: {}", usuario.getEmail());
            throw new ConflictoException("El email ya esta registrado");
        }
        log.info("Registrando usuario con email: {}", usuario.getEmail());
        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> buscarPorId(Long id) {
        log.debug("Buscando usuario con id: {}", id);
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        log.debug("Buscando usuario con email: {}", email);
        return usuarioRepository.findByEmail(email);
    }

    public Usuario actualizar(Long id, Usuario datosNuevos) {
        Optional<Usuario> resultado = usuarioRepository.findById(id);
        if (resultado.isEmpty()) {
            log.warn("Intento de actualizar usuario inexistente con id: {}", id);
            throw new RecursoNoEncontradoException("Usuario no encontrado");
        }
        Usuario usuario = resultado.get();

        usuario.setPeso(datosNuevos.getPeso());
        usuario.setAltura(datosNuevos.getAltura());
        usuario.setFechaNacimiento(datosNuevos.getFechaNacimiento());

        log.info("Usuario {} actualizado", id);
        return usuarioRepository.save(usuario);
    }

    public void eliminar(Long id) {
        log.info("Eliminando usuario con id: {}", id);
        usuarioRepository.deleteById(id);
    }
}
