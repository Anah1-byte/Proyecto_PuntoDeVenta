package Modelo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import ConexionBD.ConexionAccess;

public class ClienteImpl implements Clientee {

    @Override
    public List<Cliente> obtenerTodos() {
        List<Cliente> clientes = new ArrayList<>();
        String query = "SELECT * FROM clientes WHERE fecha_eliminacion IS NULL";

        try (Connection conn = ConexionAccess.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Cliente cliente = new Cliente(
                    rs.getString("id"),
                    rs.getString("telefono"),
                    rs.getString("nombre"),
                    rs.getString("ultima_compra"),
                    rs.getInt("puntos"),
                    rs.getDate("fecha_registro"),
                    rs.getDate("fecha_eliminacion")
                );
                clientes.add(cliente);
            }
        } catch (SQLException e) {
            System.err.println("⚠️ Error al cargar clientes: " + e.getMessage());
        }
        return clientes;
    }

    @Override
    public Cliente buscarPorId(String id) {
        String query = "SELECT * FROM clientes WHERE id = ?";
        try (Connection conn = ConexionAccess.conectar();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Cliente(
                    rs.getString("id"),
                    rs.getString("telefono"),
                    rs.getString("nombre"),
                    rs.getString("ultima_compra"),
                    rs.getInt("puntos"),
                    rs.getDate("fecha_registro"),
                    rs.getDate("fecha_eliminacion")
                );
            }
        } catch (SQLException e) {
            System.err.println("⚠️ Error al buscar cliente por ID: " + e.getMessage());
        }
        return null;
    }

    @Override
    public Cliente buscarPorTelefono(String telefono) {
        String query = "SELECT * FROM clientes WHERE telefono = ? AND fecha_eliminacion IS NULL";
        try (Connection conn = ConexionAccess.conectar();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, telefono);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Cliente(
                    rs.getString("id"),
                    rs.getString("telefono"),
                    rs.getString("nombre"),
                    rs.getString("ultima_compra"),
                    rs.getInt("puntos"),
                    rs.getDate("fecha_registro"),
                    rs.getDate("fecha_eliminacion")
                );
            }
        } catch (SQLException e) {
            System.err.println("⚠️ Error al buscar cliente por teléfono: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void agregarCliente(Cliente cliente) {
        String sql = "INSERT INTO clientes (id, telefono, nombre, ultima_compra, puntos, fecha_registro) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionAccess.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Usar Timestamp para obtener la fecha y hora actual
            java.sql.Timestamp fechaRegistro = new java.sql.Timestamp(cliente.getFechaRegistro().getTime());

            // Manejar fecha de última compra (puede ser null)
            java.sql.Date ultimaCompra = null;
            if (cliente.getUltimaCompra() != null && !cliente.getUltimaCompra().isEmpty()) {
                ultimaCompra = java.sql.Date.valueOf(cliente.getUltimaCompra());
            }

            pstmt.setString(1, cliente.getId());
            pstmt.setString(2, cliente.getTelefono());
            pstmt.setString(3, cliente.getNombre());
            pstmt.setDate(4, ultimaCompra);
            pstmt.setInt(5, cliente.getPuntos());
            pstmt.setTimestamp(6, fechaRegistro); // Establecer Timestamp con fecha y hora

            int rowsAffected = pstmt.executeUpdate();

            // Solo mostrar mensaje si no se agregó correctamente
            if (rowsAffected > 0) {
                //  mensaje de éxito, usa:
                // JOptionPane.showMessageDialog(null, "Cliente agregado exitosamente");
            }
        } catch (Exception e) {
            e.printStackTrace(); // Imprimir el error en consola
            JOptionPane.showMessageDialog(null, "Error al guardar: " + e.getMessage()); // Solo mostrar en caso de error
        }
    }


    @Override
    public void actualizarCliente(Cliente cliente) {
        String query = "UPDATE clientes SET telefono = ?, nombre = ?, ultima_compra = ?, puntos = ? WHERE id = ?";

        try (Connection conn = ConexionAccess.conectar();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, cliente.getTelefono());
            pstmt.setString(2, cliente.getNombre());
            pstmt.setString(3, cliente.getUltimaCompra());
            pstmt.setInt(4, cliente.getPuntos());
            pstmt.setString(5, cliente.getId());

            pstmt.executeUpdate();
            System.out.println("✅ Cliente actualizado.");
        } catch (SQLException e) {
            System.err.println("❌ Error al actualizar cliente: " + e.getMessage());
            throw new RuntimeException("Error en la base de datos", e);
        }
    }

    @Override
    public void eliminarCliente(String id) {
        String query = "UPDATE clientes SET fecha_eliminacion = ? WHERE id = ?"; // Borrado lógico

        try (Connection conn = ConexionAccess.conectar();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setDate(1, new java.sql.Date(System.currentTimeMillis()));
            pstmt.setString(2, id);

            pstmt.executeUpdate();
            System.out.println("✅ Cliente marcado como eliminado.");
        } catch (SQLException e) {
            System.err.println("❌ Error al eliminar cliente: " + e.getMessage());
            throw new RuntimeException("Error en la base de datos", e);
        }
    }
    
    public void verificarYEstructurarTabla() {
        try (Connection conn = ConexionAccess.conectar()) {
            // Verificar si la tabla existe
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(null, null, "CLIENTES", new String[] {"TABLE"});
            
            if (!tables.next()) {
                // La tabla no existe, crearla
                crearTablaClientes(conn);
            } else {
                // La tabla existe, verificar estructura
                verificarEstructuraTabla(conn);
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar tabla CLIENTES: " + e.getMessage());
            throw new RuntimeException("Error crítico con la tabla CLIENTES", e);
        }
    }

    private void crearTablaClientes(Connection conn) throws SQLException {
        String sql = "CREATE TABLE CLIENTES (" +
                     "id VARCHAR(50) PRIMARY KEY, " +
                     "telefono VARCHAR(15) NOT NULL, " +
                     "nombre VARCHAR(100) NOT NULL, " +
                     "ultima_compra VARCHAR(20), " +
                     "puntos INTEGER DEFAULT 0, " +
                     "fecha_registro DATETIME NOT NULL, " +
                     "fecha_eliminacion DATETIME)";
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("✅ Tabla CLIENTES creada exitosamente");
        }
    }

    private void verificarEstructuraTabla(Connection conn) throws SQLException {
        // Verificar que las columnas necesarias existan
        String[] columnasRequeridas = {"id", "telefono", "nombre", "ultima_compra", "puntos", "fecha_registro", "fecha_eliminacion"};
        
        DatabaseMetaData meta = conn.getMetaData();
        ResultSet columnas = meta.getColumns(null, null, "CLIENTES", null);
        
        List<String> columnasExistentes = new ArrayList<>();
        while (columnas.next()) {
            columnasExistentes.add(columnas.getString("COLUMN_NAME").toLowerCase());
        }
        
        for (String columna : columnasRequeridas) {
            if (!columnasExistentes.contains(columna.toLowerCase())) {
                throw new SQLException("La columna " + columna + " no existe en la tabla CLIENTES");
            }
        }
    }
}