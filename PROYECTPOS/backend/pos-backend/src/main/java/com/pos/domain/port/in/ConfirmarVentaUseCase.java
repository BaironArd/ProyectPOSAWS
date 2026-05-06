package com.pos.domain.port.in;

import com.pos.domain.model.Venta;

public interface ConfirmarVentaUseCase {
    Venta confirmar(ConfirmarVentaCommand command);
}
