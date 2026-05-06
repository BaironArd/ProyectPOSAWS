package com.pos.domain.model;

public record PagoItem(
        MetodoPago metodo,
        Dinero monto,
        String referencia
) {}
