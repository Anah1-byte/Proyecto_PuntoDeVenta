package Vista;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import Controlador.ReportesControlador;
import Modelo.Usuario;
import Modelo.Usuarioo;

public class movimientocliente extends JFrame {
    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private static String usuarioActual;
    private JTextField idField, nombreField, telefonoField, fechaField, direccionField, emailField;
    private Usuario usuario;
	private String rolUsuario;

    public movimientocliente (Usuario usuario) {
   	 this.usuario = usuario;
    	Usuarioo usuarioDAO = new Usuarioo();
	this.rolUsuario = usuario.getRol();
	 
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
                dispose();
                new menuprincipal(usuario).setVisible(true);
            }
        });
    }

    private void initUI() {
        setTitle("El Habanerito - Gestión de Cliente");
        setSize(1517, 903);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        
        JPanel northContainer = new JPanel();
        northContainer.setLayout(new BoxLayout(northContainer, BoxLayout.Y_AXIS));
        northContainer.add(crearPanelSuperior());
        northContainer.add(crearMenuHorizontal());
        northContainer.add(Box.createRigidArea(new Dimension(0, 10)));
        
        mainPanel.add(northContainer, BorderLayout.NORTH);
        
        // Panel central con el formulario
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        
        // Panel gris para el formulario
        JPanel formularioPanel = new JPanel();
        formularioPanel.setBackground(new Color(230, 230, 230));
        formularioPanel.setLayout(new BoxLayout(formularioPanel, BoxLayout.Y_AXIS));
        formularioPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        // Título de la acción DENTRO del panel gris
        JLabel tituloAccion = new JLabel("Información del Cliente", SwingConstants.CENTER);
        tituloAccion.setFont(new Font("Arial", Font.BOLD, 24));
        tituloAccion.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        formularioPanel.add(tituloAccion);
        
        // Campos del formulario - Primera fila
        JPanel primeraFila = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 10));
        primeraFila.setBackground(new Color(230, 230, 230));
        
        idField = crearCampoFormulario("ID Cliente:", 150);
        nombreField = crearCampoFormulario("Nombre Completo:", 250);
        primeraFila.add(crearGrupoCampo(idField, "ID Cliente:"));
        primeraFila.add(crearGrupoCampo(nombreField, "Nombre Completo:"));
        formularioPanel.add(primeraFila);
        
        // Campos del formulario - Segunda fila
        JPanel segundaFila = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 10));
        segundaFila.setBackground(new Color(230, 230, 230));
        
        telefonoField = crearCampoFormulario("Teléfono:", 150);
        fechaField = crearCampoFormulario("Fecha Registro:", 150);
        segundaFila.add(crearGrupoCampo(telefonoField, "Teléfono:"));
        segundaFila.add(crearGrupoCampo(fechaField, "Fecha Registro:"));
        formularioPanel.add(segundaFila);
        
        // Campos del formulario - Tercera fila
        JPanel terceraFila = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 10));
        terceraFila.setBackground(new Color(230, 230, 230));
        
        direccionField = crearCampoFormulario("Dirección:", 300);
        emailField = crearCampoFormulario("Correo Electrónico:", 250);
        terceraFila.add(crearGrupoCampo(direccionField, "Dirección:"));
        terceraFila.add(crearGrupoCampo(emailField, "Correo Electrónico:"));
        formularioPanel.add(terceraFila);
        
        // Espacio adicional antes de los botones
        formularioPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        
        centerPanel.add(formularioPanel, BorderLayout.CENTER);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(crearPanelInferior(), BorderLayout.SOUTH);
        
        getContentPane().add(mainPanel);
    }

    private JPanel crearGrupoCampo(JTextField textField, String etiqueta) {
        JPanel grupo = new JPanel();
        grupo.setLayout(new BoxLayout(grupo, BoxLayout.Y_AXIS));
        grupo.setBackground(new Color(230, 230, 230));
        grupo.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JLabel label = new JLabel(etiqueta);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        textField.setAlignmentX(Component.LEFT_ALIGNMENT);
        textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        
        grupo.add(label);
        grupo.add(Box.createRigidArea(new Dimension(0, 5)));
        grupo.add(textField);
        
        return grupo;
    }

    private JTextField crearCampoFormulario(String etiqueta, int ancho) {
        JTextField textField = new JTextField();
        textField.setFont(new Font("Arial", Font.PLAIN, 16));
        textField.setPreferredSize(new Dimension(ancho, 30));
        textField.setMaximumSize(new Dimension(ancho, 30));
        return textField;
    }

	        
	        private JPanel crearPanelSuperior() {
	            JPanel topPanel = new JPanel(new BorderLayout());
	            topPanel.setBackground(new Color(255, 198, 144));
	            topPanel.setPreferredSize(new Dimension(getWidth(), 60));
	            topPanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
	            
	            JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
	            rightPanel.setOpaque(false);
	            
	            JButton usuarioBtn = new JButton(usuarioActual);
	            usuarioBtn.setFont(new Font("Arial", Font.BOLD, 14));
	            usuarioBtn.setForeground(Color.BLACK);
	            usuarioBtn.setContentAreaFilled(false);
	            usuarioBtn.setBorderPainted(false);
	            usuarioBtn.setFocusPainted(false);
	            usuarioBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
	            usuarioBtn.addActionListener(e -> cambiarUsuario());
	            
	            rightPanel.add(usuarioBtn);
	            
	            try {
	                ImageIcon originalIcon = new ImageIcon("C:\\Users\\Anahi\\eclipse-workspace\\Punto_Venta\\Imagenes\\logo.png");
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
	            menuPanel.setPreferredSize(new Dimension(getWidth(), 50));
	            menuPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Color.GRAY));
	            
	            String[] opciones = {"Productos", "Reportes", "Inventario", "Cliente", "Salir"};
	            
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
	                	new producto(usuario).setVisible(true);
	                case "Reportes":
	                	this.dispose(); // Cierra la ventana actual
	                	  reportes vistaReportes = new reportes(usuario, new ReportesControlador(null, usuario));
	                	    vistaReportes.setVisible(true); // Muestra la ventana
	                	    break;
	                case "Inventario":
	                	new inventario(usuario).setVisible(true);
	                    break;
	                case "Cliente":
	                	new clientes (usuario, null).setVisible(true);
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
	        
	        
	        private JPanel crearPanelInferior() {
	            JPanel panelInferior = new JPanel(new BorderLayout());
	            panelInferior.setBackground(Color.PINK);
	            panelInferior.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
	            panelInferior.setPreferredSize(new Dimension(getWidth(), 90));

	            JPanel botones = crearPanelBotonesAccion();
	            botones.setOpaque(false); // para que el fondo rosa se vea

	            panelInferior.add(botones, BorderLayout.CENTER);

	            return panelInferior;
	        }

	        private JPanel crearPanelBotonesAccion() {
	            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
	            panel.setOpaque(false);

	            // Botones específicos para gestión de clientes
	            String[] nombres = {"Editar", "Eliminar", "Agregar"};

	            for (String texto : nombres) {
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
	                btn.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 50));
	                
	                // Asignar acciones específicas para clientes
	                if (texto.equals("Guardar Cliente")) {
	                    btn.addActionListener(e -> guardarCliente());
	                } else if (texto.equals("Limpiar Campos")) {
	                    btn.addActionListener(e -> limpiarCampos());
	                } else if (texto.equals("Eliminar Cliente")) {
	                    btn.addActionListener(e -> eliminarCliente());
	                }
	                
	                panel.add(btn);
	            }

	            return panel;
	        }

	        private void guardarCliente() {
	            if (validarCamposCliente()) {
	                // Lógica para guardar cliente en base de datos
	                String mensaje = "Cliente guardado exitosamente:\n" +
	                                 "ID: " + idField.getText() + "\n" +
	                                 "Nombre: " + nombreField.getText() + "\n" +
	                                 "Teléfono: " + telefonoField.getText() + "\n" +
	                                 "Dirección: " + direccionField.getText() + "\n" +
	                                 "Email: " + emailField.getText();
	                
	                JOptionPane.showMessageDialog(this, mensaje, "Cliente Guardado", JOptionPane.INFORMATION_MESSAGE);
	                limpiarCampos();
	            }
	        }

	        private boolean validarCamposCliente() {
	            if (idField.getText().isEmpty() || nombreField.getText().isEmpty() || 
	                telefonoField.getText().isEmpty() || direccionField.getText().isEmpty()) {
	                JOptionPane.showMessageDialog(this, 
	                    "Los campos ID, Nombre, Teléfono y Dirección son obligatorios", 
	                    "Error", JOptionPane.ERROR_MESSAGE);
	                return false;
	            }
	            
	            // Validar formato de email si está presente
	            if (!emailField.getText().isEmpty() && !emailField.getText().matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
	                JOptionPane.showMessageDialog(this, 
	                    "El formato del correo electrónico no es válido", 
	                    "Error", JOptionPane.ERROR_MESSAGE);
	                return false;
	            }
	            
	            // Validar que el teléfono solo contenga números
	            if (!telefonoField.getText().matches("\\d+")) {
	                JOptionPane.showMessageDialog(this, 
	                    "El teléfono solo debe contener números", 
	                    "Error", JOptionPane.ERROR_MESSAGE);
	                return false;
	            }
	            
	            return true;
	        }

	        private void limpiarCampos() {
	            idField.setText("");
	            nombreField.setText("");
	            telefonoField.setText("");
	            fechaField.setText("");
	            direccionField.setText("");
	            emailField.setText("");
	        }

	        private void eliminarCliente() {
	            if (idField.getText().isEmpty()) {
	                JOptionPane.showMessageDialog(this, 
	                    "Debe ingresar el ID del cliente a eliminar", 
	                    "Error", JOptionPane.ERROR_MESSAGE);
	                return;
	            }
	            
	            int confirm = JOptionPane.showConfirmDialog(this, 
	                "¿Está seguro que desea eliminar al cliente con ID: " + idField.getText() + "?", 
	                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
	            
	            if (confirm == JOptionPane.YES_OPTION) {
	                // Lógica para eliminar cliente de la base de datos
	                JOptionPane.showMessageDialog(this, 
	                    "Cliente con ID " + idField.getText() + " eliminado correctamente", 
	                    "Cliente Eliminado", JOptionPane.INFORMATION_MESSAGE);
	                limpiarCampos();
	            }
	        }
	        
	    }

