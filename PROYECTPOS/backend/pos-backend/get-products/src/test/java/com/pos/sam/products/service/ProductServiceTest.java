package com.pos.sam.products.service;

import com.pos.sam.products.model.ProductItem;
import com.pos.sam.products.model.ProductRecord;
import com.pos.sam.products.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ProductServiceTest — Tests unitarios para ProductService
 *
 * Cubre:
 *   ✓ search(null, null)       → retorna todos (findAll)
 *   ✓ search("all", null)      → retorna todos (findAll)
 *   ✓ search("name", "mouse")  → delega a findByName
 *   ✓ search("code", "PERI-001") → delega a findByCode
 *   ✓ search("id", "uuid")     → delega a findById
 *   ✓ search("id", "uuid") no encontrado → lista vacía
 *   ✓ search("unknown", "x")   → IllegalArgumentException
 *   ✓ search("name", null)     → IllegalArgumentException (q requerido)
 *   ✓ Error DynamoDB            → propaga RuntimeException
 *   ✓ Lista grande (1000+)      → rendimiento < 5s
 */
@DisplayName("ProductService Unit Tests")
public class ProductServiceTest {

    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        productService = new ProductService(productRepository);
    }

    // ── Test 1: search(null, null) → todos ───────────────────────────────────

    @Test
    @DisplayName("search(null, null) - Retorna todos los productos")
    void testSearch_nullType_returnsAll() {
        List<ProductRecord> mockProducts = List.of(
                product("id-1", "PERI-001", "Mouse Inalámbrico"),
                product("id-2", "PERI-002", "Teclado Mecánico"),
                product("id-3", "TECH-003", "Monitor")
        );
        when(productRepository.findAll()).thenReturn(mockProducts);

        List<ProductRecord> result = productService.search(null, null);

        assertEquals(3, result.size());
        verify(productRepository, times(1)).findAll();
    }

    // ── Test 2: search("all", null) → todos ──────────────────────────────────

    @Test
    @DisplayName("search('all', null) - Retorna todos los productos")
    void testSearch_allType_returnsAll() {
        when(productRepository.findAll()).thenReturn(List.of(
                product("id-1", "PERI-001", "Mouse")
        ));

        List<ProductRecord> result = productService.search("all", null);

        assertEquals(1, result.size());
        verify(productRepository, times(1)).findAll();
    }

    // ── Test 3: search("name", "mouse") ──────────────────────────────────────

    @Test
    @DisplayName("search('name', 'mouse') - Delega a findByName")
    void testSearch_byName_delegatesToRepository() {
        List<ProductRecord> mockProducts = List.of(
                product("id-1", "PERI-001", "Mouse Inalámbrico"),
                product("id-2", "PERI-003", "Mouse Pad Gamer")
        );
        when(productRepository.findByName("mouse")).thenReturn(mockProducts);

        List<ProductRecord> result = productService.search("name", "mouse");

        assertEquals(2, result.size());
        verify(productRepository, times(1)).findByName("mouse");
    }

    // ── Test 4: search("code", "PERI-001") ───────────────────────────────────

    @Test
    @DisplayName("search('code', 'PERI-001') - Delega a findByCode")
    void testSearch_byCode_delegatesToRepository() {
        List<ProductRecord> mockProducts = List.of(
                product("id-1", "PERI-001", "Mouse Inalámbrico")
        );
        when(productRepository.findByCode("PERI-001")).thenReturn(mockProducts);

        List<ProductRecord> result = productService.search("code", "PERI-001");

        assertEquals(1, result.size());
        assertEquals("PERI-001", result.get(0).getCode());
        verify(productRepository, times(1)).findByCode("PERI-001");
    }

    // ── Test 5: search("id", "uuid") encontrado ──────────────────────────────

    @Test
    @DisplayName("search('id', 'uuid') - Retorna producto encontrado")
    void testSearch_byId_found() {
        String uuid = "a1b2c3d4-e5f6-4789-a012-b3c4d5e6f7a8";
        ProductRecord mock = product(uuid, "PERI-001", "Mouse");
        when(productRepository.findById(uuid)).thenReturn(Optional.of(mock));

        List<ProductRecord> result = productService.search("id", uuid);

        assertEquals(1, result.size());
        assertEquals(uuid, result.get(0).getId());
        verify(productRepository, times(1)).findById(uuid);
    }

    // ── Test 6: search("id", "uuid") no encontrado ───────────────────────────

    @Test
    @DisplayName("search('id', 'uuid') - ID no encontrado retorna lista vacía")
    void testSearch_byId_notFound() {
        when(productRepository.findById("no-existe")).thenReturn(Optional.empty());

        List<ProductRecord> result = productService.search("id", "no-existe");

        assertTrue(result.isEmpty());
    }

    // ── Test 7: tipo desconocido → IllegalArgumentException ──────────────────

    @Test
    @DisplayName("search('unknown', 'x') - Tipo inválido lanza IllegalArgumentException")
    void testSearch_unknownType_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> productService.search("unknown-type", "value"));
    }

    // ── Test 8: q requerido cuando type != all ────────────────────────────────

    @Test
    @DisplayName("search('name', null) - q requerido lanza IllegalArgumentException")
    void testSearch_nameWithNullQuery_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> productService.search("name", null));
    }

    // ── Test 9: q vacío cuando type != all ───────────────────────────────────

    @Test
    @DisplayName("search('code', '') - q vacío lanza IllegalArgumentException")
    void testSearch_codeWithEmptyQuery_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> productService.search("code", ""));
    }

    // ── Test 10: Error DynamoDB propaga excepción ─────────────────────────────

    @Test
    @DisplayName("search(null, null) - Error DynamoDB propaga RuntimeException")
    void testSearch_databaseError_propagatesException() {
        when(productRepository.findAll())
                .thenThrow(new RuntimeException("DynamoDB connection failed"));

        assertThrows(RuntimeException.class,
                () -> productService.search(null, null));
    }

    // ── Test 11: Lista vacía ──────────────────────────────────────────────────

    @Test
    @DisplayName("search('name', 'XBOX') - Sin resultados retorna lista vacía")
    void testSearch_byName_noMatches() {
        when(productRepository.findByName("XBOX")).thenReturn(new ArrayList<>());

        List<ProductRecord> result = productService.search("name", "XBOX");

        assertTrue(result.isEmpty());
        verify(productRepository, times(1)).findByName("XBOX");
    }

    // ── Test 12: Lista grande — rendimiento ──────────────────────────────────

    @Test
    @DisplayName("search(null, null) - Maneja 1000+ productos en < 5s")
    void testSearch_largeList_performance() {
        List<ProductRecord> largeList = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            largeList.add(product("id-" + i, "CODE-" + i, "Producto " + i));
        }
        when(productRepository.findAll()).thenReturn(largeList);

        long start = System.currentTimeMillis();
        List<ProductRecord> result = productService.search(null, null);
        long duration = System.currentTimeMillis() - start;

        assertEquals(1000, result.size());
        assertTrue(duration < 5000, "Debe completar en < 5 segundos");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

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
