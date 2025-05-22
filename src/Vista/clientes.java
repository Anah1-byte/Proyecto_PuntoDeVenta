package Vista;
import Modelo.Cliente;
import Modelo.Clientee;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.JTextField;
import com.toedter.calendar.JDateChooser;
import Controlador.ClientesContro;
import Controlador.ReportesControlador;
import Modelo.Usuario;
import Modelo.Usuarioo;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class clientes extends JFrame {
    private Usuario usuario;
	private String rolUsuario;
	private JTable tablaClientes;
	  private DefaultTableModel modeloTabla;
   public JButton btnAgregar, btnEditar, btnEliminar;
	private Cliente nuevoCliente;
	private ClientesContro controlador;
    private final Clientee clientee;
    private JTextField txtTelefono;
    private JTextField txtNombre;
    private JTextField txtFecha;
    private JTextField txtPuntos;
    
    public clientes(Usuario usuario, Clientee clientee) {
    	this.usuario = usuario;
        this.clientee = clientee;
        
		this.modeloTabla = new DefaultTableModel(new Object[] { "ID", "TELÉFONO", "NOMBRE", "ÚLTIMA COMPRA", "PUNTOS" },
				0);
        
        // Inicializar el controlador PASANDO el clienteDAO correctamente
        this.controlador = new ClientesContro(this.clientee, this, new Cliente(), this.modeloTabla);
        
        Usuarioo usuarioDAO = new Usuarioo();
        this.rolUsuario = usuario.getRol();
        
        initUI();
       
    }
    
    private void initUI() {
        setTitle("El Habanerito - Clientes");
        setSize(1517, 903);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        // Inicializar el modelo de tabla
        modeloTabla = new DefaultTableModel(
            new Object[]{"ID", "TELÉFONO", "NOMBRE", "ÚLTIMA COMPRA", "PUNTOS"}, 0);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(null);
        
        // 1. Crear componentes superiores
        JPanel northContainer = new JPanel();
        northContainer.setLayout(new BoxLayout(northContainer, BoxLayout.Y_AXIS));
        northContainer.add(crearPanelSuperior());
        northContainer.add(crearMenuHorizontal());
        northContainer.add(Box.createRigidArea(new Dimension(0, 10)));
        
        JPanel titleWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titleWrapper.setOpaque(false);
        titleWrapper.add(crearPanelTituloRedondeado());
        northContainer.add(titleWrapper);
        northContainer.add(Box.createRigidArea(new Dimension(0, 15)));
        
        mainPanel.add(northContainer, BorderLayout.NORTH);
        
        // 2. Crear tabla y área central
        JScrollPane scrollPane = crearTablaClientes();
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setOpaque(false);
        mainContent.add(scrollPane, BorderLayout.CENTER);
        
        mainPanel.add(mainContent, BorderLayout.CENTER);
        
        // 3. Crear panel inferior
        mainPanel.add(crearPanelInferior(), BorderLayout.SOUTH);
        
        // 4. Configurar acciones y añadir al frame
        configurarAccionesBotones();
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        
        // 5. Cargar datos iniciales
        actualizarTablaClientes();
    }
    
    
    private JPanel crearPanelInferior() {
        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.setBackground(Color.PINK);
        panelInferior.setBorder(null);
        panelInferior.setPreferredSize(new Dimension(0, 90));

        JPanel botones = crearPanelBotonesAccion();
        botones.setOpaque(false); // para que el fondo rosa se vea

        panelInferior.add(botones, BorderLayout.CENTER);

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
    
  
    private JPanel crearPanelSuperior() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(255, 198, 144));
        topPanel.setPreferredSize(new Dimension(0, 60));
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
        JPanel menuPanel = new JPanel(new GridLayout(1, 5));
        menuPanel.setBackground(new Color(230, 230, 230));
        menuPanel.setPreferredSize(new Dimension(0, 50));
        menuPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Color.GRAY));
        
        String[] opciones = {"Productos", "Reportes", "Inventario", "Cliente","Proveedores","Usuarios", "Salir"};
        
        for (String opcion : opciones) {
            JButton btn = crearBotonMenu(opcion);
            btn.addActionListener(e -> manejarAccionMenu(opcion));
            menuPanel.add(btn);
        }
        
        return menuPanel;
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
        	this.dispose();
        	  reportes vistaReportes = new reportes(usuario, new ReportesControlador(null, usuario));
        	    vistaReportes.setVisible(true); // Muestra la ventana
        	    break;
        case "Inventario":
        	this.dispose();
            new inventario(usuario).setVisible(true);
            break;
        case "Cliente":
        	JOptionPane.showMessageDialog(this, "Ya estás en la ventana de Clientes.");
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
            changeUserDialog.dispose();
            this.dispose();
            new Login().setVisible(true);
        });
        
        cancelarBtn.addActionListener(e -> changeUserDialog.dispose());
       
        buttonPanel.add(cambiarBtn);
        buttonPanel.add(cancelarBtn);
        
        dialogPanel.add(instructionLabel, BorderLayout.CENTER);
        dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        changeUserDialog.getContentPane().add(dialogPanel);
        changeUserDialog.setVisible(true);
    }
    
    private JPanel crearPanelTituloRedondeado() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Fondo verde con degradado
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(208, 244, 167), // Verde claro arriba
                    0, getHeight(), new Color(208, 244, 167) // Verde más oscuro abajo
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 50, 50);
                
                // Borde sutil
                g2d.setColor(new Color(0, 100, 0, 150));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 50, 50);
            }
        };
        
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(400, 60)); // Más ancho para mejor proporción
        panel.setMaximumSize(new Dimension(400, 60)); // Evita que se estire demasiado
        
        JLabel lblTitulo = new JLabel("CLIENTES");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 28));
        lblTitulo.setForeground(Color.BLACK);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        
        panel.add(lblTitulo, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
        
        return panel;
    }
    
    private JPanel crearCeldaRedondeada(String texto, boolean esEncabezado) {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Fondo gris para encabezados
                if (esEncabezado) {
                    g2d.setColor(new Color(220, 220, 220)); // Gris claro
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                }
                
                // Borde negro para todos
                g2d.setColor(Color.BLACK);
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
            }
        };
        
        panel.setOpaque(false); // Fondo transparente (para celdas normales)
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        JLabel label = new JLabel(texto, SwingConstants.CENTER);
        label.setFont(new Font("Arial", esEncabezado ? Font.BOLD : Font.PLAIN, 
                             esEncabezado ? 16 : 14));
        panel.add(label, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel crearFilaCliente(String telefono, String nombre, 
            String ultimaCompra, String puntos) {
JPanel fila = new JPanel(new GridLayout(1, 4, 10, 0));
fila.setOpaque(false);
fila.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

fila.add(crearCeldaRedondeada(telefono, false));
fila.add(crearCeldaRedondeada(nombre, false));
fila.add(crearCeldaRedondeada(ultimaCompra, false));
fila.add(crearCeldaRedondeada(puntos, false));

return fila;
}
    
    private JPanel crearEncabezadoTabla() {
        JPanel encabezado = new JPanel(new GridLayout(1, 4, 10, 0));
        encabezado.setOpaque(false); // Panel contenedor transparente
        
        String[] titulos = {"TELÉFONO", "NOMBRE", "ÚLTIMA COMPRA", "PUNTOS"};
        
        for (String titulo : titulos) {
            // true indica que es encabezado (fondo gris)
            encabezado.add(crearCeldaRedondeada(titulo, true)); 
        }
        
        return encabezado;
    }
    
    private JScrollPane crearTablaClientes() {
        // 1. Crear el modelo de tabla primero si no existe
        if (modeloTabla == null) {
            modeloTabla = new DefaultTableModel(
                new Object[]{"ID", "TELÉFONO", "NOMBRE", "FECHA", "PUNTOS"}, 0);
        }
        
        // 2. Crear tabla con el modelo
        tablaClientes = new JTable(modeloTabla) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0, 0, 0, 0));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
     // Configuración visual de la tabla
        tablaClientes.setOpaque(false);
        tablaClientes.setRowHeight(40);
        tablaClientes.setFont(new Font("Arial", Font.PLAIN, 14));
        tablaClientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaClientes.setShowGrid(false);
        tablaClientes.setIntercellSpacing(new Dimension(0, 0));
        
        // Encabezado transparente con estilo
        JTableHeader header = tablaClientes.getTableHeader();
        header.setOpaque(false);
        header.setBackground(new Color(0, 0, 0, 0)); // Transparente
        header.setFont(new Font("Arial", Font.BOLD, 16));
        header.setForeground(Color.BLACK);
        
        // Configurar el renderizador
        tablaClientes.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                
                JPanel panel = new JPanel(new BorderLayout()) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g;
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        
                        // Fondo según selección (semi-transparente)
                        if (isSelected) {
                            g2.setColor(new Color(208, 244, 167, 200)); // Verde claro con transparencia
                        } else {
                            g2.setColor(row % 2 == 0 ? 
                                new Color(255, 255, 255, 50) : // Blanco muy transparente
                                new Color(240, 240, 240, 80)); // Gris claro transparente
                        }
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                        
                        // Borde sutil semi-transparente
                        g2.setColor(new Color(200, 200, 200, 150));
                        g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
                    }
                };
                
                panel.setOpaque(false);
                JLabel label = new JLabel(value == null ? "" : value.toString(), SwingConstants.CENTER);
                label.setFont(table.getFont());
                label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                label.setOpaque(false);
   
                panel.add(label, BorderLayout.CENTER);
                return panel;
            }
        });
        // Configurar el scroll pane
        JScrollPane scrollPane = new JScrollPane(tablaClientes) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0, 0, 0, 0));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        scrollPane.setViewportBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        return scrollPane;
    }

    private JPanel crearPanelContenido() {
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false); // Panel principal transparente
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Panel decorativo transparente con sombra
        JPanel backgroundPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Sombra sutil (semi-transparente)
                g2.setColor(new Color(0, 0, 0, 20));
                g2.fillRoundRect(2, 2, getWidth()-4, getHeight()-4, 25, 25);
            }
        };
        
        backgroundPanel.setOpaque(false);
        backgroundPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        backgroundPanel.add(crearTablaClientes(), BorderLayout.CENTER);
        
        contentPanel.add(backgroundPanel, BorderLayout.CENTER);
        return contentPanel;
    }
    
    private JPanel crearPanelBotonesAccion() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setOpaque(false);

        btnAgregar = crearBotonRedondeado("Agregar Cliente");
        btnEditar = crearBotonRedondeado("Editar");
        btnEliminar = crearBotonRedondeado("Eliminar");

        panel.add(btnAgregar);
        panel.add(btnEditar);
        panel.add(btnEliminar);

        return panel;
    }
    private JButton crearBotonRedondeado(String texto) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                super.paintComponent(g2);
                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 30, 30);
                g2.dispose();
            }
        };

        btn.setFont(new Font("Arial", Font.BOLD, 20));
        btn.setBackground(Color.LIGHT_GRAY);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 100, 10, 100));

        return btn;
    }
   
    private void configurarAccionesBotones() {
    	// Asegúrate de que el botón tenga UN solo listener
    	for (ActionListener al : btnAgregar.getActionListeners()) {
    	    btnAgregar.removeActionListener(al); // ✅ Elimina todos los listeners de forma segura
    	}
    	btnAgregar.addActionListener(e -> 
    	controlador.agregarCliente());
        
        btnEditar.addActionListener(e -> {
            controlador.editarCliente(); // Llamada directa sin validaciones
        });
        
        btnEliminar.addActionListener(e -> {
            if (btnEliminar.isEnabled()) {  // <- Previene doble ejecución
                controlador.eliminarCliente();
            }
        });
    }
    
    private void agregarCliente() {
        if (controlador == null) {
            mostrarError("Controlador no inicializado");
            return;
        }

        Cliente nuevoCliente = new Cliente();
        if (mostrarFormularioCliente(nuevoCliente)) {
            try {
                controlador.agregarCliente();
                actualizarTablaClientes();
                mostrarConfirmacion("Cliente agregado exitosamente");
            } catch (Exception ex) {
                mostrarError("Error al agregar cliente: " + ex.getMessage());
            }
        }
    }

    private void editarCliente() {
        if (controlador == null) {
            mostrarError("Controlador no inicializado");
            return;
        }

        int filaSeleccionada = tablaClientes.getSelectedRow();
        if (filaSeleccionada < 0) {
            mostrarAdvertencia("Seleccione un cliente para editar");
            return;
        }

        String telefono = (String) tablaClientes.getValueAt(filaSeleccionada, 0);
        Cliente cliente = controlador.buscarClientePorTelefono(telefono);
        
        if (cliente == null) {
            mostrarError("Cliente no encontrado");
            return;
        }

        // Guardar copia para verificar cambios
        Cliente copiaOriginal = new Cliente();
        
        if (mostrarFormularioCliente(cliente)) {
            if (!cliente.equals(copiaOriginal)) {
                try {
                    controlador.actualizarCliente(cliente);
                    actualizarTablaClientes();
                    mostrarConfirmacion("Cliente actualizado exitosamente");
                } catch (Exception ex) {
                    mostrarError("Error al actualizar cliente: " + ex.getMessage());
                    // Revertir cambios en caso de error
                    cliente.copiarDe(copiaOriginal);
                }
            } else {
                mostrarInformacion("No se realizaron cambios");
            }
        }
    }

    private void eliminarCliente() {
        if (controlador == null) {
            mostrarError("Controlador no inicializado");
            return;
        }

        int filaSeleccionada = tablaClientes.getSelectedRow();
        if (filaSeleccionada < 0) {
            mostrarAdvertencia("Seleccione un cliente para eliminar");
            return;
        }

        String telefono = (String) tablaClientes.getValueAt(filaSeleccionada, 0);
        
        if (mostrarConfirmacion("¿Está seguro de eliminar este cliente?")) {
            try {
                controlador.eliminarCliente();
                actualizarTablaClientes();
                mostrarConfirmacion("Cliente eliminado exitosamente");
            } catch (Exception ex) {
                mostrarError("Error al eliminar cliente: " + ex.getMessage());
            }
        }
    }

    public void actualizarTablaClientes() {
        modeloTabla.setRowCount(0); // Limpiar tabla
        
        try {
            // Obtener clientes desde el controlador
            List<Cliente> clientes = controlador.obtenerTodosClientes();
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            
            for (Cliente cliente : clientes) {
                // Formatear la fecha para mostrarla correctamente
                String fechaFormateada = "";
                if (cliente.getUltimaCompra() != null && !cliente.getUltimaCompra().isEmpty()) {
                    try {
                        Date fecha = sdf.parse(cliente.getUltimaCompra());
                        fechaFormateada = sdf.format(fecha);
                    } catch (ParseException e) {
                        fechaFormateada = cliente.getUltimaCompra();
                    }
                }
                
                modeloTabla.addRow(new Object[]{
                    cliente.getId(),
                    cliente.getTelefono(),
                    cliente.getNombre(),
                    fechaFormateada,
                    cliente.getPuntos()
                });
            }
        } catch (Exception e) {
            mostrarError("Error al cargar clientes: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public JButton getBtnAgregar() {
        return btnAgregar;
    }

    public JButton getBtnEditar() {
        return btnEditar;
    }

    public JButton getBtnEliminar() {
        return btnEliminar;
    }
    
    public boolean mostrarFormularioCliente(Cliente cliente) {
        JDialog dialog = new JDialog(this, "Nuevo Cliente", true);
        dialog.setLayout(new BorderLayout());
        dialog.setPreferredSize(new Dimension(500, 400));
        dialog.setResizable(false);

        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Título
        JLabel titleLabel = new JLabel("NUEVO CLIENTE", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Panel de formulario
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Información del Cliente"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Componentes del formulario
        JLabel lblTelefono = new JLabel("Teléfono:");
        txtTelefono = new JTextField(20);
        txtTelefono.setText(cliente.getTelefono() != null ? cliente.getTelefono() : "");

        JLabel lblNombre = new JLabel("Nombre:");
        txtNombre = new JTextField(20);
        txtNombre.setText(cliente.getNombre() != null ? cliente.getNombre() : "");

        JLabel lblFecha = new JLabel("Última Compra:");
        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("dd/MM/yyyy");
        dateChooser.setPreferredSize(new Dimension(150, 25));
        
        // Configurar fecha inicial si existe
        if (cliente.getUltimaCompra() != null && !cliente.getUltimaCompra().isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                Date fecha = sdf.parse(cliente.getUltimaCompra());
                dateChooser.setDate(fecha);
            } catch (ParseException e) {
                dateChooser.setDate(new Date()); // Fecha actual por defecto
            }
        } else {
            dateChooser.setDate(new Date()); // Fecha actual por defecto
        }

        JLabel lblPuntos = new JLabel("Puntos:");
        txtPuntos = new JTextField(20);
        txtPuntos.setText(String.valueOf(cliente.getPuntos()));
        txtPuntos.setEditable(false);

        // Agregar componentes al formulario
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(lblTelefono, gbc);
        gbc.gridx = 1;
        formPanel.add(txtTelefono, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(lblNombre, gbc);
        gbc.gridx = 1;
        formPanel.add(txtNombre, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(lblFecha, gbc);
        gbc.gridx = 1;
        formPanel.add(dateChooser, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(lblPuntos, gbc);
        gbc.gridx = 1;
        formPanel.add(txtPuntos, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnGuardar = new JButton("Guardar");

        // Estilo de botones
        btnGuardar.setBackground(new Color(76, 175, 80)); // Verde
        btnGuardar.setForeground(Color.WHITE);
        btnCancelar.setBackground(new Color(244, 67, 54)); // Rojo
        btnCancelar.setForeground(Color.WHITE);

        final boolean[] resultado = {false};

        // Acción para el botón Guardar
        btnGuardar.addActionListener(e -> {
            if (!validarDatos()) return;
            
            // Obtener fecha del JDateChooser y formatear para Access
            SimpleDateFormat sdfAccess = new SimpleDateFormat("yyyy-MM-dd");
            String fechaParaAccess = sdfAccess.format(dateChooser.getDate());

            // Asignar valores al objeto cliente
            cliente.setTelefono(txtTelefono.getText().trim());
            cliente.setNombre(txtNombre.getText().trim());
            cliente.setUltimaCompra(fechaParaAccess);
            
            try {
                cliente.setPuntos(Integer.parseInt(txtPuntos.getText()));
            } catch (NumberFormatException ex) {
                cliente.setPuntos(0);
            }
            
            resultado[0] = true;
            dialog.dispose();
        });

        // Acción para el botón Cancelar
        btnCancelar.addActionListener(e -> {
            resultado[0] = false;
            dialog.dispose();
        });

        buttonPanel.add(btnCancelar);
        buttonPanel.add(btnGuardar);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Configurar comportamiento de la tecla Enter
        dialog.getRootPane().setDefaultButton(btnGuardar);

        // Mostrar diálogo
        dialog.add(mainPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        
        // Enfocar el campo de teléfono al abrir
        SwingUtilities.invokeLater(() -> txtTelefono.requestFocusInWindow());
        
        dialog.setVisible(true);
        return resultado[0];
    }

    private boolean validarDatos() {
        if (txtTelefono.getText().trim().isEmpty()) {
            mostrarError("El teléfono es requerido");
            return false;
        }
        
        if (txtNombre.getText().trim().isEmpty()) {
            mostrarError("El nombre es requerido");
            return false;
        }
        
        if (!txtTelefono.getText().matches("\\d{7,15}")) {
            mostrarError("Teléfono debe contener solo números (7-15 dígitos)");
            return false;
        }
        
        return true;
    }

 
	public boolean mostrarConfirmacion(String mensaje) {
        int opcion = JOptionPane.showConfirmDialog(
            this,
            mensaje,
            "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        return opcion == JOptionPane.YES_OPTION;
    }

    public void mostrarError(String mensaje) {
        // Cargar icono desde recursos
        ImageIcon errorIcon = cargarIcono("/images/error_icon.png");
        
        JOptionPane.showMessageDialog(
            this,
            mensaje,
            "Error",
            JOptionPane.ERROR_MESSAGE,
            errorIcon != null ? errorIcon : UIManager.getIcon("OptionPane.errorIcon"));
    }

    private ImageIcon cargarIcono(String ruta) {
        try {
            URL imgURL = getClass().getResource(ruta);
            if (imgURL != null) {
                return new ImageIcon(imgURL);
            }
        } catch (Exception e) {
            System.err.println("Error al cargar icono: " + e.getMessage());
        }
        return null;
    }

    public void mostrarAdvertencia(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Advertencia", JOptionPane.WARNING_MESSAGE);
    }

    public void mostrarInformacion(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }
    public JTable getTablaClientes() {
        if (tablaClientes == null) {
            throw new IllegalStateException("Tabla no inicializada");
        }
        return tablaClientes;
    }
    public boolean mostrarFormularioEdicion(Cliente cliente) {
    	 if (cliente == null) {
    	        throw new IllegalArgumentException("Cliente no puede ser nulo");
    	    }
    	    if (cliente.getTelefono() == null) {
    	        cliente.setTelefono(""); // Valor por defecto
    	    }

        // Configuración del diálogo
        JDialog dialog = new JDialog(this, "Editar Cliente", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout());
        dialog.setPreferredSize(new Dimension(500, 350));
        dialog.setResizable(false);

        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Panel de título
        JLabel titleLabel = new JLabel("EDITAR CLIENTE", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Panel de formulario
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Información del Cliente"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Campos del formulario
        JLabel lblId = new JLabel("ID:");
        JTextField txtId = new JTextField(20);
        txtId.setText(cliente.getId() != null ? cliente.getId() : "");
        txtId.setEditable(false);

        JLabel lblTelefono = new JLabel("Teléfono:");
        JTextField txtTelefono = new JTextField(20);
        txtTelefono.setText(cliente.getTelefono() != null ? cliente.getTelefono() : "");

        JLabel lblNombre = new JLabel("Nombre:");
        JTextField txtNombre = new JTextField(20);
        txtNombre.setText(cliente.getNombre() != null ? cliente.getNombre() : "");

        JLabel lblFecha = new JLabel("Última Compra:");
        JTextField txtFecha = new JTextField(20);
        txtFecha.setText(cliente.getUltimaCompra() != null ? cliente.getUltimaCompra() : "");

        JLabel lblPuntos = new JLabel("Puntos:");
        JTextField txtPuntos = new JTextField(20);
        txtPuntos.setText(String.valueOf(cliente.getPuntos()));
        txtPuntos.setEditable(false);

        // Añadir componentes al formulario
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(lblId, gbc);
        gbc.gridx = 1;
        formPanel.add(txtId, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(lblTelefono, gbc);
        gbc.gridx = 1;
        formPanel.add(txtTelefono, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(lblNombre, gbc);
        gbc.gridx = 1;
        formPanel.add(txtNombre, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(lblFecha, gbc);
        gbc.gridx = 1;
        formPanel.add(txtFecha, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(lblPuntos, gbc);
        gbc.gridx = 1;
        formPanel.add(txtPuntos, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnGuardar = new JButton("Guardar Cambios");

        // Estilo de botones
        btnGuardar.setBackground(new Color(76, 175, 80)); // Verde
        btnGuardar.setForeground(Color.WHITE);
        btnCancelar.setBackground(new Color(244, 67, 54)); // Rojo
        btnCancelar.setForeground(Color.WHITE);

        final boolean[] resultado = {false};

        btnGuardar.addActionListener(e -> {
            // Validar campos
            if (txtTelefono.getText().trim().isEmpty()) {
                mostrarError("El teléfono es requerido");
                return;
            }

            // Verificar si hubo cambios reales
            boolean cambios = !txtTelefono.getText().trim().equals(cliente.getTelefono()) ||
                            !txtNombre.getText().trim().equals(cliente.getNombre()) ||
                            !txtFecha.getText().trim().equals(cliente.getUltimaCompra());

            if (!cambios) {
                mostrarInformacion("No se realizaron cambios");
                dialog.dispose(); // Cerrar el diálogo
                return;
            }

            // Actualizar objeto cliente
            cliente.setTelefono(txtTelefono.getText().trim());
            cliente.setNombre(txtNombre.getText().trim());
            cliente.setUltimaCompra(txtFecha.getText().trim());

            resultado[0] = true;
            dialog.dispose(); // Cerrar definitivamente
        });

        btnCancelar.addActionListener(e -> {
            dialog.dispose();
        });

        buttonPanel.add(btnCancelar);
        buttonPanel.add(btnGuardar);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Configurar comportamiento de la tecla Enter
        dialog.getRootPane().setDefaultButton(btnGuardar);

        // Mostrar diálogo
        dialog.add(mainPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        
        // Enfocar el campo de teléfono al abrir
        SwingUtilities.invokeLater(() -> txtTelefono.requestFocusInWindow());
        
        dialog.setVisible(true);
        return resultado[0];
    }
    public int getTablaSeleccionada() {
        return tablaClientes.getSelectedRow();
    }

    public String getSelectedClienteId() {
        try {
            int fila = tablaClientes.getSelectedRow();
            if (fila >= 0) {
                return tablaClientes.getValueAt(fila, 0).toString();// Columna 0 es ID
            }
        } catch (Exception e) {
            // Silenciar cualquier error
        }
        return null;
    }
    public int getSelectedRow() {
        return tablaClientes.getSelectedRow();
    }

    // Método auxiliar para obtener modelo
    public DefaultTableModel getModelo() {
        return (DefaultTableModel) tablaClientes.getModel();
    }
}