package com.pos.aws.ventas.service;

import com.pos.aws.ventas.model.ItemVentaDynamo;
import com.pos.aws.ventas.model.ItemVentaRequest;
import com.pos.aws.ventas.model.RegistrarVentaRequest;
import com.pos.aws.ventas.model.VentaDynamo;
import com.pos.aws.ventas.model.VentaResponse;
import com.pos.aws.ventas.repository.VentaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Capa de servicio del microservicio Ventas.
 *
 * Responsabilidades:
 *   - Validar el request de registro de venta.
 *   - Calcular subtotal, IVA (19%), total y cambio.
 *   - Construir la entidad VentaDynamo.
 *   - Delegar el guardado al repositorio.
 *   - Retornar el DTO de respuesta.
 *
 * NO contiene lógica de HTTP ni de infraestructura AWS.
 */
public class VentaService {

    private static final double IVA_RATE = 0.19;

    private final VentaRepository repository;

    public VentaService(VentaRepository repository) {
        this.repository = repository;
    }

    /**
     * Registra una nueva venta en DynamoDB.
     *
     * @param request datos de la venta enviados por el cliente
     * @return DTO con la venta registrada y sus totales calculados
     * @throws IllegalArgumentException si el request tiene datos inválidos
     */
    public VentaResponse registrar(RegistrarVentaRequest request) {
        validar(request);

        // Calcular totales
        long subtotal = calcularSubtotal(request.getItems());
        long iva      = Math.round(subtotal * IVA_RATE);
        long total    = subtotal + iva;
        long cambio   = request.getMontoPagado() - total;

        if (cambio < 0) {
            throw new IllegalArgumentException(
                    "Monto pagado insuficiente. Total: " + total
                    + ", Pagado: " + request.getMontoPagado());
        }

        // Construir entidad DynamoDB
        VentaDynamo venta = new VentaDynamo();
        venta.setVentaId(UUID.randomUUID().toString());
        venta.setCajero(request.getCajero() != null ? request.getCajero() : "anonimo");
        venta.setMetodoPago(request.getMetodoPago() != null ? request.getMetodoPago() : "EFECTIVO");
        venta.setMontoPagado(request.getMontoPagado());
        venta.setSubtotal(subtotal);
        venta.setIva(iva);
        venta.setTotal(total);
        venta.setCambio(cambio);
        venta.setFechaHora(Instant.now().toString());
        venta.setEstado("COMPLETADA");
        venta.setDetalle(toDetalleDynamo(request.getItems()));

        repository.guardar(venta);

        return VentaResponse.from(venta);
    }

    // ---- Cálculos ----

    private long calcularSubtotal(List<ItemVentaRequest> items) {
        return items.stream()
                .mapToLong(i -> i.getPrecioUnitario() * i.getCantidad())
                .sum();
    }

    private List<ItemVentaDynamo> toDetalleDynamo(List<ItemVentaRequest> items) {
        return items.stream()
                .map(i -> new ItemVentaDynamo(
                        i.getProductoId(),
                        i.getNombre(),
                        i.getCantidad(),
                        i.getPrecioUnitario(),
                        i.getPrecioUnitario() * i.getCantidad()
                ))
                .collect(Collectors.toList());
    }

    // ---- Validaciones ----

    private void validar(RegistrarVentaRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("El body de la venta no puede ser nulo");
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("La venta debe tener al menos un producto");
        }

        for (ItemVentaRequest item : request.getItems()) {
            if (item.getProductoId() == null || item.getProductoId().isBlank()) {
                throw new IllegalArgumentException("Cada ítem debe tener un productoId");
            }
            if (item.getCantidad() <= 0) {
                throw new IllegalArgumentException(
                        "La cantidad del producto '" + item.getProductoId() + "' debe ser mayor a 0");
            }
            if (item.getPrecioUnitario() <= 0) {
                throw new IllegalArgumentException(
                        "El precio del producto '" + item.getProductoId() + "' debe ser mayor a 0");
            }
        }

        if (request.getMontoPagado() <= 0) {
            throw new IllegalArgumentException("El monto pagado debe ser mayor a 0");
        }
    }
}
