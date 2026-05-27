package com.pos.aws.ventas.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;

import java.util.List;

/**
 * Entidad DynamoDB para la tabla Ventas.
 *
 * Estructura de la tabla:
 *   PK (ventaId)   - clave primaria (String UUID)
 *   cajero         - usuario que realizó la venta
 *   metodoPago     - EFECTIVO | TARJETA | TRANSFERENCIA
 *   montoPagado    - monto recibido del cliente (centavos)
 *   subtotal       - subtotal sin IVA (centavos)
 *   iva            - valor del IVA (centavos)
 *   total          - total con IVA (centavos)
 *   cambio         - cambio devuelto al cliente (centavos)
 *   fechaHora      - ISO-8601 timestamp
 *   estado         - COMPLETADA | PENDIENTE | ANULADA
 *   detalle        - lista de ítems (almacenada como JSON en DynamoDB)
 */
@DynamoDbBean
public class VentaDynamo {

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
    private List<ItemVentaDynamo> detalle;

    public VentaDynamo() {}

    @DynamoDbPartitionKey
    @DynamoDbAttribute("ventaId")
    public String getVentaId() { return ventaId; }
    public void setVentaId(String ventaId) { this.ventaId = ventaId; }

    @DynamoDbAttribute("cajero")
    public String getCajero() { return cajero; }
    public void setCajero(String cajero) { this.cajero = cajero; }

    @DynamoDbAttribute("metodoPago")
    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    @DynamoDbAttribute("montoPagado")
    public long getMontoPagado() { return montoPagado; }
    public void setMontoPagado(long montoPagado) { this.montoPagado = montoPagado; }

    @DynamoDbAttribute("subtotal")
    public long getSubtotal() { return subtotal; }
    public void setSubtotal(long subtotal) { this.subtotal = subtotal; }

    @DynamoDbAttribute("iva")
    public long getIva() { return iva; }
    public void setIva(long iva) { this.iva = iva; }

    @DynamoDbAttribute("total")
    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }

    @DynamoDbAttribute("cambio")
    public long getCambio() { return cambio; }
    public void setCambio(long cambio) { this.cambio = cambio; }

    @DynamoDbAttribute("fechaHora")
    public String getFechaHora() { return fechaHora; }
    public void setFechaHora(String fechaHora) { this.fechaHora = fechaHora; }

    @DynamoDbAttribute("estado")
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    @DynamoDbAttribute("detalle")
    public List<ItemVentaDynamo> getDetalle() { return detalle; }
    public void setDetalle(List<ItemVentaDynamo> detalle) { this.detalle = detalle; }
}
