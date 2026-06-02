package com.pos.aws.ventas.model;

import java.util.List;

/**
 * DTO de respuesta del endpoint POST /ventas.
 * Retorna la venta registrada con todos sus detalles financieros.
 */
public class VentaResponse {

    private String ventaId;
    private String cajero;
    private String metodoPago;
    private long montoPagado;
    private long subtotal;
    private long iva;
    private long total;
    private long cambio;
    private String fechaHora;
    private String estado;
    private List<ItemVentaResponse> detalle;

    public VentaResponse() {}

    /** Construye la respuesta desde la entidad DynamoDB ya guardada. */
    public static VentaResponse from(VentaDynamo dynamo) {
        VentaResponse r = new VentaResponse();
        r.ventaId    = dynamo.getVentaId();
        r.cajero     = dynamo.getCajero();
        r.metodoPago = dynamo.getMetodoPago();
        r.montoPagado = dynamo.getMontoPagado();
        r.subtotal   = dynamo.getSubtotal();
        r.iva        = dynamo.getIva();
        r.total      = dynamo.getTotal();
        r.cambio     = dynamo.getCambio();
        r.fechaHora  = dynamo.getFechaHora();
        r.estado     = dynamo.getEstado();
        r.detalle    = dynamo.getDetalle() == null ? List.of() :
                dynamo.getDetalle().stream()
                        .map(i -> new ItemVentaResponse(
                                i.getProductoId(), i.getNombre(),
                                i.getCantidad(), i.getPrecioUnitario(), i.getSubtotal()))
                        .toList();
        return r;
    }

    public String getVentaId() { return ventaId; }
    public void setVentaId(String ventaId) { this.ventaId = ventaId; }

    public String getCajero() { return cajero; }
    public void setCajero(String cajero) { this.cajero = cajero; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public long getMontoPagado() { return montoPagado; }
    public void setMontoPagado(long montoPagado) { this.montoPagado = montoPagado; }

    public long getSubtotal() { return subtotal; }
    public void setSubtotal(long subtotal) { this.subtotal = subtotal; }

    public long getIva() { return iva; }
    public void setIva(long iva) { this.iva = iva; }

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }

    public long getCambio() { return cambio; }
    public void setCambio(long cambio) { this.cambio = cambio; }

    public String getFechaHora() { return fechaHora; }
    public void setFechaHora(String fechaHora) { this.fechaHora = fechaHora; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public List<ItemVentaResponse> getDetalle() { return detalle; }
    public void setDetalle(List<ItemVentaResponse> detalle) { this.detalle = detalle; }

    /** DTO anidado para cada ítem del detalle. */
    public static class ItemVentaResponse {
        private String productoId;
        private String nombre;
        private int cantidad;
        private long precioUnitario;
        private long subtotal;

        public ItemVentaResponse() {}

        public ItemVentaResponse(String productoId, String nombre,
                                  int cantidad, long precioUnitario, long subtotal) {
            this.productoId = productoId;
            this.nombre = nombre;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
            this.subtotal = subtotal;
        }

        public String getProductoId() { return productoId; }
        public void setProductoId(String productoId) { this.productoId = productoId; }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public int getCantidad() { return cantidad; }
        public void setCantidad(int cantidad) { this.cantidad = cantidad; }

        public long getPrecioUnitario() { return precioUnitario; }
        public void setPrecioUnitario(long precioUnitario) { this.precioUnitario = precioUnitario; }

        public long getSubtotal() { return subtotal; }
        public void setSubtotal(long subtotal) { this.subtotal = subtotal; }
    }
}
