package Modelo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import ConexionBD.ConexionAccess;

public class Usuarioo {
    private Connection conn;

    public Usuarioo() {
        // Inicializar conexión
        conn = ConexionAccess.conectar();
        crearTablaSiNoExiste();
    }

    private void crearTablaSiNoExiste() {
        try {
            // Verificar si la tabla existe
            boolean tablaExiste = false;
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet tables = meta.getTables(null, null, "Usuarios", new String[]{"TABLE"})) {
                tablaExiste = tables.next();
            }

            if (!tablaExiste) {
                try (Statement stmt = conn.createStatement()) {
                    // Crear tabla
                    stmt.execute("CREATE TABLE Usuarios (" +
                        "username VARCHAR(50) PRIMARY KEY, " +
                        "password VARCHAR(50) NOT NULL, " +
                        "rol VARCHAR(20) NOT NULL)");
                    
                    // Insertar admin por defecto
                    stmt.execute("INSERT INTO Usuarios (username, password, rol) VALUES " +
                        "('admin', 'admin123', 'Admin')");
                    
                    System.out.println("Tabla Usuarios creada e inicializada");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error crítico al inicializar tabla Usuarios: ");
            e.printStackTrace();
            // Puedes agregar aquí un JOptionPane.showMessageDialog para informar al usuario
            throw new RuntimeException("No se pudo inicializar la tabla Usuarios", e);
        }
    }

    private boolean existeUsuario(String username) {
        String sql = "SELECT username FROM Usuarios WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            return pstmt.executeQuery().next();
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean autenticarUsuario(String username, String password) {
        String sql = "SELECT * FROM Usuarios WHERE username = ? AND password = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            return pstmt.executeQuery().next();
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean registrarUsuario(String username, String password, String rol) {
        String sql = "INSERT INTO Usuarios (username, password, rol) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, rol);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public Usuario obtenerUsuario(String username) {
        String sql = "SELECT * FROM Usuarios WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Usuario(
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("rol")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener usuario: " + e.getMessage());
        }
        return null;
    }

    public List<Usuario> obtenerTodosUsuarios() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM Usuarios";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                usuarios.add(new Usuario(
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("rol")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener usuarios: " + e.getMessage());
        }
        return usuarios;
    }

    public boolean actualizarRol(String username, String nuevoRol) {
        String sql = "UPDATE Usuarios SET rol = ? WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nuevoRol);
            pstmt.setString(2, username);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean eliminarUsuario(String username) {
        String sql = "DELETE FROM Usuarios WHERE username = ? AND username <> 'admin'";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }
    
    public String obtenerRolUsuario(String username) {
        String sql = "SELECT rol FROM Usuarios WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("rol");
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener rol: " + e.getMessage());
        }
        return null; // Retorna null si no encuentra el usuario o hay error
    }
}