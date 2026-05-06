package com.pos.domain.model;

import java.util.List;

public record ReporteCierre(
        String fechaDesde,
        String fechaHasta,
        int totalVentas,
        int totalDevueltas,
        Dinero montoTotal,
        Dinero montoDevuelto,
        Dinero montoNeto,
        List<VentasPorCajero> ventasPorCajero
) {}
