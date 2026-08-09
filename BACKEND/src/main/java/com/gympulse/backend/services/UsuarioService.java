package com.gympulse.backend.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.gympulse.backend.core.ManejoErrores.exceptions.AccesoProhibidoException;
import com.gympulse.backend.core.ManejoErrores.exceptions.ConflictoException;
import com.gympulse.backend.core.ManejoErrores.exceptions.CredencialesInvalidasException;
import com.gympulse.backend.core.ManejoErrores.exceptions.RecursoNoEncontradoException;
import com.gympulse.backend.dtos.ActualizarUsuarioDTO;
import com.gympulse.backend.dtos.LoginDTO;
import com.gympulse.backend.dtos.RegistroUsuarioDTO;
import com.gympulse.backend.dtos.SesionDTO;
import com.gympulse.backend.dtos.UsuarioDTO;
import com.gympulse.backend.helpers.Mapeador;
import com.gympulse.backend.helpers.Validaciones;
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
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
            TokenService tokenService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    public UsuarioDTO registrar(RegistroUsuarioDTO datos) {
        Validaciones.exigirTexto(datos.username(), "username");
        Validaciones.exigirTexto(datos.email(), "email");
        Validaciones.exigirTexto(datos.password(), "password");

        if (usuarioRepository.existsByEmail(datos.email())) {
            log.warn("Intento de registro con email ya existente: {}", datos.email());
            throw new ConflictoException("El email ya esta registrado");
        }

        if (usuarioRepository.findByUsername(datos.username()).isPresent()) {
            log.warn("Intento de registro con username ya existente: {}", datos.username());
            throw new ConflictoException("El nombre de usuario ya esta en uso");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(datos.username());
        usuario.setEmail(datos.email());
        usuario.setPassword(passwordEncoder.encode(datos.password()));
        usuario.setPeso(datos.peso());
        usuario.setAltura(datos.altura());
        usuario.setFechaNacimiento(datos.fechaNacimiento());

        log.info("Registrando usuario con email: {}", datos.email());
        return Mapeador.aDTO(usuarioRepository.save(usuario));
    }

    public SesionDTO autenticar(LoginDTO credenciales) {
        Validaciones.exigirTexto(credenciales.email(), "email");
        Validaciones.exigirTexto(credenciales.password(), "password");

        Usuario usuario = usuarioRepository.findByEmail(credenciales.email())
                .orElseThrow(() -> {
                    log.warn("Login fallido: email no registrado {}", credenciales.email());
                    return new CredencialesInvalidasException("Email o contrasena incorrectos");
                });

        if (!passwordEncoder.matches(credenciales.password(), usuario.getPassword())) {
            log.warn("Login fallido: contrasena incorrecta para {}", credenciales.email());
            throw new CredencialesInvalidasException("Email o contrasena incorrectos");
        }

        log.info("Login correcto del usuario {}", usuario.getId());
        return new SesionDTO(
                tokenService.generar(usuario),
                "Bearer",
                tokenService.duracionEnSegundos(),
                Mapeador.aDTO(usuario));
    }

    public UsuarioDTO buscarPorId(Long id, Long usuarioAutenticadoId) {
        exigirQueSeaElMismo(id, usuarioAutenticadoId);
        log.debug("Buscando usuario con id: {}", id);
        return Mapeador.aDTO(obtener(id));
    }

    public UsuarioDTO actualizar(Long id, ActualizarUsuarioDTO datosNuevos, Long usuarioAutenticadoId) {
        exigirQueSeaElMismo(id, usuarioAutenticadoId);
        Usuario usuario = obtener(id);

        usuario.setPeso(datosNuevos.peso());
        usuario.setAltura(datosNuevos.altura());
        usuario.setFechaNacimiento(datosNuevos.fechaNacimiento());

        log.info("Usuario {} actualizado", id);
        return Mapeador.aDTO(usuarioRepository.save(usuario));
    }

    public void eliminar(Long id, Long usuarioAutenticadoId) {
        exigirQueSeaElMismo(id, usuarioAutenticadoId);
        if (!usuarioRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Usuario no encontrado");
        }
        log.info("Eliminando usuario con id: {}", id);
        usuarioRepository.deleteById(id);
    }

    private Usuario obtener(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    private void exigirQueSeaElMismo(Long id, Long usuarioAutenticadoId) {
        if (!id.equals(usuarioAutenticadoId)) {
            log.warn("El usuario {} ha intentado operar sobre la cuenta {}", usuarioAutenticadoId, id);
            throw new AccesoProhibidoException("Solo puedes operar sobre tu propia cuenta");
        }
    }
}
