package Modelo;

import ConexionBD.ConexionAccess;
import Vista.proveedores;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Proveedorr {

    public Proveedorr() {
    }
    
    public boolean agregarProveedor(Proveedor proveedor) {
        String sql = "INSERT INTO Proveedores (ID, Nombre, Telefono, direccion, producto_suministrado, ultima_visita) VALUES (?, ?, ?, ?, ?, ?)";
        
		
		// Verificar si la tabla existe
        try (Connection conn = ConexionAccess.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String idProveedor = proveedor.getId();
            if (idProveedor == null || idProveedor.isEmpty()) {
                idProveedor = "PRV-" + System.currentTimeMillis();
            }

            pstmt.setString(1, idProveedor);
            pstmt.setString(2, proveedor.getNombre());
            pstmt.setString(3, proveedor.getTelefono());
            pstmt.setString(4, proveedor.getDireccion() != null ? proveedor.getDireccion() : "");
            pstmt.setString(5, proveedor.getProductoSuministrado() != null ? proveedor.getProductoSuministrado() : "");
            
            if (proveedor.getUltimaVisita() != null) {
                pstmt.setTimestamp(6, proveedor.getUltimaVisita());
            } else {
                pstmt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            }

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                proveedor.setId(idProveedor);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error detallado al agregar proveedor:");
            System.err.println("SQL: " + sql);
            System.err.println("Mensaje: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public List<Proveedor> obtenerTodosProveedores() {
        List<Proveedor> proveedores = new ArrayList<>();
        String query = "SELECT * FROM proveedores";
        
        try (Connection conn = ConexionAccess.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Timestamp ultimaVisita = rs.getTimestamp("ultima_visita");
                if (ultimaVisita == null) {
                    ultimaVisita = new Timestamp(System.currentTimeMillis());
                }

                Proveedor p = new Proveedor(
                    rs.getString("id"),
                    rs.getString("nombre"),
                    rs.getString("telefono"),
                    rs.getString("direccion"),
                    rs.getString("producto_suministrado"),
                    ultimaVisita
                );
                proveedores.add(p);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener proveedores: " + e.getMessage());
            e.printStackTrace();
        }
        return proveedores;
    }
    


    public Proveedor buscarProveedorPorId(String id) {
        String query = "SELECT * FROM proveedores WHERE id = ?";
        
        try (Connection conn = ConexionAccess.conectar();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Proveedor(
                        rs.getString("id"),
                        rs.getString("nombre"),
                        rs.getString("telefono"),
                        rs.getString("direccion"),
                        rs.getString("producto_suministrado"),
                        rs.getTimestamp("ultima_visita")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar proveedor: " + e.getMessage());
        }
        return null;
    }
    
    public boolean actualizarProveedor(Proveedor proveedor) {
        String sql = "UPDATE proveedores SET nombre = ?, telefono = ?, direccion = ?, producto_suministrado = ?, ultima_visita = ? WHERE id = ?";
        
        // Obtener nueva conexión para esta operación
        try (Connection conn = ConexionAccess.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, proveedor.getNombre());
            pstmt.setString(2, proveedor.getTelefono());
            pstmt.setString(3, proveedor.getDireccion());
            pstmt.setString(4, proveedor.getProductoSuministrado());
            pstmt.setTimestamp(5, proveedor.getUltimaVisita());
            pstmt.setString(6, proveedor.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error al actualizar proveedor: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminarProveedor(String id) {
        String sql = "DELETE FROM proveedores WHERE id = ?";
        
        try (Connection conn = ConexionAccess.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, id);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error al eliminar proveedor: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public List<Proveedor> obtenerProveedoresConVisitaReciente() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        Timestamp haceUnMes = new Timestamp(cal.getTimeInMillis());
        
        String query = "SELECT * FROM proveedores WHERE ultima_visita >= ?";
        
        List<Proveedor> proveedores = new ArrayList<>();
        try (Connection conn = ConexionAccess.conectar();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setTimestamp(1, haceUnMes);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    proveedores.add(new Proveedor(
                        rs.getString("id"),
                        rs.getString("nombre"),
                        rs.getString("telefono"),
                        rs.getString("direccion"),
                        rs.getString("producto_suministrado"),
                        rs.getTimestamp("ultima_visita")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener proveedores con visita reciente: " + e.getMessage());
        }
        return proveedores;
    }

    public List<Proveedor> obtenerProveedoresSinVisitaReciente() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        Timestamp haceUnMes = new Timestamp(cal.getTimeInMillis());
        
        String query = "SELECT * FROM proveedores WHERE ultima_visita IS NULL OR ultima_visita < ?";
        
        List<Proveedor> proveedores = new ArrayList<>();
        try (Connection conn = ConexionAccess.conectar();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setTimestamp(1, haceUnMes);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    proveedores.add(new Proveedor(
                        rs.getString("id"),
                        rs.getString("nombre"),
                        rs.getString("telefono"),
                        rs.getString("direccion"),
                        rs.getString("producto_suministrado"),
                        rs.getTimestamp("ultima_visita")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener proveedores sin visita reciente: " + e.getMessage());
        }
        return proveedores;
    }

    public List<Proveedor> buscarProveedoresPorProducto(String producto) {
        String query = "SELECT * FROM proveedores WHERE producto_suministrado LIKE ?";
        
        List<Proveedor> proveedores = new ArrayList<>();
        try (Connection conn = ConexionAccess.conectar();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, "%" + producto + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    proveedores.add(new Proveedor(
                        rs.getString("id"),
                        rs.getString("nombre"),
                        rs.getString("telefono"),
                        rs.getString("direccion"),
                        rs.getString("producto_suministrado"),
                        rs.getTimestamp("ultima_visita")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar proveedores por producto: " + e.getMessage());
        }
        return proveedores;
    }
}