package com.pos.domain.port.in;

import com.pos.domain.model.Devolucion;

public interface DevolverVentaUseCase {
    Devolucion devolver(String ventaId);
}
