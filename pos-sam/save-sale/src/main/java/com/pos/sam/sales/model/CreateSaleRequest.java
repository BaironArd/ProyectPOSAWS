package com.pos.sam.sales.model;

import java.util.List;

/**
 * DTO del body POST /api/v1/sales
 *
 * Ejemplo:
 * {
 *   "paymentMethod": "CASH",
 *   "amountPaid": 150000,
 *   "items": [
 *     { "productId": "PROD-001", "name": "Mouse", "quantity": 2, "unitPrice": 45000 }
 *   ]
 * }
 */
public class CreateSaleRequest {

    private String paymentMethod;
    private double amountPaid;
    private List<SaleItemRequest> items;

    public CreateSaleRequest() {}

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public double getAmountPaid() { return amountPaid; }
    public void setAmountPaid(double amountPaid) { this.amountPaid = amountPaid; }

    public List<SaleItemRequest> getItems() { return items; }
    public void setItems(List<SaleItemRequest> items) { this.items = items; }

    /** Ítem dentro del request de venta. */
    public static class SaleItemRequest {
        private String productId;
        private String name;
        private int quantity;
        private double unitPrice;

        public SaleItemRequest() {}

        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public double getUnitPrice() { return unitPrice; }
        public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    }
}
