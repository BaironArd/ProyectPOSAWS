package com.pos.domain.model;

/**
 * Entidad de dominio — ítem dentro de una venta. Sin anotaciones de Spring ni JPA.
 */
public class ItemVenta {

    private Long productoId;
    private String nombre;
    private int cantidad;
    private Dinero precioUnitario;
    private Dinero subtotal;

    public ItemVenta() {}

    public ItemVenta(Long productoId, String nombre, int cantidad, Dinero precioUnitario) {
        this.productoId = productoId;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = precioUnitario.por(cantidad);
    }

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public Dinero getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(Dinero precioUnitario) { this.precioUnitario = precioUnitario; }

    public Dinero getSubtotal() { return subtotal; }
    public void setSubtotal(Dinero subtotal) { this.subtotal = subtotal; }
}
