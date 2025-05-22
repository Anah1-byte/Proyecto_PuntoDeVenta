package Modelo;

import java.util.Date;

public class Devolucion {
    private String id;
    private String idProducto;
    private String nombreProducto;
    private int cantidad;
    private String tipo; // "CLIENTE" o "PROVEEDOR"
    private String motivo;
    private Date fecha;
    private String estado; // "PENDIENTE", "PROCESADA", "RECHAZADA"
    private String observaciones;
    private String idTransaccionOriginal; // ID de la venta o compra original
    private String idUsuario; // Usuario que registró la devolución

    public Devolucion(String id, String idProducto, String nombreProducto, int cantidad, 
                     String tipo, String motivo, Date fecha, String estado, 
                     String observaciones, String idTransaccionOriginal, String idUsuario) {
        this.id = id;
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.tipo = tipo;
        this.motivo = motivo;
        this.fecha = fecha;
        this.estado = estado;
        this.observaciones = observaciones;
        this.idTransaccionOriginal = idTransaccionOriginal;
        this.idUsuario = idUsuario;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getIdProducto() { return idProducto; }
    public void setIdProducto(String idProducto) { this.idProducto = idProducto; }
    
    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
    
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    
    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    
    public String getIdTransaccionOriginal() { return idTransaccionOriginal; }
    public void setIdTransaccionOriginal(String idTransaccionOriginal) { this.idTransaccionOriginal = idTransaccionOriginal; }
    
    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }
}