package com.pos.infrastructure.adapter.in.web.dto;

import java.time.Instant;

public record DevolucionResponse(
        String ventaId,
        long montoDevuelto,
        String estado,
        Instant fechaDevolucion
) {}
