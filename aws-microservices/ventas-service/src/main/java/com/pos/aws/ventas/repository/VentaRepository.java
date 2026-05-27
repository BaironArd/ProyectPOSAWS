package com.pos.aws.ventas.repository;

import com.pos.aws.ventas.model.VentaDynamo;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * Repositorio que encapsula las operaciones de escritura en la tabla Ventas de DynamoDB.
 *
 * Responsabilidad única: guardar una venta.
 * El microservicio de Ventas solo registra — no consulta ni elimina.
 */
public class VentaRepository {

    private static final String TABLE_NAME = System.getenv("VENTAS_TABLE_NAME") != null
            ? System.getenv("VENTAS_TABLE_NAME")
            : "Ventas";

    private final DynamoDbTable<VentaDynamo> tabla;

    public VentaRepository(DynamoDbClient dynamoDbClient) {
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
        this.tabla = enhancedClient.table(TABLE_NAME, TableSchema.fromBean(VentaDynamo.class));
    }

    /**
     * Guarda una venta en DynamoDB.
     * Usa putItem — si ya existe un item con el mismo ventaId, lo sobreescribe.
     *
     * @param venta la entidad a guardar
     */
    public void guardar(VentaDynamo venta) {
        tabla.putItem(venta);
    }
}
