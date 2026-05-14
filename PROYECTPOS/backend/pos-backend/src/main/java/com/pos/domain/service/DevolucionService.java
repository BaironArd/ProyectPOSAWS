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

    public DevolucionService(VentaRepository ventaRepository,
                              ProductoRepository productoRepository) {
        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
    }

    /** Devolución total: devuelve todos los ítems de la venta */
    @Override
    public Devolucion devolver(String ventaId) {
        Venta venta = cargarVentaDevolvible(ventaId);

        List<ItemDevolucionCommand> todosLosItems = venta.getItems().stream()
                .map(i -> new ItemDevolucionCommand(i.getProductoId(), i.getCantidad()))
                .toList();

        restaurarStock(todosLosItems, venta);

        venta.setEstado(EstadoVenta.DEVUELTA);
        ventaRepository.save(venta);

        return new Devolucion(ventaId, venta.getResumen().total(), Instant.now(), "DEVUELTA");
    }

    /** Devolución parcial: solo los ítems y cantidades indicados */
    @Override
    public Devolucion devolverParcial(String ventaId, List<ItemDevolucionCommand> itemsADevolver) {
        Venta venta = cargarVentaDevolvible(ventaId);

        Map<Long, ItemVenta> itemsVenta = venta.getItems().stream()
                .collect(Collectors.toMap(ItemVenta::getProductoId, i -> i));

        // Calcular subtotal devuelto y aplicar IVA (19%)
        long subtotalDevuelto = 0;
        for (ItemDevolucionCommand cmd : itemsADevolver) {
            ItemVenta itemOriginal = itemsVenta.get(cmd.productoId());
            if (itemOriginal == null) continue;
            int cantidad = Math.min(cmd.cantidad(), itemOriginal.getCantidad());
            if (cantidad <= 0) continue;
            subtotalDevuelto += itemOriginal.getPrecioUnitario().toPesos() * cantidad;
        }
        long ivaDevuelto = Math.round(subtotalDevuelto * Dinero.IVA_RATE);
        Dinero montoDevuelto = Dinero.dePesos(subtotalDevuelto + ivaDevuelto);

        restaurarStock(itemsADevolver, venta);

        venta.setEstado(EstadoVenta.DEVUELTA);
        ventaRepository.save(venta);

        return new Devolucion(ventaId, montoDevuelto, Instant.now(), "DEVUELTA");
    }

    // ── Helpers ──

    private Venta cargarVentaDevolvible(String ventaId) {
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new VentaNotFoundException(ventaId));

        if (venta.getEstado() == EstadoVenta.DEVUELTA) {
            throw new VentaYaDevueltaException(ventaId);
        }
        if (venta.getEstado() != EstadoVenta.COMPLETADA) {
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
