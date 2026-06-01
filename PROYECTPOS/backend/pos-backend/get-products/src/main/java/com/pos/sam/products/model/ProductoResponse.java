package com.pos.sam.products.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO de respuesta para el endpoint GET /api/v1/products.
 *
 * Estructura de respuesta al frontend:
 * {
 *   "id": "uuid",
 *   "code": "PERI-001",
 *   "name": "Mouse Inalámbrico",
 *   "price": 45000,
 *   "stock_level": 25,
 *   "low_stock_threshold": 5
 * }
 *
 * El frontend usa: id, name, price, stock_level para mostrar y agregar al carrito.
 */
public class ProductoResponse {

    private String id;
    private String code;
    private String name;
    private double price;

    @JsonProperty("stock_level")
    private int stockLevel;

    @JsonProperty("low_stock_threshold")
    private int lowStockThreshold;

    public ProductoResponse() {}

    public ProductoResponse(String id, String code, String name,
                             double price, int stockLevel, int lowStockThreshold) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.price = price;
        this.stockLevel = stockLevel;
        this.lowStockThreshold = lowStockThreshold;
    }

    /** Construye un ProductoResponse desde la entidad DynamoDB. */
    public static ProductoResponse from(ProductRecord dynamo) {
        ProductItem item = dynamo.getProducto();
        return new ProductoResponse(
                dynamo.getId(),
                dynamo.getCode(),
                item != null ? item.getName() : "",
                item != null ? item.getPrice() : 0,
                item != null ? item.getStockLevel() : 0,
                item != null ? item.getLowStockThreshold() : 0
        );
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    @JsonProperty("stock_level")
    public int getStockLevel() { return stockLevel; }
    public void setStockLevel(int stockLevel) { this.stockLevel = stockLevel; }

    @JsonProperty("low_stock_threshold")
    public int getLowStockThreshold() { return lowStockThreshold; }
    public void setLowStockThreshold(int lowStockThreshold) { this.lowStockThreshold = lowStockThreshold; }
}
