package com.pos.infrastructure.adapter.out.persistence;

import com.pos.domain.model.*;
import com.pos.domain.port.out.VentaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class VentaJpaAdapter implements VentaRepository {

    private final VentaJpaRepository jpaRepository;
    private final VentaEntityMapper mapper;

    public VentaJpaAdapter(VentaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
        this.mapper = new VentaEntityMapper();
    }

    @Override
    public Venta save(Venta venta) {
        if (venta.getVentaId() != null) {
            return jpaRepository.findById(venta.getVentaId())
                    .map(existente -> {
                        actualizarEntidadExistente(existente, venta);
                        VentaEntity saved = jpaRepository.save(existente);
                        return mapper.toDomain(saved);
                    })
                    .orElseGet(() -> {
                        VentaEntity saved = jpaRepository.save(mapper.toEntity(venta));
                        return mapper.toDomain(saved);
                    });
        }
        VentaEntity saved = jpaRepository.save(mapper.toEntity(venta));
        return mapper.toDomain(saved);
    }

    private void actualizarEntidadExistente(VentaEntity existente, Venta venta) {
        existente.setSubtotal(venta.getResumen().subtotal().toPesos());
        existente.setIva(venta.getResumen().iva().toPesos());
        existente.setTotal(venta.getResumen().total().toPesos());
        existente.setMontoDevuelto(venta.getMontoDevuelto() != null ? venta.getMontoDevuelto().toPesos() : 0L);
        existente.setMontoPagado(venta.getResumen().montoPagado().toPesos());
        existente.setCambio(venta.getResumen().cambio().toPesos());
        existente.setEstado(VentaEntity.EstadoVentaEnum.valueOf(venta.getEstado().name()));
        existente.setFechaHora(venta.getFechaHora());
        existente.setIdempotencyKey(venta.getIdempotencyKey());
        existente.setUsuarioCajero(venta.getUsuarioCajero());
        existente.setMetodoPago(venta.getPagos() != null && !venta.getPagos().isEmpty()
                ? venta.getPagos().get(0).metodo().name()
                : null);

        List<ItemVentaEntity> itemEntities = venta.getItems().stream().map(item -> {
            ItemVentaEntity entity = new ItemVentaEntity();
            entity.setVenta(existente);
            entity.setProductoId(item.getProductoId());
            entity.setNombre(item.getNombre());
            entity.setCantidad(item.getCantidad());
            entity.setPrecioUnitario(item.getPrecioUnitario().toPesos());
            entity.setSubtotal(item.getSubtotal().toPesos());
            return entity;
        }).toList();

        existente.getItems().clear();
        existente.getItems().addAll(itemEntities);
    }

    @Override
    public Optional<Venta> findById(String ventaId) {
        return jpaRepository.findById(ventaId).map(mapper::toDomain);
    }

    @Override
    public Optional<Venta> findByIdempotencyKey(String key) {
        return jpaRepository.findByIdempotencyKey(key).map(mapper::toDomain);
    }

    @Override
    public PageResponse<ResumenVentaSimple> findAll(int page, int size) {
        if (size <= 0) {
            List<VentaEntity> allResults = jpaRepository.findAllByOrderByFechaHoraDesc();
            List<ResumenVentaSimple> items = allResults.stream().map(mapper::toResumenSimple).toList();
            return PageResponse.of(items, items.size(), 0, 0);
        }

        Page<VentaEntity> pageResult = jpaRepository.findAllByOrderByFechaHoraDesc(
                PageRequest.of(page, size));
        List<ResumenVentaSimple> items = pageResult.getContent()
                .stream().map(mapper::toResumenSimple).toList();
        return PageResponse.of(items, pageResult.getTotalElements(), page, size);
    }

    @Override
    public PageResponse<ResumenVentaSimple> findAll(int page, int size, String fechaDesde, String fechaHasta) {
        if (fechaDesde == null || fechaDesde.isBlank() || fechaHasta == null || fechaHasta.isBlank()) {
            return findAll(page, size);
        }

        java.time.Instant desde = java.time.LocalDate.parse(fechaDesde)
                .atStartOfDay(java.time.ZoneOffset.UTC).toInstant();
        java.time.Instant hasta = java.time.LocalDate.parse(fechaHasta)
                .plusDays(1).atStartOfDay(java.time.ZoneOffset.UTC).toInstant();

        if (size <= 0) {
            List<VentaEntity> allResults = jpaRepository.findByFechaHoraBetweenOrderByFechaHoraDesc(desde, hasta);
            List<ResumenVentaSimple> items = allResults.stream().map(mapper::toResumenSimple).toList();
            return PageResponse.of(items, items.size(), 0, 0);
        }

        Page<VentaEntity> pageResult = jpaRepository.findByFechaHoraBetweenOrderByFechaHoraDesc(
                desde, hasta, PageRequest.of(page, size));

        List<ResumenVentaSimple> items = pageResult.getContent()
                .stream().map(mapper::toResumenSimple).toList();
        return PageResponse.of(items, pageResult.getTotalElements(), page, size);
    }

    @Override
    public ReporteCierre generarReporte(String fechaDesde, String fechaHasta) {
        // Convertir fechas String (yyyy-MM-dd) a Instant para queries compatibles con H2/PostgreSQL
        java.time.Instant desde = java.time.LocalDate.parse(fechaDesde)
                .atStartOfDay(java.time.ZoneOffset.UTC).toInstant();
        java.time.Instant hasta = java.time.LocalDate.parse(fechaHasta)
                .plusDays(1).atStartOfDay(java.time.ZoneOffset.UTC).toInstant();

        int totalVentas = jpaRepository.countVentasEnRango(desde, hasta);
        Long montoTotalRaw = jpaRepository.sumTotalEnRango(desde, hasta);
        int totalDevueltas = jpaRepository.countDevueltasEnRango(desde, hasta);
        Long montoDevueltoRaw = jpaRepository.sumDevueltasEnRango(desde, hasta);

        Dinero montoTotal = Dinero.dePesos(montoTotalRaw != null ? montoTotalRaw : 0);
        Dinero montoDevuelto = Dinero.dePesos(montoDevueltoRaw != null ? montoDevueltoRaw : 0);
        Dinero montoNeto = montoTotal.menos(montoDevuelto);

        return new ReporteCierre(
                fechaDesde,
                fechaHasta,
                totalVentas,
                totalDevueltas,
                montoTotal,
                montoDevuelto,
                montoNeto,
                List.of()
        );
    }
}
