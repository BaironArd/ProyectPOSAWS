package com.pos.infrastructure.adapter.in.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record ConfirmarVentaRequest(
        @NotEmpty(message = "El carrito no puede estar vacío")
        List<@Valid ItemVentaRequest> items,

        @Positive(message = "El monto pagado debe ser positivo")
        long montoPagado,

        @NotBlank(message = "La clave de idempotencia es requerida")
        String idempotencyKey,

        String usuarioCajero,
        String metodoPago,
        List<PagoItemRequest> pagos
) {
    public record ItemVentaRequest(
            @jakarta.validation.constraints.NotNull Long productoId,
            @Positive(message = "La cantidad debe ser positiva") int cantidad
    ) {}

    public record PagoItemRequest(String metodo, long monto, String referencia) {}
}
