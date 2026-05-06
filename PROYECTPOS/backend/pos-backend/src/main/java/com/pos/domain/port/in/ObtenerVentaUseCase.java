package com.pos.domain.port.in;

import com.pos.domain.model.Venta;

public interface ObtenerVentaUseCase {
    Venta obtener(String ventaId);
}
