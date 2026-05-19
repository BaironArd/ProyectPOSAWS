package com.pos.infrastructure.adapter.out.persistence;

import com.pos.domain.model.*;

import java.util.ArrayList;
import java.util.List;

public class VentaEntityMapper {

    public Venta toDomain(VentaEntity entity) {
        List<ItemVenta> items = new ArrayList<>(entity.getItems().stream()
                .map(i -> {
                    ItemVenta item = new ItemVenta();
                    item.setProductoId(i.getProductoId());
                    item.setNombre(i.getNombre());
                    item.setCantidad(i.getCantidad());
                    item.setPrecioUnitario(Dinero.dePesos(i.getPrecioUnitario()));
                    item.setSubtotal(Dinero.dePesos(i.getSubtotal()));
                    return item;
                }).toList());

        ResumenVenta resumen = new ResumenVenta(
                Dinero.dePesos(entity.getSubtotal()),
                Dinero.dePesos(entity.getIva()),
                Dinero.dePesos(entity.getTotal()),
                Dinero.dePesos(entity.getMontoPagado()),
                Dinero.dePesos(entity.getCambio())
        );

        EstadoVenta estado = EstadoVenta.valueOf(entity.getEstado().name());

        Venta v = new Venta(
            entity.getVentaId(),
            items,
            resumen,
            estado,
            entity.getFechaHora(),
            entity.getIdempotencyKey(),
            entity.getUsuarioCajero(),
            List.of()
        );
        v.setMontoDevuelto(Dinero.dePesos(entity.getMontoDevuelto()));
        return v;
    }

    public VentaEntity toEntity(Venta domain) {
        VentaEntity entity = new VentaEntity();
        entity.setVentaId(domain.getVentaId());
        entity.setSubtotal(domain.getResumen().subtotal().toPesos());
        entity.setIva(domain.getResumen().iva().toPesos());
        entity.setTotal(domain.getResumen().total().toPesos());
        entity.setMontoPagado(domain.getResumen().montoPagado().toPesos());
        entity.setCambio(domain.getResumen().cambio().toPesos());
        entity.setEstado(VentaEntity.EstadoVentaEnum.valueOf(domain.getEstado().name()));
        entity.setFechaHora(domain.getFechaHora());
        entity.setIdempotencyKey(domain.getIdempotencyKey());
        entity.setUsuarioCajero(domain.getUsuarioCajero());

        entity.setMontoDevuelto(domain.getMontoDevuelto() != null ? domain.getMontoDevuelto().toPesos() : 0L);

        if (domain.getPagos() != null && !domain.getPagos().isEmpty()) {
            entity.setMetodoPago(domain.getPagos().get(0).metodo().name());
        }

        List<ItemVentaEntity> itemEntities = new ArrayList<>();
        for (ItemVenta item : domain.getItems()) {
            ItemVentaEntity ie = new ItemVentaEntity();
            ie.setVenta(entity);
            ie.setProductoId(item.getProductoId());
            ie.setNombre(item.getNombre());
            ie.setCantidad(item.getCantidad());
            ie.setPrecioUnitario(item.getPrecioUnitario().toPesos());
            ie.setSubtotal(item.getSubtotal().toPesos());
            itemEntities.add(ie);
        }
        entity.setItems(itemEntities);

        return entity;
    }

    public ResumenVentaSimple toResumenSimple(VentaEntity entity) {
        List<ResumenVentaSimple.ItemVentaDTO> items = entity.getItems().stream()
                .map(i -> new ResumenVentaSimple.ItemVentaDTO(
                        i.getProductoId(),
                        i.getNombre(),
                        i.getCantidad(),
                        i.getPrecioUnitario(),
                        i.getSubtotal()
                ))
                .toList();
        
        return new ResumenVentaSimple(
                entity.getVentaId(),
                entity.getFechaHora(),
                Dinero.dePesos(entity.getTotal()),
                Dinero.dePesos(entity.getMontoDevuelto()),
                entity.getItems().size(),
                EstadoVenta.valueOf(entity.getEstado().name()),
                Dinero.dePesos(entity.getSubtotal()),
                Dinero.dePesos(entity.getIva()),
                Dinero.dePesos(entity.getMontoPagado()),
                Dinero.dePesos(entity.getCambio()),
                entity.getUsuarioCajero(),
                entity.getMetodoPago(),
                items
        );
    }
}
