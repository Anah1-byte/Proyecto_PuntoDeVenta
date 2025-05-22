package Modelo;

import java.util.Date;
import java.util.List;

public class Reportes {
    private String usuarioActual;
    private Date fechaGeneracion;
    private String tipoReporte; // "VENTAS", "INVENTARIO", "TICKET"
    private Object datosReporte; // Datos del reporte
    
    public Reportes(String usuarioActual) {
        this.usuarioActual = usuarioActual;
        this.fechaGeneracion = new Date();
    }

    // Mantener tus métodos existentes
    public String getUsuarioActual() {
        return usuarioActual;
    }

    public void setUsuarioActual(String usuarioActual) {
        this.usuarioActual = usuarioActual;
    }
    
    // Nuevos métodos para manejar reportes
    public void setTipoReporte(String tipo) {
        this.tipoReporte = tipo;
    }
    
    public String getTipoReporte() {
        return tipoReporte;
    }
    
    public void setDatosReporte(Object datos) {
        this.datosReporte = datos;
    }
    
    public Object getDatosReporte() {
        return datosReporte;
    }
    
    public Date getFechaGeneracion() {
        return fechaGeneracion;
    }
}