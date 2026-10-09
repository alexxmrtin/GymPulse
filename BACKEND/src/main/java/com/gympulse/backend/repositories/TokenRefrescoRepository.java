package com.gympulse.backend.repositories;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gympulse.backend.models.TokenRefresco;

public interface TokenRefrescoRepository extends JpaRepository<TokenRefresco, Long> {

    Optional<TokenRefresco> findByHash(String hash);

    @Modifying
    @Query("update TokenRefresco t set t.revocadoEn = :ahora where t.usuario.id = :usuarioId and t.revocadoEn is null")
    int revocarTodosDe(@Param("usuarioId") Long usuarioId, @Param("ahora") Instant ahora);

    @Modifying
    @Query("delete from TokenRefresco t where t.usuario.id = :usuarioId and t.expiraEn < :ahora")
    int borrarCaducadosDe(@Param("usuarioId") Long usuarioId, @Param("ahora") Instant ahora);

    @Modifying
    @Query("delete from TokenRefresco t where t.usuario.id = :usuarioId")
    int borrarTodosDe(@Param("usuarioId") Long usuarioId);
}
