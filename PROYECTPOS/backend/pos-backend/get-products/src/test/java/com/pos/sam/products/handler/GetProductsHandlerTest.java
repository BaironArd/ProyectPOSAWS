package com.pos.sam.products.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.pos.sam.products.model.ProductItem;
import com.pos.sam.products.model.ProductRecord;
import com.pos.sam.products.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * GetProductsHandlerTest — Tests unitarios para GetProductsHandler
 *
 * Cubre:
 *   ✓ GET /api/v1/products (todos)
 *   ✓ GET /api/v1/products?type=name&q=mouse (búsqueda por nombre)
 *   ✓ GET /api/v1/products?type=id&q=uuid (por ID)
 *   ✓ GET /api/v1/products?type=id&q=invalid (error 400)
 *   ✓ GET /api/v1/products (lista vacía)
 *   ✓ OPTIONS /api/v1/products (CORS preflight)
 *   ✓ Database error (500)
 *   ✓ GET /api/v1/products/{id} (path param)
 */
@DisplayName("GetProductsHandler Unit Tests")
public class GetProductsHandlerTest {

    private GetProductsHandler handler;

    @Mock
    private ProductService productService;

    @Mock
    private Context context;

    @Mock
    private LambdaLogger logger;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new GetProductsHandler(productService);
        when(context.getLogger()).thenReturn(logger);
    }

    // ── Test 1: GET /products → todos ────────────────────────────────────────

    @Test
    @DisplayName("GET /products - Retorna todos los productos (200 OK)")
    void testHandle_getAllProducts_success() {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setHttpMethod("GET");
        event.setPath("/api/v1/products");
        event.setQueryStringParameters(null);

        List<ProductRecord> mockProducts = Arrays.asList(
                product("id-1", "PERI-001", "Mouse Inalámbrico"),
                product("id-2", "PERI-002", "Teclado Mecánico")
        );
        when(productService.search(null, null)).thenReturn(mockProducts);

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("\"count\":2"));
        assertTrue(response.getBody().contains("PERI-001"));
        verify(productService, times(1)).search(null, null);
    }

    // ── Test 2: GET /products?type=name&q=mouse ───────────────────────────────

    @Test
    @DisplayName("GET /products?type=name&q=mouse - Búsqueda por nombre")
    void testHandle_searchByName_success() {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setHttpMethod("GET");
        event.setPath("/api/v1/products");

        Map<String, String> params = new HashMap<>();
        params.put("type", "name");
        params.put("q", "mouse");
        event.setQueryStringParameters(params);

        List<ProductRecord> mockProducts = Arrays.asList(
                product("id-1", "PERI-001", "Mouse Inalámbrico")
        );
        when(productService.search("name", "mouse")).thenReturn(mockProducts);

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("\"count\":1"));
        assertTrue(response.getBody().contains("PERI-001"));
        verify(productService, times(1)).search("name", "mouse");
    }

    // ── Test 3: GET /products?type=id&q=uuid ─────────────────────────────────

    @Test
    @DisplayName("GET /products?type=id&q=uuid - Búsqueda exacta por ID")
    void testHandle_searchById_success() {
        String uuid = "a1b2c3d4-e5f6-4789-a012-b3c4d5e6f7a8";
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setHttpMethod("GET");
        event.setPath("/api/v1/products");

        Map<String, String> params = new HashMap<>();
        params.put("type", "id");
        params.put("q", uuid);
        event.setQueryStringParameters(params);

        List<ProductRecord> mockProducts = Arrays.asList(
                product(uuid, "PERI-001", "Mouse Inalámbrico")
        );
        when(productService.search("id", uuid)).thenReturn(mockProducts);

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("\"count\":1"));
        assertTrue(response.getBody().contains(uuid));
    }

    // ── Test 4: UUID inválido → 400 ──────────────────────────────────────────

    @Test
    @DisplayName("GET /products?type=id&q=invalid - UUID inválido retorna 400")
    void testHandle_invalidUuid_returns400() {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setHttpMethod("GET");
        event.setPath("/api/v1/products");

        Map<String, String> params = new HashMap<>();
        params.put("type", "id");
        params.put("q", "not-a-valid-uuid");
        event.setQueryStringParameters(params);

        when(productService.search("id", "not-a-valid-uuid"))
                .thenThrow(new IllegalArgumentException("Invalid UUID format"));

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("error") ||
                   response.getBody().contains("Invalid UUID"));
    }

    // ── Test 5: Lista vacía → 200 con count:0 ────────────────────────────────

    @Test
    @DisplayName("GET /products - Lista vacía retorna count:0")
    void testHandle_emptyList_success() {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setHttpMethod("GET");
        event.setPath("/api/v1/products");
        event.setQueryStringParameters(null);

        when(productService.search(null, null)).thenReturn(Arrays.asList());

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("\"count\":0"));
    }

    // ── Test 6: OPTIONS → CORS preflight 200 ─────────────────────────────────

    @Test
    @DisplayName("OPTIONS /products - CORS preflight retorna 200 con headers")
    void testHandle_optionsPreflight_returns200() {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setHttpMethod("OPTIONS");
        event.setPath("/api/v1/products");

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getHeaders());
        assertEquals("*", response.getHeaders().get("Access-Control-Allow-Origin"));
        assertTrue(response.getHeaders().get("Access-Control-Allow-Methods").contains("GET"));
    }

    // ── Test 7: Error DynamoDB → 500 ─────────────────────────────────────────

    @Test
    @DisplayName("GET /products - Error DynamoDB retorna 500")
    void testHandle_databaseError_returns500() {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setHttpMethod("GET");
        event.setPath("/api/v1/products");
        event.setQueryStringParameters(null);

        when(productService.search(null, null))
                .thenThrow(new RuntimeException("DynamoDB connection failed"));

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

        assertEquals(500, response.getStatusCode());
        assertTrue(response.getBody().contains("error") ||
                   response.getBody().contains("Error"));
    }

    // ── Test 8: GET /products/{id} path param ────────────────────────────────

    @Test
    @DisplayName("GET /products/{id} - Path parameter funciona")
    void testHandle_pathParameterId_success() {
        String uuid = "a1b2c3d4-e5f6-4789-a012-b3c4d5e6f7a8";
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setHttpMethod("GET");
        event.setPath("/api/v1/products/" + uuid);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("id", uuid);
        event.setPathParameters(pathParams);
        event.setQueryStringParameters(null);

        List<ProductRecord> mockProducts = Arrays.asList(
                product(uuid, "PERI-001", "Mouse")
        );
        when(productService.search("id", uuid)).thenReturn(mockProducts);

        APIGatewayProxyResponseEvent response = handler.handleRequest(event, context);

        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("\"count\":1"));
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private ProductRecord product(String id, String code, String name) {
        ProductItem item = new ProductItem();
        item.setName(name);
        item.setPrice(45000);
        item.setStockLevel(10);

        ProductRecord p = new ProductRecord();
        p.setId(id);
        p.setCode(code);
        p.setProducto(item);
        return p;
    }
}
