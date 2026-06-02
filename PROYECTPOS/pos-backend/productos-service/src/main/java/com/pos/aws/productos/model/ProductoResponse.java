package com.pos.aws.productos.model;

/**
 * DTO de respuesta para el endpoint GET /productos.
 * Se serializa a JSON y lo retorna API Gateway al cliente.
 */
public class ProductoResponse {

    private String id;
    private String codigoBarras;
    private String codigo;
    private String nombre;
    private long precio;
    private int stock;
    private String categoria;
    private boolean activo;

    public ProductoResponse() {}

    public ProductoResponse(String id, String codigoBarras, String codigo,
                             String nombre, long precio, int stock,
                             String categoria, boolean activo) {
        this.id = id;
        this.codigoBarras = codigoBarras;
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.categoria = categoria;
        this.activo = activo;
    }

    /** Construye un ProductoResponse desde la entidad DynamoDB. */
    public static ProductoResponse from(ProductoDynamo dynamo) {
        return new ProductoResponse(
                dynamo.getId(),
                dynamo.getCodigoBarras(),
                dynamo.getCodigo(),
                dynamo.getNombre(),
                dynamo.getPrecio(),
                dynamo.getStock(),
                dynamo.getCategoria(),
                dynamo.isActivo()
        );
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

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
}
