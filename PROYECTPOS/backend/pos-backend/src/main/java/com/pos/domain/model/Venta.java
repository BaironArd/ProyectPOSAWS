package com.pos.domain.model;

import java.time.Instant;
import java.util.List;

/**
 * Entidad de dominio. Sin anotaciones de Spring ni JPA.
 */
public class Venta {

    private String ventaId;
    private List<ItemVenta> items;
    private ResumenVenta resumen;
    private EstadoVenta estado;
    private Instant fechaHora;
    private String idempotencyKey;
    private String usuarioCajero;
    private List<PagoItem> pagos;

    public Venta() {}

    public Venta(String ventaId, List<ItemVenta> items, ResumenVenta resumen,
                 EstadoVenta estado, Instant fechaHora, String idempotencyKey,
                 String usuarioCajero, List<PagoItem> pagos) {
        this.ventaId = ventaId;
        this.items = items;
        this.resumen = resumen;
        this.estado = estado;
        this.fechaHora = fechaHora;
        this.idempotencyKey = idempotencyKey;
        this.usuarioCajero = usuarioCajero;
        this.pagos = pagos;
    }

    public String getVentaId() { return ventaId; }
    public void setVentaId(String ventaId) { this.ventaId = ventaId; }

    public List<ItemVenta> getItems() { return items; }
    public void setItems(List<ItemVenta> items) { this.items = items; }

    public ResumenVenta getResumen() { return resumen; }
    public void setResumen(ResumenVenta resumen) { this.resumen = resumen; }

    public EstadoVenta getEstado() { return estado; }
    public void setEstado(EstadoVenta estado) { this.estado = estado; }

    public Instant getFechaHora() { return fechaHora; }
    public void setFechaHora(Instant fechaHora) { this.fechaHora = fechaHora; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public String getUsuarioCajero() { return usuarioCajero; }
    public void setUsuarioCajero(String usuarioCajero) { this.usuarioCajero = usuarioCajero; }

    public List<PagoItem> getPagos() { return pagos; }
    public void setPagos(List<PagoItem> pagos) { this.pagos = pagos; }
}
