package com.pos.sam.sales.service;

import com.pos.sam.sales.model.CreateSaleRequest;
import com.pos.sam.sales.model.SaleDetail;
import com.pos.sam.sales.model.SaleItem;
import com.pos.sam.sales.model.VentaRecord;
import com.pos.sam.sales.repository.SaleRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio de ventas, pagos y reportes.
 * Contiene toda la lógica de negocio — sin dependencias de HTTP ni AWS.
 */
public class SaleService {

    private static final double IVA = 0.19;

    private final SaleRepository repository;

    public SaleService(SaleRepository repository) {
        this.repository = repository;
    }

    // ── Ventas ───────────────────────────────────────────────────────────────

    /** Registra una nueva venta. */
    public VentaRecord createSale(CreateSaleRequest req) {
        validate(req);

        List<SaleItem> items = req.getItems().stream()
                .map(i -> new SaleItem(i.getProductId(), i.getName(),
                        i.getQuantity(), i.getUnitPrice()))
                .collect(Collectors.toList());

        double subtotal = items.stream().mapToDouble(SaleItem::getSubtotal).sum();
        double iva      = Math.round(subtotal * IVA * 100.0) / 100.0;
        double total    = Math.round((subtotal + iva) * 100.0) / 100.0;

        // Comparar en centavos enteros para evitar errores de punto flotante
        long totalCentavos  = Math.round(total * 100);
        long pagadoCentavos = Math.round(req.getAmountPaid() * 100);
        if (pagadoCentavos < totalCentavos) {
            throw new IllegalArgumentException(
                    "Monto insuficiente. Total: " + total + ", Pagado: " + req.getAmountPaid());
        }
        double change = Math.round((req.getAmountPaid() - total) * 100.0) / 100.0;

        SaleDetail detail = new SaleDetail();
        detail.setStatus("COMPLETED");
        detail.setTotal(total);
        detail.setCreatedAt(Instant.now().toString());
        detail.setPaymentMethod(req.getPaymentMethod() != null
                ? req.getPaymentMethod().toUpperCase() : "CASH");
        detail.setItems(items);

        VentaRecord venta = new VentaRecord();
        venta.setId(UUID.randomUUID().toString());
        venta.setDetalle(detail);

        repository.save(venta);
        return venta;
    }

    /** Obtiene una venta por id. */
    public Optional<VentaRecord> getSaleById(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El id de la venta es requerido");
        }
        return repository.findById(id);
    }

    /** Lista todas las ventas. */
    public List<VentaRecord> listSales() {
        return repository.findAll();
    }

    // ── Pagos ────────────────────────────────────────────────────────────────

    /**
     * Procesa el pago de una venta existente.
     * Actualiza el estado a COMPLETED y registra el método de pago.
     */
    public VentaRecord processPayment(String saleId, String paymentMethod, double amountPaid) {
        VentaRecord venta = repository.findById(saleId)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada: " + saleId));

        if ("COMPLETED".equals(venta.getDetalle().getStatus())) {
            throw new IllegalArgumentException("La venta ya fue pagada");
        }

        double total  = venta.getDetalle().getTotal();
        double change = Math.round((amountPaid - total) * 100.0) / 100.0;
        if (change < 0) {
            throw new IllegalArgumentException(
                    "Monto insuficiente. Total: " + total + ", Pagado: " + amountPaid);
        }

        venta.getDetalle().setStatus("COMPLETED");
        venta.getDetalle().setPaymentMethod(
                paymentMethod != null ? paymentMethod.toUpperCase() : "CASH");
        repository.save(venta);
        return venta;
    }

    /** Lista ventas por método de pago. */
    public List<VentaRecord> getSalesByPaymentMethod(String method) {
        return repository.findAll().stream()
                .filter(v -> v.getDetalle() != null
                        && method.equalsIgnoreCase(v.getDetalle().getPaymentMethod()))
                .collect(Collectors.toList());
    }

    // ── Reportes ─────────────────────────────────────────────────────────────

    /** Reporte de ventas del día (filtra por fecha ISO). */
    public List<VentaRecord> getDailySales(String date) {
        return repository.findAll().stream()
                .filter(v -> v.getDetalle() != null
                        && v.getDetalle().getCreatedAt() != null
                        && v.getDetalle().getCreatedAt().startsWith(date))
                .collect(Collectors.toList());
    }

    /** Resumen financiero: total ventas, total recaudado, cantidad de ventas. */
    public Map<String, Object> getSummary() {
        List<VentaRecord> all = repository.findAll();
        double totalRevenue = all.stream()
                .filter(v -> v.getDetalle() != null && "COMPLETED".equals(v.getDetalle().getStatus()))
                .mapToDouble(v -> v.getDetalle().getTotal())
                .sum();
        long completed = all.stream()
                .filter(v -> v.getDetalle() != null && "COMPLETED".equals(v.getDetalle().getStatus()))
                .count();

        return Map.of(
                "totalSales",    all.size(),
                "completedSales", completed,
                "totalRevenue",  Math.round(totalRevenue * 100.0) / 100.0
        );
    }

    /** Top productos más vendidos. */
    public List<Map<String, Object>> getTopProducts() {
        return repository.findAll().stream()
                .filter(v -> v.getDetalle() != null && v.getDetalle().getItems() != null)
                .flatMap(v -> v.getDetalle().getItems().stream())
                .collect(Collectors.groupingBy(
                        SaleItem::getProductId,
                        Collectors.summingInt(SaleItem::getQuantity)))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .map(e -> Map.<String, Object>of("productId", e.getKey(), "totalSold", e.getValue()))
                .collect(Collectors.toList());
    }

    // ── Validaciones ─────────────────────────────────────────────────────────

    private void validate(CreateSaleRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("El body de la venta no puede ser nulo");
        }
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new IllegalArgumentException("La venta debe tener al menos un producto");
        }
        for (var item : req.getItems()) {
            if (item.getProductId() == null || item.getProductId().isBlank()) {
                throw new IllegalArgumentException("Cada ítem debe tener productId");
            }
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
            }
            if (item.getUnitPrice() <= 0) {
                throw new IllegalArgumentException("El precio unitario debe ser mayor a 0");
            }
        }
        if (req.getAmountPaid() <= 0) {
            throw new IllegalArgumentException("El monto pagado debe ser mayor a 0");
        }
    }
}
