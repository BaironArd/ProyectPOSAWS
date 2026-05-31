package com.pos.sam.products.repository;

import com.pos.sam.products.model.ProductRecord;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repositorio DynamoDB para ProductosTable.
 * Soporta los 4 tipos de búsqueda requeridos:
 *   - id (PK)
 *   - code (GSI)
 *   - name (Scan + filter)
 *   - todos (Scan completo)
 */
public class ProductRepository {

    private static final String TABLE_NAME = System.getenv("PRODUCTOS_TABLE") != null
            ? System.getenv("PRODUCTOS_TABLE")
            : "ProductosTable";

    private final DynamoDbTable<ProductRecord> table;

    public ProductRepository(DynamoDbClient dynamoDbClient) {
        DynamoDbEnhancedClient enhanced = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
        this.table = enhanced.table(TABLE_NAME, TableSchema.fromBean(ProductRecord.class));
    }

    /** Busca por clave primaria (id). */
    public Optional<ProductRecord> findById(String id) {
        return Optional.ofNullable(table.getItem(Key.builder().partitionValue(id).build()));
    }

    /** Busca por código usando el GSI code-index. */
    public List<ProductRecord> findByCode(String code) {
        DynamoDbIndex<ProductRecord> index = table.index("code-index");
        return index.query(QueryConditional.keyEqualTo(
                        Key.builder().partitionValue(code).build()))
                .stream()
                .flatMap(p -> p.items().stream())
                .collect(Collectors.toList());
    }

    /** Busca por nombre (contains, case-insensitive) usando Scan + filtro en memoria. */
    public List<ProductRecord> findByName(String name) {
        String lower = name.toLowerCase();
        // Scan completo y filtrado en memoria (case-insensitive)
        return table.scan().stream()
                .flatMap(p -> p.items().stream())
                .filter(r -> r.getProducto() != null
                        && r.getProducto().getName() != null
                        && r.getProducto().getName().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    /** Retorna todos los productos. */
    public List<ProductRecord> findAll() {
        return table.scan().stream()
                .flatMap(p -> p.items().stream())
                .collect(Collectors.toList());
    }
}
