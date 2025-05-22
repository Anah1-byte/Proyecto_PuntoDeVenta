package Modelo;

import ConexionBD.ConexionAccess;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Devolucionn {
    public boolean registrarDevolucion(Devolucion devolucion) {
        String sql = "INSERT INTO Devoluciones (id, id_producto, nombre_producto, cantidad, tipo, motivo, " +
                     "fecha, estado, observaciones, id_transaccion_original, id_usuario) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = ConexionAccess.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, devolucion.getId());
            pstmt.setString(2, devolucion.getIdProducto());
            pstmt.setString(3, devolucion.getNombreProducto());
            pstmt.setInt(4, devolucion.getCantidad());
            pstmt.setString(5, devolucion.getTipo());
            pstmt.setString(6, devolucion.getMotivo());
            pstmt.setDate(7, new java.sql.Date(devolucion.getFecha().getTime()));
            pstmt.setString(8, devolucion.getEstado());
            pstmt.setString(9, devolucion.getObservaciones());
            pstmt.setString(10, devolucion.getIdTransaccionOriginal());
            pstmt.setString(11, devolucion.getIdUsuario());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al registrar devolución: " + e.getMessage());
            return false;
        }
    }
    
    public List<Devolucion> obtenerTodasDevoluciones() {
        List<Devolucion> devoluciones = new ArrayList<>();
        String sql = "SELECT * FROM Devoluciones ORDER BY fecha DESC";
        
        try (Connection conn = ConexionAccess.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                devoluciones.add(mapearDevolucion(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener devoluciones: " + e.getMessage());
        }
        return devoluciones;
    }
    
    public boolean actualizarEstadoDevolucion(String id, String nuevoEstado) {
        String sql = "UPDATE Devoluciones SET estado = ? WHERE id = ?";
        
        try (Connection conn = ConexionAccess.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, nuevoEstado);
            pstmt.setString(2, id);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar estado de devolución: " + e.getMessage());
            return false;
        }
    }
    
    private Devolucion mapearDevolucion(ResultSet rs) throws SQLException {
        return new Devolucion(
            rs.getString("id"),
            rs.getString("id_producto"),
            rs.getString("nombre_producto"),
            rs.getInt("cantidad"),
            rs.getString("tipo"),
            rs.getString("motivo"),
            rs.getDate("fecha"),
            rs.getString("estado"),
            rs.getString("observaciones"),
            rs.getString("id_transaccion_original"),
            rs.getString("id_usuario")
        );
    }
    
    public boolean crearTablaDevoluciones() {
        String sql = "CREATE TABLE Devoluciones (" +
                     "id VARCHAR(50) PRIMARY KEY, " +
                     "id_producto VARCHAR(50) NOT NULL, " +
                     "nombre_producto VARCHAR(100) NOT NULL, " +
                     "cantidad INTEGER NOT NULL, " +
                     "tipo VARCHAR(20) NOT NULL, " +
                     "motivo VARCHAR(100) NOT NULL, " +
                     "fecha DATETIME NOT NULL, " +
                     "estado VARCHAR(20) NOT NULL, " +
                     "observaciones VARCHAR(255), " +
                     "id_transaccion_original VARCHAR(50), " +
                     "id_usuario VARCHAR(50) NOT NULL)";
        
        try (Connection conn = ConexionAccess.conectar();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            return true;
        } catch (SQLException e) {
            System.err.println("Error al crear tabla Devoluciones: " + e.getMessage());
            return false;
        }
    }
}