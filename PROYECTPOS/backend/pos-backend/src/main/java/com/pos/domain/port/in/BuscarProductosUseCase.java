package com.pos.domain.port.in;

import com.pos.domain.model.Producto;
import java.util.List;

public interface BuscarProductosUseCase {
    List<Producto> buscar(String query);
}
