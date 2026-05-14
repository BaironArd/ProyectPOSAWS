package com.pos.domain.port.in;

import com.pos.domain.model.PageResponse;
import com.pos.domain.model.ResumenVentaSimple;

public interface ListarVentasUseCase {
    PageResponse<ResumenVentaSimple> listar(int page, int size, String fechaDesde, String fechaHasta);
}
