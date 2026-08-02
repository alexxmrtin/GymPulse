package com.gympulse.backend.controllers;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gympulse.backend.models.Usuario;
import com.gympulse.backend.services.UsuarioService;

/*
POST /usuario               -> registrar un usuario nuevo
GET  /usuario/{id}          -> obtener un usuario por id
PUT  /usuario/{id}          -> actualizar datos del usuario
DELETE /usuario/{id}        -> eliminar un usuario
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
    public ResponseEntity<Usuario> registrar(@RequestBody Usuario usuario) {
        Usuario nuevo = usuarioService.registrar(usuario);
        return ResponseEntity.ok(nuevo);
    }

    /*
     GET /usuarios/{id}
     @PathVariable extrae el {id} de la URL y lo pasa como parametro.
     Si el usuario existe devuelve 200 con el objeto; si no, devuelve 404.
    */
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscarPorId(@PathVariable Long id) {
        Optional<Usuario> usuario = usuarioService.buscarPorId(id);

        if (usuario.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(usuario.get());
    }

    /*
     PUT /usuarios/{id}
     Actualiza los datos del usuario. Devuelve 404 si no existe.
    */
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizar(@PathVariable Long id, @RequestBody Usuario datosNuevos) {
        Optional<Usuario> existente = usuarioService.buscarPorId(id);

        if (existente.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Usuario actualizado = usuarioService.actualizar(id, datosNuevos);
        return ResponseEntity.ok(actualizado);
    }

    /*
     DELETE /usuarios/{id}
     Elimina el usuario. Devuelve 204 (exito sin contenido).
    */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
