package com.pos.domain.model;

import java.time.Instant;

public class Devolucion {

    private String ventaId;
    private Dinero montoDevuelto;
    private Instant fechaDevolucion;

    public Devolucion() {}

    public Devolucion(String ventaId, Dinero montoDevuelto, Instant fechaDevolucion) {
        this.ventaId = ventaId;
        this.montoDevuelto = montoDevuelto;
        this.fechaDevolucion = fechaDevolucion;
    }

    public String getVentaId() { return ventaId; }
    public void setVentaId(String ventaId) { this.ventaId = ventaId; }

    public Dinero getMontoDevuelto() { return montoDevuelto; }
    public void setMontoDevuelto(Dinero montoDevuelto) { this.montoDevuelto = montoDevuelto; }

    public Instant getFechaDevolucion() { return fechaDevolucion; }
    public void setFechaDevolucion(Instant fechaDevolucion) { this.fechaDevolucion = fechaDevolucion; }
}
