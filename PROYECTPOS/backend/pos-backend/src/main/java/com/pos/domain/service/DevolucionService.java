package com.pos.domain.service;

import com.pos.domain.exception.VentaNoDevolvibleException;
import com.pos.domain.exception.VentaNotFoundException;
import com.pos.domain.exception.VentaYaDevueltaException;
import com.pos.domain.model.*;
import com.pos.domain.port.in.DevolverVentaUseCase;
import com.pos.domain.port.out.ProductoRepository;
import com.pos.domain.port.out.VentaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio de dominio — POJO sin anotaciones de Spring.
 * Soporta devolución total y devolución parcial por ítems.
 * La transacción la gestiona el @Transactional del controlador.
 */
public class DevolucionService implements DevolverVentaUseCase {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final CalculadoraVenta calculadora;

    public DevolucionService(VentaRepository ventaRepository,
                              ProductoRepository productoRepository,
                              CalculadoraVenta calculadora) {
        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
        this.calculadora = calculadora;
    }

    /** Devolución total: devuelve todos los ítems de la venta */
    @Override
    public Devolucion devolver(String ventaId) {
        Venta venta = cargarVentaDevolvible(ventaId);

        Dinero montoDevuelto = venta.getResumen().total();
        List<ItemDevolucionCommand> todosLosItems = venta.getItems().stream()
                .map(i -> new ItemDevolucionCommand(i.getProductoId(), i.getCantidad()))
                .toList();

        restaurarStock(todosLosItems, venta);

        venta.getItems().clear();
        venta.setResumen(calculadora.calcular(venta.getItems(), venta.getResumen().montoPagado()));
        venta.setEstado(EstadoVenta.DEVUELTA);
        ventaRepository.save(venta);

        return new Devolucion(ventaId, montoDevuelto, Instant.now(), "DEVUELTA");
    }

    /** Devolución parcial: solo los ítems y cantidades indicados */
    @Override
    public Devolucion devolverParcial(String ventaId, List<ItemDevolucionCommand> itemsADevolver) {
        Venta venta = cargarVentaDevolvible(ventaId);

        Map<Long, ItemVenta> itemsVenta = venta.getItems().stream()
                .collect(Collectors.toMap(ItemVenta::getProductoId, i -> i));

        Dinero subtotalDevuelto = Dinero.CERO;
        for (ItemDevolucionCommand cmd : itemsADevolver) {
            ItemVenta itemOriginal = itemsVenta.get(cmd.productoId());
            if (itemOriginal == null) continue;
            int cantidadReal = Math.min(cmd.cantidad(), itemOriginal.getCantidad());
            if (cantidadReal <= 0) continue;

            Dinero subtotalItem = itemOriginal.getPrecioUnitario().por(cantidadReal);
            subtotalDevuelto = subtotalDevuelto.mas(subtotalItem);

            itemOriginal.setCantidad(itemOriginal.getCantidad() - cantidadReal);
            itemOriginal.setSubtotal(itemOriginal.getPrecioUnitario().por(itemOriginal.getCantidad()));
        }

        venta.getItems().removeIf(item -> item.getCantidad() <= 0);
        Dinero ivaDevuelto = subtotalDevuelto.iva();
        Dinero montoDevuelto = subtotalDevuelto.mas(ivaDevuelto);

        restaurarStock(itemsADevolver, venta);

        venta.setResumen(calculadora.calcular(venta.getItems(), venta.getResumen().montoPagado()));
        EstadoVenta estadoFinal = venta.getItems().isEmpty()
                ? EstadoVenta.DEVUELTA
                : EstadoVenta.PARCIALMENTE_DEVUELTA;
        venta.setEstado(estadoFinal);
        ventaRepository.save(venta);

        return new Devolucion(ventaId, montoDevuelto, Instant.now(), estadoFinal == EstadoVenta.DEVUELTA ? "DEVUELTA" : "PARCIAL");
    }

    // ── Helpers ──

    private Venta cargarVentaDevolvible(String ventaId) {
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new VentaNotFoundException(ventaId));

        if (venta.getEstado() == EstadoVenta.DEVUELTA) {
            throw new VentaYaDevueltaException(ventaId);
        }
        // Permitir devoluciones en ventas COMPLETADAS o PARCIALMENTE_DEVUELTAS
        if (venta.getEstado() != EstadoVenta.COMPLETADA && venta.getEstado() != EstadoVenta.PARCIALMENTE_DEVUELTA) {
            throw new VentaNoDevolvibleException(ventaId, venta.getEstado().name());
        }
        return venta;
    }

    private void restaurarStock(List<ItemDevolucionCommand> items, Venta venta) {
        Map<Long, ItemVenta> itemsVenta = venta.getItems().stream()
                .collect(Collectors.toMap(ItemVenta::getProductoId, i -> i));

        for (ItemDevolucionCommand cmd : items) {
            ItemVenta itemOriginal = itemsVenta.get(cmd.productoId());
            if (itemOriginal == null) continue;

            final int cantidadReal = Math.min(cmd.cantidad(), itemOriginal.getCantidad());
            if (cantidadReal <= 0) continue;

            // Cargar, modificar y guardar explícitamente — sin lambda que pueda tragarse errores
            Producto producto = productoRepository.findById(cmd.productoId())
                    .orElse(null);
            if (producto == null) continue;

            producto.restaurarStock(cantidadReal);
            productoRepository.save(producto);
        }
    }
}
