package com.pos.domain.port.in;

import com.pos.domain.model.Producto;
import java.util.List;

public interface GestionarProductoUseCase {
    List<Producto> listarTodos();
    Producto crear(NuevoProductoCommand command);
    Producto actualizar(Long id, ActualizarProductoCommand command);
    Producto toggleActivo(Long id);
}
