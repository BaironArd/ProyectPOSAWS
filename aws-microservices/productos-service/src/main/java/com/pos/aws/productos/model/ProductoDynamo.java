package com.pos.aws.productos.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;

/**
 * Entidad DynamoDB para la tabla Productos.
 *
 * Estructura de la tabla:
 *   PK (id)          - clave primaria (String)
 *   codigoBarras     - GSI para búsqueda por código de barras
 *   codigo           - GSI para búsqueda por código alfanumérico
 *   nombre           - atributo para búsqueda por nombre (scan con filter)
 *   precio           - precio en centavos (long)
 *   stock            - cantidad disponible
 *   categoria        - categoría del producto
 *   activo           - si el producto está activo
 */
@DynamoDbBean
public class ProductoDynamo {

    private String id;
    private String codigoBarras;
    private String codigo;
    private String nombre;
    private long precio;
    private int stock;
    private String categoria;
    private boolean activo;

    public ProductoDynamo() {}

    @DynamoDbPartitionKey
    @DynamoDbAttribute("id")
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @DynamoDbSecondaryPartitionKey(indexNames = "codigoBarras-index")
    @DynamoDbAttribute("codigoBarras")
    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }

    @DynamoDbSecondaryPartitionKey(indexNames = "codigo-index")
    @DynamoDbAttribute("codigo")
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    @DynamoDbAttribute("nombre")
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    @DynamoDbAttribute("precio")
    public long getPrecio() { return precio; }
    public void setPrecio(long precio) { this.precio = precio; }

    @DynamoDbAttribute("stock")
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    @DynamoDbAttribute("categoria")
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    @DynamoDbAttribute("activo")
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
