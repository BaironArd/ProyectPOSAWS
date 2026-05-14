package com.pos.domain.port.in;

import com.pos.domain.model.Devolucion;
import java.util.List;

public interface DevolverVentaUseCase {
    /** Devolución total de la venta */
    Devolucion devolver(String ventaId);

    /** Devolución parcial — solo los ítems indicados con sus cantidades */
    Devolucion devolverParcial(String ventaId, List<ItemDevolucionCommand> items);

    record ItemDevolucionCommand(Long productoId, int cantidad) {}
}
