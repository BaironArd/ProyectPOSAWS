package com.pos.domain.model;

import com.pos.domain.exception.StockInsuficienteException;

/**
 * Entidad de dominio. Sin anotaciones de Spring ni JPA.
 */
public class Producto {

    private Long id;
    private String nombre;
    private Dinero precio;
    private int stock;
    private String categoria;
    private boolean activo;

    public Producto() {}

    public Producto(Long id, String nombre, Dinero precio, int stock, String categoria, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.categoria = categoria;
        this.activo = activo;
    }

    public boolean tieneStock(int cantidad) {
        return this.stock >= cantidad;
    }

    public void descontarStock(int cantidad) {
        if (!tieneStock(cantidad)) {
            throw new StockInsuficienteException(this.id, cantidad, this.stock);
        }
        this.stock -= cantidad;
    }

    public void restaurarStock(int cantidad) {
        this.stock += cantidad;
    }

    public void toggleActivo() {
        this.activo = !this.activo;
    }

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Dinero getPrecio() { return precio; }
    public void setPrecio(Dinero precio) { this.precio = precio; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
