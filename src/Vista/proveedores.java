package Vista;

import java.awt.*;
import java.awt.event.*;
import java.sql.Timestamp;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

import com.toedter.calendar.JDateChooser;

import Modelo.Usuario;
import Modelo.Proveedor;
import Modelo.Proveedorr;
import Controlador.ProveedoresContro;
import Controlador.ReportesControlador;

import java.text.SimpleDateFormat;
import java.util.Date;

public class proveedores extends JFrame {

    private Usuario usuario;
    private JTable tablaProveedores;
    private DefaultTableModel modeloTabla;
    private JButton btnAgregar, btnEditar, btnEliminar;
    private ProveedoresContro controlador;
    private final Proveedorr proveedorr;

    public proveedores(Usuario usuario) {
        this.usuario = usuario;
        this.proveedorr = new Proveedorr();
        this.modeloTabla = new DefaultTableModel(
            new Object[]{"ID", "Nombre", "Teléfono", "Dirección", "Producto", "Última Visita"}, 0);
        this.controlador = new ProveedoresContro(proveedorr, this, modeloTabla);
        
        initUI();
        cargarDatosIniciales();
    }

    private void cargarDatosIniciales() {
        SwingUtilities.invokeLater(() -> {
            try {
                controlador.actualizarTablaProveedores();
            } catch (Exception e) {
                mostrarMensaje("Error al cargar proveedores: " + e.getMessage(), 3);
                e.printStackTrace();
            }
        });
    }
    
    
	private void initUI() {
        setTitle("El Habanerito - Proveedores");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        mainPanel.setBackground(new Color(240, 240, 240));
        
        // 1. Panel superior (logo + usuario)
        JPanel topPanel = crearPanelSuperior();
        
        // 2. Menú horizontal
        JPanel menuPanel = crearMenuHorizontal();
        
        // 3. Panel de tabla
        tablaProveedores = new JTable(modeloTabla);
        configurarTabla();
        JScrollPane scrollPane = new JScrollPane(tablaProveedores);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(15, 15, 15, 15),
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true)
        ));
        
        // 4. Panel inferior rosa con botones
        JPanel bottomPanel = crearPanelInferior();
        
        // Panel contenedor para menu + tabla
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(menuPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Ensamblar componentes
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER); // Aquí agregamos el panel combinado
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        getContentPane().add(mainPanel);
    }

    private JPanel crearPanelSuperior() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(255, 198, 144));
        topPanel.setPreferredSize(new Dimension(0, 60));
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        
        // Panel contenedor para logo + usuario (ahora en el mismo FlowLayout)
        JPanel logoUserPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        logoUserPanel.setOpaque(false);
        
        // Logo
        try {
        	 ImageIcon originalIcon = new ImageIcon("imagen\\logo.png");
            Image resizedImage = originalIcon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
            JLabel logo = new JLabel(new ImageIcon(resizedImage));
            logoUserPanel.add(logo);
        } catch (Exception e) {
            logoUserPanel.add(new JLabel("Logo"));
        }
        
        // Usuario (pegado al logo)
        JLabel lblUsuario = new JLabel(usuario.getUsername());
        lblUsuario.setFont(new Font("Arial", Font.BOLD, 14));
        lblUsuario.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblUsuario.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                mostrarMenuUsuario();
            }
        });
        
        // Espaciado mínimo entre logo y usuario
        logoUserPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        logoUserPanel.add(lblUsuario);
        
        // Alinear todo a la derecha
        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.setOpaque(false);
        containerPanel.add(logoUserPanel, BorderLayout.EAST);
        
        topPanel.add(containerPanel, BorderLayout.CENTER);
        
        return topPanel;
    }

    private JPanel crearMenuHorizontal() {
        JPanel menuPanel = new JPanel(new GridLayout(1, 7));
        menuPanel.setBackground(new Color(230, 230, 230));
        menuPanel.setPreferredSize(new Dimension(0, 50));
        menuPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Color.GRAY));
        
        String[] opciones = {"Productos", "Reportes", "Inventario", "Cliente", "Proveedores", "Usuarios", "Salir"};
        
        for (String opcion : opciones) {
            JButton btn = crearBotonMenu(opcion, opcion.equals("Proveedores"));
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
                this.dispose();
                new producto(usuario).setVisible(true);
                break;
            case "Reportes":
				this.dispose();
				// Crear una nueva instancia de ReportesControlador
				ReportesControlador reportesControlador = new ReportesControlador(null, usuario);
				// Crear y mostrar la ventana de reportes
            	  reportes vistaReportes = new reportes(usuario, new ReportesControlador(null, usuario));
            	    vistaReportes.setVisible(true); // Muestra la ventana
            	    break;
            case "Inventario":
                this.dispose();
                new inventario(usuario).setVisible(true);
                break;
            case "Cliente":
                this.dispose();
                new clientes(usuario, null).setVisible(true);
                break;
            case "Proveedores":
            	JOptionPane.showMessageDialog(this, "Ya estás en la ventana de Proveedores.");
                break;
            case "Usuarios":
                this.dispose();
                new gestionUsuario(usuario).setVisible(true);
                break;
        }
    }

    private JPanel crearPanelInferior() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(0, 100));
        panel.setBackground(new Color(255, 182, 193)); // Color rosa
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));
        
        // Panel para centrar los botones
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        
        // Crear botones con bordes redondeados
        btnAgregar = crearBotonAccion("Agregar");
        btnEditar = crearBotonAccion("Editar");
        btnEliminar = crearBotonAccion("Eliminar");
        
        // Acciones
        btnAgregar.addActionListener(e -> agregarProveedor());
        btnEditar.addActionListener(e -> editarProveedor());
        btnEliminar.addActionListener(e -> eliminarProveedor());
        
        // Espaciado entre botones
        buttonPanel.add(btnAgregar);
        buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        buttonPanel.add(btnEditar);
        buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        buttonPanel.add(btnEliminar);
        
        panel.add(buttonPanel, BorderLayout.CENTER);
        return panel;
    }

    private JButton crearBotonAccion(String texto) {
        JButton boton = new JButton(texto) {
            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.DARK_GRAY);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
                g2.dispose();
            }
        };
        
        boton.setFont(new Font("Arial", Font.BOLD, 16));
        boton.setBackground(Color.GRAY);
        boton.setForeground(Color.BLACK);
        boton.setFocusPainted(false);
        boton.setContentAreaFilled(false);
        boton.setOpaque(true);
        boton.setPreferredSize(new Dimension(255, 50));
        boton.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
        
        boton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                boton.setBackground(Color.LIGHT_GRAY);
                boton.setForeground(Color.WHITE);
            }
            public void mouseExited(MouseEvent e) {
                boton.setBackground(Color.GRAY);
                boton.setForeground(Color.BLACK);
            }
        });
        
        return boton;
    }

    public void configurarTabla() {
        tablaProveedores.setRowHeight(30);
        tablaProveedores.setFont(new Font("Arial", Font.PLAIN, 14));
        tablaProveedores.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        tablaProveedores.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Renderizador para fechas
        DefaultTableCellRenderer dateRenderer = new DefaultTableCellRenderer() {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                
                if (value instanceof Timestamp) {
                    value = sdf.format((Timestamp)value);
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        };
        dateRenderer.setHorizontalAlignment(JLabel.CENTER);
        
        // Aplicar renderizador a todas las columnas para consistencia
        for (int i = 0; i < tablaProveedores.getColumnCount(); i++) {
            tablaProveedores.getColumnModel().getColumn(i).setCellRenderer(dateRenderer);
        }
    }

    private void agregarProveedor() {
        // Generar un ID temporal único
    	 String nuevoId = "PRV-" + System.currentTimeMillis();
    	    Proveedor nuevoProveedor = new Proveedor(nuevoId, "", "", "", "", new Timestamp(System.currentTimeMillis()));  if (mostrarFormularioProveedor(nuevoProveedor, true)) {
            controlador.agregarProveedor(nuevoProveedor);
        }
    }

    private void editarProveedor() {
        int fila = tablaProveedores.getSelectedRow();
        if (fila < 0) {
            mostrarMensaje("Seleccione un proveedor para editar", 2);
            return;
        }
        
        String id = (String) modeloTabla.getValueAt(fila, 0);
        Proveedor proveedor = controlador.buscarProveedorPorId(id);
        
        if (proveedor != null && mostrarFormularioProveedor(proveedor, false)) {
            controlador.editarProveedor(proveedor);
        }
    }

    private void eliminarProveedor() {
        int fila = tablaProveedores.getSelectedRow();
        if (fila < 0) {
            mostrarMensaje("Seleccione un proveedor para eliminar", 2);
            return;
        }
        
        String id = (String) modeloTabla.getValueAt(fila, 0);
        if (confirmarAccion("¿Eliminar este proveedor?")) {
            controlador.eliminarProveedor(id);
        }
    }

    private boolean mostrarFormularioProveedor(Proveedor proveedor, boolean esNuevo) {
        JDialog dialog = new JDialog(this, esNuevo ? "Nuevo Proveedor" : "Editar Proveedor", true);
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.setSize(500, 400); // Aumentamos un poco el tamaño
        dialog.setLocationRelativeTo(this);
        
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Campos del formulario
        JTextField txtNombre = new JTextField(proveedor.getNombre());
        JTextField txtTelefono = new JTextField(proveedor.getTelefono());
        JTextField txtDireccion = new JTextField(proveedor.getDireccion());
        JTextField txtProducto = new JTextField(proveedor.getProductoSuministrado());
        
        // Configuración del JDateChooser para la fecha
        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("dd/MM/yyyy");
        
     // Parsear la fecha actual del proveedor
        try {
            if (proveedor.getUltimaVisita() != null) {
                // Convertir el Timestamp a Date
                Date fecha = new Date(proveedor.getUltimaVisita().getTime());
                dateChooser.setDate(fecha);
            } else {
                dateChooser.setDate(new Date()); // Fecha actual por defecto si es null
            }
        } catch (Exception e) {
            System.err.println("Error al establecer fecha: " + e.getMessage());
            dateChooser.setDate(new Date()); // Fecha actual por defecto en caso de error
        }
        
        // Botón para fecha actual
        JButton btnHoy = new JButton("Hoy");
        btnHoy.addActionListener(e -> dateChooser.setDate(new Date()));
        
        JPanel fechaPanel = new JPanel(new BorderLayout());
        fechaPanel.add(dateChooser, BorderLayout.CENTER);
        fechaPanel.add(btnHoy, BorderLayout.EAST);
        
        formPanel.add(new JLabel("Nombre:"));
        formPanel.add(txtNombre);
        formPanel.add(new JLabel("Teléfono:"));
        formPanel.add(txtTelefono);
        formPanel.add(new JLabel("Dirección:"));
        formPanel.add(txtDireccion);
        formPanel.add(new JLabel("Producto:"));
        formPanel.add(txtProducto);
        formPanel.add(new JLabel("Última Visita:"));
        formPanel.add(fechaPanel);
        
        // Botón para registrar visita hoy
        JButton btnRegistrarVisita = new JButton("Registrar Visita Hoy");
        btnRegistrarVisita.addActionListener(e -> dateChooser.setDate(new Date()));
        formPanel.add(btnRegistrarVisita);
        
        JButton btnGuardar = new JButton(esNuevo ? "Registrar" : "Actualizar");
        JButton btnCancelar = new JButton("Cancelar");
        
        btnGuardar.addActionListener(e -> {
            if (validarCampos(txtNombre, txtTelefono)) {
                proveedor.setNombre(txtNombre.getText().trim());
                proveedor.setTelefono(txtTelefono.getText().trim());
                proveedor.setDireccion(txtDireccion.getText().trim());
                proveedor.setProductoSuministrado(txtProducto.getText().trim());
                
                // Obtener la fecha seleccionada y convertir a Timestamp
                java.util.Date fechaSeleccionada = dateChooser.getDate();
                if (fechaSeleccionada != null) {
                    proveedor.setUltimaVisita(new Timestamp(fechaSeleccionada.getTime()));
                } else {
                    proveedor.setUltimaVisita(new Timestamp(System.currentTimeMillis()));
                }
                
                dialog.dispose();
            }
        });
        
        btnCancelar.addActionListener(e -> dialog.dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.add(btnCancelar);
        buttonPanel.add(btnGuardar);
        
        dialog.getContentPane().add(formPanel, BorderLayout.CENTER);
        dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
        
        return !proveedor.getNombre().isEmpty();
    }

    private boolean validarCampos(JTextField nombre, JTextField telefono) {
        if (nombre.getText().trim().isEmpty()) {
            mostrarMensaje("El nombre es requerido", 3);
            return false;
        }
        if (telefono.getText().trim().isEmpty()) {
            mostrarMensaje("El teléfono es requerido", 3);
            return false;
        }
        return true;
    }

    public void mostrarMensaje(String mensaje, int tipo) {
        String titulo = "";
        int messageType = JOptionPane.PLAIN_MESSAGE;
        
        switch (tipo) {
            case 1: titulo = "Información"; messageType = JOptionPane.INFORMATION_MESSAGE; break;
            case 2: titulo = "Advertencia"; messageType = JOptionPane.WARNING_MESSAGE; break;
            case 3: titulo = "Error"; messageType = JOptionPane.ERROR_MESSAGE; break;
        }
        
        JOptionPane.showMessageDialog(this, mensaje, titulo, messageType);
    }

    public boolean confirmarAccion(String mensaje) {
        return JOptionPane.showConfirmDialog(
            this, mensaje, "Confirmar", 
            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    private void mostrarMenuUsuario() {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem cambiarUsuario = new JMenuItem("Cambiar usuario");
        JMenuItem salir = new JMenuItem("Salir");
        
        cambiarUsuario.addActionListener(e -> {
            dispose();
            new Login().setVisible(true);
        });
        
        salir.addActionListener(e -> System.exit(0));
        
        menu.add(cambiarUsuario);
        menu.add(salir);
        
        // Mostrar menú junto al nombre de usuario
        Component usuarioLabel = ((JPanel)getContentPane().getComponent(0)).getComponent(0);
        menu.show(usuarioLabel, 0, usuarioLabel.getHeight());
    }
    
    public JTable getTablaProveedores() {
        return this.tablaProveedores;
    }
}