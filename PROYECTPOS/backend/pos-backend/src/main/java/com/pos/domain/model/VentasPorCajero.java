package com.pos.domain.model;

public record VentasPorCajero(
        String usuario,
        int ventas,
        Dinero monto
) {}
