package Modelo;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Venta {
    private int id;
    private Date fecha;  // Debe incluir fecha y hora
    private double total;
    private double descuento;
    private String metodoPago;
    private double montoRecibido;
    private Cliente cliente;
    private List<Producto> productos;
    
    // Constructores
    public Venta() {
        this.fecha = new Date(); // Fecha y hora actual por defecto
        this.productos = new ArrayList<>();
    }
    
    public Venta(int id, Date fecha, double total, String metodoPago, List<Producto> productos) {
        this.id = id;
        this.fecha = fecha;
        this.total = total;
        this.metodoPago = metodoPago;
        this.productos = productos;
    }
    
    public Venta(String id, Timestamp fecha, double total, String metodoPago, List<Producto> productos) {
        this(Integer.parseInt(id), fecha, total, metodoPago, productos);
        this.fecha = fecha;
        this.total = total;
        this.metodoPago = metodoPago;
        this.productos = productos != null ? productos : new ArrayList<>(); // Garantiza lista no nula
    }
    
    
    // Getters y Setters para todos los campos
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }

	public Timestamp getFechaTimestamp() {
		return new Timestamp(fecha.getTime());
	}

	public void setFechaTimestamp(Timestamp fecha) {
		this.fecha = new Date(fecha.getTime());
	}
    public Date getFecha() {
        return fecha;
    }
    
    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }
    
    public double getTotal() {
        return total;
    }
    
    public void setTotal(double total) {
        this.total = total;
    }
    
    public double getDescuento() {
        return descuento;
    }
    
    public void setDescuento(double descuento) {
        this.descuento = descuento;
    }
    
    public String getMetodoPago() {
        return metodoPago;
    }
    
    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }
    
    public double getMontoRecibido() {
        return montoRecibido;
    }
    
    public void setMontoRecibido(double montoRecibido) {
        this.montoRecibido = montoRecibido;
    }
    
    public Cliente getCliente() {
        return cliente;
    }
    
    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }
    
    public List<Producto> getProductos() {
        return productos;
    }
    
    public void setProductos(List<Producto> productos) {
        this.productos = productos;
    }

	public void agregarProducto(Producto producto) {
		this.productos.add(producto);
	}

	
}