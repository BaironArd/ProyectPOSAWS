package com.pos.sam.products.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pos.sam.products.model.ProductRecord;
import com.pos.sam.products.repository.ProductRepository;
import com.pos.sam.products.service.ProductService;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.List;
import java.util.Map;

/**
 * GetProductsFunction — Handler principal de la Lambda de Productos.
 *
 * Maneja los siguientes endpoints de API Gateway:
 *
 *   GET /api/v1/products              → todos los productos
 *   GET /api/v1/products?type=id&q=X  → por id
 *   GET /api/v1/products?type=code&q=X → por código
 *   GET /api/v1/products?type=name&q=X → por nombre
 *   GET /api/v1/products/{id}          → por id (path param)
 *
 * Flujo: API Gateway → GetProductsHandler → ProductService → ProductRepository → DynamoDB
 */
public class GetProductsHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final ProductService productService;
    private final ObjectMapper objectMapper;

    /** Constructor invocado por AWS Lambda (warm start). */
    public GetProductsHandler() {
        DynamoDbClient client = DynamoDbClient.builder()
                .region(Region.of(System.getenv("AWS_REGION") != null
                        ? System.getenv("AWS_REGION") : "us-east-1"))
                .build();
        this.productService = new ProductService(new ProductRepository(client));
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    /** Constructor para tests. */
    public GetProductsHandler(ProductService productService) {
        this.productService = productService;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent event, Context context) {

        context.getLogger().log("[GetProducts] path=" + event.getPath()
                + " params=" + event.getQueryStringParameters());

        try {
            // ── CORS Preflight ──────────────────────────────────────────────
            if ("OPTIONS".equals(event.getHttpMethod())) {
                return preflight();
            }

            Map<String, String> params = event.getQueryStringParameters();
            Map<String, String> pathParams = event.getPathParameters();

            String type  = params != null ? params.get("type") : null;
            String query = params != null ? params.get("q")    : null;

            // Si viene /{id} como path param, buscar por id directamente
            if (pathParams != null && pathParams.containsKey("id")) {
                query = pathParams.get("id");
                type  = "id";
            }

            List<ProductRecord> products = productService.search(type, query);

            String body = objectMapper.writeValueAsString(Map.of(
                    "success", true,
                    "data",    products,
                    "count",   products.size()
            ));
            return ok(body);

        } catch (IllegalArgumentException e) {
            context.getLogger().log("[GetProducts] Validación: " + e.getMessage());
            return error(400, e.getMessage());
        } catch (Exception e) {
            context.getLogger().log("[GetProducts] Error: " + e.getMessage());
            return error(500, "Error interno del servidor");
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private APIGatewayProxyResponseEvent ok(String body) {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withHeaders(corsHeaders())
                .withBody(body);
    }

    private APIGatewayProxyResponseEvent preflight() {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withHeaders(corsHeaders())
                .withBody("");
    }

    private APIGatewayProxyResponseEvent error(int code, String msg) {
        try {
            String body = objectMapper.writeValueAsString(
                    Map.of("success", false, "error", msg));
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(code)
                    .withHeaders(corsHeaders())
                    .withBody(body);
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(code)
                    .withHeaders(corsHeaders())
                    .withBody("{\"success\":false,\"error\":\"" + msg + "\"}");
        }
    }

    private Map<String, String> corsHeaders() {
        return Map.of(
                "Content-Type",                "application/json",
                "Access-Control-Allow-Origin", "*",
                "Access-Control-Allow-Methods","GET,OPTIONS",
                "Access-Control-Allow-Headers","Content-Type,Authorization"
        );
    }
}
