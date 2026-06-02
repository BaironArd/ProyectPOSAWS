package com.pos.aws.productos.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pos.aws.productos.model.ProductoResponse;
import com.pos.aws.productos.repository.ProductoRepository;
import com.pos.aws.productos.service.ProductoService;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.List;
import java.util.Map;

/**
 * Handler principal del microservicio Productos.
 *
 * Punto de entrada que AWS Lambda ejecuta cuando llega un request de API Gateway.
 *
 * Flujo:
 *   API Gateway → ProductosHandler → ProductoService → ProductoRepository → DynamoDB
 *
 * Endpoint: GET /productos?tipo={tipo}&q={valor}
 *
 * Parámetros query:
 *   tipo  - tipo de búsqueda: id | codigoBarras | codigo | nombre | todos
 *   q     - valor a buscar (no requerido cuando tipo=todos)
 *
 * Ejemplos:
 *   GET /productos?tipo=nombre&q=mouse
 *   GET /productos?tipo=codigoBarras&q=7501234567890
 *   GET /productos?tipo=codigo&q=PROD-001
 *   GET /productos?tipo=id&q=abc-123
 *   GET /productos?tipo=todos
 */
public class ProductosHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final ProductoService productoService;
    private final ObjectMapper objectMapper;

    /**
     * Constructor sin argumentos — AWS Lambda lo invoca por reflexión.
     * Inicializa el cliente DynamoDB y el servicio una sola vez (fuera del handler)
     * para aprovechar el warm start de Lambda.
     */
    public ProductosHandler() {
        DynamoDbClient dynamoDbClient = DynamoDbClient.builder()
                .region(Region.of(System.getenv("AWS_REGION") != null
                        ? System.getenv("AWS_REGION")
                        : "us-east-1"))
                .build();

        ProductoRepository repository = new ProductoRepository(dynamoDbClient);
        this.productoService = new ProductoService(repository);
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Constructor para tests — permite inyectar dependencias.
     */
    public ProductosHandler(ProductoService productoService) {
        this.productoService = productoService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Método principal que AWS Lambda ejecuta.
     * Recibe el evento de API Gateway, extrae los query params,
     * llama al servicio y retorna la respuesta HTTP.
     */
    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent event, Context context) {

        context.getLogger().log("ProductosHandler invocado. Path: " + event.getPath());

        try {
            Map<String, String> queryParams = event.getQueryStringParameters();

            String tipo = queryParams != null ? queryParams.get("tipo") : null;
            String q    = queryParams != null ? queryParams.get("q")    : null;

            // Si no se envía tipo, default a "todos"
            if (tipo == null || tipo.isBlank()) {
                tipo = "todos";
            }

            List<ProductoResponse> productos = productoService.buscar(tipo, q);

            String body = objectMapper.writeValueAsString(Map.of(
                    "success", true,
                    "data", productos,
                    "total", productos.size()
            ));

            return response(200, body);

        } catch (IllegalArgumentException e) {
            context.getLogger().log("Validación fallida: " + e.getMessage());
            return errorResponse(400, e.getMessage());

        } catch (Exception e) {
            context.getLogger().log("Error interno: " + e.getMessage());
            return errorResponse(500, "Error interno del servidor");
        }
    }

    // ---- Helpers ----

    private APIGatewayProxyResponseEvent response(int statusCode, String body) {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(corsHeaders())
                .withBody(body);
    }

    private APIGatewayProxyResponseEvent errorResponse(int statusCode, String mensaje) {
        try {
            String body = objectMapper.writeValueAsString(Map.of(
                    "success", false,
                    "error", mensaje
            ));
            return response(statusCode, body);
        } catch (Exception e) {
            return response(statusCode, "{\"success\":false,\"error\":\"" + mensaje + "\"}");
        }
    }

    private Map<String, String> corsHeaders() {
        return Map.of(
                "Content-Type", "application/json",
                "Access-Control-Allow-Origin", "*",
                "Access-Control-Allow-Methods", "GET,OPTIONS",
                "Access-Control-Allow-Headers", "Content-Type,Authorization"
        );
    }
}
