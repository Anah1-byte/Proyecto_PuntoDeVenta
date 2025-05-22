package Controlador;

import Modelo.Devolucion;
import Modelo.Devolucionn;
import Modelo.Inventario;
import Modelo.Inventarioo;
import Modelo.Producto;
import Vista.inventario;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ConexionBD.ConexionAccess;

public class ControladorInventario {
    private Inventario modelo;
    private inventario vista;
    private Inventarioo inventarioo;
    private Devolucionn devolucionn;
    
    public ControladorInventario(inventario vista) {
        this.modelo = new Inventario();
        this.inventarioo = new Inventarioo();
        this.devolucionn = new Devolucionn();
        this.vista = vista;
        if (vista != null) {  // Verificar si la vista no es null
            vista.setControlador(this);
        }
        verificarTablaDevoluciones();
    }
    
    // Métodos para manejar productos
    public boolean agregarProducto(Producto producto) {
        return modelo.agregarProducto(producto);
    }

    public boolean eliminarProducto(String id) {
        return modelo.eliminarProducto(id);
    }

    public boolean actualizarProducto(Producto producto) {
        return modelo.actualizarProducto(producto);
    }

    // Métodos de búsqueda
    public List<Producto> buscarPorNombre(String nombre) {
        return modelo.buscarPorNombre(nombre);
    }

    public List<Producto> buscarPorCategoria(String categoria) {
        return modelo.buscarPorCategoria(categoria);
    }

    public List<String> getCategorias() {
        // Verifica que realmente esté consultando la base de datos
        System.out.println("Consultando categorías desde BD..."); // Debug
        List<String> categorias = inventarioo.obtenerTodasCategorias();
        System.out.println("Categorías obtenidas: " + categorias); // Debug
        return categorias;
    }
    
    public boolean agregarCategoria(String nombre, String descripcion) {
        return inventarioo.agregarCategoria(nombre, descripcion);
    }

    public boolean eliminarCategoria(String nombre) {
        return inventarioo.eliminarCategoria(nombre);
    }

    public List<Producto> buscarPorProveedor(String proveedor) {
        return modelo.buscarPorProveedor(proveedor);
    }

    public List<Producto> buscarPorEstado(String estado) {
        return modelo.buscarPorEstado(estado);
    }

    public List<Producto> buscarPorRangoPrecio(double min, double max) {
        return modelo.buscarPorRangoPrecio(min, max);
    }

    public List<Producto> buscarPorRangoStock(int min, int max) {
        return modelo.buscarPorRangoStock(min, max);
    }

    public List<Producto> buscarPorFecha(Date desde, Date hasta) {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Las fechas no pueden ser nulas");
        }
        return modelo.buscarPorFecha(desde, hasta);
    }

    public List<Producto> buscarProductosConIVA() {
        return modelo.buscarProductosConIVA();
    }

    public List<Producto> buscarProductosSinIVA() {
        return modelo.buscarProductosSinIVA();
    }

    public List<Producto> buscarProductosNecesitanReposicion() {
        return modelo.buscarProductosNecesitanReposicion();
    }

    public List<Producto> buscarProductosConExcesoStock() {
        return modelo.buscarProductosConExcesoStock();
    }

  
    public void mostrarProductosDeCategoria(String categoria) {
        vista.mostrarProductos(modelo.buscarPorCategoria(categoria));
    }

    public void mostrarDetalleProducto(String id) {
        Producto producto = modelo.getProductoPorId(id);
        if (producto != null) {
            vista.mostrarDetalleProducto(producto);
        }
    }

    public List<Producto> getTodosProductos() {
        return modelo.getProductos();
    }
    
    public List<Producto> getProductosBajoStock() {
        return modelo.getProductosBajoStock();
    }
    
    public List<Producto> getProductosSobreStock() {
        return modelo.getProductosSobreStock();
    }
    
    public Producto getProductoPorId(String id) {
        return modelo.getProductoPorId(id);
    }
    
    public List<Producto> buscarPorDescripcion(String descripcion) {
        return modelo.buscarPorDescripcion(descripcion);
    }
    
    public List<Producto> buscarProductosMultiCriterio(String texto) {
        List<Producto> resultados = new ArrayList<>();
        
        // 1. Buscar por ID exacto
        Producto producto = inventarioo.obtenerProductoPorId(texto);
        if (producto != null) {
            resultados.add(producto);
            return resultados; // Si encontró por ID, no busca más
        }
        
        // 2. Buscar por nombre
        resultados = inventarioo.buscarPorNombre(texto);
        
        // 3. Si no hay resultados, buscar por descripción
        if (resultados.isEmpty()) {
            resultados = inventarioo.buscarPorDescripcion(texto);
        }
        
        // 4. Si no hay resultados, buscar por categoría
        if (resultados.isEmpty()) {
            resultados = inventarioo.buscarPorCategoria(texto);
        }
        
        return resultados;
    }
    
    private void verificarTablaDevoluciones() {
        try (Connection conn = ConexionAccess.conectar()) {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(null, null, "Devoluciones", null);
            if (!tables.next()) {
                devolucionn.crearTablaDevoluciones();
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar tabla Devoluciones: " + e.getMessage());
        }
    }
    public boolean registrarDevolucion(Devolucion devolucion) {
        boolean exito = devolucionn.registrarDevolucion(devolucion);
        
        if (exito) {
            // Actualizar el stock del producto según el tipo de devolución
            if (devolucion.getTipo().equals("CLIENTE")) {
                // Devolución de cliente: aumentar stock
                inventarioo.actualizarStock(devolucion.getIdProducto(), devolucion.getCantidad());
            } else if (devolucion.getTipo().equals("PROVEEDOR")) {
                // Devolución a proveedor: disminuir stock
                inventarioo.actualizarStock(devolucion.getIdProducto(), -devolucion.getCantidad());
            }
        }
        
        return exito;
    }

    public List<Devolucion> obtenerDevoluciones() {
        return devolucionn.obtenerTodasDevoluciones();
    }

    public boolean actualizarEstadoDevolucion(String id, String nuevoEstado) {
        return devolucionn.actualizarEstadoDevolucion(id, nuevoEstado);
    }
}