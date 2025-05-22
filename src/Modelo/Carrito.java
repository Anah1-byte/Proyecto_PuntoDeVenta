package Modelo;

import java.util.ArrayList;
import java.util.List;

public class Carrito {
    private List<Producto> carrito = new ArrayList<>();
    private double totalVenta = 0.0;


    public void agregarProducto(String id, String nombre, double precio, int cantidad) {
        // Buscar si el producto ya está en el carrito
        for (Producto item : carrito) {
            if (item.getId().equals(id)) {
                item.setCantidad(item.getCantidad() + cantidad);
                item.setPrecioVenta(precio); // Actualizamos el precio si cambió
                item.setPrecioUnitario(precio); // Mantener sincronizados
                actualizarTotal();
                return;
            }
        }
        
        // Si no existe, crear nuevo producto con el constructor simplificado
        Producto nuevo = new Producto(id, nombre, precio, cantidad);
        carrito.add(nuevo);
        actualizarTotal();
    }

    public void actualizarTotal() {
        double nuevoTotal = 0.0;
        for (Producto item : carrito) {
            if (item != null && item.getPrecioUnitario() >= 0 && item.getCantidad() > 0) {
                nuevoTotal += item.getPrecioUnitario() * item.getCantidad();
            }
        }
        this.totalVenta = nuevoTotal;
    }

    public void cancelarVenta() {
        carrito.clear();
        totalVenta = 0.0;
    }

    public double getTotalVenta() {
        return totalVenta;
    }

    public void setTotalVenta(double total) {
        this.totalVenta = total;
    }

    public List<Producto> getProductos() {
        return new ArrayList<>(carrito); // Devolver una copia para evitar modificaciones externas
    }
    
    public List<Producto> getCarrito() {
        return carrito;
    }
    
    public boolean eliminarProducto(String idProducto) {
        for (Producto producto : carrito) {
            if (producto.getId().equals(idProducto)) {
                carrito.remove(producto);
                actualizarTotal();
                return true;
            }
        }
        return false;
    }
    
    
}