package ConexionBD;

import java.sql.*;

public class TestConexion {
    public static void main(String[] args) {
        try (Connection conn = ConexionAccess.conectar()) {
            System.out.println("✅ Conexión exitosa a Access!");

            // Consultar clientes
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM clientes");

            while (rs.next()) {
                System.out.println(
                    "Cliente: " + rs.getString("nombre") + 
                    " | Tel: " + rs.getString("telefono") +
                    " | Puntos: " + rs.getInt("puntos")
                );
            }

        } catch (SQLException e) {
            System.err.println("❌ Error en la conexión: " + e.getMessage());
        }
    }
}