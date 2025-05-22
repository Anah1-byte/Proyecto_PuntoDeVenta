package Vista;

import Modelo.Usuario;
import Modelo.Usuarioo;
import Controlador.GestorUsuario;
import Controlador.ReportesControlador;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.util.List;

public class gestionUsuario extends JFrame {

    private Usuario usuario;
    private GestorUsuario gestorUsuario;
    private JTable tablaUsuarios;
    private DefaultTableModel modeloTabla;
    private JButton btnAgregar, btnEditar, btnEliminar;

    public gestionUsuario(Usuario usuario) {
        this.usuario = usuario;
        this.gestorUsuario = new GestorUsuario();
        this.modeloTabla = new DefaultTableModel(new Object[]{"Usuario", "Rol", "Estado"}, 0);
        
        initUI();
        cargarUsuarios();
    }

    private void initUI() {
        setTitle("El Habanerito - Gestión de Usuarios");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Panel principal con BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        mainPanel.setBackground(new Color(240, 240, 240));
        
        // 1. Panel superior (logo + usuario)
        JPanel topPanel = crearPanelSuperior();
        
        // 2. Menú horizontal
        JPanel menuPanel = crearMenuHorizontal();
        
        // 3. Panel para la tabla (usamos un panel contenedor)
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablaUsuarios = new JTable(modeloTabla);
        configurarTabla();
        JScrollPane scrollPane = new JScrollPane(tablaUsuarios);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(15, 15, 15, 15),
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true)
        ));
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        // 4. Panel inferior rosa con botones
        JPanel bottomPanel = crearPanelInferior();
        
        // Panel contenedor para menú y tabla
        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.add(menuPanel, BorderLayout.NORTH);
        centerContainer.add(tablePanel, BorderLayout.CENTER);
        
        // Ensamblar componentes
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerContainer, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        getContentPane().add(mainPanel);
    }
    
    // ==================== MÉTODOS DE DISEÑO (iguales a proveedores.java) ====================
    private JPanel crearPanelSuperior() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(255, 198, 144));
        topPanel.setPreferredSize(new Dimension(0, 60));
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        
        JPanel logoUserPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        logoUserPanel.setOpaque(false);
        
        try {
            ImageIcon originalIcon = new ImageIcon("imagen\\logo.png");
            Image resizedImage = originalIcon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
            JLabel logo = new JLabel(new ImageIcon(resizedImage));
            logoUserPanel.add(logo);
        } catch (Exception e) {
            logoUserPanel.add(new JLabel("Logo"));
        }
        
        JLabel lblUsuario = new JLabel(usuario.getUsername());
        lblUsuario.setFont(new Font("Arial", Font.BOLD, 14));
        lblUsuario.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblUsuario.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                mostrarMenuUsuario();
            }
        });
        
        logoUserPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        logoUserPanel.add(lblUsuario);
        
        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.setOpaque(false);
        containerPanel.add(logoUserPanel, BorderLayout.EAST);
        
        topPanel.add(containerPanel, BorderLayout.CENTER);
        
        return topPanel;
    }

    private JPanel crearMenuHorizontal() {
        JPanel menuPanel = new JPanel(new GridBagLayout()); // Cambiamos a GridBagLayout
        menuPanel.setBackground(new Color(230, 230, 230));
        menuPanel.setPreferredSize(new Dimension(getWidth(), 50)); // Altura fija de 50px
        menuPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Color.GRAY));
        
        String[] opciones = {"Productos", "Reportes", "Inventario", "Cliente", "Proveedores", "Usuarios", "Salir"};
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0; // Distribuye el espacio equitativamente
        gbc.weighty = 1.0;
        
        for (String opcion : opciones) {
            JButton btn = crearBotonMenu(opcion, opcion.equals("Usuarios"));
            btn.addActionListener(e -> manejarAccionMenu(opcion));
            
            // Configuración específica para cada botón
            gbc.gridwidth = 1; // Cada botón ocupa 1 celda
            menuPanel.add(btn, gbc);
        }
        
        return menuPanel;
    }
    
    private JButton crearBotonMenu(String texto, boolean esActivo) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Arial", Font.BOLD, 14));
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5)); // Padding interno reducido
        
        // Estilo para el estado normal
        if (esActivo) {
            boton.setBackground(new Color(216, 237, 88));
            boton.setForeground(Color.BLACK);
        } else {
            boton.setBackground(Color.GRAY);
            boton.setForeground(Color.BLACK);
        }
        
        // Efecto hover
        boton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!esActivo) {
                    boton.setBackground(new Color(216, 237, 88));
                    boton.setForeground(Color.WHITE);
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                if (!esActivo) {
                    boton.setBackground(Color.GRAY);
                    boton.setForeground(Color.BLACK);
                }
            }
        });
        
        // Tamaño preferido más compacto
        boton.setPreferredSize(new Dimension(100, 40)); // Ancho mínimo sugerido
        
        return boton;
    }

    private JPanel crearPanelInferior() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(0, 100));
        panel.setBackground(new Color(255, 182, 193));
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        
        btnAgregar = crearBotonAccion("Agregar");
        btnEditar = crearBotonAccion("Editar");
        btnEliminar = crearBotonAccion("Eliminar");
        
        btnAgregar.addActionListener(e -> agregarUsuario());
        btnEditar.addActionListener(e -> editarUsuario());
        btnEliminar.addActionListener(e -> eliminarUsuario());
        
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
        
        boton.setFont(new Font("Arial", Font.BOLD, 14));
        boton.setBackground(Color.GRAY);
        boton.setForeground(Color.BLACK);
        boton.setFocusPainted(false);
        boton.setContentAreaFilled(false);
        boton.setOpaque(true);
        boton.setPreferredSize(new Dimension(255, 60));
        boton.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
        
        boton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                boton.setBackground(new Color(216, 237, 88));
                boton.setForeground(Color.WHITE);
            }
            public void mouseExited(MouseEvent e) {
                boton.setBackground(Color.GRAY);
                boton.setForeground(Color.BLACK);
            }
        });
        
        return boton;
    }

    private void configurarTabla() {
        tablaUsuarios.setRowHeight(30);
        tablaUsuarios.setFont(new Font("Arial", Font.PLAIN, 14));
        tablaUsuarios.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        tablaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < tablaUsuarios.getColumnCount(); i++) {
            tablaUsuarios.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        tablaUsuarios.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                if (isSelected) {
                    label.setBackground(new Color(216, 237, 88));
                    label.setForeground(Color.BLACK);
                } else {
                    label.setBackground(row % 2 == 0 ? Color.WHITE : new Color(240, 240, 240));
                }
                return label;
            }
        });
    }

    // ==================== MÉTODOS DE GESTIÓN ====================
    private void cargarUsuarios() {
        // Limpiar la tabla
        modeloTabla.setRowCount(0);
        
        // Obtener todos los usuarios del gestor
        List<Usuario> usuarios = gestorUsuario.listarUsuarios();
        
        // Agregar cada usuario a la tabla
        for (Usuario usuario : usuarios) {
            modeloTabla.addRow(new Object[]{
                usuario.getUsername(),
                usuario.getRol(),
                "Activo" // Estado por defecto
            });
        }
    }

    private void agregarUsuario() {
        JDialog dialog = new JDialog(this, "Nuevo Usuario", true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JTextField txtUsername = new JTextField();
        JPasswordField txtPassword = new JPasswordField();
        JComboBox<String> comboRol = new JComboBox<>(new String[]{"Admin", "Trab"});
        
        formPanel.add(new JLabel("Usuario:"));
        formPanel.add(txtUsername);
        formPanel.add(new JLabel("Contraseña:"));
        formPanel.add(txtPassword);
        formPanel.add(new JLabel("Rol:"));
        formPanel.add(comboRol);
        
        JButton btnGuardar = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");
        
        btnGuardar.addActionListener(e -> {
            String username = txtUsername.getText().trim();
            String password = new String(txtPassword.getPassword()).trim();
            String rol = (String) comboRol.getSelectedItem();
            
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, 
                    "Complete todos los campos", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Intenta registrar el usuario
            if (gestorUsuario.registrarUsuario(username, password, rol)) {
                // Actualiza la tabla con los nuevos datos
                cargarUsuarios();
                JOptionPane.showMessageDialog(dialog, 
                    "Usuario registrado exitosamente", 
                    "Éxito", 
                    JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, 
                    "El usuario ya existe", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        btnCancelar.addActionListener(e -> dialog.dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.add(btnCancelar);
        buttonPanel.add(btnGuardar);
        
        dialog.getContentPane().add(formPanel, BorderLayout.CENTER);
        dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }

    private void editarUsuario() {
        int fila = tablaUsuarios.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un usuario", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String username = (String) modeloTabla.getValueAt(fila, 0);
        String rolActual = (String) modeloTabla.getValueAt(fila, 1);
        
        String nuevoRol = (String) JOptionPane.showInputDialog(
            this,
            "Seleccione el nuevo rol:",
            "Editar Rol",
            JOptionPane.PLAIN_MESSAGE,
            null,
            new String[]{"Admin", "Trab"},
            rolActual
        );
        
        if (nuevoRol != null && !nuevoRol.equals(rolActual)) {
            if (gestorUsuario.cambiarRolUsuario(username, nuevoRol)) {
                cargarUsuarios();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo actualizar el rol", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void eliminarUsuario() {
        int fila = tablaUsuarios.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un usuario", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String username = (String) modeloTabla.getValueAt(fila, 0);
        
        if (username.equals("admin")) {
            JOptionPane.showMessageDialog(this, "No se puede eliminar al administrador", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "¿Eliminar al usuario " + username + "?",
            "Confirmar",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (gestorUsuario.eliminarUsuario(username)) {
                cargarUsuarios();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo eliminar el usuario", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ==================== MÉTODOS DE NAVEGACIÓN ====================
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
                this.dispose();
                new clientes(usuario, null).setVisible(true);
                break;
            case "Proveedores":
                this.dispose();
                new proveedores(usuario).setVisible(true);
                break;
            case "Usuarios":
            	JOptionPane.showMessageDialog(this, "Ya estás en la ventana de Usuario.");
                break;
        }
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
        
        Component usuarioLabel = ((JPanel)getContentPane().getComponent(0)).getComponent(0);
        menu.show(usuarioLabel, 0, usuarioLabel.getHeight());
    }
}