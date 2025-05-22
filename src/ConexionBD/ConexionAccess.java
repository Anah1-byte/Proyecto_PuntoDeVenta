package ConexionBD;

import java.sql.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConexionAccess {
    private static final Logger logger = Logger.getLogger(ConexionAccess.class.getName());
    private static final String URL = "jdbc:ucanaccess://C:\\Users\\Anahi\\eclipse-workspace\\punto_venta_2\\Punto_Venta.accdb";
    private static volatile Connection conn = null;

    static {
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            logger.info("Driver UCanAccess registrado correctamente");
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Error al registrar el driver UCanAccess", e);
            throw new ExceptionInInitializerError("No se pudo registrar el driver JDBC");
        }
    }

    public static Connection conectar() {
        try {
            if (conn == null || conn.isClosed()) {
                synchronized (ConexionAccess.class) {
                    if (conn == null || conn.isClosed()) {
                        Properties props = new Properties();
                        props.put("user", "");
                        props.put("password", "");
                        props.put("shutdown", "true");
                        
                        conn = DriverManager.getConnection(URL, props);
                        logger.info("✅ Conexión a Access establecida exitosamente");
                    }
                }
            }
            return conn;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "❌ Error al conectar con Access", e);
            throw new RuntimeException("Error de conexión a la base de datos", e);
        }
    }

    public static void cerrarConexion() {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                    logger.info("🔌 Conexión cerrada correctamente");
                }
            } catch (SQLException e) {
                logger.log(Level.WARNING, "⚠️ Error al cerrar la conexión", e);
            } finally {
                conn = null;
            }
        }
    }
}