package com.pos.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "productos")
public class ProductoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private long precio;

    @Column(nullable = false)
    private int stock;

    private String categoria;

    @Column(nullable = false)
    private boolean activo = true;

    @Version
    private Long version;

    public ProductoEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public long getPrecio() { return precio; }
    public void setPrecio(long precio) { this.precio = precio; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
