package com.pos.domain.model;

import java.time.Instant;
import java.util.List;

/**
 * Value Object para el historial de ventas (SPEC-BE-006).
 * Incluye detalles de ítems para reconstruir facturas en el frontend.
 */
public record ResumenVentaSimple(
        String ventaId,
        Instant fechaHora,
        Dinero total,
        Dinero montoDevuelto,
        int cantidadItems,
        EstadoVenta estado,
        Dinero subtotal,
        Dinero iva,
        Dinero montoPagado,
        Dinero cambio,
        String usuarioCajero,
        String metodoPago,
        List<ItemVentaDTO> items
) {
    public record ItemVentaDTO(
            Long productoId,
            String nombre,
            int cantidad,
            long precioUnitario,
            long subtotal
    ) {}
}
