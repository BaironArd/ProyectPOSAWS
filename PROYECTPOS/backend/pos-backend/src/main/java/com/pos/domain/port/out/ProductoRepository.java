package com.pos.domain.port.out;

import com.pos.domain.model.Producto;
import java.util.List;
import java.util.Optional;

public interface ProductoRepository {
    List<Producto> buscarPorNombre(String query);
    List<Producto> buscarPorNombrePaginado(String query, int page, int size);
    long contarPorNombre(String query);
    Optional<Producto> findById(Long id);
    List<Producto> findAll();
    Optional<Producto> findByNombreIgnoreCase(String nombre);
    Producto save(Producto producto);
    void saveAll(List<Producto> productos);
}
