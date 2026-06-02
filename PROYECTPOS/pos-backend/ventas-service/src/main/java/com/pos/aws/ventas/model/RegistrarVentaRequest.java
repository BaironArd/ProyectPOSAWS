package com.pos.aws.ventas.model;

import java.util.List;

/**
 * DTO del body del request POST /ventas.
 *
 * Ejemplo JSON:
 * {
 *   "cajero": "juan.perez",
 *   "metodoPago": "EFECTIVO",
 *   "montoPagado": 100000,
 *   "items": [
 *     { "productoId": "prod-001", "nombre": "Mouse", "cantidad": 2, "precioUnitario": 45000 },
 *     { "productoId": "prod-002", "nombre": "Teclado", "cantidad": 1, "precioUnitario": 80000 }
 *   ]
 * }
 */
public class RegistrarVentaRequest {

    private String cajero;
    private String metodoPago;
    private long montoPagado;
    private List<ItemVentaRequest> items;

    public RegistrarVentaRequest() {}

    public RegistrarVentaRequest(String cajero, String metodoPago,
                                  long montoPagado, List<ItemVentaRequest> items) {
        this.cajero = cajero;
        this.metodoPago = metodoPago;
        this.montoPagado = montoPagado;
        this.items = items;
    }

    public String getCajero() { return cajero; }
    public void setCajero(String cajero) { this.cajero = cajero; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public long getMontoPagado() { return montoPagado; }
    public void setMontoPagado(long montoPagado) { this.montoPagado = montoPagado; }

    public List<ItemVentaRequest> getItems() { return items; }
    public void setItems(List<ItemVentaRequest> items) { this.items = items; }
}
