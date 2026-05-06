package com.pos.domain.model;

import java.time.Instant;

/**
 * Value Object para el historial de ventas (SPEC-BE-006).
 * Contiene solo los campos necesarios para la lista — sin ítems detallados.
 */
public record ResumenVentaSimple(
        String ventaId,
        Instant fechaHora,
        Dinero total,
        int cantidadItems,
        EstadoVenta estado
) {}
