package com.pos.infrastructure.adapter.in.web;

import com.pos.domain.model.Venta;
import com.pos.domain.model.Devolucion;
import com.pos.domain.port.in.*;
import com.pos.infrastructure.adapter.in.web.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.io.IOException;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/ventas")
public class VentaController {

    private final ConfirmarVentaUseCase confirmarVenta;
    private final ObtenerVentaUseCase obtenerVenta;
    private final ListarVentasUseCase listarVentas;
    private final DevolverVentaUseCase devolverVenta;

    public VentaController(ConfirmarVentaUseCase confirmarVenta,
                            ObtenerVentaUseCase obtenerVenta,
                            ListarVentasUseCase listarVentas,
                            DevolverVentaUseCase devolverVenta) {
        this.confirmarVenta = confirmarVenta;
        this.obtenerVenta = obtenerVenta;
        this.listarVentas = listarVentas;
        this.devolverVenta = devolverVenta;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<ApiResponse<VentaResponse>> confirmar(
            @RequestBody @Valid ConfirmarVentaRequest request) {

        ConfirmarVentaCommand command = toCommand(request);
        Venta venta = confirmarVenta.confirmar(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(toResponse(venta)));
    }

    @GetMapping("/{ventaId}")
    public ResponseEntity<ApiResponse<VentaResponse>> obtener(@PathVariable String ventaId) {
        Venta venta = obtenerVenta.obtener(ventaId);
        return ResponseEntity.ok(ApiResponse.of(toResponse(venta)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String fechaDesde,
            @RequestParam(required = false) String fechaHasta) {
        var resultado = listarVentas.listar(page, size, fechaDesde, fechaHasta);
        // Mapear a DTO con todos los datos necesarios para factura desde el historial
        var items = resultado.items().stream()
                .map(r -> new VentaResumenResponse(
                        r.ventaId(),
                        r.fechaHora().toString(),
                        r.total().toPesos(),
                        r.montoDevuelto().toPesos(),
                        r.cantidadItems(),
                        r.estado().name(),
                        r.subtotal().toPesos(),
                        r.iva().toPesos(),
                        r.montoPagado().toPesos(),
                        r.cambio().toPesos(),
                        r.usuarioCajero(),
                        r.metodoPago(),
                        r.items().stream()
                                .map(i -> new VentaItemResumenResponse(
                                        i.productoId(),
                                        i.nombre(),
                                        i.cantidad(),
                                        i.precioUnitario(),
                                        i.subtotal()))
                                .toList()
                ))
                .toList();
        var pageDto = new com.pos.domain.model.PageResponse<>(
                items, resultado.total(), resultado.page(), resultado.size(), resultado.totalPages());
        return ResponseEntity.ok(ApiResponse.of(pageDto));
    }

    public record VentaItemResumenResponse(
            Long productoId,
            String nombre,
            int cantidad,
            long precioUnitario,
            long subtotal
    ) {}

    public record VentaResumenResponse(
            String ventaId,
            String fechaHora,
            long total,
            long montoDevuelto,
            int cantidadItems,
            String estado,
            long subtotal,
            long iva,
            long montoPagado,
            long cambio,
            String cajero,
            String metodoPago,
            List<VentaItemResumenResponse> items
    ) {}
        @PostMapping("/{ventaId}/devolucion")
        @Transactional
        public ResponseEntity<ApiResponse<DevolucionResponse>> devolver(
                        @PathVariable String ventaId,
                        HttpServletRequest httpRequest) {

                DevolucionRequest request = null;
                try {
                        request = readDevolucionRequest(httpRequest);
                } catch (IOException e) {
                        throw new RuntimeException("Invalid request payload for devolucion", e);
                }

                Devolucion devolucion;
                if (request != null && request.items() != null && !request.items().isEmpty()) {
                        List<DevolverVentaUseCase.ItemDevolucionCommand> cmds = request.items().stream()
                                        .map(i -> new DevolverVentaUseCase.ItemDevolucionCommand(i.productoId(), i.cantidad()))
                                        .toList();
                        devolucion = devolverVenta.devolverParcial(ventaId, cmds);
                } else {
                        devolucion = devolverVenta.devolver(ventaId);
                }

                return ResponseEntity.ok(ApiResponse.of(new DevolucionResponse(
                                devolucion.getVentaId(),
                                devolucion.getMontoDevuelto().toPesos(),
                                devolucion.getEstado(),
                                devolucion.getFechaDevolucion()
                )));
        }

        private DevolucionRequest readDevolucionRequest(HttpServletRequest req) throws IOException {
                String contentType = req.getContentType();
                int contentLength = req.getContentLength();

                // No body => treat as null (devolución total)
                if (contentLength <= 0) return null;

                // Handle form submissions gracefully: if no 'items' parameter, treat as null
                if (contentType != null && contentType.contains("application/x-www-form-urlencoded")) {
                        String itemsParam = req.getParameter("items");
                        if (itemsParam == null || itemsParam.isBlank()) return null;
                        ObjectMapper mapper = new ObjectMapper();
                        String json = "{\"items\":" + itemsParam + "}";
                        return mapper.readValue(json, DevolucionRequest.class);
                }

                // Default: try to parse JSON body
                String body = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
                if (body == null || body.isBlank()) return null;
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(body, DevolucionRequest.class);
        }

    public record DevolucionRequest(List<DevolucionItemRequest> items) {}
    public record DevolucionItemRequest(Long productoId, int cantidad) {}

    // ---- Mappers ----

    private ConfirmarVentaCommand toCommand(ConfirmarVentaRequest req) {
        List<ConfirmarVentaCommand.ItemCommand> items = req.items().stream()
                .map(i -> new ConfirmarVentaCommand.ItemCommand(i.productoId(), i.cantidad()))
                .toList();

        List<ConfirmarVentaCommand.PagoItemCommand> pagos = req.pagos() != null
                ? req.pagos().stream()
                        .map(p -> new ConfirmarVentaCommand.PagoItemCommand(p.metodo(), p.monto(), p.referencia()))
                        .toList()
                : List.of();

        // Si el frontend no envía idempotencyKey, generar uno en el servidor
        String idempotencyKey = (req.idempotencyKey() != null && !req.idempotencyKey().isBlank())
                ? req.idempotencyKey()
                : java.util.UUID.randomUUID().toString();

        return new ConfirmarVentaCommand(
                items,
                req.montoPagado(),
                idempotencyKey,
                req.usuarioCajero(),
                req.metodoPago(),
                pagos
        );
    }

    private VentaResponse toResponse(Venta venta) {
        List<VentaResponse.ItemVentaResponse> items = venta.getItems().stream()
                .map(i -> new VentaResponse.ItemVentaResponse(
                        i.getProductoId(),
                        i.getNombre(),
                        i.getCantidad(),
                        i.getPrecioUnitario().toPesos(),
                        i.getSubtotal().toPesos()))
                .toList();

        VentaResponse.ResumenResponse resumen = new VentaResponse.ResumenResponse(
                venta.getResumen().subtotal().toPesos(),
                venta.getResumen().iva().toPesos(),
                venta.getResumen().total().toPesos(),
                venta.getResumen().montoPagado().toPesos(),
                venta.getResumen().cambio().toPesos()
        );

        return new VentaResponse(
                venta.getVentaId(),
                items,
                resumen,
                venta.getEstado().name(),
                venta.getFechaHora()
        );
    }
}
