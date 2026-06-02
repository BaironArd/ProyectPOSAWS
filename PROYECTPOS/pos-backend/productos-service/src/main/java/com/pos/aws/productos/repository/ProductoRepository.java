package com.pos.aws.productos.repository;

import com.pos.aws.productos.model.ProductoDynamo;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repositorio que encapsula todas las consultas a DynamoDB para la tabla Productos.
 *
 * Soporta búsqueda por:
 *   - id (PK)
 *   - codigoBarras (GSI)
 *   - codigo alfanumérico (GSI)
 *   - nombre (Scan con FilterExpression)
 */
public class ProductoRepository {

    private static final String TABLE_NAME = System.getenv("PRODUCTOS_TABLE_NAME") != null
            ? System.getenv("PRODUCTOS_TABLE_NAME")
            : "Productos";

    private final DynamoDbTable<ProductoDynamo> tabla;

    public ProductoRepository(DynamoDbClient dynamoDbClient) {
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
        this.tabla = enhancedClient.table(TABLE_NAME, TableSchema.fromBean(ProductoDynamo.class));
    }

    /**
     * Busca un producto por su clave primaria (id).
     */
    public Optional<ProductoDynamo> buscarPorId(String id) {
        Key key = Key.builder().partitionValue(id).build();
        ProductoDynamo resultado = tabla.getItem(key);
        return Optional.ofNullable(resultado);
    }

    /**
     * Busca productos por código de barras usando el GSI codigoBarras-index.
     */
    public List<ProductoDynamo> buscarPorCodigoBarras(String codigoBarras) {
        DynamoDbIndex<ProductoDynamo> index = tabla.index("codigoBarras-index");
        QueryConditional condition = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(codigoBarras).build()
        );
        return index.query(condition)
                .stream()
                .flatMap(page -> page.items().stream())
                .collect(Collectors.toList());
    }

    /**
     * Busca productos por código alfanumérico usando el GSI codigo-index.
     */
    public List<ProductoDynamo> buscarPorCodigo(String codigo) {
        DynamoDbIndex<ProductoDynamo> index = tabla.index("codigo-index");
        QueryConditional condition = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(codigo).build()
        );
        return index.query(condition)
                .stream()
                .flatMap(page -> page.items().stream())
                .collect(Collectors.toList());
    }

    /**
     * Busca productos cuyo nombre contenga el texto dado (case-insensitive).
     * Usa Scan con FilterExpression — adecuado para tablas pequeñas/medianas.
     */
    public List<ProductoDynamo> buscarPorNombre(String nombre) {
        String nombreLower = nombre.toLowerCase();

        ScanEnhancedRequest scanRequest = ScanEnhancedRequest.builder()
                .filterExpression(software.amazon.awssdk.enhanced.dynamodb.Expression.builder()
                        .expression("contains(#n, :nombre)")
                        .expressionNames(Map.of("#n", "nombre"))
                        .expressionValues(Map.of(
                                ":nombre", AttributeValue.builder().s(nombreLower).build()
                        ))
                        .build())
                .build();

        return tabla.scan(scanRequest)
                .stream()
                .flatMap(page -> page.items().stream())
                .filter(p -> p.getNombre() != null
                        && p.getNombre().toLowerCase().contains(nombreLower))
                .collect(Collectors.toList());
    }

    /**
     * Retorna todos los productos activos (Scan completo).
     * Usar con precaución en tablas grandes.
     */
    public List<ProductoDynamo> listarTodos() {
        return tabla.scan()
                .stream()
                .flatMap(page -> page.items().stream())
                .filter(ProductoDynamo::isActivo)
                .collect(Collectors.toList());
    }
}
