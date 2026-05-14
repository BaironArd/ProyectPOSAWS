package com.pos.infrastructure.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface VentaJpaRepository extends JpaRepository<VentaEntity, String> {

    Optional<VentaEntity> findByIdempotencyKey(String idempotencyKey);

    Page<VentaEntity> findAllByOrderByFechaHoraDesc(Pageable pageable);

    Page<VentaEntity> findByFechaHoraBetweenOrderByFechaHoraDesc(Instant desde, Instant hasta, Pageable pageable);

    // Queries compatibles con H2 y PostgreSQL usando Instant range

    /** Total de ventas en el rango (completadas + devueltas) */
    @Query("SELECT COUNT(v) FROM VentaEntity v WHERE v.estado IN ('COMPLETADA', 'DEVUELTA') " +
           "AND v.fechaHora >= :desde AND v.fechaHora < :hasta")
    int countVentasEnRango(@Param("desde") Instant desde, @Param("hasta") Instant hasta);

    /** Suma del monto bruto de todas las ventas (completadas + devueltas) */
    @Query("SELECT SUM(v.total) FROM VentaEntity v WHERE v.estado IN ('COMPLETADA', 'DEVUELTA') " +
           "AND v.fechaHora >= :desde AND v.fechaHora < :hasta")
    Long sumTotalEnRango(@Param("desde") Instant desde, @Param("hasta") Instant hasta);

    /** Cantidad de ventas devueltas en el rango */
    @Query("SELECT COUNT(v) FROM VentaEntity v WHERE v.estado = 'DEVUELTA' " +
           "AND v.fechaHora >= :desde AND v.fechaHora < :hasta")
    int countDevueltasEnRango(@Param("desde") Instant desde, @Param("hasta") Instant hasta);

    /** Suma del monto de las ventas devueltas (monto a reembolsar = total de esas ventas) */
    @Query("SELECT SUM(v.total) FROM VentaEntity v WHERE v.estado = 'DEVUELTA' " +
           "AND v.fechaHora >= :desde AND v.fechaHora < :hasta")
    Long sumDevueltasEnRango(@Param("desde") Instant desde, @Param("hasta") Instant hasta);
}
