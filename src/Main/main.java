package Main;

import Controlador.ventanas;
import Vista.reportes;
import Vista.Login;
import Vista.clientes;
import Vista.menuprincipal;
import Modelo.Clientee;
import Modelo.Reportes;

import java.sql.Connection;

import javax.swing.SwingUtilities;

import ConexionBD.ConexionAccess;
import Controlador.ClientesContro;
import Controlador.ReportesControlador;

public class main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Login login = new Login();
           login.setVisible(true);
           Connection conn = ConexionAccess.conectar();
        });
    }
}