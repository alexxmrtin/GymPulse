package com.gympulse.backend.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gympulse.backend.models.Usuario;

/*
 Un repositorio es una interfaz que extiende JpaRepository.No hay que implementar nada, JpaRepository viene con funciones ya elaboradas que hace las queries

 JpaRepository recibe dos tipos entre angulos:
   - Usuario: la entidad que maneja este repositorio
   - Long:    el tipo del @Id de esa entidad

 Con solo esto ya tienes gratis estos metodos:
   save(usuario)          → INSERT o UPDATE segun si ya tiene id
   findById(id)           → SELECT WHERE id = ?
   findAll()              → SELECT * FROM usuarios
   deleteById(id)         → DELETE WHERE id = ?
   existsById(id)         → SELECT COUNT(*) WHERE id = ?
   count()                → SELECT COUNT(*) FROM usuarios
*/
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /*
     Si necesitas una consulta que JpaRepository no trae de serie, la declaras aqui
     siguiendo la convencion de nombres de Spring: findBy + nombre del campo.
     Spring lee el nombre del metodo y genera el SQL automaticamente, sin que escribas nada.

     findByEmail("alex@gmail.com")
       → SELECT * FROM usuarios WHERE email = 'alex@gmail.com'

    */
   
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByUsername(String username);
    boolean existsByEmail(String email);
    Optional<Usuario> findByVerificacionHash(String verificacionHash);
    Optional<Usuario> findByGoogleId(String googleId);
    boolean existsByUsername(String username);

}
