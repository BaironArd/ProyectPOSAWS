package com.pos.domain.port.out;

import com.pos.domain.model.PageResponse;
import com.pos.domain.model.ReporteCierre;
import com.pos.domain.model.ResumenVentaSimple;
import com.pos.domain.model.Venta;
import java.util.Optional;

public interface VentaRepository {
    Venta save(Venta venta);
    Optional<Venta> findById(String ventaId);
    Optional<Venta> findByIdempotencyKey(String key);
    PageResponse<ResumenVentaSimple> findAll(int page, int size);
    ReporteCierre generarReporte(String fechaDesde, String fechaHasta);
}
