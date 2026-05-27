package com.pos.sam.products.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;

/**
 * Objeto anidado dentro de ProductRecord.
 * Representa el campo "producto" en DynamoDB:
 * { name, price, stock_level, low_stock_threshold }
 */
@DynamoDbBean
public class ProductItem {

    private String name;
    private double price;
    private int stockLevel;
    private int lowStockThreshold;

    public ProductItem() {}

    public ProductItem(String name, double price, int stockLevel, int lowStockThreshold) {
        this.name = name;
        this.price = price;
        this.stockLevel = stockLevel;
        this.lowStockThreshold = lowStockThreshold;
    }

    @DynamoDbAttribute("name")
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @DynamoDbAttribute("price")
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    @DynamoDbAttribute("stock_level")
    public int getStockLevel() { return stockLevel; }
    public void setStockLevel(int stockLevel) { this.stockLevel = stockLevel; }

    @DynamoDbAttribute("low_stock_threshold")
    public int getLowStockThreshold() { return lowStockThreshold; }
    public void setLowStockThreshold(int lowStockThreshold) { this.lowStockThreshold = lowStockThreshold; }
}
