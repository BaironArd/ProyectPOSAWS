package com.pos.infrastructure.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface VentaJpaRepository extends JpaRepository<VentaEntity, String> {

    Optional<VentaEntity> findByIdempotencyKey(String idempotencyKey);

    Page<VentaEntity> findAllByOrderByFechaHoraDesc(Pageable pageable);

    @Query("SELECT COUNT(v) FROM VentaEntity v WHERE v.estado = 'COMPLETADA' " +
           "AND CAST(v.fechaHora AS date) BETWEEN :desde AND :hasta")
    int countVentasEnRango(@Param("desde") String desde, @Param("hasta") String hasta);

    @Query("SELECT SUM(v.total) FROM VentaEntity v WHERE v.estado = 'COMPLETADA' " +
           "AND CAST(v.fechaHora AS date) BETWEEN :desde AND :hasta")
    Long sumTotalEnRango(@Param("desde") String desde, @Param("hasta") String hasta);

    @Query("SELECT COUNT(v) FROM VentaEntity v WHERE v.estado = 'DEVUELTA' " +
           "AND CAST(v.fechaHora AS date) BETWEEN :desde AND :hasta")
    int countDevueltasEnRango(@Param("desde") String desde, @Param("hasta") String hasta);

    @Query("SELECT SUM(v.total) FROM VentaEntity v WHERE v.estado = 'DEVUELTA' " +
           "AND CAST(v.fechaHora AS date) BETWEEN :desde AND :hasta")
    Long sumDevueltasEnRango(@Param("desde") String desde, @Param("hasta") String hasta);
}
