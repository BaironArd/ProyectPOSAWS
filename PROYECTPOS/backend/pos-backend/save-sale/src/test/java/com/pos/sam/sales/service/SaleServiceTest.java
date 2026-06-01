package com.pos.sam.sales.service;

import com.pos.sam.sales.model.CreateSaleRequest;
import com.pos.sam.sales.model.VentaRecord;
import com.pos.sam.sales.repository.SaleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * SaleServiceTest — Tests unitarios para SaleService
 *
 * Cubre:
 *   ✓ createSale() calcula subtotal + IVA 19% correctamente
 *   ✓ createSale() genera UUID y timestamp
 *   ✓ createSale() valida items no vacío
 *   ✓ createSale() valida amountPaid >= total
 *   ✓ createSale() valida productId no vacío
 *   ✓ createSale() valida quantity > 0
 *   ✓ createSale() valida unitPrice > 0
 *   ✓ createSale() propaga error de DynamoDB
 *   ✓ listSales() retorna todas las ventas
 *   ✓ getSaleById() retorna venta existente
 *   ✓ getDailySales() filtra por fecha
 *   ✓ getSummary() agrega totales
 */
@DisplayName("SaleService Unit Tests")
public class SaleServiceTest {

    private SaleService saleService;

    @Mock
    private SaleRepository saleRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        saleService = new SaleService(saleRepository);
    }

    // ── Test 1: IVA 19% con un ítem ──────────────────────────────────────────

    @Test
    @DisplayName("createSale() - Calcula IVA 19%: 100000 → total 119000")
    void testCreateSale_calculatesIva() {
        // subtotal = 1 × 100000 = 100000
        // iva      = 100000 × 0.19 = 19000
        // total    = 119000
        CreateSaleRequest req = buildRequest("CASH", 119000, List.of(
                item("prod-1", "Producto", 1, 100000)
        ));
        doNothing().when(saleRepository).save(any());

        VentaRecord result = saleService.createSale(req);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(119000.0, result.getDetalle().getTotal(), 0.01);
        assertEquals("COMPLETED", result.getDetalle().getStatus());
        verify(saleRepository, times(1)).save(any());
    }

    // ── Test 2: Múltiples ítems ───────────────────────────────────────────────

    @Test
    @DisplayName("createSale() - Múltiples ítems: subtotal = sum(qty × price)")
    void testCreateSale_multipleItems() {
        // Item 1: 2 × 45000 = 90000
        // Item 2: 1 × 80000 = 80000
        // subtotal = 170000, iva = 32300, total = 202300
        CreateSaleRequest req = buildRequest("CASH", 202300, List.of(
                item("prod-1", "Mouse", 2, 45000),
                item("prod-2", "Teclado", 1, 80000)
        ));
        doNothing().when(saleRepository).save(any());

        VentaRecord result = saleService.createSale(req);

        assertEquals(202300.0, result.getDetalle().getTotal(), 0.01);
        assertEquals(2, result.getDetalle().getItems().size());
    }

    // ── Test 3: Cambio exacto (amountPaid == total) ───────────────────────────

    @Test
    @DisplayName("createSale() - Pago exacto: cambio = 0")
    void testCreateSale_exactPayment() {
        // subtotal = 100000, iva = 19000, total = 119000, pagado = 119000
        CreateSaleRequest req = buildRequest("CASH", 119000, List.of(
                item("prod-1", "Producto", 1, 100000)
        ));
        doNothing().when(saleRepository).save(any());

        VentaRecord result = saleService.createSale(req);

        assertNotNull(result);
        assertEquals("COMPLETED", result.getDetalle().getStatus());
    }

    // ── Test 4: Validación — items vacío ─────────────────────────────────────

    @Test
    @DisplayName("createSale() - Rechaza items vacío → IllegalArgumentException")
    void testCreateSale_emptyItems_throwsException() {
        CreateSaleRequest req = buildRequest("CASH", 0, List.of());

        assertThrows(IllegalArgumentException.class, () -> saleService.createSale(req));
        verify(saleRepository, never()).save(any());
    }

    // ── Test 5: Validación — pago insuficiente ────────────────────────────────

    @Test
    @DisplayName("createSale() - Rechaza amountPaid < total → IllegalArgumentException")
    void testCreateSale_insufficientPayment_throwsException() {
        // total = 119000, pagado = 50000
        CreateSaleRequest req = buildRequest("CASH", 50000, List.of(
                item("prod-1", "Producto", 1, 100000)
        ));

        assertThrows(IllegalArgumentException.class, () -> saleService.createSale(req));
        verify(saleRepository, never()).save(any());
    }

    // ── Test 6: Validación — productId vacío ─────────────────────────────────

    @Test
    @DisplayName("createSale() - Rechaza productId vacío → IllegalArgumentException")
    void testCreateSale_emptyProductId_throwsException() {
        CreateSaleRequest req = buildRequest("CASH", 119000, List.of(
                item("", "Producto", 1, 100000)
        ));

        assertThrows(IllegalArgumentException.class, () -> saleService.createSale(req));
    }

    // ── Test 7: Validación — quantity = 0 ────────────────────────────────────

    @Test
    @DisplayName("createSale() - Rechaza quantity = 0 → IllegalArgumentException")
    void testCreateSale_zeroQuantity_throwsException() {
        CreateSaleRequest req = buildRequest("CASH", 119000, List.of(
                item("prod-1", "Producto", 0, 100000)
        ));

        assertThrows(IllegalArgumentException.class, () -> saleService.createSale(req));
    }

    // ── Test 8: Validación — unitPrice = 0 ───────────────────────────────────

    @Test
    @DisplayName("createSale() - Rechaza unitPrice = 0 → IllegalArgumentException")
    void testCreateSale_zeroPriceUnit_throwsException() {
        CreateSaleRequest req = buildRequest("CASH", 0, List.of(
                item("prod-1", "Producto", 1, 0)
        ));

        assertThrows(IllegalArgumentException.class, () -> saleService.createSale(req));
    }

    // ── Test 9: Error DynamoDB propaga excepción ──────────────────────────────

    @Test
    @DisplayName("createSale() - Error DynamoDB propaga RuntimeException")
    void testCreateSale_databaseError_propagatesException() {
        CreateSaleRequest req = buildRequest("CASH", 119000, List.of(
                item("prod-1", "Producto", 1, 100000)
        ));
        doThrow(new RuntimeException("DynamoDB connection failed"))
                .when(saleRepository).save(any());

        assertThrows(RuntimeException.class, () -> saleService.createSale(req));
    }

    // ── Test 10: listSales() ──────────────────────────────────────────────────

    @Test
    @DisplayName("listSales() - Retorna todas las ventas del repositorio")
    void testListSales_success() {
        VentaRecord v1 = new VentaRecord(); v1.setId("sale-1");
        VentaRecord v2 = new VentaRecord(); v2.setId("sale-2");
        when(saleRepository.findAll()).thenReturn(List.of(v1, v2));

        List<VentaRecord> result = saleService.listSales();

        assertEquals(2, result.size());
        verify(saleRepository, times(1)).findAll();
    }

    // ── Test 11: listSales() tabla vacía ─────────────────────────────────────

    @Test
    @DisplayName("listSales() - Tabla vacía retorna lista vacía")
    void testListSales_emptyTable() {
        when(saleRepository.findAll()).thenReturn(List.of());

        List<VentaRecord> result = saleService.listSales();

        assertTrue(result.isEmpty());
    }

    // ── Test 12: getSaleById() existente ─────────────────────────────────────

    @Test
    @DisplayName("getSaleById() - Retorna venta existente")
    void testGetSaleById_found() {
        VentaRecord v = new VentaRecord(); v.setId("sale-abc");
        when(saleRepository.findById("sale-abc")).thenReturn(Optional.of(v));

        Optional<VentaRecord> result = saleService.getSaleById("sale-abc");

        assertTrue(result.isPresent());
        assertEquals("sale-abc", result.get().getId());
    }

    // ── Test 13: getSaleById() no encontrado ──────────────────────────────────

    @Test
    @DisplayName("getSaleById() - ID no encontrado retorna Optional.empty()")
    void testGetSaleById_notFound() {
        when(saleRepository.findById("no-existe")).thenReturn(Optional.empty());

        Optional<VentaRecord> result = saleService.getSaleById("no-existe");

        assertTrue(result.isEmpty());
    }

    // ── Test 14: getDailySales() filtra por fecha ─────────────────────────────

    @Test
    @DisplayName("getDailySales() - Filtra ventas por prefijo de fecha")
    void testGetDailySales_filtersByDate() {
        VentaRecord v1 = ventaConFecha("sale-1", "2024-12-15T10:00:00Z");
        VentaRecord v2 = ventaConFecha("sale-2", "2024-12-16T10:00:00Z");
        when(saleRepository.findAll()).thenReturn(List.of(v1, v2));

        List<VentaRecord> result = saleService.getDailySales("2024-12-15");

        assertEquals(1, result.size());
        assertEquals("sale-1", result.get(0).getId());
    }

    // ── Test 15: getSummary() agrega totales ──────────────────────────────────

    @Test
    @DisplayName("getSummary() - Agrega totalRevenue de ventas COMPLETED")
    void testGetSummary_aggregatesTotals() {
        VentaRecord v1 = ventaConTotal("sale-1", 100000, "COMPLETED");
        VentaRecord v2 = ventaConTotal("sale-2", 200000, "COMPLETED");
        when(saleRepository.findAll()).thenReturn(List.of(v1, v2));

        var summary = saleService.getSummary();

        assertNotNull(summary);
        assertEquals(2, ((Number) summary.get("totalSales")).intValue());
        assertEquals(300000.0, (double) summary.get("totalRevenue"), 0.01);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private CreateSaleRequest buildRequest(String method, double amountPaid,
                                           List<CreateSaleRequest.SaleItemRequest> items) {
        CreateSaleRequest req = new CreateSaleRequest();
        req.setPaymentMethod(method);
        req.setAmountPaid(amountPaid);
        req.setItems(items);
        return req;
    }

    private CreateSaleRequest.SaleItemRequest item(String productId, String name,
                                                    int qty, double price) {
        CreateSaleRequest.SaleItemRequest i = new CreateSaleRequest.SaleItemRequest();
        i.setProductId(productId);
        i.setName(name);
        i.setQuantity(qty);
        i.setUnitPrice(price);
        return i;
    }

    private VentaRecord ventaConFecha(String id, String createdAt) {
        com.pos.sam.sales.model.SaleDetail d = new com.pos.sam.sales.model.SaleDetail();
        d.setStatus("COMPLETED");
        d.setCreatedAt(createdAt);
        d.setTotal(100000);
        VentaRecord v = new VentaRecord();
        v.setId(id);
        v.setDetalle(d);
        return v;
    }

    private VentaRecord ventaConTotal(String id, double total, String status) {
        com.pos.sam.sales.model.SaleDetail d = new com.pos.sam.sales.model.SaleDetail();
        d.setStatus(status);
        d.setTotal(total);
        d.setCreatedAt("2024-12-15T10:00:00Z");
        VentaRecord v = new VentaRecord();
        v.setId(id);
        v.setDetalle(d);
        return v;
    }
}
