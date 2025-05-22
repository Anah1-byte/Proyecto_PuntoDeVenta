package Modelo;

import ConexionBD.ConexionAccess;

import java.awt.Component;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Inventarioo {

	public List<Producto> obtenerTodosProductos() {
	    List<Producto> productos = new ArrayList<>();
	    // Filtrar solo productos con stock disponible
	    String sql = "SELECT * FROM Productos WHERE cantidad_disponible > 0";
	    
	    try (Connection conn = ConexionAccess.conectar();
	         Statement stmt = conn.createStatement();
	         ResultSet rs = stmt.executeQuery(sql)) {
	        
	        while (rs.next()) {
	            Producto producto = mapearProducto(rs);
	            productos.add(producto);
	        }
	    } catch (SQLException e) {
	        System.err.println("Error al obtener productos: " + e.getMessage());
	    }
	    return productos;
	}

    public boolean agregarProducto(Producto producto) {
        String sql = "INSERT INTO Productos (id, nombre, descripcion, categoria, proveedor, cantidad_disponible, " +
                     "stock_minimo, stock_maximo, precio_compra, precio_venta, tiene_iva, descuento, " +
                     "fecha_ingreso, estado, imagen_path, unidad_medida) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"; // 16 parámetros
        
        try (Connection conn = ConexionAccess.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, producto.getId());
            pstmt.setString(2, producto.getNombre());
            pstmt.setString(3, producto.getDescripcion());
            pstmt.setString(4, producto.getCategoria());
            pstmt.setString(5, producto.getProveedor());
            pstmt.setInt(6, producto.getCantidadDisponible());
            pstmt.setInt(7, producto.getStockMinimo());
            pstmt.setInt(8, producto.getStockMaximo());
            pstmt.setDouble(9, producto.getPrecioCompra());
            pstmt.setDouble(10, producto.getPrecioVenta());
            pstmt.setBoolean(11, producto.isTieneIVA());
            pstmt.setDouble(12, producto.getDescuento());
            
            // Manejo de fecha
            if (producto.getFechaIngreso() != null) {
                pstmt.setDate(13, new java.sql.Date(producto.getFechaIngreso().getTime()));
            } else {
                pstmt.setNull(13, java.sql.Types.DATE);
            }
            
            pstmt.setString(14, producto.getEstado());
            pstmt.setString(15, producto.getImagenPath());
            pstmt.setString(16, producto.getUnidadMedida()); // Unidad de medida
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error al agregar producto: " + e.getMessage());
            return false;
        }
    }
    public boolean actualizarProducto(Producto producto) {
        String sql = "UPDATE Productos SET nombre = ?, descripcion = ?, categoria = ?, proveedor = ?, " +
                     "cantidad_disponible = ?, stock_minimo = ?, stock_maximo = ?, precio_compra = ?, " +
                     "precio_venta = ?, tiene_iva = ?, descuento = ?, fecha_ingreso = ?, estado = ?, " +
                     "imagen_path = ?, unidad_medida = ? WHERE id = ?";
        
        try (Connection conn = ConexionAccess.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, producto.getNombre());
            pstmt.setString(2, producto.getDescripcion());
            pstmt.setString(3, producto.getCategoria());
            pstmt.setString(4, producto.getProveedor());
            pstmt.setInt(5, producto.getCantidadDisponible());
            pstmt.setInt(6, producto.getStockMinimo());
            pstmt.setInt(7, producto.getStockMaximo());
            pstmt.setDouble(8, producto.getPrecioCompra());
            pstmt.setDouble(9, producto.getPrecioVenta());
            pstmt.setBoolean(10, producto.isTieneIVA());
            pstmt.setDouble(11, producto.getDescuento());
            
            // Manejo de fecha
            if (producto.getFechaIngreso() != null) {
                pstmt.setDate(12, new java.sql.Date(producto.getFechaIngreso().getTime()));
            } else {
                pstmt.setNull(12, java.sql.Types.DATE);
            }
            
            pstmt.setString(13, producto.getEstado());
            pstmt.setString(14, producto.getImagenPath());
            pstmt.setString(15, producto.getUnidadMedida()); // Unidad de medida
            pstmt.setString(16, producto.getId()); // ID
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar producto:");
            System.err.println("SQL: " + sql);
            System.err.println("Producto ID: " + producto.getId());
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminarProducto(String id) {
        String sql = "DELETE FROM Productos WHERE id = ?";
        
        try (Connection conn = ConexionAccess.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar producto: " + e.getMessage());
            return false;
        }
    }

    public List<Producto> buscarPorNombre(String nombre) {
        List<Producto> productos = new ArrayList<>();
        // Modificar la consulta para incluir condición de stock
        String sql = "SELECT * FROM Productos WHERE nombre LIKE ? AND cantidad_disponible > 0";
        
        try (Connection conn = ConexionAccess.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + nombre + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                productos.add(mapearProducto(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar por nombre: " + e.getMessage());
        }
        return productos;
    }

    public List<Producto> buscarPorCategoria(String categoria) {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT * FROM Productos WHERE categoria = ?";
        
        try (Connection conn = ConexionAccess.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, categoria);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                productos.add(mapearProducto(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar por categoría: " + e.getMessage());
        }
        return productos;
    }

    public List<Producto> buscarPorProveedor(String proveedor) {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT * FROM Productos WHERE proveedor LIKE ?";
        
        try (Connection conn = ConexionAccess.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + proveedor + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                productos.add(mapearProducto(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar por proveedor: " + e.getMessage());
        }
        return productos;
    }
    	
    public List<Producto> buscarPorEstado(String estado) {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT * FROM Productos WHERE estado = ?";
        
        try (Connection conn = ConexionAccess.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, estado);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                productos.add(mapearProducto(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar por estado: " + e.getMessage());
        }
        return productos;
    }

    public List<Producto> buscarPorRangoPrecio(double min, double max) {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT * FROM Productos WHERE precio_venta BETWEEN ? AND ?";
        
        try (Connection conn = ConexionAccess.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, min);
            pstmt.setDouble(2, max);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                productos.add(mapearProducto(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar por rango de precio: " + e.getMessage());
        }
        return productos;
    }

    public List<Producto> buscarPorRangoStock(int min, int max) {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT * FROM Productos WHERE cantidad_disponible BETWEEN ? AND ?";
        
        try (Connection conn = ConexionAccess.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, min);
            pstmt.setInt(2, max);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                productos.add(mapearProducto(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar por rango de stock: " + e.getMessage());
        }
        return productos;
    }

    public List<Producto> buscarProductosConIVA() {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT * FROM Productos WHERE tiene_iva = true";
        
        try (Connection conn = ConexionAccess.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                productos.add(mapearProducto(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar productos con IVA: " + e.getMessage());
        }
        return productos;
    }

    public List<Producto> buscarProductosSinIVA() {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT * FROM Productos WHERE tiene_iva = false";
        
        try (Connection conn = ConexionAccess.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                productos.add(mapearProducto(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar productos sin IVA: " + e.getMessage());
        }
        return productos;
    }

    public List<Producto> buscarProductosNecesitanReposicion() {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT * FROM Productos WHERE cantidad_disponible <= stock_minimo";
        
        try (Connection conn = ConexionAccess.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                productos.add(mapearProducto(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar productos que necesitan reposición: " + e.getMessage());
        }
        return productos;
    }

    public List<Producto> buscarProductosConExcesoStock() {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT * FROM Productos WHERE cantidad_disponible > stock_maximo";
        
        try (Connection conn = ConexionAccess.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                productos.add(mapearProducto(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar productos con exceso de stock: " + e.getMessage());
        }
        return productos;
    }

    public Producto obtenerProductoPorId(String id) {
        String sql = "SELECT * FROM Productos WHERE id = ?";
        
        try (Connection conn = ConexionAccess.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapearProducto(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener producto por ID: " + e.getMessage());
        }
        return null;
    }

    private Producto mapearProducto(ResultSet rs) throws SQLException {
    	return new Producto(
    		    rs.getString("id"),
    		    rs.getString("nombre"),
    		    rs.getString("descripcion"),
    		    rs.getString("categoria"),
    		    rs.getString("proveedor"),
    		    rs.getInt("cantidad_disponible"),
    		    rs.getInt("stock_minimo"),
    		    rs.getInt("stock_maximo"),
    		    rs.getDouble("precio_compra"),
    		    rs.getDouble("precio_venta"),
    		    rs.getBoolean("tiene_iva"),
    		    rs.getDouble("descuento"),
    		    rs.getDate("fecha_ingreso"),
    		    rs.getString("estado"),
    		    rs.getString("imagen_path"),
    		    rs.getString("unidad_medida")
    		);
    }

    private void setProductoParameters(PreparedStatement pstmt, Producto producto) throws SQLException {
        pstmt.setString(1, producto.getId()); // ID
        pstmt.setString(2, producto.getNombre());
        pstmt.setString(3, producto.getDescripcion());
        pstmt.setString(4, producto.getCategoria());
        pstmt.setString(5, producto.getProveedor());
        pstmt.setInt(6, producto.getCantidadDisponible());
        pstmt.setInt(7, producto.getStockMinimo());
        pstmt.setInt(8, producto.getStockMaximo());
        pstmt.setDouble(9, producto.getPrecioCompra());
        pstmt.setDouble(10, producto.getPrecioVenta());
        pstmt.setBoolean(11, producto.isTieneIVA());
        pstmt.setDouble(12, producto.getDescuento());
        pstmt.setDate(13, new java.sql.Date(producto.getFechaIngreso().getTime()));
        pstmt.setString(14, producto.getEstado());
        pstmt.setString(15, producto.getImagenPath());
        pstmt.setString(16, producto.getUnidadMedida());
        
    }

    
    public List<Producto> buscarPorFecha(java.util.Date desde, java.util.Date hasta) {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT * FROM Productos WHERE fecha_ingreso BETWEEN ? AND ?";
        
        try (Connection conn = ConexionAccess.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDate(1, new java.sql.Date(desde.getTime()));
            pstmt.setDate(2, new java.sql.Date(hasta.getTime()));
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                productos.add(mapearProducto(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar por fecha: " + e.getMessage());
        }
        return productos;
    }
    
    // Método para obtener todas las categorías
    public List<String> obtenerTodasCategorias() {
        List<String> categorias = new ArrayList<>();
        
        try {
            // Primero verifica si la tabla existe
            if (!existeTabla("Categorias")) {
                crearTablaCategorias(); //crear la tabla si no existe
            }
            
            String sql = "SELECT nombre FROM Categorias ORDER BY nombre";
            try (Connection conn = ConexionAccess.conectar();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                while (rs.next()) {
                    categorias.add(rs.getString("nombre"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error crítico al obtener categorías: " + e.getMessage());
            // Categorías por defecto
            categorias.add("General");
            categorias.add("Alimentos");
            categorias.add("Bebidas");
        }
        return categorias;
    }

    private boolean existeTabla(String nombreTabla) throws SQLException {
        try (Connection conn = ConexionAccess.conectar()) {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(null, null, nombreTabla, null);
            return tables.next();
        }
    }

    private void crearTablaCategorias() {
        String sql = "CREATE TABLE Categorias (" +
                     "id AUTOINCREMENT PRIMARY KEY, " +
                     "nombre VARCHAR(100) NOT NULL, " +
                     "descripcion VARCHAR(255))";
        
        try (Connection conn = ConexionAccess.conectar();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            // Insertar categorías básicas
            insertarCategoriaInicial("General", "Productos generales");
            insertarCategoriaInicial("Alimentos", "Productos alimenticios");
        } catch (SQLException e) {
            System.err.println("Error al crear tabla Categorias: " + e.getMessage());
        }
    }

    private void insertarCategoriaInicial(String nombre, String descripcion) {
        String sql = "INSERT INTO Categorias (nombre, descripcion) VALUES (?, ?)";
        try (Connection conn = ConexionAccess.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nombre);
            pstmt.setString(2, descripcion);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al insertar categoría inicial: " + e.getMessage());
        }
    }
    
    // agregar nueva categoría
    public boolean agregarCategoria(String nombre, String descripcion) {
        String sql = "INSERT INTO Categorias (nombre, descripcion) VALUES (?, ?)";
        
        try (Connection conn = ConexionAccess.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, nombre);
            pstmt.setString(2, descripcion);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al agregar categoría: " + e.getMessage());
            return false;
        }
    }
    //eliminar categoría
    public boolean eliminarCategoria(String nombre) {
        String sql = "DELETE FROM Categorias WHERE nombre = ?";
        try (Connection conn = ConexionAccess.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nombre);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar categoría: " + e.getMessage());
            return false;
        }
    }
    
    public List<Producto> buscarPorDescripcion(String descripcion) {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT * FROM Productos WHERE descripcion LIKE ?";
        
        try (Connection conn = ConexionAccess.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + descripcion + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                productos.add(mapearProducto(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar por descripción: " + e.getMessage());
        }
        return productos;
    }
    
    public Producto obtenerProductoPorNombre(String nombre) {
        String sql = "SELECT * FROM Productos WHERE nombre = ?";
        try (Connection conn = ConexionAccess.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nombre);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapearProducto(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar producto por nombre: " + e.getMessage());
        }
        return null;
    }
    
    public int obtenerIdProductoPorNombre(String nombre) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = ConexionAccess.conectar();
            String sql = "SELECT id FROM Productos WHERE nombre = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, nombre);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("id");
            }
            return -1; // Retorna -1 si no encuentra el producto
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        } finally {
            // Cerrar recursos (rs, pstmt, conn)
        }
    }

    public boolean actualizarStock(String idProducto, int cantidad) {
        String sql = "UPDATE Productos SET cantidad_disponible = cantidad_disponible + ? WHERE id = ?";
        
        try (Connection conn = ConexionAccess.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, cantidad);
            pstmt.setString(2, idProducto);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar stock: " + e.getMessage());
            return false;
        }
    }
    public int obtenerStockProducto(int idProducto) throws SQLException {
        String sql = "SELECT cantidad_disponible FROM Productos WHERE id = ?";
        
        try (Connection conn = ConexionAccess.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idProducto);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cantidad_disponible");
                }
            }
        }
        throw new SQLException("Producto no encontrado");
    }
    
    public Producto obtenerProductoConBloqueo(String nombre) throws SQLException {
        // Consulta  para Access
        String sql = "SELECT * FROM Productos WHERE nombre = ?";
        
        try (Connection conn = ConexionAccess.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, nombre);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapearProducto(rs);
            }
        }
        return null;
    }
    
    public boolean reservarProducto(String idProducto, int cantidad) throws SQLException {
        String sql = "UPDATE Productos SET cantidad_disponible = cantidad_disponible - ? " +
                    "WHERE id = ? AND cantidad_disponible >= ?";
        
        try (Connection conn = ConexionAccess.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, cantidad);
            pstmt.setString(2, idProducto);
            pstmt.setInt(3, cantidad);
            
            return pstmt.executeUpdate() > 0;
        }
    }

    public List<Producto> buscarProductosSinStock() {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT * FROM Productos WHERE cantidad_disponible = 0";
        
        try (Connection conn = ConexionAccess.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Producto producto = mapearProducto(rs);
                productos.add(producto);
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar productos sin stock: " + e.getMessage());
        }
        return productos;
    }
    
    
}