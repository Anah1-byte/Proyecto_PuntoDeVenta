package Vista;

import Modelo.Usuario;
import Modelo.Venta;
import Modelo.Clientee;
import Modelo.ClienteImpl;
import Modelo.Producto;
import Controlador.ReportesControlador;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.text.SimpleDateFormat;

import javax.swing.border.*;

import com.toedter.calendar.JDateChooser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class reportes extends JFrame {
    private Usuario usuario;
    private String rolUsuario;
    private JPanel panelContenido; // Panel dinámico para cambiar vistas
    private CardLayout cardLayout;
    private final ReportesControlador controlador;
    private ReporteVentasPanel panelVentas;
    private ReporteInventarioPanel panelInventario;
	private ReporteClientePanel panelClientes;
    
    // Constructor
	public reportes(Usuario usuario, ReportesControlador controlador) {
	    this.usuario = usuario;
	    this.controlador = controlador;
	    
	    // Primero crear la interfaz
	    initUI();
	    
	    // Luego crear los paneles
	    this.panelVentas = new ReporteVentasPanel(usuario, controlador);    
	    this.panelInventario = new ReporteInventarioPanel(usuario, controlador, cardLayout, panelContenido);
	    this.panelClientes = new ReporteClientePanel(usuario, controlador, cardLayout, panelContenido);
	    
	    // Registrar paneles
	    panelContenido.add(panelVentas, "reporte_ventas");
	    panelContenido.add(panelInventario, "reporte_inventario");
	    panelContenido.add(panelClientes, "reporte_clientes");
	    
	    // Configurar paneles en el controlador
	    controlador.setPanelVentas(panelVentas);
	    controlador.setPanelInventario(panelInventario);
	    controlador.setPanelClientes(panelClientes);
	}

	private void initUI() {
	    setTitle("El Habanerito - Reportes");
	    setSize(1517, 903);
	    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	    setLocationRelativeTo(null);
	    setResizable(true);

	    JPanel mainPanel = new JPanel(new BorderLayout());
	    
	    // Panel superior con encabezado y menú
	    JPanel topContainer = new JPanel();
	    topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
	    topContainer.add(createHeaderPanel());
	    topContainer.add(crearMenuHorizontal());
	    mainPanel.add(topContainer, BorderLayout.NORTH);
	    
	    // Panel central dinámico
	    panelContenido = new JPanel();
	    cardLayout = new CardLayout();
	    panelContenido.setLayout(cardLayout);
	    
	    // Agregar vistas
	    panelContenido.add(crearMenuPrincipalPanel(), "menu_principal");
	    
	    mainPanel.add(panelContenido, BorderLayout.CENTER);
	    add(mainPanel);
	}

	private JPanel crearMenuPrincipalPanel() {
	    JPanel menuPanel = new JPanel(new GridBagLayout());
	    menuPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
	    menuPanel.setBackground(new Color(240, 240, 240));
	    
	    GridBagConstraints gbc = new GridBagConstraints();
	    gbc.insets = new Insets(20, 20, 20, 20);
	    gbc.fill = GridBagConstraints.BOTH;
	    gbc.weightx = 1;
	    gbc.weighty = 1;
	    
	    // Tarjeta de Reporte de Ventas (existente)
	    gbc.gridx = 0;
	    gbc.gridy = 0;
	    JPanel cardVentas = crearTarjetaReporte(
	        "VENTAS", 
	        "/imagen/ventas_icon.png", 
	        "Reportes detallados de ventas por período", 
	        new Color(144, 238, 144)
	    );
	    cardVentas.addMouseListener(new MouseAdapter() {
	        public void mouseClicked(MouseEvent e) {
	            mostrarReporteVentas();
	        }
	    });
	    menuPanel.add(cardVentas, gbc);
	    
	    // Tarjeta de Reporte de Inventario (existente)
	    gbc.gridx = 1;
	    JPanel cardInventario = crearTarjetaReporte(
	        "INVENTARIO", 
	        "/imagen/inventario_icon.png", 
	        "Estado actual del inventario y alertas", 
	        new Color(135, 206, 250)
	    );
	    cardInventario.addMouseListener(new MouseAdapter() {
	        public void mouseClicked(MouseEvent e) {
	            mostrarReporteInventario();
	        }
	    });
	    menuPanel.add(cardInventario, gbc);
	    
	    // Tarjeta de Reporte de Clientes (existente)
	    gbc.gridx = 0;
	    gbc.gridy = 1;
	    JPanel cardClientes = crearTarjetaReporte(
	        "CLIENTES", 
	        "/imagen/clientes_icon.png", 
	        "Comportamiento y fidelidad de clientes", 
	        new Color(255, 182, 193)
	    );
	    cardClientes.addMouseListener(new MouseAdapter() {
	        public void mouseClicked(MouseEvent e) {
	            mostrarReporteClientes();
	        }
	    });
	    menuPanel.add(cardClientes, gbc);
	    
	    // Tarjeta de Reporte de Proveedores (existente)
	    gbc.gridx = 1;
	    JPanel cardProveedores = crearTarjetaReporte(
	        "PROVEEDORES", 
	        "/imagen/proveedores_icon.png", 
	        "Desempeño y relación con proveedores", 
	        new Color(221, 160, 221)
	    );
	    cardProveedores.addMouseListener(new MouseAdapter() {
	        public void mouseClicked(MouseEvent e) {
	            mostrarReporteProveedores();
	        }
	    });
	    menuPanel.add(cardProveedores, gbc);
	    
	    // --- NUEVA TARJETA PARA REIMPRESIÓN ---
	    gbc.gridx = 0;
	    gbc.gridy = 2;
	    gbc.gridwidth = 2; // Ocupa 2 columnas
	    JPanel cardReimpresion = crearTarjetaReporte(
	        "REIMPRESIÓN", 
	        "/imagen/printer_icon.png",  
	        "Reimprimir tickets de venta o reportes", 
	        new Color(255, 215, 0)  // Color dorado para distinguirlo
	    );
	    cardReimpresion.addMouseListener(new MouseAdapter() {
	        public void mouseClicked(MouseEvent e) {
	            new ReimprimirDialog(usuario,reportes.this, controlador).setVisible(true);
	        }
	    });
	    menuPanel.add(cardReimpresion, gbc);
	    
	    return menuPanel;
	}

    private JPanel crearTarjetaReporte(String titulo, String icono, String descripcion, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker(), 2),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        card.setBackground(color);
        card.setPreferredSize(new Dimension(350, 250));
        
        // Efecto hover
        card.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.BLACK, 2),
                    BorderFactory.createEmptyBorder(25, 25, 25, 25)
                ));
                card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
            
            public void mouseExited(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(color.darker(), 2),
                    BorderFactory.createEmptyBorder(25, 25, 25, 25)
                ));
                card.setCursor(Cursor.getDefaultCursor());
            }
        });
        
        JLabel lblTitulo = new JLabel(titulo, SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitulo.setForeground(Color.DARK_GRAY);
        
        JLabel lblDesc = new JLabel("<html><div style='text-align: center;'>" + descripcion + "</div></html>", SwingConstants.CENTER);
        lblDesc.setFont(new Font("Arial", Font.PLAIN, 14));
        lblDesc.setForeground(Color.DARK_GRAY);
        
        // Intenta cargar el icono
        try {
            URL imgUrl = getClass().getResource(icono);
            if (imgUrl != null) {
                ImageIcon originalIcon = new ImageIcon(imgUrl);
                Image scaledImage = originalIcon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                JLabel lblIcono = new JLabel(new ImageIcon(scaledImage), SwingConstants.CENTER);
                card.add(lblIcono, BorderLayout.NORTH);
            }
        } catch (Exception e) {
            System.out.println("No se pudo cargar el icono: " + icono);
            card.add(Box.createVerticalStrut(30), BorderLayout.NORTH);
        }
        
        card.add(lblTitulo, BorderLayout.CENTER);
        card.add(lblDesc, BorderLayout.SOUTH);
        
        return card;
    }

    public void mostrarMenuPrincipalReportes() {
        cardLayout.show(panelContenido, "menu_principal");
    }

    private void mostrarReporteVentas() {
        try {
            System.out.println("[DEBUG] Intentando mostrar reporte de ventas");
            
            // Verificar si el panel ya existe
            Component ventasPanel = findPanel("reporte_ventas");
            
            if (ventasPanel == null) {
                System.out.println("[DEBUG] Creando nuevo panel de ventas");
                ventasPanel = new ReporteVentasPanel(usuario, controlador);
                
                // Crear panel con botón de regreso
                JPanel panelConBoton = new JPanel(new BorderLayout());
                panelConBoton.add(ventasPanel, BorderLayout.CENTER);
                
                JButton btnRegresar = new JButton("Regresar al Menú");
                btnRegresar.addActionListener(e -> mostrarMenuPrincipalReportes());
                panelConBoton.add(btnRegresar, BorderLayout.SOUTH);
                
                panelContenido.add(panelConBoton, "reporte_ventas");
            }
            
            // Mostrar el panel
            cardLayout.show(panelContenido, "reporte_ventas");
            System.out.println("[DEBUG] Panel de ventas mostrado");
            
            // Actualizar la interfaz
            revalidate();
            repaint();
            
        } catch (Exception e) {
            System.err.println("[ERROR] Al mostrar ventas: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al abrir reporte de ventas", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarReporteInventario() {
        Component inventarioPanel = findPanel("reporte_inventario");
        if (inventarioPanel == null) {
            inventarioPanel = new ReporteInventarioPanel(
                usuario, 
                controlador,
                cardLayout, 
                panelContenido
            );
            panelContenido.add(inventarioPanel, "reporte_inventario");
        }
        cardLayout.show(panelContenido, "reporte_inventario");
    }
     
    private void mostrarReporteClientes() {
        Component clientesPanel = findPanel("reporte_clientes");
        if (clientesPanel == null) {
            clientesPanel = new ReporteClientePanel(
                usuario, 
                controlador,
                cardLayout, 
                panelContenido
            );
            panelContenido.add(clientesPanel, "reporte_clientes");
            
            // Configurar el controlador con el panel de clientes
            controlador.setPanelClientes((ReporteClientePanel) clientesPanel);
            
            // Cargar datos iniciales
            controlador.cargarDatosClientes();
        }
        cardLayout.show(panelContenido, "reporte_clientes");
    }

    private void mostrarReporteProveedores() {
		Component proveedoresPanel = findPanel("reporte_proveedores");
		if (proveedoresPanel == null) {
			proveedoresPanel = new ReporteProveedoresPanel(usuario, controlador, cardLayout, panelContenido);
			panelContenido.add(proveedoresPanel, "reporte_proveedores");
		}
		cardLayout.show(panelContenido, "reporte_proveedores");
    }

    private Component findPanel(String name) {
        for (Component comp : panelContenido.getComponents()) {
            if (comp.getName() != null && comp.getName().equals(name)) {
                return comp;
            }
        }
        return null;
    }

 
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(255, 198, 144));
        panel.setPreferredSize(new Dimension(getWidth(), 80));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);

        JButton usuarioBtn = new JButton(usuario.getUsername());
        usuarioBtn.setFont(new Font("Arial", Font.BOLD, 14));
        usuarioBtn.setForeground(Color.BLACK);
        usuarioBtn.setContentAreaFilled(false);
        usuarioBtn.setBorderPainted(false);
        usuarioBtn.setFocusPainted(false);
        usuarioBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        usuarioBtn.addActionListener(e -> cambiarUsuario());

        rightPanel.add(usuarioBtn);

        try {
            ImageIcon originalIcon = new ImageIcon("imagen\\logo.png");
            Image originalImage = originalIcon.getImage();
            int logoHeight = 60;
            int logoWidth = (int) ((double) originalIcon.getIconWidth() / originalIcon.getIconHeight() * logoHeight);
            Image resizedImage = originalImage.getScaledInstance(logoWidth, logoHeight, Image.SCALE_SMOOTH);

            JLabel logo = new JLabel(new ImageIcon(resizedImage));
            rightPanel.add(logo, 0);

        } catch (Exception e) {
            System.err.println("Error cargando el logo: " + e.getMessage());
        }

        panel.add(rightPanel, BorderLayout.EAST);
        return panel;
    }

    private JPanel crearMenuHorizontal() {
        JPanel menuPanel = new JPanel(new GridLayout(1, 7));
        menuPanel.setBackground(new Color(230, 230, 230));
        menuPanel.setPreferredSize(new Dimension(0, 50));
        menuPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Color.GRAY));
        
        String[] opciones = {"Productos", "Reportes", "Inventario", "Cliente", "Proveedores", "Usuarios", "Salir"};
        
        for (String opcion : opciones) {
            JButton btn = crearBotonMenu(opcion, opcion.equals("Reportes"));
            btn.addActionListener(e -> manejarAccionMenu(opcion));
            menuPanel.add(btn);
        }
        
        return menuPanel;
    }

    private JButton crearBotonMenu(String texto, boolean esActivo) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Arial", Font.BOLD, 14));
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        boton.setPreferredSize(new Dimension(0, 50));
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        
        if (esActivo) {
            boton.setBackground(new Color(216, 237, 88));
            boton.setForeground(Color.BLACK);
        } else {
            boton.setBackground(Color.GRAY);
            boton.setForeground(Color.BLACK);
        }
        
        if (!esActivo) {
            boton.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    boton.setBackground(new Color(216, 237, 88));
                    boton.setForeground(Color.WHITE);
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    boton.setBackground(Color.GRAY);
                    boton.setForeground(Color.BLACK);
                }
            });
        }
        
        return boton;
    }

    private void manejarAccionMenu(String opcion) {
        switch (opcion) {
            case "Salir":
                this.dispose();
                new menuprincipal(usuario).setVisible(true);
                break;
            case "Productos":
                this.dispose();
                new producto(usuario).setVisible(true);
                break;
            case "Reportes":
                // Ya estamos en reportes
                break;
            case "Inventario":
                this.dispose();
                new inventario(usuario).setVisible(true);
                break;
            case "Cliente":
                this.dispose();
                Clientee clienteDAO = new ClienteImpl();
                new clientes(usuario, clienteDAO).setVisible(true);
                break;
            case "Proveedores":
                this.dispose();
                new proveedores(usuario).setVisible(true);
                break;
            case "Usuarios":
                this.dispose();
                new gestionUsuario(usuario).setVisible(true);
                break;
        }
    }

    private void cambiarUsuario() {
        JDialog changeUserDialog = new JDialog(this, "Cambiar Usuario", true);
        changeUserDialog.setSize(300, 150);
        changeUserDialog.setLocationRelativeTo(this);
        
        JPanel dialogPanel = new JPanel(new BorderLayout(10, 10));
        dialogPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel instructionLabel = new JLabel("¿Desea cambiar de usuario?", SwingConstants.CENTER);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        
        JButton cambiarBtn = new JButton("Cambiar de Usuario");
        JButton cancelarBtn = new JButton("Cancelar");
        
        cambiarBtn.addActionListener(e -> {
            Window[] windows = Window.getWindows();
            for (Window window : windows) {
                if (window instanceof producto) {
                    window.setVisible(false);
                    window.dispose();
                }
            }
            new Login().setVisible(true);
            changeUserDialog.dispose();
        });
        
        cancelarBtn.addActionListener(e -> changeUserDialog.dispose());
        
        buttonPanel.add(cambiarBtn);
        buttonPanel.add(cancelarBtn);
        
        dialogPanel.add(instructionLabel, BorderLayout.CENTER);
        dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        changeUserDialog.getContentPane().add(dialogPanel);
        changeUserDialog.setVisible(true);
    }
    
    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }
    
}