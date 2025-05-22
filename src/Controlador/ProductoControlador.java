package Controlador;

import Modelo.Producto;
import java.util.ArrayList;
import java.util.List;

public class ProductoControlador {

    private List<Producto> productos;

    // Constructor
    public ProductoControlador() {
        this.productos = new ArrayList<>();
    }

    // Método para agregar un producto
    public void agregarProducto(String nombre, int cantidad, String fecha, double precioUnitario, String rutaImagen) {
        Producto nuevoProducto = new Producto(nombre, cantidad, fecha, precioUnitario, rutaImagen);
        productos.add(nuevoProducto);
    }

    // Método para eliminar un producto (por nombre)
    public boolean eliminarProducto(String nombre) {
        for (Producto producto : productos) {
            if (producto.getNombre().equalsIgnoreCase(nombre)) {
                productos.remove(producto);
                return true;
            }
        }
        return false;
    }

    // Método para modificar un producto (por nombre)
    public boolean modificarProducto(String nombre, int nuevaCantidad, String nuevaFecha, double nuevoPrecioUnitario, String nuevaRutaImagen) {
        for (Producto producto : productos) {
            if (producto.getNombre().equalsIgnoreCase(nombre)) {
                producto.setCantidad(nuevaCantidad);
                producto.setFecha(nuevaFecha);
                producto.setPrecioUnitario(nuevoPrecioUnitario);
                producto.setRutaImagen(nuevaRutaImagen);
                return true;
            }
        }
        return false;
    }

    // Método para obtener todos los productos
    public List<Producto> obtenerProductos() {
        return productos;
    }

    // Método para obtener un producto por su nombre
    public Producto obtenerProductoPorNombre(String nombre) {
        for (Producto producto : productos) {
            if (producto.getNombre().equalsIgnoreCase(nombre)) {
                return producto;
            }
        }
        return null; // Si no se encuentra el producto
    }

	public void mostrarProductos() {
		// TODO Auto-generated method stub
		
		
	}

	public boolean actualizarProducto(Producto producto) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean agregarProducto(Producto producto) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
}
