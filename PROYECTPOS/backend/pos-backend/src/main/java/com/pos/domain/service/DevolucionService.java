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

        restaurarStock(todosLosItems);

        venta.getItems().clear();
        venta.setResumen(calculadora.calcular(venta.getItems(), venta.getResumen().montoPagado()));
        // Actualizar monto devuelto acumulado
        Dinero previo = venta.getMontoDevuelto() != null ? venta.getMontoDevuelto() : Dinero.CERO;
        venta.setMontoDevuelto(previo.mas(montoDevuelto));
        venta.setEstado(EstadoVenta.DEVUELTA);
        ventaRepository.save(venta);

        return new Devolucion(ventaId, montoDevuelto, Instant.now(), "DEVUELTA");
    }

    /** Devolución parcial: solo los ítems y cantidades indicados */
    @Override
    public Devolucion devolverParcial(String ventaId, List<ItemDevolucionCommand> itemsADevolver) {
        Venta venta = cargarVentaDevolvible(ventaId);
        Map<Long, ItemVenta> itemsVentaOriginal = venta.getItems().stream()
            .collect(Collectors.toMap(ItemVenta::getProductoId, i -> i));

        Dinero subtotalDevuelto = Dinero.CERO;
        List<ItemDevolucionCommand> itemsParaRestaurar = new java.util.ArrayList<>();

        // Fase 1: montos y cantidades contra las cantidades vigentes en la venta; sin mutar aún
        for (ItemDevolucionCommand cmd : itemsADevolver) {
            ItemVenta itemOriginal = itemsVentaOriginal.get(cmd.productoId());
            if (itemOriginal == null) continue;
            int cantidadReal = Math.min(cmd.cantidad(), itemOriginal.getCantidad());
            if (cantidadReal <= 0) continue;

            Dinero subtotalItem = itemOriginal.getPrecioUnitario().por(cantidadReal);
            subtotalDevuelto = subtotalDevuelto.mas(subtotalItem);
            itemsParaRestaurar.add(new ItemDevolucionCommand(cmd.productoId(), cantidadReal));
        }

        restaurarStock(itemsParaRestaurar);

        // Fase 2: mismo orden que la fase 1 para aplicar las reducciones sobre la venta
        for (ItemDevolucionCommand cmd : itemsParaRestaurar) {
            ItemVenta itemOriginal = itemsVentaOriginal.get(cmd.productoId());
            int cantidadReal = cmd.cantidad();
            itemOriginal.setCantidad(itemOriginal.getCantidad() - cantidadReal);
            itemOriginal.setSubtotal(itemOriginal.getPrecioUnitario().por(itemOriginal.getCantidad()));
        }

        venta.getItems().removeIf(item -> item.getCantidad() <= 0);
        Dinero ivaDevuelto = subtotalDevuelto.iva();
        Dinero montoDevuelto = subtotalDevuelto.mas(ivaDevuelto);

        venta.setResumen(calculadora.calcular(venta.getItems(), venta.getResumen().montoPagado()));
        EstadoVenta estadoFinal = venta.getItems().isEmpty()
            ? EstadoVenta.DEVUELTA
            : EstadoVenta.PARCIALMENTE_DEVUELTA;
        venta.setEstado(estadoFinal);
        // Actualizar monto devuelto acumulado
        Dinero previo = venta.getMontoDevuelto() != null ? venta.getMontoDevuelto() : Dinero.CERO;
        venta.setMontoDevuelto(previo.mas(montoDevuelto));
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

    private void restaurarStock(List<ItemDevolucionCommand> items) {
        for (ItemDevolucionCommand cmd : items) {
            final int cantidadReal = Math.max(0, cmd.cantidad());
            if (cantidadReal <= 0) continue;

            // Cargar, modificar y guardar explícitamente — no dependemos de la venta
            Producto producto = productoRepository.findById(cmd.productoId())
                    .orElse(null);
            if (producto == null) continue;

            producto.restaurarStock(cantidadReal);
            productoRepository.save(producto);
        }
    }
}
