package com.gympulse.backend.services;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.text.Normalizer;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.gympulse.backend.core.ManejoErrores.exceptions.AccesoProhibidoException;
import com.gympulse.backend.core.ManejoErrores.exceptions.ConflictoException;
import com.gympulse.backend.core.ManejoErrores.exceptions.CredencialesInvalidasException;
import com.gympulse.backend.core.ManejoErrores.exceptions.RecursoNoEncontradoException;
import com.gympulse.backend.dtos.ActualizarUsuarioDTO;
import com.gympulse.backend.dtos.FotoDTO;
import com.gympulse.backend.dtos.LoginDTO;
import com.gympulse.backend.dtos.LoginGoogleDTO;
import com.gympulse.backend.dtos.RefrescoDTO;
import com.gympulse.backend.dtos.RegistroUsuarioDTO;
import com.gympulse.backend.dtos.SesionDTO;
import com.gympulse.backend.dtos.UsuarioDTO;
import com.gympulse.backend.helpers.Mapeador;
import com.gympulse.backend.helpers.Privacidad;
import com.gympulse.backend.helpers.TipoImagen;
import com.gympulse.backend.models.FotoPerfil;
import com.gympulse.backend.models.Usuario;
import com.gympulse.backend.repositories.EjercicioRepository;
import com.gympulse.backend.repositories.EntrenamientoRepository;
import com.gympulse.backend.repositories.FotoPerfilRepository;
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
    private final FotoPerfilRepository fotoPerfilRepository;
    private final EntrenamientoRepository entrenamientoRepository;
    private final EjercicioRepository ejercicioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final TokenRefrescoService tokenRefrescoService;
    private final LimiteIntentosService limiteIntentos;
    private final VerificacionEmailService verificacionEmail;
    private final VerificadorTokenGoogle verificadorGoogle;
    private final String hashFicticio;

    public UsuarioService(UsuarioRepository usuarioRepository, FotoPerfilRepository fotoPerfilRepository,
            EntrenamientoRepository entrenamientoRepository, EjercicioRepository ejercicioRepository,
            PasswordEncoder passwordEncoder, TokenService tokenService, TokenRefrescoService tokenRefrescoService,
            LimiteIntentosService limiteIntentos, VerificacionEmailService verificacionEmail,
            VerificadorTokenGoogle verificadorGoogle) {
        this.usuarioRepository = usuarioRepository;
        this.fotoPerfilRepository = fotoPerfilRepository;
        this.entrenamientoRepository = entrenamientoRepository;
        this.ejercicioRepository = ejercicioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.tokenRefrescoService = tokenRefrescoService;
        this.limiteIntentos = limiteIntentos;
        this.verificacionEmail = verificacionEmail;
        this.verificadorGoogle = verificadorGoogle;
        this.hashFicticio = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    @Transactional
    public void registrar(RegistroUsuarioDTO datos) {

        if (usuarioRepository.existsByEmail(datos.email())) {
            passwordEncoder.encode(datos.password());
            log.warn("Intento de registro con email ya existente: {}", Privacidad.enmascararEmail(datos.email()));
            verificacionEmail.avisarRegistroDuplicado(datos.email());
            return;
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

        log.info("Registrando usuario con email: {}", Privacidad.enmascararEmail(datos.email()));
        verificacionEmail.iniciar(usuario);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public SesionDTO autenticar(LoginDTO credenciales, String ip) {
        limiteIntentos.comprobar(credenciales.email(), ip);

        Usuario usuario = usuarioRepository.findByEmail(credenciales.email()).orElse(null);
        if (usuario == null) {
            passwordEncoder.matches(credenciales.password(), hashFicticio);
            log.warn("Login fallido: email no registrado {}", Privacidad.enmascararEmail(credenciales.email()));
            limiteIntentos.registrarFallo(credenciales.email(), ip);
            throw new CredencialesInvalidasException("Email o contrasena incorrectos");
        }

        if (!passwordEncoder.matches(credenciales.password(), usuario.getPassword())) {
            log.warn("Login fallido: contrasena incorrecta para {}", Privacidad.enmascararEmail(credenciales.email()));
            limiteIntentos.registrarFallo(credenciales.email(), ip);
            throw new CredencialesInvalidasException("Email o contrasena incorrectos");
        }

        limiteIntentos.registrarExito(credenciales.email());

        if (usuario.getEmailVerificadoEn() == null) {
            log.info("Login de un usuario sin verificar: {}", usuario.getId());
            throw new AccesoProhibidoException(
                    "Verifica tu email antes de iniciar sesion. Revisa tu correo o pide que te lo reenviemos");
        }

        log.info("Login correcto del usuario {}", usuario.getId());
        return crearSesion(usuario, tokenRefrescoService.emitir(usuario));
    }

    @Transactional
    public SesionDTO loginConGoogle(LoginGoogleDTO datos) {
        VerificadorTokenGoogle.IdentidadGoogle google = verificadorGoogle.verificar(datos.idToken());

        Usuario usuario;
        Optional<Usuario> yaVinculado = usuarioRepository.findByGoogleId(google.id());
        Optional<Usuario> mismoEmail = yaVinculado.isPresent() ? Optional.empty() : usuarioRepository.findByEmail(google.email());

        if (yaVinculado.isPresent()) {
            usuario = yaVinculado.get();
        } else if (mismoEmail.isPresent()) {
            usuario = vincularGoogle(mismoEmail.get(), google);
        } else {
            usuario = crearDesdeGoogle(google);
        }

        log.info("Login con Google del usuario {}", usuario.getId());
        return crearSesion(usuario, tokenRefrescoService.emitir(usuario));
    }

    private Usuario vincularGoogle(Usuario usuario, VerificadorTokenGoogle.IdentidadGoogle google) {
        if (usuario.getEmailVerificadoEn() == null) {
            log.warn("Se vincula Google a la cuenta sin verificar {}: se anula su contrasena y sus sesiones", usuario.getId());
            usuario.setPassword(contrasenaInutilizable());
            usuario.setEmailVerificadoEn(Instant.now());
            usuario.setVerificacionHash(null);
            usuario.setVerificacionExpiraEn(null);
            tokenRefrescoService.revocarTodosDe(usuario.getId());
        }
        usuario.setGoogleId(google.id());
        log.info("Cuenta {} vinculada con Google", usuario.getId());
        return usuario;
    }

    private Usuario crearDesdeGoogle(VerificadorTokenGoogle.IdentidadGoogle google) {
        Usuario usuario = new Usuario();
        usuario.setUsername(usernameLibre(google));
        usuario.setEmail(google.email());
        usuario.setPassword(contrasenaInutilizable());
        usuario.setGoogleId(google.id());
        usuario.setEmailVerificadoEn(Instant.now());
        log.info("Creando usuario desde Google con email: {}", Privacidad.enmascararEmail(google.email()));
        return usuarioRepository.save(usuario);
    }

    private String usernameLibre(VerificadorTokenGoogle.IdentidadGoogle google) {
        String origen = google.nombre() != null && !google.nombre().isBlank()
                ? google.nombre()
                : google.email().substring(0, google.email().indexOf('@'));
        String base = Normalizer.normalize(origen, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "");
        if (base.length() < 3) {
            base = "usuario";
        }
        base = base.substring(0, Math.min(base.length(), 24));

        String candidato = base;
        for (int sufijo = 2; usuarioRepository.existsByUsername(candidato); sufijo++) {
            candidato = base + sufijo;
        }
        return candidato;
    }

    private String contrasenaInutilizable() {
        return passwordEncoder.encode(UUID.randomUUID().toString());
    }

    @Transactional(noRollbackFor = CredencialesInvalidasException.class)
    public SesionDTO refrescar(RefrescoDTO datos) {
        TokenRefrescoService.Rotacion rotacion = tokenRefrescoService.rotar(datos.tokenRefresco());
        return crearSesion(rotacion.usuario(), rotacion.nuevoToken());
    }

    public void cerrarSesion(RefrescoDTO datos) {
        tokenRefrescoService.revocar(datos.tokenRefresco());
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

    @Transactional
    public void eliminar(Long id, Long usuarioAutenticadoId) {
        exigirQueSeaElMismo(id, usuarioAutenticadoId);
        if (!usuarioRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Usuario no encontrado");
        }
        log.info("Eliminando usuario con id: {}", id);
        tokenRefrescoService.borrarTodosDe(id);
        fotoPerfilRepository.deleteById(id);
        entrenamientoRepository.deleteAll(entrenamientoRepository.findByUsuarioId(id));
        ejercicioRepository.borrarTodosDe(id);
        usuarioRepository.deleteById(id);
    }

    @Transactional
    public UsuarioDTO guardarFoto(Long id, MultipartFile archivo, Long usuarioAutenticadoId) {
        exigirQueSeaElMismo(id, usuarioAutenticadoId);
        Usuario usuario = obtener(id);

        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("No se ha enviado ninguna imagen");
        }
        if (archivo.getSize() > FotoPerfil.TAMANO_MAXIMO) {
            throw new IllegalArgumentException("La imagen no puede superar los 5 MB");
        }

        byte[] datos = leer(archivo);
        String tipo = TipoImagen.detectar(datos)
                .orElseThrow(() -> new IllegalArgumentException("Formato no admitido: usa JPEG, PNG o WebP"));

        FotoPerfil foto = fotoPerfilRepository.findById(id).orElseGet(FotoPerfil::new);
        foto.setUsuario(usuario);
        foto.setDatos(datos);
        foto.setTipo(tipo);
        fotoPerfilRepository.save(foto);

        usuario.setFotoActualizadaEn(Instant.now());
        log.info("Usuario {} ha actualizado su foto de perfil ({}, {} bytes)", id, tipo, datos.length);
        return Mapeador.aDTO(usuarioRepository.save(usuario));
    }

    @Transactional(readOnly = true)
    public FotoDTO obtenerFoto(Long id, Long usuarioAutenticadoId) {
        exigirQueSeaElMismo(id, usuarioAutenticadoId);
        FotoPerfil foto = fotoPerfilRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El usuario no tiene foto de perfil"));
        return new FotoDTO(foto.getDatos(), foto.getTipo());
    }

    @Transactional
    public void eliminarFoto(Long id, Long usuarioAutenticadoId) {
        exigirQueSeaElMismo(id, usuarioAutenticadoId);
        Usuario usuario = obtener(id);

        fotoPerfilRepository.deleteById(id);
        usuario.setFotoActualizadaEn(null);
        usuarioRepository.save(usuario);
        log.info("Usuario {} ha quitado su foto de perfil", id);
    }

    private SesionDTO crearSesion(Usuario usuario, String tokenRefresco) {
        return new SesionDTO(
                tokenService.generar(usuario),
                "Bearer",
                tokenService.duracionEnSegundos(),
                tokenRefresco,
                tokenRefrescoService.duracionEnSegundos(),
                Mapeador.aDTO(usuario));
    }

    private byte[] leer(MultipartFile archivo) {
        try {
            return archivo.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer la imagen subida", e);
        }
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
