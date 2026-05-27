package com.pos.sam.sales.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;

import java.util.List;

/**
 * Objeto anidado "detalle" dentro de VentaRecord.
 *
 * Estructura exacta requerida por el profesor:
 * {
 *   status:        String   (COMPLETED | PENDING | CANCELLED)
 *   total:         Number
 *   createdAt:     String   (ISO-8601)
 *   paymentMethod: String   (CASH | CARD | TRANSFER)
 *   items:         [ { productId, name, quantity, unitPrice, subtotal } ]
 * }
 */
@DynamoDbBean
public class SaleDetail {

    private String status;
    private double total;
    private String createdAt;
    private String paymentMethod;
    private List<SaleItem> items;

    public SaleDetail() {}

    @DynamoDbAttribute("status")
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @DynamoDbAttribute("total")
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    @DynamoDbAttribute("createdAt")
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @DynamoDbAttribute("paymentMethod")
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    @DynamoDbAttribute("items")
    public List<SaleItem> getItems() { return items; }
    public void setItems(List<SaleItem> items) { this.items = items; }
}
