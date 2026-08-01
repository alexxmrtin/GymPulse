package com.gympulse.backend.services;

import com.gympulse.backend.models.Usuario;
import com.gympulse.backend.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;

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

    private final UsuarioRepository usuarioRepository;

    /*
     Inyeccion de dependencias por constructor: Spring detecta que este servicio
     necesita un UsuarioRepository y se lo pasa solo al crearlo.
     Es la forma recomendada frente a @Autowired en el campo porque hace
     las dependencias explicitas y facilita los tests.
    */
    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /*
     Registrar un usuario nuevo.
     Antes de guardar comprueba que el email no este ya en uso:
     esa es la logica de negocio que no puede vivir en el repositorio.
    */
    public Usuario registrar(Usuario usuario) {
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new IllegalArgumentException("El email ya esta registrado");
        }
        return usuarioRepository.save(usuario);
    }

    /*
     Buscar por id. Devuelve Optional porque el usuario puede no existir.
     El controlador decide que respuesta HTTP mandar segun este resultado.
    */
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    /*
     Actualizar los datos de un usuario existente.
     Solo actualiza los campos editables: no se puede cambiar el email desde aqui.
    */
    public Usuario actualizar(Long id, Usuario datosNuevos) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        usuario.setPeso(datosNuevos.getPeso());
        usuario.setAltura(datosNuevos.getAltura());
        usuario.setFechaNacimiento(datosNuevos.getFechaNacimiento());

        return usuarioRepository.save(usuario);
    }

    public void eliminar(Long id) {
        usuarioRepository.deleteById(id);
    }
}
