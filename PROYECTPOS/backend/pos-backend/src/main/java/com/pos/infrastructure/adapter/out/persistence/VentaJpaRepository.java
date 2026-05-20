package com.pos.infrastructure.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface VentaJpaRepository extends JpaRepository<VentaEntity, String> {

    Optional<VentaEntity> findByIdempotencyKey(String idempotencyKey);

    Page<VentaEntity> findAllByOrderByFechaHoraDesc(Pageable pageable);
    List<VentaEntity> findAllByOrderByFechaHoraDesc();

    Page<VentaEntity> findByFechaHoraBetweenOrderByFechaHoraDesc(Instant desde, Instant hasta, Pageable pageable);
    List<VentaEntity> findByFechaHoraBetweenOrderByFechaHoraDesc(Instant desde, Instant hasta);

    // Queries compatibles con H2 y PostgreSQL usando Instant range

    /** Total de ventas en el rango (completadas + devueltas) */
    @Query("SELECT COUNT(v) FROM VentaEntity v WHERE v.estado IN ('COMPLETADA', 'DEVUELTA', 'PARCIALMENTE_DEVUELTA') " +
           "AND v.fechaHora >= :desde AND v.fechaHora < :hasta")
    int countVentasEnRango(@Param("desde") Instant desde, @Param("hasta") Instant hasta);

    /** Suma del monto bruto de todas las ventas (completadas + devueltas + parciales) */
    @Query("SELECT SUM(v.total) FROM VentaEntity v WHERE v.estado IN ('COMPLETADA', 'DEVUELTA', 'PARCIALMENTE_DEVUELTA') " +
           "AND v.fechaHora >= :desde AND v.fechaHora < :hasta")
    Long sumTotalEnRango(@Param("desde") Instant desde, @Param("hasta") Instant hasta);

    /** Cantidad de ventas con devoluciones (completas o parciales) en el rango */
    @Query("SELECT COUNT(v) FROM VentaEntity v WHERE v.montoDevuelto > 0 " +
           "AND v.fechaHora >= :desde AND v.fechaHora < :hasta")
    int countDevueltasEnRango(@Param("desde") Instant desde, @Param("hasta") Instant hasta);

    /** Suma del monto devuelto acumulado (montoDevuelto en cada venta, incluyendo parciales) */
    @Query("SELECT SUM(v.montoDevuelto) FROM VentaEntity v WHERE v.montoDevuelto > 0 " +
           "AND v.fechaHora >= :desde AND v.fechaHora < :hasta")
    Long sumDevueltasEnRango(@Param("desde") Instant desde, @Param("hasta") Instant hasta);
}
