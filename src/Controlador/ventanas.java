package Controlador;

import Vista.*;
import java.awt.event.*;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import Vista.producto;
import Modelo.Cliente;
import Modelo.Clientee;
import Modelo.ClienteImpl;
import Modelo.Usuario;

public class ventanas  {
	private Usuario usuario;
    private menuprincipal menu;
    private gestionUsuario gestionUsuario;
    private clientes clientes;
    private producto producto;
    private inventario inventario;
    private reportes reportes;
    private proveedores proveedores;
    private Clientee clientee;
    private Cliente cliente;


    public ventanas(menuprincipal menu, Usuario usuario) {
        this.menu = menu;
        this.usuario = usuario;
        inicializarEventos();
    }

    private void inicializarEventos() {
        menu.setGestionUsuariosListener(e -> abrirGestionUsuarios());
        menu.setGestionClientesListener(e -> abrirGestionClientes(usuario));
        menu.setRegistroVentasListener(e -> abrirRegistroVentas());
        menu.setInventarioListener(e -> abrirInventario());
        menu.setReportesListener(e -> abrirReportes());
        menu.setProveedoresListener(e -> abrirProveedores());
        menu.setCerrarSesionListener(e -> cerrarSesion());
    }

    private void abrirGestionUsuarios() {
    	menu.dispose();  // Cierra la ventana (menu)
        new gestionUsuario(usuario).setVisible(true); // Reemplaza con clase real
    }

    public void abrirGestionClientes(Usuario usuario) {
        try {
            Clientee clienteDAO = new ClienteImpl();
            clientes vistaClientes = new clientes(usuario, clienteDAO);
            
            // Asegurar que la ventana se configure correctamente
            vistaClientes.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            vistaClientes.setLocationRelativeTo(null);
            vistaClientes.setVisible(true);
            
            menu.dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, 
                "Error al abrir clientes: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace(); // Para depuración
        }
    }

    private void abrirRegistroVentas() {
        if (producto == null || !producto.isVisible()) {
            if (producto != null) {
                producto.dispose();
            }
            producto = new producto(usuario);
            producto.setVisible(true);
            menu.dispose();
        }
    }
    
    private void abrirInventario() {
    	menu.dispose();  // Cierra la ventana actual (menu)
        new inventario(usuario).setVisible(true); // Reemplaza con  clase real
    }

    public void abrirReportes() {
        ReportesControlador controlador = new ReportesControlador(reportes, usuario);
        reportes ventana = new reportes(usuario, controlador); 	
        ventana.setVisible(true);
    }

    private void abrirProveedores() {
    	menu.dispose();  // Cierra la ventana actual (menu)
        new proveedores(usuario).setVisible(true); // Reemplaza con clase real
    }

    private void cerrarSesion() {
    	menu.dispose();  // Cierra la ventana actual (menu)
        new Login().setVisible(true);
    }
}
