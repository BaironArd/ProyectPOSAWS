package com.pos.aws.productos.service;

import com.pos.aws.productos.model.ProductoDynamo;
import com.pos.aws.productos.model.ProductoResponse;
import com.pos.aws.productos.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository repository;

    private ProductoService service;

    @BeforeEach
    void setUp() {
        service = new ProductoService(repository);
    }

    @Test
    void buscarPorNombre_retornaProductos() {
        ProductoDynamo producto = productoEjemplo();
        when(repository.buscarPorNombre("mouse")).thenReturn(List.of(producto));

        List<ProductoResponse> resultado = service.buscar("nombre", "mouse");

        assertEquals(1, resultado.size());
        assertEquals("Mouse Inalámbrico", resultado.get(0).getNombre());
        verify(repository).buscarPorNombre("mouse");
    }

    @Test
    void buscarPorId_retornaProducto() {
        ProductoDynamo producto = productoEjemplo();
        when(repository.buscarPorId("prod-001")).thenReturn(Optional.of(producto));

        List<ProductoResponse> resultado = service.buscar("id", "prod-001");

        assertEquals(1, resultado.size());
        assertEquals("prod-001", resultado.get(0).getId());
    }

    @Test
    void buscarPorCodigoBarras_retornaProductos() {
        ProductoDynamo producto = productoEjemplo();
        when(repository.buscarPorCodigoBarras("7501234567890")).thenReturn(List.of(producto));

        List<ProductoResponse> resultado = service.buscar("codigoBarras", "7501234567890");

        assertEquals(1, resultado.size());
        verify(repository).buscarPorCodigoBarras("7501234567890");
    }

    @Test
    void buscarPorCodigo_retornaProductos() {
        ProductoDynamo producto = productoEjemplo();
        when(repository.buscarPorCodigo("PROD-001")).thenReturn(List.of(producto));

        List<ProductoResponse> resultado = service.buscar("codigo", "PROD-001");

        assertEquals(1, resultado.size());
        verify(repository).buscarPorCodigo("PROD-001");
    }

    @Test
    void buscarTodos_retornaListaCompleta() {
        when(repository.listarTodos()).thenReturn(List.of(productoEjemplo(), productoEjemplo()));

        List<ProductoResponse> resultado = service.buscar("todos", null);

        assertEquals(2, resultado.size());
        verify(repository).listarTodos();
    }

    @Test
    void buscar_tipoInvalido_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> service.buscar("invalido", "valor"));
    }

    @Test
    void buscar_tipoNulo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> service.buscar(null, "valor"));
    }

    @Test
    void buscar_valorVacioConTipoNombre_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> service.buscar("nombre", ""));
    }

    @Test
    void buscar_valorNuloConTipoNombre_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> service.buscar("nombre", null));
    }

    // ---- Helpers ----

    private ProductoDynamo productoEjemplo() {
        ProductoDynamo p = new ProductoDynamo();
        p.setId("prod-001");
        p.setCodigoBarras("7501234567890");
        p.setCodigo("PROD-001");
        p.setNombre("Mouse Inalámbrico");
        p.setPrecio(45000L);
        p.setStock(10);
        p.setCategoria("Periféricos");
        p.setActivo(true);
        return p;
    }
}
