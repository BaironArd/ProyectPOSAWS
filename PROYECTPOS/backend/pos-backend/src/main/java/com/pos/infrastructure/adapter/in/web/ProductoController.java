package com.pos.infrastructure.adapter.in.web;

import com.pos.domain.model.PageResponse;
import com.pos.domain.model.Producto;
import com.pos.domain.service.ProductoService;
import com.pos.infrastructure.adapter.in.web.dto.ApiResponse;
import com.pos.infrastructure.adapter.in.web.dto.ProductoResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public ResponseEntity<?> buscar(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "-1") int size) {

        if (size > 100) {
            throw new jakarta.validation.ConstraintViolationException(
                    "size: debe ser menor o igual a 100", java.util.Set.of());
        }

        if (size <= 0) {
            List<Producto> productos = productoService.buscar(q);
            List<ProductoResponse> response = productos.stream()
                    .map(this::toResponse).toList();
            return ResponseEntity.ok(ApiResponse.of(response));
        }

        List<Producto> productos = productoService.buscar(q);
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
        Producto producto = productoService.obtener(id);
        return ResponseEntity.ok(ApiResponse.of(toResponse(producto)));
    }

    private ProductoResponse toResponse(Producto p) {
        return new ProductoResponse(
                p.getId(), p.getNombre(), p.getPrecio().toPesos(),
                p.getStock(), p.getCategoria(), p.isActivo());
    }
}
