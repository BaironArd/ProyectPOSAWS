package com.pos.sam.sales.repository;

import com.pos.sam.sales.model.VentaRecord;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repositorio DynamoDB para VentasTable.
 * Soporta: guardar, buscar por id, listar todas.
 */
public class SaleRepository {

    private static final String TABLE_NAME = System.getenv("VENTAS_TABLE") != null
            ? System.getenv("VENTAS_TABLE")
            : "VentasTable";

    private final DynamoDbTable<VentaRecord> table;

    public SaleRepository(DynamoDbClient dynamoDbClient) {
        DynamoDbEnhancedClient enhanced = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
        this.table = enhanced.table(TABLE_NAME, TableSchema.fromBean(VentaRecord.class));
    }

    /** Guarda una venta nueva. */
    public void save(VentaRecord venta) {
        table.putItem(venta);
    }

    /** Busca una venta por su id (PK). */
    public Optional<VentaRecord> findById(String id) {
        return Optional.ofNullable(
                table.getItem(Key.builder().partitionValue(id).build()));
    }

    /** Lista todas las ventas (Scan). */
    public List<VentaRecord> findAll() {
        return table.scan().stream()
                .flatMap(p -> p.items().stream())
                .collect(Collectors.toList());
    }

    /** Lista ventas por estado (Scan + filter en memoria). */
    public List<VentaRecord> findByStatus(String status) {
        return findAll().stream()
                .filter(v -> v.getDetalle() != null
                        && status.equalsIgnoreCase(v.getDetalle().getStatus()))
                .collect(Collectors.toList());
    }
}
