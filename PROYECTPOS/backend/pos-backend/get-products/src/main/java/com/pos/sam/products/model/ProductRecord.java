package com.pos.sam.products.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;

/**
 * Entidad DynamoDB para la tabla ProductosTable.
 *
 * Estructura exacta requerida por el profesor:
 * {
 *   id:      String  (PK)
 *   code:    String  (GSI code-index)
 *   producto: {
 *     name:               String
 *     price:              Number
 *     stock_level:        Number
 *     low_stock_threshold: Number
 *   }
 * }
 */
@DynamoDbBean
public class ProductRecord {

    private String id;
    private String code;
    private ProductItem producto;

    public ProductRecord() {}

    @DynamoDbPartitionKey
    @DynamoDbAttribute("id")
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @DynamoDbSecondaryPartitionKey(indexNames = "code-index")
    @DynamoDbAttribute("code")
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    @DynamoDbAttribute("producto")
    public ProductItem getProducto() { return producto; }
    public void setProducto(ProductItem producto) { this.producto = producto; }
}
