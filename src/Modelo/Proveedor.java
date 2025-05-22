package Modelo;

import java.sql.Date;
import java.sql.Timestamp;

public class Proveedor {
    private String id;
    private String nombre;
    private String telefono;
    private String direccion;
    private String productoSuministrado;
    private Timestamp ultimaVisita;

    public Proveedor(String id, String nombre, String telefono, String direccion, 
            String productoSuministrado, Timestamp ultimaVisita) {
        this.id = id;
        this.nombre = nombre;
        this.telefono = telefono;
        this.direccion = direccion;
        this.productoSuministrado = productoSuministrado;
        this.ultimaVisita = ultimaVisita;
    }
    
    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getProductoSuministrado() {
        return productoSuministrado;
    }

    public void setProductoSuministrado(String productoSuministrado) {
        this.productoSuministrado = productoSuministrado;
    }
    public Timestamp getUltimaVisita() {
        return ultimaVisita;
    }

    public void setUltimaVisita(Timestamp ultimaVisita) {
        this.ultimaVisita = ultimaVisita;
    }

    @Override
    public String toString() {
        return "Proveedor{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", telefono='" + telefono + '\'' +
                ", direccion='" + direccion + '\'' +
                ", productoSuministrado='" + productoSuministrado +  ", ultimaVisita='" + ultimaVisita + '\'' +
                '}';
    }
}