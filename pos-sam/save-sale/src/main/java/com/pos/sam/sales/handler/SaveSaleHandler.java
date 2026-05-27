package com.pos.sam.sales.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pos.sam.sales.model.CreateSaleRequest;
import com.pos.sam.sales.model.VentaRecord;
import com.pos.sam.sales.repository.SaleRepository;
import com.pos.sam.sales.service.SaleService;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * SaveSaleFunction — Handler principal de la Lambda de Ventas/Pagos/Reportes.
 *
 * Maneja 9 endpoints de API Gateway:
 *
 *   VENTAS:
 *     POST   /api/v1/sales              → crear venta
 *     GET    /api/v1/sales              → listar ventas
 *     GET    /api/v1/sales/{id}         → obtener venta por id
 *
 *   PAGOS:
 *     POST   /api/v1/payments           → procesar pago de una venta
 *     GET    /api/v1/payments           → listar ventas por método de pago
 *
 *   REPORTES:
 *     GET    /api/v1/reports/daily      → ventas del día
 *     GET    /api/v1/reports/summary    → resumen financiero
 *     GET    /api/v1/reports/top-products → top productos vendidos
 *
 * Flujo: API Gateway → SaveSaleHandler → SaleService → SaleRepository → DynamoDB
 */
public class SaveSaleHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final SaleService saleService;
    private final ObjectMapper objectMapper;

    /** Constructor invocado por AWS Lambda (warm start). */
    public SaveSaleHandler() {
        DynamoDbClient client = DynamoDbClient.builder()
                .region(Region.of(System.getenv("AWS_REGION") != null
                        ? System.getenv("AWS_REGION") : "us-east-1"))
                .build();
        this.saleService  = new SaleService(new SaleRepository(client));
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    /** Constructor para tests. */
    public SaveSaleHandler(SaleService saleService) {
        this.saleService  = saleService;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent event, Context context) {

        String method = event.getHttpMethod();
        String path   = event.getPath();
        context.getLogger().log("[SaveSale] " + method + " " + path);

        try {
            // ── VENTAS ──────────────────────────────────────────────────────
            if (path.matches(".*/api/v1/sales/?") && "POST".equals(method)) {
                return handleCreateSale(event);
            }
            if (path.matches(".*/api/v1/sales/?") && "GET".equals(method)) {
                return handleListSales();
            }
            if (path.matches(".*/api/v1/sales/[^/]+") && "GET".equals(method)) {
                return handleGetSaleById(event);
            }

            // ── PAGOS ────────────────────────────────────────────────────────
            if (path.matches(".*/api/v1/payments/?") && "POST".equals(method)) {
                return handleProcessPayment(event);
            }
            if (path.matches(".*/api/v1/payments/?") && "GET".equals(method)) {
                return handleListByPaymentMethod(event);
            }

            // ── REPORTES ─────────────────────────────────────────────────────
            if (path.contains("/api/v1/reports/daily") && "GET".equals(method)) {
                return handleDailyReport(event);
            }
            if (path.contains("/api/v1/reports/summary") && "GET".equals(method)) {
                return handleSummary();
            }
            if (path.contains("/api/v1/reports/top-products") && "GET".equals(method)) {
                return handleTopProducts();
            }

            return error(404, "Endpoint no encontrado: " + method + " " + path);

        } catch (IllegalArgumentException e) {
            context.getLogger().log("[SaveSale] Validación: " + e.getMessage());
            return error(400, e.getMessage());
        } catch (Exception e) {
            context.getLogger().log("[SaveSale] Error: " + e.getMessage());
            return error(500, "Error interno del servidor");
        }
    }

    // ── Handlers de ventas ───────────────────────────────────────────────────

    private APIGatewayProxyResponseEvent handleCreateSale(APIGatewayProxyRequestEvent event)
            throws Exception {
        String body = event.getBody();
        if (body == null || body.isBlank()) {
            return error(400, "El body es requerido");
        }
        CreateSaleRequest req = objectMapper.readValue(body, CreateSaleRequest.class);
        VentaRecord venta = saleService.createSale(req);
        return created(Map.of("success", true, "data", venta,
                "message", "Venta registrada exitosamente"));
    }

    private APIGatewayProxyResponseEvent handleListSales() throws Exception {
        List<VentaRecord> sales = saleService.listSales();
        return ok(Map.of("success", true, "data", sales, "count", sales.size()));
    }

    private APIGatewayProxyResponseEvent handleGetSaleById(APIGatewayProxyRequestEvent event)
            throws Exception {
        String id = extractLastPathSegment(event.getPath());
        Optional<VentaRecord> venta = saleService.getSaleById(id);
        if (venta.isEmpty()) {
            return error(404, "Venta no encontrada: " + id);
        }
        return ok(Map.of("success", true, "data", venta.get()));
    }

    // ── Handlers de pagos ────────────────────────────────────────────────────

    private APIGatewayProxyResponseEvent handleProcessPayment(APIGatewayProxyRequestEvent event)
            throws Exception {
        String body = event.getBody();
        if (body == null || body.isBlank()) {
            return error(400, "El body es requerido");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> req = objectMapper.readValue(body, Map.class);
        String saleId        = (String) req.get("saleId");
        String paymentMethod = (String) req.get("paymentMethod");
        double amountPaid    = ((Number) req.getOrDefault("amountPaid", 0)).doubleValue();

        VentaRecord venta = saleService.processPayment(saleId, paymentMethod, amountPaid);
        return ok(Map.of("success", true, "data", venta,
                "message", "Pago procesado exitosamente"));
    }

    private APIGatewayProxyResponseEvent handleListByPaymentMethod(
            APIGatewayProxyRequestEvent event) throws Exception {
        Map<String, String> params = event.getQueryStringParameters();
        String method = params != null ? params.getOrDefault("method", "CASH") : "CASH";
        List<VentaRecord> sales = saleService.getSalesByPaymentMethod(method);
        return ok(Map.of("success", true, "data", sales, "count", sales.size()));
    }

    // ── Handlers de reportes ─────────────────────────────────────────────────

    private APIGatewayProxyResponseEvent handleDailyReport(APIGatewayProxyRequestEvent event)
            throws Exception {
        Map<String, String> params = event.getQueryStringParameters();
        String date = params != null ? params.get("date") : null;
        if (date == null || date.isBlank()) {
            date = java.time.LocalDate.now().toString(); // hoy por defecto
        }
        List<VentaRecord> sales = saleService.getDailySales(date);
        return ok(Map.of("success", true, "date", date,
                "data", sales, "count", sales.size()));
    }

    private APIGatewayProxyResponseEvent handleSummary() throws Exception {
        Map<String, Object> summary = saleService.getSummary();
        return ok(Map.of("success", true, "data", summary));
    }

    private APIGatewayProxyResponseEvent handleTopProducts() throws Exception {
        List<Map<String, Object>> top = saleService.getTopProducts();
        return ok(Map.of("success", true, "data", top));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String extractLastPathSegment(String path) {
        String[] parts = path.split("/");
        return parts[parts.length - 1];
    }

    private APIGatewayProxyResponseEvent ok(Object data) throws Exception {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withHeaders(corsHeaders())
                .withBody(objectMapper.writeValueAsString(data));
    }

    private APIGatewayProxyResponseEvent created(Object data) throws Exception {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(201)
                .withHeaders(corsHeaders())
                .withBody(objectMapper.writeValueAsString(data));
    }

    private APIGatewayProxyResponseEvent error(int code, String msg) {
        try {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(code)
                    .withHeaders(corsHeaders())
                    .withBody(objectMapper.writeValueAsString(
                            Map.of("success", false, "error", msg)));
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(code)
                    .withHeaders(corsHeaders())
                    .withBody("{\"success\":false,\"error\":\"" + msg + "\"}");
        }
    }

    private Map<String, String> corsHeaders() {
        return Map.of(
                "Content-Type",                 "application/json",
                "Access-Control-Allow-Origin",  "*",
                "Access-Control-Allow-Methods", "GET,POST,OPTIONS",
                "Access-Control-Allow-Headers", "Content-Type,Authorization"
        );
    }
}
