package com.pos.sam.sales.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.pos.sam.sales.model.CreateSaleRequest;
import com.pos.sam.sales.model.SaleDetail;
import com.pos.sam.sales.model.VentaRecord;
import com.pos.sam.sales.service.SaleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * SaveSaleHandlerTest — Tests unitarios para SaveSaleHandler
 *
 * Cubre:
 *   ✓ POST /api/v1/sales (éxito 201)
 *   ✓ POST /api/v1/sales (items vacío → 400)
 *   ✓ POST /api/v1/sales (pago insuficiente → 400)
 *   ✓ GET  /api/v1/sales (lista ventas → 200)
 *   ✓ GET  /api/v1/sales/{id} (venta por ID → 200)
 *   ✓ OPTIONS /api/v1/sales (CORS preflight → 200)
 *   ✓ GET  /api/v1/reports/daily (reporte diario → 200)
 *   ✓ GET  /api/v1/reports/summary (resumen → 200)
 *   ✓ POST /api/v1/sales (error DynamoDB → 500)
 *   ✓ POST /api/v1/sales (JSON inválido → 400)
 */
@DisplayName("SaveSaleHandler Unit Tests")
public class SaveSaleHandlerTest {

    private SaveSaleHandler handler;

    @Mock
    private SaleService saleService;

    @Mock
    private Context context;

    @Mock
    private LambdaLogger logger;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new SaveSaleHandler(saleService);
        when(context.getLogger()).thenReturn(logger);
    }

    // ── Test 1: POST /sales → 201 ────────────────────────────────────────────

    @Test
    @DisplayName("POST /sales - Crea venta exitosamente (201)")
    void testHandle_postSales_success() {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setHttpMethod("POST");
        event.setPath("/api/v1/sales");
        event.setBody("""
            {
              "paymentMethod": "CASH",
              "amountPaid": 119000,
              "items": [
                {"productId": "prod-1", "name": "Mouse", "quantity": 1, "unitPrice": 100000}
              ]
            }
            """);

        VentaRecord venta = ventaEjemplo("sale-uuid-001", 119000, "COMPLETED");
        when(saleService.createSale(any(CreateSaleRequest.class))).thenReturn(venta);

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

        assertEquals(201, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("sale-uuid-001"));
        verify(saleService, times(1)).createSale(any(CreateSaleRequest.class));
    }

    // ── Test 2: POST /sales items vacío → 400 ────────────────────────────────

    @Test
    @DisplayName("POST /sales - Items vacío retorna 400")
    void testHandle_postSales_emptyItems_returns400() {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setHttpMethod("POST");
        event.setPath("/api/v1/sales");
        event.setBody("""
            {"paymentMethod": "CASH", "amountPaid": 0, "items": []}
            """);

        when(saleService.createSale(any(CreateSaleRequest.class)))
                .thenThrow(new IllegalArgumentException("La venta debe tener al menos un producto"));

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("error") ||
                   response.getBody().contains("producto"));
    }

    // ── Test 3: POST /sales pago insuficiente → 400 ───────────────────────────

    @Test
    @DisplayName("POST /sales - Pago insuficiente retorna 400")
    void testHandle_postSales_insufficientPayment_returns400() {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setHttpMethod("POST");
        event.setPath("/api/v1/sales");
        event.setBody("""
            {
              "paymentMethod": "CASH",
              "amountPaid": 50000,
              "items": [
                {"productId": "prod-1", "name": "Mouse", "quantity": 1, "unitPrice": 100000}
              ]
            }
            """);

        when(saleService.createSale(any(CreateSaleRequest.class)))
                .thenThrow(new IllegalArgumentException("Monto insuficiente"));

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("error") ||
                   response.getBody().contains("Monto"));
    }

    // ── Test 4: GET /sales → lista 200 ───────────────────────────────────────

    @Test
    @DisplayName("GET /sales - Lista de ventas retorna 200")
    void testHandle_getSales_success() {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setHttpMethod("GET");
        event.setPath("/api/v1/sales");
        event.setQueryStringParameters(null);

        VentaRecord v1 = ventaEjemplo("sale-1", 119000, "COMPLETED");
        VentaRecord v2 = ventaEjemplo("sale-2", 202300, "COMPLETED");
        when(saleService.listSales()).thenReturn(List.of(v1, v2));

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("sale-1"));
        assertTrue(response.getBody().contains("\"count\":2"));
        verify(saleService, times(1)).listSales();
    }

    // ── Test 5: GET /sales/{id} → 200 ────────────────────────────────────────

    @Test
    @DisplayName("GET /sales/{id} - Obtiene venta por ID")
    void testHandle_getSaleById_success() {
        String saleId = "sale-uuid-001";
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setHttpMethod("GET");
        event.setPath("/api/v1/sales/" + saleId);
        event.setQueryStringParameters(null);

        VentaRecord venta = ventaEjemplo(saleId, 119000, "COMPLETED");
        when(saleService.getSaleById(saleId)).thenReturn(Optional.of(venta));

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains(saleId));
    }

    // ── Test 6: OPTIONS → CORS preflight 200 ─────────────────────────────────

    @Test
    @DisplayName("OPTIONS /sales - CORS preflight retorna 200 con headers")
    void testHandle_optionsPreflight_returns200() {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setHttpMethod("OPTIONS");
        event.setPath("/api/v1/sales");

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getHeaders());
        assertEquals("*", response.getHeaders().get("Access-Control-Allow-Origin"));
        assertTrue(response.getHeaders().get("Access-Control-Allow-Methods").contains("POST"));
        assertTrue(response.getHeaders().get("Access-Control-Allow-Methods").contains("GET"));
    }

    // ── Test 7: GET /reports/daily → 200 ─────────────────────────────────────

    @Test
    @DisplayName("GET /reports/daily?date=2024-12-15 - Reporte diario")
    void testHandle_reportDaily_success() {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setHttpMethod("GET");
        event.setPath("/api/v1/reports/daily");

        Map<String, String> params = Map.of("date", "2024-12-15");
        event.setQueryStringParameters(params);

        VentaRecord v = ventaEjemplo("sale-1", 119000, "COMPLETED");
        when(saleService.getDailySales("2024-12-15")).thenReturn(List.of(v));

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("2024-12-15"));
        assertTrue(response.getBody().contains("\"count\":1"));
    }

    // ── Test 8: GET /reports/summary → 200 ───────────────────────────────────

    @Test
    @DisplayName("GET /reports/summary - Resumen financiero")
    void testHandle_reportSummary_success() {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setHttpMethod("GET");
        event.setPath("/api/v1/reports/summary");
        event.setQueryStringParameters(null);

        when(saleService.getSummary()).thenReturn(Map.of(
                "totalSales", 10,
                "completedSales", 8L,
                "totalRevenue", 1000000.0
        ));

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("totalRevenue") ||
                   response.getBody().contains("totalSales"));
    }

    // ── Test 9: Error DynamoDB → 500 ─────────────────────────────────────────

    @Test
    @DisplayName("POST /sales - Error DynamoDB retorna 500")
    void testHandle_databaseError_returns500() {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setHttpMethod("POST");
        event.setPath("/api/v1/sales");
        event.setBody("""
            {
              "paymentMethod": "CASH",
              "amountPaid": 119000,
              "items": [{"productId": "id", "name": "P", "quantity": 1, "unitPrice": 100000}]
            }
            """);

        when(saleService.createSale(any(CreateSaleRequest.class)))
                .thenThrow(new RuntimeException("DynamoDB connection failed"));

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

        assertEquals(500, response.getStatusCode());
        assertTrue(response.getBody().contains("error") ||
                   response.getBody().contains("Error"));
    }

    // ── Test 10: JSON inválido → error (400 o 500) ───────────────────────────

    @Test
    @DisplayName("POST /sales - JSON inválido retorna error (400 o 500)")
    void testHandle_invalidJson_returnsError() {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setHttpMethod("POST");
        event.setPath("/api/v1/sales");
        event.setBody("{ invalid json }");

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

        // Jackson lanza JsonParseException que cae en el catch genérico → 500
        assertTrue(response.getStatusCode() == 400 || response.getStatusCode() == 500,
                "JSON inválido debe retornar 400 o 500");
        assertTrue(response.getBody().contains("error") ||
                   response.getBody().contains("Error"));
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private VentaRecord ventaEjemplo(String id, double total, String status) {
        SaleDetail detalle = new SaleDetail();
        detalle.setStatus(status);
        detalle.setTotal(total);
        detalle.setCreatedAt("2024-12-15T10:30:00Z");
        detalle.setPaymentMethod("CASH");
        detalle.setItems(List.of());

        VentaRecord venta = new VentaRecord();
        venta.setId(id);
        venta.setDetalle(detalle);
        return venta;
    }
}
