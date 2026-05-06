package com.pos.domain.port.in;

import com.pos.domain.model.Producto;

public interface ObtenerProductoUseCase {
    Producto obtener(Long id);
}
