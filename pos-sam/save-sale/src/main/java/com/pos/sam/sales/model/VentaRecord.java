package com.pos.sam.sales.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;

/**
 * Entidad DynamoDB para la tabla VentasTable.
 *
 * Estructura exacta requerida por el profesor:
 * {
 *   id:      String  (PK - UUID)
 *   detalle: {
 *     status:        String
 *     total:         Number
 *     createdAt:     String
 *     paymentMethod: String
 *     items:         [ ... ]
 *   }
 * }
 */
@DynamoDbBean
public class VentaRecord {

    private String id;
    private SaleDetail detalle;

    public VentaRecord() {}

    @DynamoDbPartitionKey
    @DynamoDbAttribute("id")
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @DynamoDbAttribute("detalle")
    public SaleDetail getDetalle() { return detalle; }
    public void setDetalle(SaleDetail detalle) { this.detalle = detalle; }
}
