package com.pos.domain.model;

/**
 * Value Object inmutable con el resumen financiero de una venta.
 * Calculado por CalculadoraVenta — nunca modificado directamente.
 */
public record ResumenVenta(
        Dinero subtotal,
        Dinero iva,
        Dinero total,
        Dinero montoPagado,
        Dinero cambio
) {}
