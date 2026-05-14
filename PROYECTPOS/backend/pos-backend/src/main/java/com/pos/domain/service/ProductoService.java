package com.pos.domain.service;

import com.pos.domain.exception.ProductoDuplicadoException;
import com.pos.domain.exception.ProductoNotFoundException;
import com.pos.domain.exception.QueryDemasiadoCortaException;
import com.pos.domain.model.Dinero;
import com.pos.domain.model.Producto;
import com.pos.domain.port.in.ActualizarProductoCommand;
import com.pos.domain.port.in.BuscarProductosUseCase;
import com.pos.domain.port.in.GestionarProductoUseCase;
import com.pos.domain.port.in.NuevoProductoCommand;
import com.pos.domain.port.in.ObtenerProductoUseCase;
import com.pos.domain.port.out.ProductoRepository;

import java.util.List;

/**
 * Servicio de dominio — POJO sin anotaciones de Spring.
 * Implementa BuscarProductosUseCase, ObtenerProductoUseCase y GestionarProductoUseCase.
 */
public class ProductoService
        implements BuscarProductosUseCase, ObtenerProductoUseCase, GestionarProductoUseCase {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    // ---- BuscarProductosUseCase ----

    @Override
    public List<Producto> buscar(String query) {
        if (query == null || query.trim().length() < 2) {
            throw new QueryDemasiadoCortaException(query);
        }
        return productoRepository.buscarPorNombreOCodigo(query.trim());
    }

    // ---- ObtenerProductoUseCase ----

    @Override
    public Producto obtener(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNotFoundException(id));
    }

    // ---- GestionarProductoUseCase ----

    @Override
    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    @Override
    public Producto crear(NuevoProductoCommand command) {
        productoRepository.findByNombreIgnoreCase(command.nombre())
                .ifPresent(p -> { throw new ProductoDuplicadoException(command.nombre()); });

        Producto nuevo = new Producto(
                null,
                command.nombre(),
                Dinero.dePesos(command.precio()),
                command.stock(),
                command.categoria(),
                true
        );
        return productoRepository.save(nuevo);
    }

    @Override
    public Producto actualizar(Long id, ActualizarProductoCommand command) {
        Producto existente = obtener(id);
        existente.setNombre(command.nombre());
        existente.setPrecio(Dinero.dePesos(command.precio()));
        existente.setStock(command.stock());
        existente.setCategoria(command.categoria());
        return productoRepository.save(existente);
    }

    @Override
    public Producto toggleActivo(Long id) {
        Producto existente = obtener(id);
        existente.toggleActivo();
        return productoRepository.save(existente);
    }
}
