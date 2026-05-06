package com.pos.infrastructure.adapter.in.web;

import com.pos.domain.model.PageResponse;
import com.pos.domain.model.Producto;
import com.pos.domain.port.in.BuscarProductosUseCase;
import com.pos.domain.port.in.ObtenerProductoUseCase;
import com.pos.infrastructure.adapter.in.web.dto.ApiResponse;
import com.pos.infrastructure.adapter.in.web.dto.ProductoResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {

    private final BuscarProductosUseCase buscarProductos;
    private final ObtenerProductoUseCase obtenerProducto;

    public ProductoController(BuscarProductosUseCase buscarProductos,
                               ObtenerProductoUseCase obtenerProducto) {
        this.buscarProductos = buscarProductos;
        this.obtenerProducto = obtenerProducto;
    }

    @GetMapping
    public ResponseEntity<?> buscar(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "-1") int size) {

        if (size > 100) {
            throw new com.pos.domain.exception.QueryDemasiadoCortaException("size > 100");
        }

        if (size <= 0) {
            // Sin paginación — retorna lista simple
            List<Producto> productos = buscarProductos.buscar(q);
            List<ProductoResponse> response = productos.stream()
                    .map(this::toResponse).toList();
            return ResponseEntity.ok(ApiResponse.of(response));
        }

        // Con paginación
        List<Producto> productos = buscarProductos.buscar(q);
        List<ProductoResponse> items = productos.stream()
                .skip((long) page * size)
                .limit(size)
                .map(this::toResponse).toList();

        PageResponse<ProductoResponse> pageResponse = PageResponse.of(
                items, productos.size(), page, size);
        return ResponseEntity.ok(ApiResponse.of(pageResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductoResponse>> obtener(@PathVariable Long id) {
        Producto producto = obtenerProducto.obtener(id);
        return ResponseEntity.ok(ApiResponse.of(toResponse(producto)));
    }

    private ProductoResponse toResponse(Producto p) {
        return new ProductoResponse(
                p.getId(),
                p.getNombre(),
                p.getPrecio().toPesos(),
                p.getStock(),
                p.getCategoria(),
                p.isActivo()
        );
    }
}
