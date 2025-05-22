package Modelo;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import ConexionBD.ConexionAccess;

public class Inventario {
    private Inventarioo inventarioo;
    private List<String> categorias;

    public Inventario() {
        this.inventarioo = new Inventarioo();
        inicializarCategorias();
    }

    private void inicializarCategorias() {
        categorias = new ArrayList<>();
        categorias.add("TODOS");
        categorias.add("Abarrotes");
        categorias.add("Panaderia y Tortilleria");
        categorias.add("Carnes y embutidos");
        categorias.add("Botanas y dulces");
        categorias.add("Bebidas");
        categorias.add("Lacteos");
        categorias.add("Limpieza del hogar");
        categorias.add("Cuidado personal");
    }

    public boolean agregarProducto(Producto producto) {
        return inventarioo.agregarProducto(producto);
    }

    public boolean eliminarProducto(String id) {
        return inventarioo.eliminarProducto(id);
    }

    public boolean actualizarProducto(Producto producto) {
        return inventarioo.actualizarProducto(producto);
    }

    public List<Producto> buscarPorNombre(String nombre) {
        return inventarioo.buscarPorNombre(nombre);
    }

    public List<Producto> buscarPorCategoria(String categoria) {
        if (categoria.equalsIgnoreCase("TODOS")) {
            return inventarioo.obtenerTodosProductos();
        }
        return inventarioo.buscarPorCategoria(categoria);
    }

    public List<Producto> buscarPorProveedor(String proveedor) {
        return inventarioo.buscarPorProveedor(proveedor);
    }

    public List<Producto> buscarPorEstado(String estado) {
        return inventarioo.buscarPorEstado(estado);
    }

    public List<Producto> buscarPorRangoPrecio(double min, double max) {
        return inventarioo.buscarPorRangoPrecio(min, max);
    }

    public List<Producto> buscarPorRangoStock(int min, int max) {
        return inventarioo.buscarPorRangoStock(min, max);
    }

    public List<Producto> buscarProductosConIVA() {
        return inventarioo.buscarProductosConIVA();
    }

    public List<Producto> buscarProductosSinIVA() {
        return inventarioo.buscarProductosSinIVA();
    }

    public List<Producto> buscarProductosNecesitanReposicion() {
        return inventarioo.buscarProductosNecesitanReposicion();
    }

    public List<Producto> buscarProductosConExcesoStock() {
        return inventarioo.buscarProductosConExcesoStock();
    }

    public List<Producto> getProductos() {
        return inventarioo.obtenerTodosProductos();
    }

    public List<String> getCategorias() {
        List<String> categorias = new ArrayList<>();
        String sql = "SELECT DISTINCT categoria FROM Productos";
        
        try (Connection conn = ConexionAccess.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                categorias.add(rs.getString("categoria"));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener categorías: " + e.getMessage());
        }
        
        // Ordenar alfabéticamente
        Collections.sort(categorias);
        return categorias;
    }

    public Producto getProductoPorId(String id) {
        return inventarioo.obtenerProductoPorId(id);
    }
    
    public List<Producto> getProductosBajoStock() {
        return inventarioo.buscarProductosNecesitanReposicion();
    }
    
    public List<Producto> getProductosSobreStock() {
        return inventarioo.buscarProductosConExcesoStock();
    }
    public List<Producto> buscarPorFecha(Date desde, Date hasta) {
        return inventarioo.buscarPorFecha(desde, hasta);
    }

	public List<Producto> buscarPorDescripcion(String descripcion) {
		return inventarioo.buscarPorDescripcion(descripcion);
	}
}