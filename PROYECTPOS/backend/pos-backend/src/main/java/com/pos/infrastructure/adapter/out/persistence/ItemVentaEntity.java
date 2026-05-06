package com.pos.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "items_venta")
public class ItemVentaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", nullable = false)
    private VentaEntity venta;

    @Column(nullable = false)
    private Long productoId;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private int cantidad;

    @Column(nullable = false)
    private long precioUnitario;

    @Column(nullable = false)
    private long subtotal;

    public ItemVentaEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public VentaEntity getVenta() { return venta; }
    public void setVenta(VentaEntity venta) { this.venta = venta; }

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public long getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(long precioUnitario) { this.precioUnitario = precioUnitario; }

    public long getSubtotal() { return subtotal; }
    public void setSubtotal(long subtotal) { this.subtotal = subtotal; }
}
