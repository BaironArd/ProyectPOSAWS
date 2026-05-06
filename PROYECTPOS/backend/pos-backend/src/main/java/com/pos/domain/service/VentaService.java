package com.pos.domain.service;

import com.pos.domain.exception.CarritoVacioException;
import com.pos.domain.exception.MontoInsuficienteException;
import com.pos.domain.exception.ProductoNotFoundException;
import com.pos.domain.exception.VentaNotFoundException;
import com.pos.domain.model.*;
import com.pos.domain.port.in.ConfirmarVentaCommand;
import com.pos.domain.port.in.ConfirmarVentaUseCase;
import com.pos.domain.port.in.ObtenerVentaUseCase;
import com.pos.domain.port.out.ProductoRepository;
import com.pos.domain.port.out.VentaRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Servicio de dominio — POJO sin anotaciones de Spring.
 * Orquesta la confirmación de ventas con validaciones, cálculo y persistencia.
 */
public class VentaService implements ConfirmarVentaUseCase, ObtenerVentaUseCase {

    private final ProductoRepository productoRepository;
    private final VentaRepository ventaRepository;
    private final CalculadoraVenta calculadora;

    private static final AtomicInteger contador = new AtomicInteger(1);

    public VentaService(ProductoRepository productoRepository,
                        VentaRepository ventaRepository,
                        CalculadoraVenta calculadora) {
        this.productoRepository = productoRepository;
        this.ventaRepository = ventaRepository;
        this.calculadora = calculadora;
    }

    @Override
    public Venta confirmar(ConfirmarVentaCommand command) {
        // 1. Validar carrito no vacío
        if (command.items() == null || command.items().isEmpty()) {
            throw new CarritoVacioException();
        }

        // 2. Idempotencia: si la clave ya fue procesada, retornar venta existente
        if (command.idempotencyKey() != null && !command.idempotencyKey().isBlank()) {
            var existente = ventaRepository.findByIdempotencyKey(command.idempotencyKey());
            if (existente.isPresent()) return existente.get();
        }

        // 3. Resolver productos
        List<Producto> productos = resolverProductos(command.items());

        // 4. Construir ítems con subtotales
        List<ItemVenta> items = construirItems(command.items(), productos);

        // 5. Calcular resumen
        ResumenVenta resumen = calculadora.calcular(items, Dinero.dePesos(command.montoPagado()));

        // 6. Validar monto pagado
        if (resumen.cambio().esMenorQue(Dinero.CERO)) {
            throw new MontoInsuficienteException(resumen.total(), Dinero.dePesos(command.montoPagado()));
        }

        // 7. Descontar stock (lanza StockInsuficienteException si no hay suficiente)
        for (ConfirmarVentaCommand.ItemCommand itemCmd : command.items()) {
            Producto p = productos.stream()
                    .filter(pr -> pr.getId().equals(itemCmd.productoId()))
                    .findFirst()
                    .orElseThrow(() -> new ProductoNotFoundException(itemCmd.productoId()));
            p.descontarStock(itemCmd.cantidad());
        }

        // 8. Persistir productos actualizados
        productoRepository.saveAll(productos);

        // 9. Construir pagos
        List<PagoItem> pagos = construirPagos(command);

        // 10. Crear y persistir venta
        Venta venta = new Venta(
                generarVentaId(),
                items,
                resumen,
                EstadoVenta.COMPLETADA,
                Instant.now(),
                command.idempotencyKey(),
                command.usuarioCajero(),
                pagos
        );

        return ventaRepository.save(venta);
    }

    @Override
    public Venta obtener(String ventaId) {
        return ventaRepository.findById(ventaId)
                .orElseThrow(() -> new VentaNotFoundException(ventaId));
    }

    // ---- Helpers privados ----

    private List<Producto> resolverProductos(List<ConfirmarVentaCommand.ItemCommand> items) {
        List<Producto> productos = new ArrayList<>();
        for (ConfirmarVentaCommand.ItemCommand item : items) {
            Producto p = productoRepository.findById(item.productoId())
                    .orElseThrow(() -> new ProductoNotFoundException(item.productoId()));
            productos.add(p);
        }
        return productos;
    }

    private List<ItemVenta> construirItems(List<ConfirmarVentaCommand.ItemCommand> itemCmds,
                                           List<Producto> productos) {
        List<ItemVenta> items = new ArrayList<>();
        for (ConfirmarVentaCommand.ItemCommand cmd : itemCmds) {
            Producto p = productos.stream()
                    .filter(pr -> pr.getId().equals(cmd.productoId()))
                    .findFirst()
                    .orElseThrow(() -> new ProductoNotFoundException(cmd.productoId()));
            items.add(new ItemVenta(p.getId(), p.getNombre(), cmd.cantidad(), p.getPrecio()));
        }
        return items;
    }

    private List<PagoItem> construirPagos(ConfirmarVentaCommand command) {
        if (command.pagos() == null || command.pagos().isEmpty()) {
            MetodoPago metodo = command.metodoPago() != null
                    ? MetodoPago.valueOf(command.metodoPago())
                    : MetodoPago.EFECTIVO;
            return List.of(new PagoItem(metodo, Dinero.dePesos(command.montoPagado()), null));
        }
        return command.pagos().stream()
                .map(p -> new PagoItem(
                        MetodoPago.valueOf(p.metodo()),
                        Dinero.dePesos(p.monto()),
                        p.referencia()))
                .toList();
    }

    private String generarVentaId() {
        String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("VNT-%s-%03d", fecha, contador.getAndIncrement());
    }
}
