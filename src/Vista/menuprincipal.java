package Vista;

import javax.swing.*;

import Controlador.ventanas;
import Modelo.Usuario;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;

public class menuprincipal extends JFrame {
    private JButton btnGestionUsuarios;
    private JButton btnGestionClientes;
    private JButton btnRegistroVentas;
    private JButton btnReportes;
    private JButton btnInventario;
    private JButton btnCerrarSesion;
    private JButton btnProveedores;
	private JLabel lblUsuario;
	private Usuario usuario;
    
    public menuprincipal(Usuario usuario) {
    	 this.usuario = usuario;
        initComponents();
        new ventanas(this , usuario); // <- Se pasa el usuario al controlador

    }
    
    public Usuario getUsuario() {
        return usuario;
    }
    
    private void initComponents() {
        setTitle("El Habanerito - Menú Principal");
        setSize(930, 704);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Panel principal con diseño similar al login
        JLayeredPane mainLayeredPane = new JLayeredPane();
        mainLayeredPane.setPreferredSize(new Dimension(900, 650));
        
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(0, 0, new Color(245, 245, 245), 
                                     0, getHeight(), new Color(230, 230, 230));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        backgroundPanel.setBounds(0, 0, 900, 650);
        mainLayeredPane.add(backgroundPanel, JLayeredPane.DEFAULT_LAYER);
        
        // Círculos decorativos
        addCirculosDecorativos(mainLayeredPane);
        
        // Panel de contenido principal
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBounds(0, 0, 900, 650);
        
        // Cabecera con el nombre de la tienda y logo
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 0));
        
        // Panel para el logo y título
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        titlePanel.setOpaque(false);
        
        try {
            ImageIcon originalIcon = new ImageIcon("imagen\\logo.png");
            Image originalImage = originalIcon.getImage();
            int logoHeight = 200;
            int logoWidth = (int) ((double) originalIcon.getIconWidth() / originalIcon.getIconHeight() * logoHeight);
            Image resizedImage = originalImage.getScaledInstance(logoWidth, logoHeight, Image.SCALE_SMOOTH);
            JLabel logo = new JLabel(new ImageIcon(resizedImage));
            titlePanel.add(logo);
        } catch (Exception e) {
            System.err.println("Error cargando el logo: " + e.getMessage());
        }
        
        JLabel lblTitulo = new JLabel("El Habanerito");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 28));
        lblTitulo.setForeground(new Color(70, 70, 70));
        titlePanel.add(lblTitulo);
        
        lblUsuario = new JLabel("", SwingConstants.RIGHT);
        lblUsuario.setFont(new Font("Arial", Font.PLAIN, 14));
        lblUsuario.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 30));
        
        headerPanel.add(titlePanel, BorderLayout.CENTER);
        headerPanel.add(lblUsuario, BorderLayout.EAST);
        contentPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Panel central con botones
        JPanel centerPanel = crearPanelCentral();
        contentPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Panel inferior con botón de cerrar sesión
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 30));
        
        btnCerrarSesion = new JButton("Cerrar Sesión");
        btnCerrarSesion.setFont(new Font("Arial", Font.PLAIN, 14));
        btnCerrarSesion.setBackground(new Color(123, 118, 118));
        btnCerrarSesion.setForeground(Color.WHITE);
        btnCerrarSesion.setFocusPainted(false);
        btnCerrarSesion.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        btnCerrarSesion.addActionListener(e -> {
            this.dispose();
            new Login().setVisible(true);
        });
        
        footerPanel.add(btnCerrarSesion);
        contentPanel.add(footerPanel, BorderLayout.SOUTH);
        
        mainLayeredPane.add(contentPanel, JLayeredPane.MODAL_LAYER);
        getContentPane().add(mainLayeredPane);
    }
    
    private void addCirculosDecorativos(JLayeredPane layeredPane) {
        int circleSize = 500;
        
        JPanel circleTopLeft = createCirclePanel(new Color(235, 215, 107, 100));
        circleTopLeft.setBounds(-circleSize/2, -circleSize/2, circleSize, circleSize);
        
        JPanel circleTopRight = createCirclePanel(new Color(216, 237, 88, 100));
        circleTopRight.setBounds(900-circleSize/2, -circleSize/2, circleSize, circleSize);
        
        JPanel circleBottomLeft = createCirclePanel(new Color(235, 184, 35, 100));
        circleBottomLeft.setBounds(-circleSize/2, 650-circleSize/2, circleSize, circleSize);
        
        JPanel circleBottomRight = createCirclePanel(new Color(241, 81, 17, 100));
        circleBottomRight.setBounds(900-circleSize/2, 650-circleSize/2, circleSize, circleSize);
        
        layeredPane.add(circleTopLeft, JLayeredPane.PALETTE_LAYER);
        layeredPane.add(circleTopRight, JLayeredPane.PALETTE_LAYER);
        layeredPane.add(circleBottomLeft, JLayeredPane.PALETTE_LAYER);
        layeredPane.add(circleBottomRight, JLayeredPane.PALETTE_LAYER);
    }
    
    private JPanel createCirclePanel(Color color) {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(color);
                
                int diameter = Math.min(getWidth(), getHeight());
                int x = (getWidth() - diameter) / 2;
                int y = (getHeight() - diameter) / 2;
                
                g2d.fillOval(x, y, diameter, diameter);
            }
        };
    }
    
    private JPanel crearPanelCentral() {
        JPanel panel = new JPanel(new GridLayout(2, 3, 30, 30));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        
        // Botones del menú principal
        btnGestionUsuarios = crearBotonMenu("Gestión de Usuarios", Color.LIGHT_GRAY);
        btnGestionClientes = crearBotonMenu("Clientes", Color.LIGHT_GRAY);
        btnRegistroVentas = crearBotonMenu("Ventas", Color.LIGHT_GRAY);
        btnInventario = crearBotonMenu("Inventario", Color.LIGHT_GRAY);
        btnReportes = crearBotonMenu("Reportes", Color.LIGHT_GRAY);
        btnProveedores = crearBotonMenu("Proveedores", Color.LIGHT_GRAY);
        
        panel.add(btnGestionUsuarios);
        panel.add(btnGestionClientes);
        panel.add(btnRegistroVentas);
        panel.add(btnInventario);
        panel.add(btnReportes);
        panel.add(btnProveedores);
        
        return panel;
    }
    
    private JButton crearBotonMenu(String texto, Color color) {
        JButton boton = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                if (!isOpaque()) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    g2d.setColor(getBackground());
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                    
                    g2d.setColor(getForeground());
                    FontMetrics fm = g2d.getFontMetrics();
                    Rectangle2D r = fm.getStringBounds(getText(), g2d);
                    int x = (this.getWidth() - (int) r.getWidth()) / 2;
                    int y = (this.getHeight() - (int) r.getHeight()) / 2 + fm.getAscent();
                    g2d.drawString(getText(), x, y);
                    
                    g2d.dispose();
                } else {
                    super.paintComponent(g);
                }
            }
            
            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground().darker());
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 30, 30);
                g2d.dispose();
            }
        };
        
        boton.setFont(new Font("Arial", Font.BOLD, 18));
        boton.setBackground(color);
        boton.setForeground(Color.BLACK);
        boton.setFocusPainted(false);
        boton.setContentAreaFilled(false);
        boton.setOpaque(false);
        boton.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        
        boton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                boton.setBackground(color.brighter());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                boton.setBackground(color);
            }
        });
        
        return boton;
    }
    
    private JButton crearBotonFooter(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Arial", Font.PLAIN, 14));
        boton.setBackground(new Color(123, 118, 118));
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return boton;
    }
    
    // Métodos para integración con controlador
    
    public void setGestionUsuariosListener(ActionListener listener) {
        btnGestionUsuarios.addActionListener(listener);
    }
    
    public void setGestionClientesListener(ActionListener listener) {
        btnGestionClientes.addActionListener(listener);
    }
    
    public void setRegistroVentasListener(ActionListener listener) {
        btnRegistroVentas.addActionListener(listener);
    }
    
    public void setInventarioListener(ActionListener listener) {
        btnInventario.addActionListener(listener);
    }
    
    public void setReportesListener(ActionListener listener) {
        btnReportes.addActionListener(listener);
    }
    
    public void setProveedoresListener(ActionListener listener) {
        btnProveedores.addActionListener(listener);
    }
    
    public void setCerrarSesionListener(ActionListener listener) {
        btnCerrarSesion.addActionListener(listener);
    }
    
    public void mostrarMensaje(String mensaje, String titulo, int tipo) {
        JOptionPane.showMessageDialog(this, mensaje, titulo, tipo);
    }
 
    public void ocultarOpcionGestionUsuarios() {
        this.btnGestionUsuarios.setVisible(false);
    }
    

    public void setBienvenida(String nombreUsuario, String rol) {
        setTitle("El Habanerito - Menú Principal (" + nombreUsuario + ")");
        lblUsuario.setText("Bienvenido: " + nombreUsuario + " (" + rol + ")");
        }

   
    public void configurarVisibilidadBotones(boolean mostrarAdmin) {
        btnGestionUsuarios.setVisible(mostrarAdmin);
        btnGestionUsuarios.setEnabled(mostrarAdmin);
        btnReportes.setEnabled(mostrarAdmin);
    }
   
   public boolean mostrarConfirmacion(String mensaje, String titulo) {
       int respuesta = JOptionPane.showConfirmDialog(
           this, 
           mensaje, 
           titulo, 
           JOptionPane.YES_NO_OPTION
       );
       return respuesta == JOptionPane.YES_OPTION;
   }
   

}