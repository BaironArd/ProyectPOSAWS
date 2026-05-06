package com.pos.infrastructure.adapter.in.web.dto;

import java.time.Instant;
import java.util.List;

public record VentaResponse(
        String ventaId,
        List<ItemVentaResponse> items,
        ResumenResponse resumen,
        String estado,
        Instant fechaHora
) {
    public record ItemVentaResponse(
            Long productoId,
            String nombre,
            int cantidad,
            long precioUnitario,
            long subtotal
    ) {}

    public record ResumenResponse(
            long subtotal,
            long iva,
            long total,
            long montoPagado,
            long cambio
    ) {}
}
