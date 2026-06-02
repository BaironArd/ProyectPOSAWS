package com.pos.aws.ventas.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pos.aws.ventas.model.RegistrarVentaRequest;
import com.pos.aws.ventas.model.VentaResponse;
import com.pos.aws.ventas.repository.VentaRepository;
import com.pos.aws.ventas.service.VentaService;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.Map;

/**
 * Handler principal del microservicio Ventas.
 *
 * Punto de entrada que AWS Lambda ejecuta cuando llega un request de API Gateway.
 *
 * Flujo:
 *   API Gateway → VentasHandler → VentaService → VentaRepository → DynamoDB
 *
 * Endpoint: POST /ventas
 *
 * Body JSON esperado:
 * {
 *   "cajero": "juan.perez",
 *   "metodoPago": "EFECTIVO",
 *   "montoPagado": 100000,
 *   "items": [
 *     { "productoId": "prod-001", "nombre": "Mouse", "cantidad": 2, "precioUnitario": 45000 }
 *   ]
 * }
 */
public class VentasHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final VentaService ventaService;
    private final ObjectMapper objectMapper;

    /**
     * Constructor sin argumentos — AWS Lambda lo invoca por reflexión.
     * Inicializa el cliente DynamoDB y el servicio una sola vez (fuera del handler)
     * para aprovechar el warm start de Lambda.
     */
    public VentasHandler() {
        DynamoDbClient dynamoDbClient = DynamoDbClient.builder()
                .region(Region.of(System.getenv("AWS_REGION") != null
                        ? System.getenv("AWS_REGION")
                        : "us-east-1"))
                .build();

        VentaRepository repository = new VentaRepository(dynamoDbClient);
        this.ventaService = new VentaService(repository);
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Constructor para tests — permite inyectar dependencias.
     */
    public VentasHandler(VentaService ventaService) {
        this.ventaService = ventaService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Método principal que AWS Lambda ejecuta.
     * Deserializa el body JSON, llama al servicio y retorna la respuesta HTTP 201.
     */
    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent event, Context context) {

        context.getLogger().log("VentasHandler invocado. Path: " + event.getPath());

        try {
            String body = event.getBody();

            if (body == null || body.isBlank()) {
                return errorResponse(400, "El body de la venta es requerido");
            }

            RegistrarVentaRequest request = objectMapper.readValue(body, RegistrarVentaRequest.class);
            VentaResponse venta = ventaService.registrar(request);

            String responseBody = objectMapper.writeValueAsString(Map.of(
                    "success", true,
                    "data", venta,
                    "mensaje", "Venta registrada exitosamente"
            ));

            return response(201, responseBody);

        } catch (IllegalArgumentException e) {
            context.getLogger().log("Validación fallida: " + e.getMessage());
            return errorResponse(400, e.getMessage());

        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            context.getLogger().log("JSON inválido: " + e.getMessage());
            return errorResponse(400, "El formato del JSON es inválido: " + e.getMessage());

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
                "Access-Control-Allow-Methods", "POST,OPTIONS",
                "Access-Control-Allow-Headers", "Content-Type,Authorization"
        );
    }
}
