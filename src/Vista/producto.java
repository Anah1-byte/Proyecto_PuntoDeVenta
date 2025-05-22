package Vista;

import Modelo.Producto;
import javax.swing.*;
import javax.swing.border.Border;

import Controlador.ReportesControlador;
import Controlador.VentaContro;
import Modelo.Carrito;
import Modelo.Cliente;
import Modelo.Clientee;
import Modelo.ClienteImpl;
import Modelo.Inventarioo;
import Modelo.Producto;
import Modelo.Usuario;
import Modelo.Usuarioo;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import ConexionBD.ConexionAccess;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class producto extends JFrame {
    private JPanel productosPanel;
    private Map<String, List<Object[]>> productosPorCategoria;
    private JLabel totalLabel;
    private JPanel panelResumenDerecho;
    private JTextField montoRecibidoField;
    private JLabel montoRecibidoLabel;
    private JLabel cambioLabel;
    private JTextField telefonoField;
    private JLabel puntosLabel;
	private JLabel lblInfoCliente;
	private Usuario usuario;
	private String rolUsuario;
	private VentaContro  controlador;
	private Carrito carrito;
	private JLabel lblNumeroCantidad;
	private String metodoPago = "Efectivo";
    private int cantidad = 1;
    private Clientee clientee;
    private Inventarioo inventarioo;
    private JButton btnDisminuir;
    private JButton btnAumentar;
	private Container pagoMontoContainer;
	private Timestamp version;
	    private JTable tablaCarrito;
    
    public producto(Usuario usuario) {
        this.usuario = usuario;
        this.controlador = new VentaContro(usuario);
        this.inventarioo = new Inventarioo();
        this.clientee = new ClienteImpl();
        cargarProductosDesdeBD();
        initUI();
    
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                menuprincipal menu = new menuprincipal(usuario);
               menu.setVisible(true);
            }
        });
        
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancelar");
        getRootPane().getActionMap().put("cancelar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelarVenta();
            }
        });
        
    }
    private void cargarProductosDesdeBD() {
        productosPorCategoria = new HashMap<>();
        List<Producto> todosProductos = inventarioo.obtenerTodosProductos();
        
        for (Producto producto : todosProductos) {
            String categoria = producto.getCategoria();
            if (!productosPorCategoria.containsKey(categoria)) {
                productosPorCategoria.put(categoria, new ArrayList<>());
            }
            
            // Usar getImagenPath() que viene de la base de datos
            productosPorCategoria.get(categoria).add(new Object[]{
                producto.getImagenPath(), // Nombre del archivo o ruta
                producto.getNombre(),
                producto.getPrecioVenta()
            });
        }
    }
	private Component encontrarComponentePorNombre(String name) {
        for (Component comp : getContentPane().getComponents()) {
            if (name.equals(comp.getName())) {
                return comp;
            }
        }
        return null;
    }
   
	private void initUI() {
	    setTitle("El Habanerito - Pagina Principal");
	    setSize(1517, 903);
	    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	    setLocationRelativeTo(null);
	    setResizable(true);

	    JPanel mainPanel = new JPanel(new BorderLayout());
	    mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
	    
	    // 1. Paneles superiores
	    JPanel topPanel = crearPanelSuperior();
	    JPanel menuPanel = crearMenuHorizontal();
	    
	    JPanel northContainer = new JPanel(new BorderLayout());
	    northContainer.add(topPanel, BorderLayout.NORTH);
	    northContainer.add(menuPanel, BorderLayout.SOUTH);
	    
	    // 2. Panel central (categorías + productos)
	    JPanel leftMenuPanel = crearMenuClasificaciones();
	    productosPanel = new JPanel();
	    productosPanel.setLayout(new GridLayout(0, 3, 10, 10));
	 //  productosPanel.setAutoscrolls(true);
	    //productosPanel.setLayout(new BoxLayout(productosPanel, BoxLayout.Y_AXIS));
	    
	    JScrollPane scrollPane = new JScrollPane(productosPanel,
	            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
	            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	        scrollPane.setBorder(BorderFactory.createEmptyBorder());
	        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
	    	
	    JPanel centerPanel = new JPanel(new BorderLayout());
	    centerPanel.add(scrollPane, BorderLayout.CENTER);
	    
	    // 3. Panel derecho completo (pago + carrito)
	    JPanel rightPanel = new JPanel(new BorderLayout());
	    
	    // 3.1 Panel de pago (izquierda)
	    JPanel paymentPanel = new JPanel();
	    paymentPanel.setLayout(new BoxLayout(paymentPanel, BoxLayout.Y_AXIS));
	    
	    // Total en semicírculo
	    JPanel totalPanel = new JPanel();
	    totalPanel.setMaximumSize(new Dimension(300, 80));
	    
	    JPanel circuloPanel = new JPanel() {
	        @Override
	        protected void paintComponent(Graphics g) {
	            super.paintComponent(g);
	            Graphics2D g2 = (Graphics2D) g;
	            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	            g2.setColor(new Color(255, 198, 144));
	            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 50, 50);
	        }
	    };
	    circuloPanel.setPreferredSize(new Dimension(300, 60));
	    circuloPanel.setLayout(new BorderLayout());
	    
	    totalLabel = new JLabel("Total: $0.00", SwingConstants.CENTER);
	    totalLabel.setFont(new Font("Arial", Font.BOLD, 20));
	    circuloPanel.add(totalLabel, BorderLayout.CENTER);
	    
	    totalPanel.add(circuloPanel);
	    paymentPanel.add(totalPanel);
	    
	    // Método de pago y teclado
	    paymentPanel.add(crearPanelPagoYResumen());
	    paymentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
	    paymentPanel.add(crearPanelTelefono());
	    paymentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
	    paymentPanel.add(crearTecladoNumerico());
	    paymentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
	    paymentPanel.add(crearBotonesAccion());
	    
	    // 3.2 Carrito (derecha)
	    panelResumenDerecho = crearPanelResumenDerecho();
	    panelResumenDerecho.setPreferredSize(new Dimension(350, 600));
	    
	    // Organización final
	    rightPanel.add(paymentPanel, BorderLayout.WEST);
	    rightPanel.add(panelResumenDerecho, BorderLayout.CENTER);
	    
	    // Contenedor principal
	    JPanel contentPanel = new JPanel(new BorderLayout());
	    contentPanel.add(leftMenuPanel, BorderLayout.WEST);
	    contentPanel.add(centerPanel, BorderLayout.CENTER);
	    contentPanel.add(rightPanel, BorderLayout.EAST);

	    mainPanel.add(northContainer, BorderLayout.NORTH);
	    mainPanel.add(contentPanel, BorderLayout.CENTER);
	    mainPanel.add(crearPanelInferior(), BorderLayout.SOUTH);
	    
	    getContentPane().add(mainPanel);
	    mostrarProductosDeCategoria("TODOS");
	}
    
    private JPanel crearMenuClasificaciones() {
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new GridLayout(0, 1));
        menuPanel.setBackground(Color.GRAY);
        menuPanel.setPreferredSize(new Dimension(200, getHeight()));
        menuPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        String[] categorias = {
            "TODOS",
            "Abarrotes",
            "Panaderia y Tortilleria",
            "Carnes y embutidos",
            "Botanas y dulces", 
            "Bebidas",
            "Lacteos",
            "Limpieza del hogar",
            "Cuidado personal"
        };

        for (String categoria : categorias) {
            JButton btn = new JButton(categoria);
            btn.setFont(new Font("Arial", Font.PLAIN, 14));
            btn.setBackground(Color.GRAY);
            btn.setForeground(Color.BLACK);
            btn.setFocusPainted(false);
            btn.setMargin(new Insets(5, 5, 5, 5));
            
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    btn.setBackground(Color.PINK);
                    btn.setForeground(Color.WHITE);
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    btn.setBackground(Color.GRAY);
                    btn.setForeground(Color.BLACK);
                }
            });
            
            btn.addActionListener(e -> mostrarProductosDeCategoria(categoria));
            menuPanel.add(btn);
        }

        return menuPanel;
    }
    
    private void mostrarProductosDeCategoria(String categoria) {
        productosPanel.removeAll();
        productosPanel.setLayout(new GridLayout(0, 3, 10, 10));
        
        List<Producto> productos;
        if (categoria.equals("TODOS")) {
            productos = inventarioo.obtenerTodosProductos();
        } else {
            productos = inventarioo.buscarPorCategoria(categoria);
        }
        
        for (Producto producto : productos) {
            JPanel card = crearCardProducto(
                producto.getImagenPath(),
                producto.getNombre(),
                producto.getPrecioVenta()
            );
            
            // Resaltar productos con bajo stock
            if (producto.getCantidadDisponible() <= producto.getStockMinimo()) {
                card.setBackground(new Color(255, 200, 200)); // Fondo rojo claro
                card.setToolTipText("¡Stock bajo! Disponible: " + producto.getCantidadDisponible());
            }
            
            productosPanel.add(card);
        }
        
        // Calcular el tamaño preferido correctamente
        int rows = (int) Math.ceil((double) productos.size() / 5);
        int cardHeight = 180; // Altura de cada card
        int vGap = 10; // Espacio vertical entre cards
        
        productosPanel.setPreferredSize(
            new Dimension(
                productosPanel.getPreferredSize().width,
                rows * (cardHeight + vGap) - vGap
            )
        );
        
        // Forzar actualizaciones
        productosPanel.revalidate();
        productosPanel.repaint();
        
        // Esto es clave para que funcione el scroll:
        productosPanel.getParent().getParent().validate();
    }

    private JPanel crearPanelInferior() {
        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.setBackground(Color.PINK); // Color rosa claro
        panelInferior.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        panelInferior.setPreferredSize(new Dimension(getWidth(), 70));

        // Panel de cantidad (izquierda) con botones redondos
        JPanel panelCantidad = new JPanel();
        panelCantidad.setLayout(new BoxLayout(panelCantidad, BoxLayout.X_AXIS));
        panelCantidad.setBackground(Color.PINK);
        
        JLabel lblCantidad = new JLabel("Cantidad:");
        lblCantidad.setFont(new Font("Arial", Font.BOLD, 16));
        
        // Botón disminuir redondo
        JButton btnDisminuir = new JButton("-") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillOval(0, 0, getSize().width-1, getSize().height-1);
                super.paintComponent(g);
            }
            
            @Override
            public void updateUI() {
                super.updateUI();
                setContentAreaFilled(false);
                setFocusPainted(false);
                setBorderPainted(false);
                setOpaque(false);
            }
        };
        btnDisminuir.setBackground(new Color(220, 220, 220));
        btnDisminuir.setForeground(Color.BLACK);
        btnDisminuir.setFont(new Font("Arial", Font.BOLD, 14));
        btnDisminuir.setPreferredSize(new Dimension(30, 30));
        btnDisminuir.addActionListener(e -> modificarCantidad(-1));
        
        // Label de cantidad
        lblNumeroCantidad = new JLabel("1", SwingConstants.CENTER);
        lblNumeroCantidad.setFont(new Font("Arial", Font.BOLD, 14));
        lblNumeroCantidad.setPreferredSize(new Dimension(40, 30));
        
        // Botón aumentar redondo
        JButton btnAumentar = new JButton("+") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillOval(0, 0, getSize().width-1, getSize().height-1);
                super.paintComponent(g);
            }
            
            @Override
            public void updateUI() {
                super.updateUI();
                setContentAreaFilled(false);
                setFocusPainted(false);
                setBorderPainted(false);
                setOpaque(false);
            }
        };
        btnAumentar.setBackground(new Color(220, 220, 220));
        btnAumentar.setForeground(Color.BLACK);
        btnAumentar.setFont(new Font("Arial", Font.BOLD, 14));
        btnAumentar.setPreferredSize(new Dimension(50, 30));
        btnAumentar.addActionListener(e -> modificarCantidad(1));
        
        panelCantidad.add(lblCantidad);
        panelCantidad.add(Box.createRigidArea(new Dimension(10, 0)));
        panelCantidad.add(btnDisminuir);
        panelCantidad.add(lblNumeroCantidad);
        panelCantidad.add(btnAumentar);

        JPanel panelBusqueda = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(220, 220, 220)); // Color gris claro
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20); // Esquinas redondeadas
            }
        };
        panelBusqueda.setLayout(new BoxLayout(panelBusqueda, BoxLayout.X_AXIS));
        panelBusqueda.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Padding interno
        panelBusqueda.setOpaque(false);

        // Campo de texto sin fondo
        JTextField buscadorField = new JTextField();
        buscadorField.setPreferredSize(new Dimension(250, 30));
        buscadorField.setMaximumSize(new Dimension(250, 30));
        buscadorField.setFont(new Font("Arial", Font.PLAIN, 14));
        buscadorField.setToolTipText("Buscar producto por nombre");
        buscadorField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Padding interno
        buscadorField.setOpaque(false);

        // Botón de búsqueda con estilo consistente
        JButton btnBuscar = new JButton("Buscar");
        btnBuscar.setBackground(new Color(180, 180, 180)); // Gris un poco más oscuro
        btnBuscar.setForeground(Color.WHITE);
        btnBuscar.setFont(new Font("Arial", Font.BOLD, 12));
        btnBuscar.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        btnBuscar.setFocusPainted(false);
        btnBuscar.addActionListener(e -> buscarProducto(buscadorField.getText()));
        
        // Hacer que el botón tenga esquinas redondeadas
        btnBuscar.setBorder(new RoundedBorder(15)); // 15 es el radio de las esquinas
        btnBuscar.setContentAreaFilled(false);
        btnBuscar.setOpaque(true);

        panelBusqueda.add(buscadorField);
        panelBusqueda.add(Box.createRigidArea(new Dimension(5, 0)));
        panelBusqueda.add(btnBuscar);

        JButton btnPagar = new JButton("PAGAR");
        btnPagar.setBackground(Color.GRAY);
        btnPagar.setForeground(Color.WHITE);
        btnPagar.setFont(new Font("Arial", Font.BOLD, 16));
        btnPagar.setPreferredSize(new Dimension(150, 45));
        btnPagar.addActionListener(e -> realizarPago());

        JPanel leftContainer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftContainer.setBackground(Color.PINK);
        leftContainer.add(panelCantidad);
        
        JPanel centerContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerContainer.setBackground(Color.PINK);
        centerContainer.add(panelBusqueda);
        
        JPanel rightContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightContainer.setBackground(Color.PINK);
        rightContainer.add(btnPagar);
        
        panelInferior.add(leftContainer, BorderLayout.WEST);
        panelInferior.add(centerContainer, BorderLayout.CENTER);
        panelInferior.add(rightContainer, BorderLayout.EAST);

        return panelInferior;
    }
    
    private JPanel crearPanelFondoRedondeado(Color colorFondo) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(colorFondo);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        panel.setOpaque(false);
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return panel;
    }
   
    private JPanel crearPanelCarrito() {
        JPanel carritoPanel = new JPanel();
        carritoPanel.setLayout(new BoxLayout(carritoPanel, BoxLayout.Y_AXIS));
        carritoPanel.setPreferredSize(new Dimension(350, getHeight()));
        carritoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        carritoPanel.setBackground(new Color(240, 240, 240));
      
        totalLabel = new JLabel("Total: $0.00", SwingConstants.CENTER);
        totalLabel.setFont(new Font("Arial", Font.BOLD, 20));
       
        carritoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Añadir el panel de resumen (ya inicializado)
        if (panelResumenDerecho != null) {
            carritoPanel.add(panelResumenDerecho);
        }
        
        carritoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        carritoPanel.add(crearPanelPagoYResumen());
        carritoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        carritoPanel.add(crearPanelTelefono());
        carritoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        carritoPanel.add(crearTecladoNumerico());
        carritoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        carritoPanel.add(crearBotonesAccion());
        
        return carritoPanel;
    }

    private JPanel crearPanelResumenDerecho() {
        // Panel principal con borde y título
    	 JPanel panelPrincipal = new JPanel(new BorderLayout());
    	    panelPrincipal.setBorder(BorderFactory.createCompoundBorder(
    	        BorderFactory.createEmptyBorder(10, 10, 10, 10), // Margen interno
    	        BorderFactory.createTitledBorder(
    	            BorderFactory.createLineBorder(new Color(210, 210, 210)), // Borde sutil
    	            "CARRITO DE COMPRA", 
    	            TitledBorder.LEFT, 
    	            TitledBorder.TOP,
    	            new Font("Segoe UI", Font.BOLD, 14), // Tipografía profesional
    	            new Color(60, 60, 60) // Color de texto oscuro
    	        )
    	       ));
    	    panelPrincipal.setBackground(Color.WHITE);
    	    
    	    DefaultTableModel modeloTabla = new DefaultTableModel() {
    	        @Override public boolean isCellEditable(int row, int column) { return false; }
    	    };
    	    modeloTabla.setColumnIdentifiers(new String[]{"Producto", "Cant.", "P. Unitario", "Subtotal"});

    	    tablaCarrito = new JTable(modeloTabla) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 248, 248));
                }
                
                String nombre = (String)getModel().getValueAt(row, 0);
                Producto p = controlador.getProductoEnCarrito(nombre);
                if (p != null && p.getCantidadDisponible() <= p.getStockMinimo()) {
                    c.setBackground(new Color(255, 230, 230));
                    if (!isRowSelected(row)) {
                        c.setForeground(Color.RED);
                    }
                }
                
                return c;
            }
        };
        
        // Configuración de la tabla
        tablaCarrito.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaCarrito.getTableHeader().setReorderingAllowed(false);
        tablaCarrito.setRowHeight(30);
        tablaCarrito.setShowGrid(false);
        tablaCarrito.setIntercellSpacing(new Dimension(0, 0));
        tablaCarrito.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        // Personalizar encabezados
        JTableHeader header = tablaCarrito.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(240, 240, 240));
        header.setForeground(new Color(80, 80, 80));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        
        // Configurar anchos de columnas
        tablaCarrito.getColumnModel().getColumn(0).setPreferredWidth(160);
        tablaCarrito.getColumnModel().getColumn(1).setPreferredWidth(60);
        tablaCarrito.getColumnModel().getColumn(2).setPreferredWidth(80);
        tablaCarrito.getColumnModel().getColumn(3).setPreferredWidth(80);
        
        // Alinear a la derecha las columnas numéricas
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        tablaCarrito.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
        tablaCarrito.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        tablaCarrito.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        
        // Formatear moneda
        NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance();
        DefaultTableCellRenderer monedaRenderer = new DefaultTableCellRenderer() {
            @Override
            public void setValue(Object value) {
                if (value instanceof Double) {
                    setText(formatoMoneda.format(value));
                } else {
                    setText(value.toString());
                }
            }
        };
        monedaRenderer.setHorizontalAlignment(JLabel.RIGHT);
        tablaCarrito.getColumnModel().getColumn(2).setCellRenderer(monedaRenderer);
        tablaCarrito.getColumnModel().getColumn(3).setCellRenderer(monedaRenderer);
        
        // Botón para eliminar producto seleccionado
        JButton btnEliminar = new JButton("Eliminar");
        try {
            ImageIcon borrarIcon = new ImageIcon(getClass().getResource("/imagen/eliminar.jpeg"));
            btnEliminar.setIcon(new ImageIcon(borrarIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH)));
        } catch (Exception e) {
            btnEliminar.setIcon(UIManager.getIcon("OptionPane.errorIcon"));
        }
        btnEliminar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnEliminar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
            
        ));
        btnEliminar.setBackground(Color.WHITE);
        btnEliminar.setForeground(new Color(80, 80, 80));
        btnEliminar.addActionListener(e -> {
            int filaSeleccionada = tablaCarrito.getSelectedRow();
            if (filaSeleccionada >= 0) {
                String nombreProducto = (String) tablaCarrito.getValueAt(filaSeleccionada, 0);
                
                if (!nombreProducto.equals("El carrito está vacío")) {
                    try {
                        Producto productoEliminado = controlador.eliminarProductoDelCarrito(nombreProducto);
                        
                        if (productoEliminado != null) {
                            // Actualizar modelo de tabla
                            DefaultTableModel model = (DefaultTableModel) tablaCarrito.getModel();
                            model.removeRow(filaSeleccionada);
                            
                            // Si no quedan productos, agregar fila vacía
                            if (model.getRowCount() == 0) {
                                model.addRow(new Object[]{"El carrito está vacío", "", "", ""});
                            }
                            
                            // Actualizar total
                            actualizarTotal();
                            
                            // Mostrar confirmación
                            JOptionPane.showMessageDialog(
                                this,
                                productoEliminado.getNombre() + " eliminado del carrito",
                                "Producto eliminado",
                                JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(
                            this,
                            "Error al actualizar inventario: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "Seleccione un producto para eliminar",
                    "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        
        // Panel para el botón
        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBoton.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        panelBoton.setBackground(Color.WHITE);
        panelBoton.add(btnEliminar);
        
        // Scroll pane para la tabla
        JScrollPane scrollPane = new JScrollPane(tablaCarrito);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        // Panel contenedor principal
        JPanel panelContenido = new JPanel(new BorderLayout());
        panelContenido.add(scrollPane, BorderLayout.CENTER);
        panelContenido.add(panelBoton, BorderLayout.SOUTH);
        panelContenido.setBackground(Color.WHITE);
        
        panelPrincipal.add(panelContenido, BorderLayout.CENTER);
        
        // Actualizar la tabla con los productos del carrito
        actualizarTablaCarrito();
        
        return panelPrincipal;
    }

    // Método auxiliar para actualizar la tabla del carrito
    private void actualizarTablaCarrito() {
        DefaultTableModel model = (DefaultTableModel) tablaCarrito.getModel();
        model.setRowCount(0); // Limpiar tabla completamente
        
        List<Producto> productos = controlador.getProductosEnCarrito();
        
        if (productos.isEmpty()) {
            model.addRow(new Object[]{"El carrito está vacío", "", "", ""});
        } else {
            NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance();
            
            for (Producto p : productos) {
                model.addRow(new Object[]{
                    p.getNombre(),
                    p.getCantidad(),
                    formatoMoneda.format(p.getPrecioUnitario()),
                    formatoMoneda.format(p.getPrecioUnitario() * p.getCantidad())
                });
            }
        }
        
        // Forzar actualización visual
        tablaCarrito.revalidate();
        tablaCarrito.repaint();
    }

    private JPanel crearPanelPagoMonto() {
        JPanel pagoMontoContainer = crearPanelFondoRedondeado(Color.LIGHT_GRAY);
        pagoMontoContainer.setMaximumSize(new Dimension(320, 150)); // Reducido ya que no necesita campos de tarjeta
        pagoMontoContainer.setLayout(new BoxLayout(pagoMontoContainer, BoxLayout.Y_AXIS));
        
        // Panel para métodos de pago simplificado
        JPanel pagoPanel = new JPanel();
        pagoPanel.setLayout(new BoxLayout(pagoPanel, BoxLayout.Y_AXIS));
        pagoPanel.setBorder(BorderFactory.createTitledBorder("Método de Pago"));
        pagoPanel.setOpaque(false);
        
        ButtonGroup grupoPago = new ButtonGroup();
        
        JRadioButton efectivoBtn = new JRadioButton("Efectivo");
        efectivoBtn.setSelected(true);
        
        JRadioButton tarjetaBtn = new JRadioButton("Tarjeta");
        
        grupoPago.add(efectivoBtn);
        grupoPago.add(tarjetaBtn);
        
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        radioPanel.setOpaque(false);
        radioPanel.add(efectivoBtn);
        radioPanel.add(tarjetaBtn);
        
        pagoPanel.add(radioPanel);
        pagoMontoContainer.add(pagoPanel);
        
        // Panel para monto (solo visible para efectivo)
        JPanel montoPanel = new JPanel();
        montoPanel.setLayout(new BoxLayout(montoPanel, BoxLayout.Y_AXIS));
        montoPanel.setBorder(BorderFactory.createTitledBorder("Monto Recibido"));
        montoPanel.setOpaque(false);
        
        montoRecibidoLabel = new JLabel("$0.00");
        montoRecibidoLabel.setFont(new Font("Arial", Font.BOLD, 18));
        montoRecibidoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        montoRecibidoField = new JTextField();
        montoRecibidoField.setMaximumSize(new Dimension(140, 30));
        montoRecibidoField.setHorizontalAlignment(JTextField.CENTER);
        montoRecibidoField.setFont(new Font("Arial", Font.PLAIN, 16));
        montoRecibidoField.addActionListener(e -> calcularCambio());
        
        cambioLabel = new JLabel("Cambio: $0.00");
        cambioLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        cambioLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        montoPanel.add(montoRecibidoLabel);
        montoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        montoPanel.add(montoRecibidoField);
        montoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        montoPanel.add(cambioLabel);
        
        pagoMontoContainer.add(Box.createRigidArea(new Dimension(0, 5)));
        pagoMontoContainer.add(montoPanel);
        
        // Listener para mostrar/ocultar panel de monto
        efectivoBtn.addActionListener(e -> {
            metodoPago = "EFECTIVO";
            montoPanel.setVisible(true);
            montoRecibidoField.setText("");
            montoRecibidoLabel.setText("$0.00");
            cambioLabel.setText("Cambio: $0.00");
        });
        
        tarjetaBtn.addActionListener(e -> {
            metodoPago = "TARJETA";
            montoPanel.setVisible(false);
            // Establecer el monto recibido como el total exacto
            montoRecibidoField.setText(String.format("%.2f", controlador.getTotalVenta()));
        });
        
        return pagoMontoContainer;
    }

    private void actualizarPanelResumenDerecho() {
    	   try {
    	        // 1. Obtener el modelo de la tabla directamente
    	        DefaultTableModel modelo = (DefaultTableModel)tablaCarrito.getModel();
    	        
    	        // 2. Limpiar y actualizar
    	        modelo.setRowCount(0); // Limpiar tabla
    	        
    	        // 3. Llenar con los productos actuales
    	        for (Producto producto : controlador.getProductosEnCarrito()) {
    	            modelo.addRow(new Object[]{
    	                producto.getNombre(),
    	                producto.getCantidad(),
    	                producto.getPrecioUnitario(),
    	                producto.getPrecioUnitario() * producto.getCantidad()
    	            });
    	        }
    	        
    	        // 4. Actualizar totales
    	        actualizarTotal();
    	        
    	    } catch (Exception e) {
    	        JOptionPane.showMessageDialog(this, 
    	            "Error al actualizar carrito: " + e.getMessage(),
    	            "Error", JOptionPane.ERROR_MESSAGE);
    	    }
    }

    private JPanel crearPanelEncabezados() {
        JPanel panelEncabezados = new JPanel(new GridLayout(1, 3));
        panelEncabezados.setBackground(Color.WHITE);

        JLabel lblProducto = new JLabel("PRODUCTO", SwingConstants.CENTER);
        lblProducto.setFont(new Font("Arial", Font.BOLD, 12));
        lblProducto.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));

        JLabel lblPrecioUnit = new JLabel("PRECIO UNIT.", SwingConstants.CENTER);
        lblPrecioUnit.setFont(new Font("Arial", Font.BOLD, 12));
        lblPrecioUnit.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));

        JLabel lblTotal = new JLabel("TOTAL", SwingConstants.CENTER);
        lblTotal.setFont(new Font("Arial", Font.BOLD, 12));
        lblTotal.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));

        panelEncabezados.add(lblProducto);
        panelEncabezados.add(lblPrecioUnit);
        panelEncabezados.add(lblTotal);

        return panelEncabezados;
    }

    private JPanel crearPanelPagoYResumen() {
        JPanel panelHorizontal = new JPanel();
        panelHorizontal.setLayout(new BoxLayout(panelHorizontal, BoxLayout.X_AXIS));
        panelHorizontal.setAlignmentX(CENTER_ALIGNMENT);
        panelHorizontal.setMaximumSize(new Dimension(550, 255));

        // Panel monto
        JPanel panelMonto = crearPanelPagoMonto();
        panelMonto.setMaximumSize(new Dimension(250, 200));
        
        // Agregar ambos al contenedor horizontal
        panelHorizontal.add(panelMonto);
        panelHorizontal.add(Box.createRigidArea(new Dimension(20, 0)));

        return panelHorizontal;
    }
    private void actualizarResumenFinanciero(double subtotal) {
        // Buscar los componentes del panel financiero
        Component[] components = panelResumenDerecho.getParent().getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                if (panel.getComponentCount() == 6) { // Nuestro panel financiero tiene 6 componentes
                    // Calcular IVA y total
                    double iva = subtotal * 0.16;
                    double total = subtotal + iva;

                    // Actualizar los valores en los componentes del panel
                    ((JLabel) panel.getComponent(1)).setText("$" + String.format("%.2f", subtotal));
                    ((JLabel) panel.getComponent(3)).setText("$" + String.format("%.2f", iva));
                    ((JLabel) panel.getComponent(5)).setText("$" + String.format("%.2f", total));

                    controlador.setTotalVenta(total);
                    actualizarTotal();
                    break;
                }
            }
        }
    } 
    private void actualizarTotal() {
        // Obtener el total del controlador
        double total = controlador.getTotalVenta();
        
        // Formatear el número con 2 decimales
        DecimalFormat df = new DecimalFormat("#,##0.00");
        
        // Actualizar el JLabel del semicírculo
        totalLabel.setText("Total: $" + df.format(total));
        
        // Si hay monto recibido, actualizar el cambio también
        if (!montoRecibidoField.getText().isEmpty()) {
            calcularCambio();
        }
    }
    
    private JPanel crearPanelTelefono() {
        JPanel telefonoPanelContainer = crearPanelFondoRedondeado( Color.LIGHT_GRAY);
        telefonoPanelContainer.setMaximumSize(new Dimension(320, 150));
        
        JPanel telefonoPanel = new JPanel();
        telefonoPanel.setLayout(new BoxLayout(telefonoPanel, BoxLayout.Y_AXIS));
        telefonoPanel.setOpaque(false);
        
        JLabel telefonoLabel = new JLabel("Teléfono Cliente:");
        telefonoLabel.setFont(new Font("Irish Grover", Font.BOLD, 14));
        telefonoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        telefonoField = new JTextField();
        telefonoField.setMaximumSize(new Dimension(300, 30));
        telefonoField.setHorizontalAlignment(JTextField.CENTER);
        telefonoField.setFont(new Font("Arial", Font.PLAIN, 16));
        
        MouseAdapter focusAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                telefonoField.requestFocusInWindow();
            }
        };
        
        telefonoPanel.addMouseListener(focusAdapter);
        telefonoLabel.addMouseListener(focusAdapter);
        telefonoPanelContainer.addMouseListener(focusAdapter);
        
        telefonoField.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!(Character.isDigit(c) || (c == KeyEvent.VK_BACK_SPACE) || (c == KeyEvent.VK_DELETE))) {
                    e.consume();
                }
            }
        });
        
        puntosLabel = new JLabel("Puntos: 0");
        puntosLabel.setFont(new Font("Arial", Font.BOLD, 14));
        puntosLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton buscarBtn = new JButton("Buscar (Enter)");
        buscarBtn.setMaximumSize(new Dimension(120, 25));
        buscarBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        buscarBtn.addActionListener(e -> buscarPuntosCliente());
        
        telefonoPanel.add(telefonoLabel);
        telefonoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        telefonoPanel.add(telefonoField);
        telefonoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        telefonoPanel.add(puntosLabel);
        telefonoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        telefonoPanel.add(buscarBtn);
        
        telefonoPanelContainer.add(telefonoPanel);
        return telefonoPanelContainer;
    }
    
    private JPanel crearTecladoNumerico() {
        JPanel tecladoMainPanel = new JPanel(new BorderLayout(5, 5));
        tecladoMainPanel.setMaximumSize(new Dimension(300, 200));
        tecladoMainPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel tecladoNumerosPanel = new JPanel(new GridLayout(4, 3, 5, 5));
        
        JPanel verticalButtonsPanel = new JPanel();
        verticalButtonsPanel.setLayout(new BoxLayout(verticalButtonsPanel, BoxLayout.Y_AXIS));
        verticalButtonsPanel.setPreferredSize(new Dimension(80, 0));

        JButton btn7 = new JButton("7");
        JButton btn8 = new JButton("8");
        JButton btn9 = new JButton("9");
        JButton btn4 = new JButton("4");
        JButton btn5 = new JButton("5");
        JButton btn6 = new JButton("6");
        JButton btn1 = new JButton("1");
        JButton btn2 = new JButton("2");
        JButton btn3 = new JButton("3");
        JButton btn0 = new JButton("0");
        JButton btnPunto = new JButton(".");
        
        JButton btnBorrar = new JButton();
        try {
            ImageIcon borrarIcon = new ImageIcon("imagen\\borrar.jpeg");
            Image img = borrarIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            btnBorrar.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            btnBorrar.setText("⌫");
        }
        btnBorrar.setToolTipText("Borrar");
        btnBorrar.setPreferredSize(new Dimension(70, 50));
        btnBorrar.setMaximumSize(new Dimension(70, 50));
        
        JButton btnEnter = new JButton();
        try {
            ImageIcon enterIcon = new ImageIcon("imagen\\enter.jpeg");
            Image img = enterIcon.getImage().getScaledInstance(20, 60, Image.SCALE_SMOOTH);
            btnEnter.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            btnEnter.setText("ENTER");
        }
        btnEnter.setBackground(Color.WHITE);
        btnEnter.setForeground(Color.WHITE);
        btnEnter.setPreferredSize(new Dimension(70, 110));
        btnEnter.setMaximumSize(new Dimension(70, 110));
        
        btn7.addActionListener(e -> manejarTeclaCalculadora("7"));
        btn8.addActionListener(e -> manejarTeclaCalculadora("8"));
        btn9.addActionListener(e -> manejarTeclaCalculadora("9"));
        btn4.addActionListener(e -> manejarTeclaCalculadora("4"));
        btn5.addActionListener(e -> manejarTeclaCalculadora("5"));
        btn6.addActionListener(e -> manejarTeclaCalculadora("6"));
        btn1.addActionListener(e -> manejarTeclaCalculadora("1"));
        btn2.addActionListener(e -> manejarTeclaCalculadora("2"));
        btn3.addActionListener(e -> manejarTeclaCalculadora("3"));
        btn0.addActionListener(e -> manejarTeclaCalculadora("0"));
        btnPunto.addActionListener(e -> manejarTeclaCalculadora("."));
        btnBorrar.addActionListener(e -> manejarTeclaCalculadora("C"));
        btnEnter.addActionListener(e -> calcularCambio());
        
        Font btnFont = new Font("Arial", Font.BOLD, 16);
        btn7.setFont(btnFont);
        btn8.setFont(btnFont);
        btn9.setFont(btnFont);
        btn4.setFont(btnFont);
        btn5.setFont(btnFont);
        btn6.setFont(btnFont);
        btn1.setFont(btnFont);
        btn2.setFont(btnFont);
        btn3.setFont(btnFont);
        btn0.setFont(btnFont);
        btnPunto.setFont(btnFont);
        
        tecladoNumerosPanel.add(btn7);
        tecladoNumerosPanel.add(btn8);
        tecladoNumerosPanel.add(btn9);
        tecladoNumerosPanel.add(btn4);
        tecladoNumerosPanel.add(btn5);
        tecladoNumerosPanel.add(btn6);
        tecladoNumerosPanel.add(btn1);
        tecladoNumerosPanel.add(btn2);
        tecladoNumerosPanel.add(btn3);
        tecladoNumerosPanel.add(btn0);
        tecladoNumerosPanel.add(btnPunto);
        
        verticalButtonsPanel.add(btnBorrar);
        verticalButtonsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        verticalButtonsPanel.add(btnEnter);
        
        tecladoMainPanel.add(tecladoNumerosPanel, BorderLayout.CENTER);
        tecladoMainPanel.add(verticalButtonsPanel, BorderLayout.EAST);
        
        return tecladoMainPanel;
    }
    
    private JPanel crearBotonesAccion() {
        JPanel botonesPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        botonesPanel.setMaximumSize(new Dimension(300, 50));
        botonesPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton cancelarBtn = new JButton("Cancelar");
        cancelarBtn.addActionListener(e -> cancelarVenta());
       
        botonesPanel.add(cancelarBtn);
        
        return botonesPanel;
    }
    
    private void manejarTeclaCalculadora(String tecla) {
        JTextField campoDestino = telefonoField.hasFocus() ? telefonoField : montoRecibidoField;
        
        String textoActual = campoDestino.getText();
        
        switch (tecla) {
            case "C":
                campoDestino.setText("");
                break;
            case ".":
                if (campoDestino == montoRecibidoField && !textoActual.contains(".")) {
                    campoDestino.setText(textoActual + ".");
                }
                break;
            default:
                if (campoDestino == telefonoField) {
                    if (textoActual.length() < 10) {
                        campoDestino.setText(textoActual + tecla);
                    }
                } else {
                    campoDestino.setText(textoActual + tecla);
                }
                break;
        }
        
        if (campoDestino == montoRecibidoField) {
            calcularCambio();
        }
    }
    
    private void buscarPuntosCliente() {
        String telefono = telefonoField.getText().trim();
        
        if (!validarTelefono(telefono)) {
            JOptionPane.showMessageDialog(this, 
                "Teléfono debe tener 10 dígitos", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            Cliente cliente = clientee.buscarPorTelefono(telefono);
            
            if (cliente == null) {
                int opcion = JOptionPane.showConfirmDialog(
                    this,
                    "Cliente no registrado\n¿Desea registrarlo ahora?",
                    "Nuevo cliente",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
                
                if (opcion == JOptionPane.YES_OPTION) {
                    cliente = registrarClienteRapido(telefono);
                }
            }
            
            actualizarInfoCliente(cliente);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error: " + e.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void actualizarInfoCliente(Cliente cliente) {
        String telefono = telefonoField.getText().trim();
        
        if (cliente != null) {
            puntosLabel.setText("Puntos: " + cliente.getPuntos());
            if (lblInfoCliente != null) {
                lblInfoCliente.setText(cliente.getNombre() + " - " + telefono);
            }
            
            // Mostrar mensaje de bienvenida para nuevos clientes
            if (cliente.getPuntos() == 0) {
                JOptionPane.showMessageDialog(this,
                    "¡Bienvenido " + cliente.getNombre() + "!\n" +
                    "Comienza a acumular puntos con esta compra.",
                    "Nuevo cliente",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            puntosLabel.setText("Puntos: 0");
            if (lblInfoCliente != null) {
                lblInfoCliente.setText("Cliente no registrado - " + telefono);
            }
        }
    }

    private boolean validarTelefono(String telefono) {
        return telefono.matches("^\\d{10}$"); // Exactamente 10 dígitos
    }
    
    private Cliente registrarClienteRapido(String telefono) {
        // Panel con solo los campos necesarios
        JPanel panel = new JPanel(new GridLayout(1, 2, 5, 5));
        
        JTextField txtNombre = new JTextField(20);
        
        panel.add(new JLabel("Nombre completo:"));
        panel.add(txtNombre);
        
        int result = JOptionPane.showConfirmDialog(
            this,
            panel,
            "Registro rápido - Teléfono: " + telefono,
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String nombre = txtNombre.getText().trim();
            
            if (nombre.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El nombre es obligatorio", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            
            try {
                Cliente nuevoCliente = new Cliente();
                nuevoCliente.setTelefono(telefono);
                nuevoCliente.setNombre(nombre);
                nuevoCliente.setPuntos(0); // Inicia con 0 puntos
                
                // Generar ID simple (puedes mejorar esto)
                String id = "CLI-" + System.currentTimeMillis();
                nuevoCliente.setId(id);
                
                clientee.agregarCliente(nuevoCliente);
                
                JOptionPane.showMessageDialog(this, 
                    "Cliente registrado:\n" + nombre + "\nTel: " + telefono,
                    "Registro exitoso", 
                    JOptionPane.INFORMATION_MESSAGE);
                    
                return nuevoCliente;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error al registrar cliente: " + e.getMessage(),
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return null;
            }
        }
        return null;
    }
    
    
    private JPanel crearCardProducto(String imagenPath, String nombre, double precio) {
        // Panel principal con bordes más sutiles y mayor padding
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230)), // Borde más claro
            BorderFactory.createEmptyBorder(8, 8, 8, 8) // Más espacio interno
        ));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setPreferredSize(new Dimension(180, 180)); // Tamaño preferido para cada tarjeta
        card.setMaximumSize(new Dimension(180, 180)); // Tamaño máximo para cada tarjeta
        card.setSize(180,180);
        // Tamaño aumentado para imágenes
        int anchoImagen = 100; 
        int altoImagen = 100;  
        
        // Contenedor de imagen con sombra sutil
        JPanel imagenContainer = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Sombra sutil en la parte inferior
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(220, 220, 220, 100));
                g2d.fillRoundRect(2, 4, getWidth()-4, getHeight()-2, 10, 10);
            }
        };
        imagenContainer.setOpaque(false);
        imagenContainer.setPreferredSize(new Dimension(anchoImagen, altoImagen));
       // imagenContainer.setMaximumSize(new Dimension(anchoImagen, altoImagen));
        
        // Imagen centrada con fondo blanco
        JLabel imagenLabel = new JLabel();
        imagenLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imagenLabel.setVerticalAlignment(SwingConstants.CENTER);
        imagenLabel.setBackground(Color.WHITE);
        imagenLabel.setOpaque(true);
        imagenLabel.setBorder(BorderFactory.createLineBorder(new Color(240, 240, 240)));
        
        ImageIcon icono = cargarImagenDesdeRuta(imagenPath, anchoImagen-20, altoImagen-20); // Imagen ligeramente más pequeña que el contenedor
        imagenLabel.setIcon(icono);
        imagenContainer.add(imagenLabel, BorderLayout.CENTER);
        
        card.add(imagenContainer);
        card.add(Box.createRigidArea(new Dimension(0, 2))); // Espacio entre imagen y texto
        
        // Nombre del producto con mejor formato
        JLabel nombreLabel = new JLabel("<html><center style='width:150px; padding:5px;'>" + nombre + "</center></html>");
        nombreLabel.setFont(new Font("Segoe UI", Font.BOLD, 11)); // Fuente más grande y moderna
        nombreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nombreLabel.setForeground(new Color(60, 60, 60)); // Texto más oscuro
        card.add(nombreLabel);
        
        card.add(Box.createRigidArea(new Dimension(0, 2))); // Espacio entre nombre y precio
        
        // Precio con diseño más destacado
        JLabel precioLabel = new JLabel(String.format("$%,.2f", precio));
        precioLabel.setFont(new Font("Segoe UI", Font.BOLD, 12)); // Más grande
        precioLabel.setForeground(new Color(0, 120, 60)); // Verde más profesional
        precioLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Fondo sutil para el precio
        JPanel precioContainer = new JPanel();
        precioContainer.setBackground(new Color(245, 250, 245));
        precioContainer.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        precioContainer.add(precioLabel);
        card.add(precioContainer);
        
        // Efectos hover mejorados
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                card.setBackground(new Color(248, 248, 248));
              //  card.setBorder(BorderFactory.createCompoundBorder(
                //    BorderFactory.createLineBorder(new Color(200, 220, 240)),
              //      BorderFactory.createEmptyBorder(10, 10, 10, 10)
              //  ));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                card.setBackground(Color.WHITE);
               // card.setBorder(BorderFactory.createCompoundBorder(
               //     BorderFactory.createLineBorder(new Color(230, 230, 230)),
                //    BorderFactory.createEmptyBorder(10, 10, 10, 10)
               // ));
            }
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                agregarProducto(nombre, precio);
            }
        });
        
        return card;
    }
    
    private ImageIcon cargarImagenDesdeRuta(String imagenPath, int anchoDeseado, int altoDeseado) {
        try {
            ImageIcon iconoOriginal;
            
            // Cargar la imagen desde la ruta especificada
            if (imagenPath.startsWith("/") || imagenPath.contains(":")) {
                // Ruta absoluta
                iconoOriginal = new ImageIcon(imagenPath);
            } else {
                // Buscar en recursos (src/imagen/)
                URL imageUrl = getClass().getResource("/imagen/" + imagenPath);
                if (imageUrl != null) {
                    iconoOriginal = new ImageIcon(imageUrl);
                } else {
                    throw new Exception("Imagen no encontrada en recursos");
                }
            }
            
            // Redimensionar manteniendo relación de aspecto
            Image imagenOriginal = iconoOriginal.getImage();
            Image imagenRedimensionada = redimensionarImagen(imagenOriginal, anchoDeseado, altoDeseado);
            
            return new ImageIcon(imagenRedimensionada);
        } catch (Exception e) {
            System.err.println("Error al cargar imagen: " + e.getMessage());
            // Crear un icono de placeholder
            return crearPlaceholder(anchoDeseado, altoDeseado);
        }
    }

    private Image redimensionarImagen(Image imagenOriginal, int anchoDeseado, int altoDeseado) {
        // Calcular nuevas dimensiones manteniendo relación de aspecto
        double relacionOriginal = (double)imagenOriginal.getWidth(null) / imagenOriginal.getHeight(null);
        double relacionDeseada = (double)anchoDeseado / altoDeseado;
        
        int anchoFinal = anchoDeseado;
        int altoFinal = altoDeseado;
        
        if (relacionOriginal > relacionDeseada) {
            // La imagen es más ancha que lo deseado
            altoFinal = (int)(anchoDeseado / relacionOriginal);
        } else {
            // La imagen es más alta que lo deseado
            anchoFinal = (int)(altoDeseado * relacionOriginal);
        }
        
        // Redimensionar con suavizado
        return imagenOriginal.getScaledInstance(anchoFinal, altoFinal, Image.SCALE_SMOOTH);
    }

    private ImageIcon crearPlaceholder(int ancho, int alto) {
        // Crear una imagen de placeholder simple
        BufferedImage imagen = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = imagen.createGraphics();
        
        // Fondo blanco
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, ancho, alto);
        
        // Texto "Sin imagen"
        g2d.setColor(Color.GRAY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        String texto = "Sin imagen";
        FontMetrics fm = g2d.getFontMetrics();
        int x = (ancho - fm.stringWidth(texto)) / 2;
        int y = (alto - fm.getHeight()) / 2 + fm.getAscent();
        g2d.drawString(texto, x, y);
        
        g2d.dispose();
        return new ImageIcon(imagen);
    }
    
    private JPanel crearPanelSuperior() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(255, 198, 144));
        topPanel.setPreferredSize(new Dimension(getWidth(), 60));
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        
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
        
        topPanel.add(rightPanel, BorderLayout.EAST);
        return topPanel;
    }
    
    private JPanel crearMenuHorizontal() {
        JPanel menuPanel = new JPanel(new GridLayout(1, 7));
        menuPanel.setBackground(new Color(230, 230, 230));
        menuPanel.setPreferredSize(new Dimension(0, 50));
        menuPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Color.GRAY));
        
        String[] opciones = {"Productos", "Reportes", "Inventario", "Cliente", "Proveedores", "Usuarios", "Salir"};
        
        for (String opcion : opciones) {
            JButton btn = crearBotonMenu(opcion, opcion.equals("Productos"));
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
        
        // Configuración de colores según estado
        if (esActivo) {
            // Estilo para el botón activo (Proveedores)
            boton.setBackground(new Color(216, 237, 88));
            boton.setForeground(Color.BLACK);
        } else {
            // Estilo para botones inactivos
            boton.setBackground(Color.GRAY);
            boton.setForeground(Color.BLACK);
        }
        
        // Comportamiento al pasar el mouse (solo para botones inactivos)
        if (!esActivo) {
            boton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    boton.setBackground(new Color(216, 237, 88));
                    boton.setForeground(Color.WHITE);
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    boton.setBackground(Color.GRAY);
                    boton.setForeground(Color.BLACK);
                }
            });
        }
        
        return boton;
    }
    private JButton crearBotonMenu(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Arial", Font.BOLD, 14));
        boton.setBackground(Color.GRAY);
        boton.setForeground(Color.BLACK);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        boton.setPreferredSize(new Dimension(0, 50));
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        
        // Guardar el color original
        Color originalBg = boton.getBackground();
        Color originalFg = boton.getForeground();
        
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                // Solo cambiar si no es el botón activo
                if (!boton.getBackground().equals(new Color(216, 237, 88))) {
                    boton.setBackground(new Color(216, 237, 88));
                    boton.setForeground(Color.WHITE);
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                // Solo cambiar si no es el botón activo
                if (!boton.getBackground().equals(new Color(216, 237, 88))) {
                    boton.setBackground(originalBg);
                    boton.setForeground(originalFg);
                }
            }
        });
        
        return boton;
    }
    
    private void manejarAccionMenu(String opcion) {
        switch (opcion) {
            case "Salir":
                this.dispose();
                new menuprincipal(usuario).setVisible(true);
                break;
            case "Productos":
            	JOptionPane.showMessageDialog(this, "Ya estás en la ventana de Ventas.");
                break;
            case "Reportes":
				this.dispose();
            	  reportes vistaReportes = new reportes(usuario, new ReportesControlador(null, usuario));
            	    vistaReportes.setVisible(true); // Muestra la ventana
            	    break;
            case "Inventario":
            	this.dispose();
                new inventario(usuario).setVisible(true);
                break;
            case "Cliente":
            	this.dispose();
                new clientes(usuario, clientee).setVisible(true);
                break;
            case "Proveedores":
            	this.dispose();
                new proveedores(usuario).setVisible(true);
                break;
            case "Usuario":
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
            // Primero, aseguramos que la ventana 'producto' se cierre antes de mostrar 'Login'
            
            // Cerrar la ventana 'producto' de forma segura
            Window[] windows = Window.getWindows();
            for (Window window : windows) {
                if (window instanceof producto) {
                    window.setVisible(false); // Aseguramos que la ventana 'producto' se haga invisible
                    window.dispose(); // Y luego la eliminamos
                }
            }
            // Ahora abrimos la ventana 'Login'
            new Login().setVisible(true);
            
            // Cerramos el JDialog
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
    
    private void agregarProductosAlPanel(List<Object[]> productos) {
        for (Object[] producto : productos) {
            JPanel cardProducto = crearCardProducto(
                (String)producto[0], 
                (String)producto[1], 
                (double)producto[2]
            );
            productosPanel.add(cardProducto);
        }
    }
    
    private void buscarProducto(String textoBusqueda) {
        if (textoBusqueda.isEmpty()) {
            mostrarProductosDeCategoria("TODOS");
            return;
        }
        
        productosPanel.removeAll();
        textoBusqueda = textoBusqueda.toLowerCase();
        
        for (List<Object[]> productos : productosPorCategoria.values()) {
            for (Object[] producto : productos) {
                String nombreProducto = ((String)producto[1]).toLowerCase();
                if (nombreProducto.contains(textoBusqueda)) {
                    JPanel cardProducto = crearCardProducto(
                        (String)producto[0], 
                        (String)producto[1], 
                        (double)producto[2]
                    );
                    productosPanel.add(cardProducto);
                }
            }
        }
        
        productosPanel.revalidate();
        productosPanel.repaint();
    }
   
    private class RoundedBorder implements Border {
        private int radius;
        
        public RoundedBorder(int radius) {
            this.radius = radius;
        }
        
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(this.radius+1, this.radius+1, this.radius+1, this.radius+1);
        }
        
        @Override
        public boolean isBorderOpaque() {
            return true;
        }
        
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c.getBackground());
            g2.drawRoundRect(x, y, width-1, height-1, radius, radius);
        }
    }   

    private void realizarPago() {
        // Obtener datos del formulario
        String telefono = telefonoField.getText().trim();
        String montoTexto = montoRecibidoField.getText().trim();
        
        // Validación básica del carrito
        if (controlador.getCarrito().getProductos().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No hay productos en el carrito", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            // Procesar el pago con el controlador
            boolean exito = controlador.procesarPago(metodoPago, montoTexto, telefono);
            
            if (exito) {
                // Mostrar mensaje de éxito
                String mensajeExito = "Pago realizado exitosamente\n" +
                                   "Método: " + metodoPago + "\n" +
                                   "Total: $" + String.format("%,.2f", controlador.getTotalVenta());
                
                // Mostrar cambio si es pago en efectivo
                if (metodoPago.equalsIgnoreCase("EFECTIVO")) {
                    try {
                        double montoRecibido = Double.parseDouble(montoTexto);
                        double total = controlador.getTotalVenta();
                        if (montoRecibido > total) {
                            mensajeExito += "\nCambio: $" + String.format("%,.2f", montoRecibido - total);
                        }
                    } catch (NumberFormatException e) {
                        // Ignorar error de formato ya que ya pasó validación
                    }
                }
                
                try {
                    String metodoPago = "TARJETA"; // O obtenerlo de la interfaz
                    // Verificación previa
                    if (controlador.getCarrito().getProductos().isEmpty()) {
                        JOptionPane.showMessageDialog(this, "No hay productos en el carrito", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (exito) {
                        JOptionPane.showMessageDialog(this, 
                            "Pago con tarjeta realizado exitosamente", 
                            "Éxito", 
                            JOptionPane.INFORMATION_MESSAGE);
                        resetearInterfazVenta();
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, 
                        "Error al procesar pago con tarjeta: " + e.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
                
                JOptionPane.showMessageDialog(this, 
                    mensajeExito, 
                    "Pago exitoso", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Resetear la interfaz
                resetearInterfazVenta();
                
                // Mostrar el ticket generado
                mostrarTicketGenerado();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error al procesar el pago: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarTicketGenerado() {
        if (Desktop.isDesktopSupported()) {
            try {
                // Buscar el archivo más reciente de ticket
                File dir = new File(".");
                File[] files = dir.listFiles((dir1, name) -> 
                    name.startsWith("ticket_venta_") && name.endsWith(".pdf"));
                
                if (files != null && files.length > 0) {
                    // Ordenar por fecha de modificación (el más reciente primero)
                    Arrays.sort(files, (f1, f2) -> 
                        Long.compare(f2.lastModified(), f1.lastModified()));
                    Desktop.getDesktop().open(files[0]);
                }
            } catch (IOException e) {
                System.out.println("No se pudo abrir el ticket automáticamente: " + e.getMessage());
            }
        }
    }
    private void resetearInterfazVenta() {
        // Limpiar campos
        montoRecibidoField.setText("");
        cambioLabel.setText("Cambio: $0.00");
        montoRecibidoLabel.setText("$0.00");
        telefonoField.setText("");
        puntosLabel.setText("Puntos: 0");
        
        // Actualizar componentes
        actualizarTotal();
        actualizarPanelResumenDerecho();
        
        // Resetear otros estados si es necesario
        cantidad = 1;
        updateCantidadLabel();
    }

    private void mostrarComprobante() {
        // Opcional: Mostrar resumen de la compra
        String resumen = controlador.generarResumenVenta();
        JOptionPane.showMessageDialog(this, 
            "Venta realizada con éxito\n\n" + resumen,
            "Comprobante de pago", 
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void cancelarVenta() {
        controlador.cancelarVenta();
        actualizarTotal();
        montoRecibidoField.setText("");
        cambioLabel.setText("Cambio: $0.00");
        montoRecibidoLabel.setText("$0.00");
        telefonoField.setText("");
        puntosLabel.setText("Puntos: 0");
        actualizarResumenFinanciero(0.0);
    }
    private void calcularCambio() {
        try {
            double monto = Double.parseDouble(montoRecibidoField.getText());
            double cambio = monto - controlador.getTotalVenta();

            if (cambio >= 0) {
                cambioLabel.setText("Cambio: $" + String.format("%.2f", cambio));
                cambioLabel.setForeground(Color.BLACK);
            } else {
                cambioLabel.setText("Faltan: $" + String.format("%.2f", -cambio));
                cambioLabel.setForeground(Color.RED);
            }

            montoRecibidoLabel.setText("$" + String.format("%.2f", monto));
        } catch (NumberFormatException e) {
            cambioLabel.setText("Cambio: $0.00");
            montoRecibidoLabel.setText("$0.00");
        }
    }

    private void modificarCantidad(int cambio) {
        cantidad += cambio;
        
        // Validar límites (mínimo 1, máximo 99)
        if (cantidad < 1) cantidad = 1;
        if (cantidad > 99) cantidad = 99;
        
        updateCantidadLabel();
    }

    private void updateCantidadLabel() {
        // Ahora tenemos referencia directa al JLabel
        lblNumeroCantidad.setText(String.valueOf(cantidad));
        
        // Cambiar color si la cantidad es alta
        if (cantidad > 10) {
            lblNumeroCantidad.setForeground(Color.RED);
        } else {
            lblNumeroCantidad.setForeground(Color.BLACK);
        }
    }

    private void agregarProducto(String nombre, double precio) {
        if (cantidad <= 0) {
            JOptionPane.showMessageDialog(this, "Cantidad inválida", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            Producto productoBD = inventarioo.obtenerProductoConBloqueo(nombre);
            
            if (productoBD == null) {
                JOptionPane.showMessageDialog(this, "Producto no encontrado", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (productoBD.getCantidadDisponible() < cantidad) {
                JOptionPane.showMessageDialog(this, 
                    "Stock insuficiente\nDisponible: " + productoBD.getCantidadDisponible(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Reservar producto
            if (!inventarioo.reservarProducto(productoBD.getId(), cantidad)) {
                JOptionPane.showMessageDialog(this, 
                    "No se pudo reservar el producto", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Agregar al carrito y actualizar interfaz
            controlador.agregarProducto(
                productoBD.getId(),
                productoBD.getNombre(),
                productoBD.getPrecioVenta(),
                cantidad
            );
            
            // Actualizaciones necesarias:
            actualizarTablaCarrito();  // Actualiza la tabla del carrito
            actualizarTotal();         // Actualiza el semicírculo
            updateCantidadLabel();     // Restablece la cantidad a 1
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error al verificar disponibilidad: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarVistaCompletaCarrito() {
        actualizarTablaCarrito();
        panelResumenDerecho.revalidate();
        panelResumenDerecho.repaint();
    }
    
	public boolean actualizarProductoConConcurrencia(Producto producto) throws SQLException {
	    String sql = "UPDATE Productos SET nombre = ?, cantidad_disponible = ?, version = ? " +
	                "WHERE id = ? AND version = ?";
	    
	    try (Connection conn = ConexionAccess.conectar();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        
	        pstmt.setString(1, producto.getNombre());
	        pstmt.setInt(2, producto.getCantidadDisponible());
	        pstmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
	        pstmt.setString(4, producto.getId());
	        pstmt.setTimestamp(5, producto.getVersion());
	        
	        int affectedRows = pstmt.executeUpdate();
	        return affectedRows > 0;
	    }
	}
}