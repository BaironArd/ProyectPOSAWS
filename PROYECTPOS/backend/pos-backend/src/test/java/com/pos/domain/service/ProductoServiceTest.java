package com.pos.domain.service;

import com.pos.domain.exception.ProductoNotFoundException;
import com.pos.domain.exception.QueryDemasiadoCortaException;
import com.pos.domain.model.Dinero;
import com.pos.domain.model.Producto;
import com.pos.domain.port.out.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    private ProductoService productoService;

    @BeforeEach
    void setUp() {
        productoService = new ProductoService(productoRepository);
    }

    private Producto productoMock(Long id, String nombre) {
        return new Producto(id, nombre, Dinero.dePesos(30000), 10, "Test", true);
    }

    // ---- buscar ----

    @Test
    void buscar_conQueryValida_delegaAlRepositorio() {
        when(productoRepository.buscarPorNombreOCodigo("mo"))
                .thenReturn(List.of(productoMock(1L, "Mouse")));

        List<Producto> resultado = productoService.buscar("mo");

        assertThat(resultado).hasSize(1);
        verify(productoRepository).buscarPorNombreOCodigo("mo");
    }

    @Test
    void buscar_conQueryNula_lanzaQueryDemasiadoCortaException() {
        assertThatThrownBy(() -> productoService.buscar(null))
                .isInstanceOf(QueryDemasiadoCortaException.class);
        verifyNoInteractions(productoRepository);
    }

    @Test
    void buscar_conQueryDeUnCaracter_lanzaQueryDemasiadoCortaException() {
        assertThatThrownBy(() -> productoService.buscar("a"))
                .isInstanceOf(QueryDemasiadoCortaException.class);
        verifyNoInteractions(productoRepository);
    }

    @Test
    void buscar_conQueryVacia_lanzaQueryDemasiadoCortaException() {
        assertThatThrownBy(() -> productoService.buscar(""))
                .isInstanceOf(QueryDemasiadoCortaException.class);
    }

    @Test
    void buscar_sinResultados_retornaListaVacia() {
        when(productoRepository.buscarPorNombreOCodigo("xyz")).thenReturn(List.of());

        List<Producto> resultado = productoService.buscar("xyz");

        assertThat(resultado).isEmpty();
    }

    // ---- obtener ----

    @Test
    void obtener_conIdExistente_retornaProducto() {
        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(productoMock(1L, "Mouse")));

        Producto p = productoService.obtener(1L);

        assertThat(p.getId()).isEqualTo(1L);
        assertThat(p.getNombre()).isEqualTo("Mouse");
    }

    @Test
    void obtener_conIdInexistente_lanzaProductoNotFoundException() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.obtener(99L))
                .isInstanceOf(ProductoNotFoundException.class)
                .hasMessageContaining("99");
    }
}
