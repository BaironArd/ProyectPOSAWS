package com.pos.aws.ventas.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;

/**
 * Entidad anidada dentro de VentaDynamo.
 * Representa un ítem del detalle de la venta almacenado en DynamoDB.
 *
 * DynamoDB almacena esta lista como un atributo de tipo List<Map>.
 */
@DynamoDbBean
public class ItemVentaDynamo {

    private String productoId;
    private String nombre;
    private int cantidad;
    private long precioUnitario;
    private long subtotal;

    public ItemVentaDynamo() {}

    public ItemVentaDynamo(String productoId, String nombre,
                            int cantidad, long precioUnitario, long subtotal) {
        this.productoId = productoId;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

    @DynamoDbAttribute("productoId")
    public String getProductoId() { return productoId; }
    public void setProductoId(String productoId) { this.productoId = productoId; }

    @DynamoDbAttribute("nombre")
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    @DynamoDbAttribute("cantidad")
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    @DynamoDbAttribute("precioUnitario")
    public long getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(long precioUnitario) { this.precioUnitario = precioUnitario; }

    @DynamoDbAttribute("subtotal")
    public long getSubtotal() { return subtotal; }
    public void setSubtotal(long subtotal) { this.subtotal = subtotal; }
}
