package Modelo;

import java.sql.Timestamp;
import java.util.Date;

import javax.swing.ImageIcon;

public class Producto {
    // Atributos
	 private String id;
    private String nombre;
    private int cantidad;
    private String fecha;
    private double precioUnitario;  // Precio unitario del producto
    private String rutaImagen;      // Ruta de la imagen del producto, si se necesita almacenar una ruta.
    private ImageIcon imagen;       // Objeto ImageIcon para la imagen
	private Usuario usuario;
	public int total;
	private String descripcion;
	private String categoria;
	private String proveedor;
	private int cantidadDisponible;
	private int stockMinimo;
	private int stockMaximo;
	private double precioCompra;
	private double precioVenta;
	private boolean tieneIVA;
	private double descuento;
	private Date fechaIngreso;
	private String estado; // "Activo", "Descontinuado", "Dañado"
    private String imagenPath;
    private String unidadMedida;
    // Constructor
    public Producto(String nombre, int cantidad, String fecha, double precioUnitario, String rutaImagen) {
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.fecha = fecha;
        this.precioUnitario = precioUnitario;
        this.rutaImagen = rutaImagen;
        this.imagen = new ImageIcon(rutaImagen); // Cargar la imagen desde la ruta
    }


    public Producto(String nombre, double precioUnitario, int cantidad) {
        this.nombre = nombre;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
    }

    
    public Producto(Usuario usuario) {
        this.usuario = usuario;
        initComponents(); // método de inicialización
    }


    public Producto(String id, String nombre, String descripcion, String categoria, 
            String proveedor, int cantidadDisponible, int stockMinimo, 
            int stockMaximo, double precioCompra, double precioVenta, 
            boolean tieneIVA, double descuento, Date fechaIngreso, 
            String estado, String imagenPath, String unidadMedida) {
 this.id = id;
 this.nombre = nombre;
 this.descripcion = descripcion;
 this.categoria = categoria;
 this.proveedor = proveedor;
 this.cantidadDisponible = cantidadDisponible;
 this.stockMinimo = stockMinimo;
 this.stockMaximo = stockMaximo;
 this.precioCompra = precioCompra;
 this.precioVenta = precioVenta;
 this.tieneIVA = tieneIVA;
 this.descuento = descuento;
 this.fechaIngreso = fechaIngreso;
 this.estado = estado;
 this.imagenPath = imagenPath;
 this.unidadMedida = unidadMedida;
}


    public Producto(String id, String nombre, double precioVenta, int cantidad) {
        this.id = id;
        this.nombre = nombre;
        this.precioVenta = precioVenta; // Usamos precioVenta como campo principal
        this.precioUnitario = precioVenta; // Mantenemos ambos precios sincronizados
        this.cantidad = cantidad;
    }
	private void initComponents() {
		// TODO Auto-generated method stub
	}

	// Getters y Setters
	
	 public String getId() { return id; }
	    public void setId(String id) { this.id = id; }
	    
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public String getRutaImagen() {
        return rutaImagen;
    }

    public void setRutaImagen(String rutaImagen) {
        this.rutaImagen = rutaImagen;
        this.imagen = new ImageIcon(rutaImagen); // Actualiza la imagen cuando cambia la ruta
    }

    public ImageIcon getImagen() {
        return imagen;
    }

    public double setTotal(double d) {
        return precioUnitario * cantidad;
    }
    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    
    public String getProveedor() { return proveedor; }
    public void setProveedor(String proveedor) { this.proveedor = proveedor; }
    
    public int getCantidadDisponible() { return cantidadDisponible; }
    public void setCantidadDisponible(int cantidadDisponible) { this.cantidadDisponible = cantidadDisponible; }
    
    public int getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }
    
    public int getStockMaximo() { return stockMaximo; }
    public void setStockMaximo(int stockMaximo) { this.stockMaximo = stockMaximo; }
    
    public double getPrecioCompra() { return precioCompra; }
    public void setPrecioCompra(double precioCompra) { this.precioCompra = precioCompra; }
    
    public double getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(double precioVenta) { this.precioVenta = precioVenta; }
    
    public boolean isTieneIVA() { return tieneIVA; }
    public void setTieneIVA(boolean tieneIVA) { this.tieneIVA = tieneIVA; }
    
    public double getDescuento() { return descuento; }
    public void setDescuento(double descuento) { this.descuento = descuento; }
    
    public Date getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(Date fechaIngreso) { this.fechaIngreso = fechaIngreso; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public String getImagenPath() { return imagenPath; }
    public void setImagenPath(String imagenPath) { this.imagenPath = imagenPath; }

    public boolean necesitaReposicion() {
        return cantidadDisponible <= stockMinimo;
    }

    public boolean tieneExcesoStock() {
        return cantidadDisponible > stockMaximo;
    }
    
    public double getPrecioConDescuento() {
        return precioVenta * (1 - descuento / 100);
    }
    
    public double getPrecioConIVA() {
        return tieneIVA ? getPrecioConDescuento() * 1.16 : getPrecioConDescuento();
    }
    
    // Método toString (opcional, para representación legible)
    @Override
    public String toString() {
        return "Producto: " + nombre + ", Cantidad: " + cantidad + ", Precio Unitario: $" + precioUnitario + ", Fecha de vencimiento: " + (fecha.isEmpty() ? "No disponible" : fecha);
    }

	public void setVisible(boolean b) {
		// TODO Auto-generated method stub
	}


	public Timestamp getVersion() {
		// TODO Auto-generated method stub
		return null;
	}

}
