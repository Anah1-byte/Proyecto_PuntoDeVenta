package Modelo;

import java.util.Date;
import java.util.Objects;

import Controlador.ClientesContro;

public class Cliente {
	  private String id;
    private String telefono;
    private String nombre;
    private String ultimaCompra;
    private int puntos;
    private Date fechaRegistro;
    private Date fechaEliminacion; 
    
    public Cliente() {
        this.fechaRegistro = new Date(); // Fecha actual por defecto
    }

    // Constructor con parámetros esenciales
    public Cliente(String telefono, String nombre) {
        this();
        this.telefono = telefono;
        this.nombre = nombre;
    }

    // Constructor completo
    public Cliente(String id, String telefono, String nombre, String ultimaCompra, 
                  int puntos, Date fechaRegistro, Date fechaEliminacion) {
        this.id = id;
        this.telefono = telefono;
        this.nombre = nombre;
        this.ultimaCompra = ultimaCompra;
        this.puntos = puntos;
        this.fechaRegistro = fechaRegistro != null ? fechaRegistro : new Date();
        this.fechaEliminacion = fechaEliminacion;
    }

    public void copiarDe(Cliente otro) {
        this.id = otro.id;
        this.telefono = otro.telefono;
        this.nombre = otro.nombre;
        this.ultimaCompra = otro.ultimaCompra;
        this.puntos = otro.puntos;
        this.fechaRegistro = otro.fechaRegistro;
        this.fechaEliminacion = otro.fechaEliminacion;
    }
    
	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getUltimaCompra() {
		return ultimaCompra;
	}

	public void setUltimaCompra(String ultimaCompra) {
		this.ultimaCompra = ultimaCompra;
	}

	public int getPuntos() {
		return puntos;
	}

	public void setPuntos(int puntos) {
		this.puntos = puntos;
	}
	 public void setFechaRegistro(Date fechaRegistro) {
	        this.fechaRegistro = fechaRegistro;
	    }

	    // Getter para el campo fechaRegistro
	    public Date getFechaRegistro() {
	        return fechaRegistro;
	    }
	public Date getFechaEliminacion() {
        return fechaEliminacion;  // Método getter para fechaEliminacion
    }

    public void setFechaEliminacion(Date fechaEliminacion) {
        this.fechaEliminacion = fechaEliminacion;  // Método setter para fechaEliminacion
    }

    public void setId(String id) {
        this.id = id;
    }

    // Getter para el campo id
    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Cliente other = (Cliente) obj;
        return Objects.equals(id, other.id) &&
               Objects.equals(telefono, other.telefono) &&
               Objects.equals(nombre, other.nombre) &&
               Objects.equals(ultimaCompra, other.ultimaCompra) &&
               puntos == other.puntos;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, telefono, nombre, ultimaCompra, puntos);
    }
}