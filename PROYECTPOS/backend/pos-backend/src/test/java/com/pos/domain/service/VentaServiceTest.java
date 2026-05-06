package com.pos.domain.service;

import com.pos.domain.exception.*;
import com.pos.domain.model.*;
import com.pos.domain.port.in.ConfirmarVentaCommand;
import com.pos.domain.port.out.ProductoRepository;
import com.pos.domain.port.out.VentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock private ProductoRepository productoRepository;
    @Mock private VentaRepository ventaRepository;

    private VentaService ventaService;

    @BeforeEach
    void setUp() {
        ventaService = new VentaService(productoRepository, ventaRepository, new CalculadoraVenta());
    }

    private Producto producto(Long id, long precio, int stock) {
        return new Producto(id, "Producto " + id, Dinero.dePesos(precio), stock, "Test", true);
    }

    private ConfirmarVentaCommand command(long montoPagado, ConfirmarVentaCommand.ItemCommand... items) {
        return new ConfirmarVentaCommand(List.of(items), montoPagado, "key-" + System.nanoTime(), "cajero01", "EFECTIVO", List.of());
    }

    private Venta ventaMock(String id) {
        ResumenVenta resumen = new ResumenVenta(
                Dinero.dePesos(100000), Dinero.dePesos(19000),
                Dinero.dePesos(119000), Dinero.dePesos(119000), Dinero.CERO);
        return new Venta(id, List.of(), resumen, EstadoVenta.COMPLETADA, Instant.now(), null, "cajero01", List.of());
    }

    // ---- confirmar ----

    @Test
    void confirmar_exitoso_retornaVentaCompletada() {
        Producto p = producto(1L, 100000, 5);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(p));
        when(ventaRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
        when(ventaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Venta venta = ventaService.confirmar(command(119000,
                new ConfirmarVentaCommand.ItemCommand(1L, 1)));

        assertThat(venta.getEstado()).isEqualTo(EstadoVenta.COMPLETADA);
        verify(productoRepository).saveAll(anyList());
        verify(ventaRepository).save(any());
    }

    @Test
    void confirmar_carritoVacio_lanzaCarritoVacioException() {
        ConfirmarVentaCommand cmd = new ConfirmarVentaCommand(
                List.of(), 119000, "key", "cajero01", "EFECTIVO", List.of());

        assertThatThrownBy(() -> ventaService.confirmar(cmd))
                .isInstanceOf(CarritoVacioException.class);

        verifyNoInteractions(productoRepository);
        verifyNoInteractions(ventaRepository);
    }

    @Test
    void confirmar_productoInexistente_lanzaProductoNotFoundException() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());
        when(ventaRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ventaService.confirmar(command(119000,
                new ConfirmarVentaCommand.ItemCommand(99L, 1))))
                .isInstanceOf(ProductoNotFoundException.class);

        verify(ventaRepository, never()).save(any());
    }

    @Test
    void confirmar_montoInsuficiente_lanzaMontoInsuficienteException() {
        Producto p = producto(1L, 100000, 5);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(p));
        when(ventaRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ventaService.confirmar(command(50000,
                new ConfirmarVentaCommand.ItemCommand(1L, 1))))
                .isInstanceOf(MontoInsuficienteException.class);

        verify(productoRepository, never()).saveAll(anyList());
        verify(ventaRepository, never()).save(any());
    }

    @Test
    void confirmar_stockInsuficiente_lanzaStockInsuficienteException() {
        Producto p = producto(1L, 100000, 1); // solo 1 en stock
        when(productoRepository.findById(1L)).thenReturn(Optional.of(p));
        when(ventaRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ventaService.confirmar(command(238000,
                new ConfirmarVentaCommand.ItemCommand(1L, 2)))) // pide 2
                .isInstanceOf(StockInsuficienteException.class);

        verify(ventaRepository, never()).save(any());
    }

    @Test
    void confirmar_idempotencia_retornaVentaExistente() {
        Venta existente = ventaMock("VNT-20250115-001");
        when(ventaRepository.findByIdempotencyKey("key-existente"))
                .thenReturn(Optional.of(existente));

        ConfirmarVentaCommand cmd = new ConfirmarVentaCommand(
                List.of(new ConfirmarVentaCommand.ItemCommand(1L, 1)),
                119000, "key-existente", "cajero01", "EFECTIVO", List.of());

        Venta resultado = ventaService.confirmar(cmd);

        assertThat(resultado.getVentaId()).isEqualTo("VNT-20250115-001");
        verifyNoInteractions(productoRepository);
        verify(ventaRepository, never()).save(any());
    }

    @Test
    void confirmar_atomicidad_siFallaAntesDeGuardar_noSeGuardaVenta() {
        when(productoRepository.findById(1L)).thenReturn(Optional.empty());
        when(ventaRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ventaService.confirmar(command(119000,
                new ConfirmarVentaCommand.ItemCommand(1L, 1))))
                .isInstanceOf(ProductoNotFoundException.class);

        verify(ventaRepository, never()).save(any());
        verify(productoRepository, never()).saveAll(anyList());
    }

    // ---- obtener ----

    @Test
    void obtener_conIdExistente_retornaVenta() {
        Venta v = ventaMock("VNT-001");
        when(ventaRepository.findById("VNT-001")).thenReturn(Optional.of(v));

        Venta resultado = ventaService.obtener("VNT-001");

        assertThat(resultado.getVentaId()).isEqualTo("VNT-001");
    }

    @Test
    void obtener_conIdInexistente_lanzaVentaNotFoundException() {
        when(ventaRepository.findById("FALSO")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ventaService.obtener("FALSO"))
                .isInstanceOf(VentaNotFoundException.class)
                .hasMessageContaining("FALSO");
    }
}
