package com.gympulse.backend.controllers;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.gympulse.backend.dtos.ActualizarUsuarioDTO;
import com.gympulse.backend.dtos.FotoDTO;
import com.gympulse.backend.dtos.LoginDTO;
import com.gympulse.backend.dtos.RegistroUsuarioDTO;
import com.gympulse.backend.dtos.SesionDTO;
import com.gympulse.backend.dtos.UsuarioDTO;
import com.gympulse.backend.helpers.UsuarioAutenticado;
import com.gympulse.backend.services.UsuarioService;

/*
POST /usuario               -> registrar un usuario nuevo
POST /usuario/login         -> comprobar email y contrasena y devolver el token JWT
GET  /usuario/{id}          -> obtener un usuario por id
PUT  /usuario/{id}          -> actualizar datos del usuario
DELETE /usuario/{id}        -> eliminar un usuario
PUT  /usuario/{id}/foto     -> subir o sustituir la foto de perfil (multipart, campo "foto")
GET  /usuario/{id}/foto     -> descargar la foto de perfil
DELETE /usuario/{id}/foto   -> quitar la foto de perfil
*/

@RestController
@RequestMapping("/usuario")
public class UsuarioController {

    private final UsuarioService usuarioService; //Esto es necesario para inyectar la logica de negocio, en proyectos anteriores esta logica la hacias aqui directamente, en este proyecto hemos agregado una capa de abstraccion mas para facilitar el escalado del proyecto

    //Contructor para el controlador
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /*
     POST /usuarios
     Recibe un JSON con los datos del usuario en el cuerpo de la peticion.
     @RequestBody le dice a Spring que deserialice el JSON al objeto Usuario.
     ResponseEntity permite controlar el codigo HTTP de la respuesta.
    */
    @PostMapping
    public ResponseEntity<UsuarioDTO> registrar(@RequestBody RegistroUsuarioDTO datos) {
        return ResponseEntity.ok(usuarioService.registrar(datos));
    }

    @PostMapping("/login")
    public ResponseEntity<SesionDTO> login(@RequestBody LoginDTO credenciales) {
        return ResponseEntity.ok(usuarioService.autenticar(credenciales));
    }

    /*
     GET /usuarios/{id}
     @PathVariable extrae el {id} de la URL y lo pasa como parametro.
     Si el usuario existe devuelve 200 con el objeto; si no, devuelve 404.
    */
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> buscarPorId(@PathVariable Long id, @AuthenticationPrincipal Jwt token) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id, UsuarioAutenticado.id(token)));
    }

    /*
     PUT /usuarios/{id}
     Actualiza los datos del usuario. Devuelve 404 si no existe.
    */
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTO> actualizar(@PathVariable Long id,
            @RequestBody ActualizarUsuarioDTO datosNuevos,
            @AuthenticationPrincipal Jwt token) {
        return ResponseEntity.ok(usuarioService.actualizar(id, datosNuevos, UsuarioAutenticado.id(token)));
    }

    /*
     DELETE /usuarios/{id}
     Elimina el usuario. Devuelve 204 (exito sin contenido).
    */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, @AuthenticationPrincipal Jwt token) {
        usuarioService.eliminar(id, UsuarioAutenticado.id(token));
        return ResponseEntity.noContent().build();
    }

    @PutMapping(path = "/{id}/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UsuarioDTO> subirFoto(@PathVariable Long id,
            @RequestParam("foto") MultipartFile foto,
            @AuthenticationPrincipal Jwt token) {
        return ResponseEntity.ok(usuarioService.guardarFoto(id, foto, UsuarioAutenticado.id(token)));
    }

    @GetMapping("/{id}/foto")
    public ResponseEntity<byte[]> foto(@PathVariable Long id, @AuthenticationPrincipal Jwt token) {
        FotoDTO foto = usuarioService.obtenerFoto(id, UsuarioAutenticado.id(token));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(foto.tipo()))
                .cacheControl(CacheControl.noCache().cachePrivate())
                .body(foto.datos());
    }

    @DeleteMapping("/{id}/foto")
    public ResponseEntity<Void> eliminarFoto(@PathVariable Long id, @AuthenticationPrincipal Jwt token) {
        usuarioService.eliminarFoto(id, UsuarioAutenticado.id(token));
        return ResponseEntity.noContent().build();
    }
}
