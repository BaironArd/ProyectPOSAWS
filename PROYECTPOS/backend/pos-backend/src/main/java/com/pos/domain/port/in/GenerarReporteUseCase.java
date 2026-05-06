package com.pos.domain.port.in;

import com.pos.domain.model.ReporteCierre;

public interface GenerarReporteUseCase {
    ReporteCierre generar(String fechaDesde, String fechaHasta);
}
