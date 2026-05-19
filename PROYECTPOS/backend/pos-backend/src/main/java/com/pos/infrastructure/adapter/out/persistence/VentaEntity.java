package com.pos.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ventas")
public class VentaEntity {

    @Id
    @Column(nullable = false, unique = true)
    private String ventaId;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ItemVentaEntity> items = new ArrayList<>();

    @Column(nullable = false)
    private long subtotal;

    @Column(nullable = false)
    private long iva;

    @Column(nullable = false)
    private long total;

    @Column(name = "monto_devuelto", nullable = false)
    private long montoDevuelto;

    @Column(nullable = false)
    private long montoPagado;

    @Column(nullable = false)
    private long cambio;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EstadoVentaEnum estado;

    @Column(nullable = false)
    private Instant fechaHora;

    @Column(unique = true)
    private String idempotencyKey;

    private String usuarioCajero;

    private String metodoPago;

    public enum EstadoVentaEnum { COMPLETADA, PARCIALMENTE_DEVUELTA, DEVUELTA, CANCELADA }

    public VentaEntity() {}

    public String getVentaId() { return ventaId; }
    public void setVentaId(String ventaId) { this.ventaId = ventaId; }

    public List<ItemVentaEntity> getItems() { return items; }
    public void setItems(List<ItemVentaEntity> items) { this.items = items; }

    public long getSubtotal() { return subtotal; }
    public void setSubtotal(long subtotal) { this.subtotal = subtotal; }

    public long getIva() { return iva; }
    public void setIva(long iva) { this.iva = iva; }

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }

    public long getMontoPagado() { return montoPagado; }
    public void setMontoPagado(long montoPagado) { this.montoPagado = montoPagado; }

    public long getCambio() { return cambio; }
    public void setCambio(long cambio) { this.cambio = cambio; }

    public EstadoVentaEnum getEstado() { return estado; }
    public void setEstado(EstadoVentaEnum estado) { this.estado = estado; }

    public Instant getFechaHora() { return fechaHora; }
    public void setFechaHora(Instant fechaHora) { this.fechaHora = fechaHora; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public String getUsuarioCajero() { return usuarioCajero; }
    public void setUsuarioCajero(String usuarioCajero) { this.usuarioCajero = usuarioCajero; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public long getMontoDevuelto() { return montoDevuelto; }
    public void setMontoDevuelto(long montoDevuelto) { this.montoDevuelto = montoDevuelto; }
}
