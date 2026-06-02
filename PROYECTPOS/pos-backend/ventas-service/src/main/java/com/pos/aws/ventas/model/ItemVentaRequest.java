package com.pos.aws.ventas.model;

/**
 * DTO que representa un ítem dentro del request POST /ventas.
 *
 * Ejemplo JSON:
 * {
 *   "productoId": "prod-001",
 *   "nombre": "Mouse Inalámbrico",
 *   "cantidad": 2,
 *   "precioUnitario": 45000
 * }
 */
public class ItemVentaRequest {

    private String productoId;
    private String nombre;
    private int cantidad;
    private long precioUnitario;

    public ItemVentaRequest() {}

    public ItemVentaRequest(String productoId, String nombre, int cantidad, long precioUnitario) {
        this.productoId = productoId;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public String getProductoId() { return productoId; }
    public void setProductoId(String productoId) { this.productoId = productoId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public long getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(long precioUnitario) { this.precioUnitario = precioUnitario; }
}
