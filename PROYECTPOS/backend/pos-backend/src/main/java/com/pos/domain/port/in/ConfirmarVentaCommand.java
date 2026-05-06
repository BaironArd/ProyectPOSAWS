package com.pos.domain.port.in;

import java.util.List;

public record ConfirmarVentaCommand(
        List<ItemCommand> items,
        long montoPagado,
        String idempotencyKey,
        String usuarioCajero,
        String metodoPago,
        List<PagoItemCommand> pagos
) {
    public record ItemCommand(Long productoId, int cantidad) {}
    public record PagoItemCommand(String metodo, long monto, String referencia) {}
}
