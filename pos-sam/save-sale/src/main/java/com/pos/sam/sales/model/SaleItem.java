package com.pos.sam.sales.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;

/**
 * Ítem dentro del array "items" del detalle de una venta.
 * { productId, name, quantity, unitPrice, subtotal }
 */
@DynamoDbBean
public class SaleItem {

    private String productId;
    private String name;
    private int quantity;
    private double unitPrice;
    private double subtotal;

    public SaleItem() {}

    public SaleItem(String productId, String name, int quantity, double unitPrice) {
        this.productId  = productId;
        this.name       = name;
        this.quantity   = quantity;
        this.unitPrice  = unitPrice;
        this.subtotal   = unitPrice * quantity;
    }

    @DynamoDbAttribute("productId")
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    @DynamoDbAttribute("name")
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @DynamoDbAttribute("quantity")
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    @DynamoDbAttribute("unitPrice")
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    @DynamoDbAttribute("subtotal")
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
}
