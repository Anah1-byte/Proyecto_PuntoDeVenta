package Vista;

import Modelo.Usuario;
import Modelo.Usuarioo;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import Controlador.ventanas;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Login extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private Usuarioo usuarioo;

    public Login() {
        configurarVentana();
        initComponents();
        usuarioo = new Usuarioo();
    }

    private void configurarVentana() {
        setTitle("El Habanerito - Inicio de Sesión");
        setSize(796, 513);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void initComponents() {
        JLayeredPane mainLayeredPane = new JLayeredPane();
        mainLayeredPane.setPreferredSize(new Dimension(787, 455));
        
        // Panel de fondo con gradiente
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
        backgroundPanel.setBounds(0, 0, 787, 455);
        mainLayeredPane.add(backgroundPanel, JLayeredPane.DEFAULT_LAYER);
        
        // Círculos decorativos
        JPanel circleTopLeft = createCirclePanel(new Color(235, 215, 107, 150));
        circleTopLeft.setBounds(-100, -100, 255, 255);
        
        JPanel circleTopRight = createCirclePanel(new Color(216, 237, 88, 150));
        circleTopRight.setBounds(787-120, -100, 255, 255);
        
        JPanel circleBottomLeft = createCirclePanel(new Color(235, 184, 35, 150));
        circleBottomLeft.setBounds(-100, 455-120, 255, 255);
        
        JPanel circleBottomRight = createCirclePanel(new Color(241, 81, 17, 150));
        circleBottomRight.setBounds(787-120, 455-120, 255, 255);
        
        mainLayeredPane.add(circleTopLeft, JLayeredPane.PALETTE_LAYER);
        mainLayeredPane.add(circleTopRight, JLayeredPane.PALETTE_LAYER);
        mainLayeredPane.add(circleBottomLeft, JLayeredPane.PALETTE_LAYER);
        mainLayeredPane.add(circleBottomRight, JLayeredPane.PALETTE_LAYER);
        
        // Panel de contenido
        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBounds(0, 0, 787, 455);
        
        // Panel de título con logo y nombre
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        titlePanel.setOpaque(false);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 0));
        
        // Logo clickeable con área oculta para registro
        try {
            ImageIcon originalIcon = new ImageIcon("imagen\\logo.png");
            Image resizedImage = originalIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
            JLabel logo = new JLabel(new ImageIcon(resizedImage)) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    // Dibujar un pequeño indicador (solo para desarrollo, quitar en producción)
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setColor(new Color(255, 0, 0, 50));
                    g2d.fillRect(getWidth() - 30, getHeight() - 30, 20, 20);
                    g2d.dispose();
                }
            };
            
            logo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            logo.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // Área oculta: esquina inferior derecha (30x30px)
                    if (e.getX() > logo.getWidth() - 30 && e.getY() > logo.getHeight() - 30) {
                        mostrarRegistroOculto();
                    }
                }
            });
            titlePanel.add(logo);
        } catch (Exception e) {
            System.err.println("Error cargando el logo: " + e.getMessage());
        }
        
        JLabel titleLabel = new JLabel("El Habanerito");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(70, 70, 70));
        titlePanel.add(titleLabel);
        
        contentPanel.add(titlePanel, BorderLayout.NORTH);

        // Panel de formulario
        JPanel formPanel = new JPanel();
        formPanel.setOpaque(false);
        formPanel.setLayout(new GridLayout(2, 2, 15, 15));
        formPanel.setBorder(BorderFactory.createEmptyBorder(0, 150, 0, 150));

        // Campos de texto
        JLabel userLabel = new JLabel("Ingrese Usuario:");
        userLabel.setFont(new Font("Arial", Font.BOLD, 14));
        usernameField = new JTextField();
        styleTextField(usernameField);
        
        JLabel passLabel = new JLabel("Ingrese Contraseña:");
        passLabel.setFont(new Font("Arial", Font.BOLD, 14));
        passwordField = new JPasswordField();
        passwordField.setEchoChar('•');
        styleTextField(passwordField);

        formPanel.add(userLabel);
        formPanel.add(usernameField);
        formPanel.add(passLabel);
        formPanel.add(passwordField);
        
        JPanel formContainer = new JPanel(new BorderLayout());
        formContainer.setOpaque(false);
        formContainer.add(formPanel, BorderLayout.CENTER);
        contentPanel.add(formContainer, BorderLayout.CENTER);

        // Botón de login
        loginButton = createButton("Acceder", new Color(123, 118, 118));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        buttonPanel.add(loginButton);
        
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        mainLayeredPane.add(contentPanel, JLayeredPane.MODAL_LAYER);
        getContentPane().add(mainLayeredPane);
        
        // Acción del botón de login
        loginButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Por favor complete todos los campos", 
                    "Campos vacíos", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (usuarioo.autenticarUsuario(username, password)) {
                this.dispose();
                Usuario usuario = usuarioo.obtenerUsuario(username);
                String rol = usuario.getRol();
                
                SwingUtilities.invokeLater(() -> {
                    menuprincipal menu = new menuprincipal(usuario);
                    menu.setBienvenida(username, rol);
                    ventanas controlador = new ventanas(menu, usuario);
                    menu.setVisible(true);
                });
                
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Usuario o contraseña incorrectos", 
                    "Error de autenticación", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    // Método para mostrar el registro oculto
        private void mostrarRegistroOculto() {
            JDialog dialog = new JDialog(this, "Registro Temporal", true);
            dialog.setSize(300, 200);
            dialog.setLocationRelativeTo(this);
            
            JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
            panel.setBorder(new EmptyBorder(10, 10, 10, 10));
            
            JTextField txtUser = new JTextField();
            JPasswordField txtPass = new JPasswordField();
            JComboBox<String> comboRol = new JComboBox<>(new String[]{"Trab"});
            
            panel.add(new JLabel("Usuario:"));
            panel.add(txtUser);
            panel.add(new JLabel("Contraseña:"));
            panel.add(txtPass);
            panel.add(new JLabel("Rol:"));
            panel.add(comboRol);
            
            JButton btnRegistrar = new JButton("Registrar");
            JButton btnCancelar = new JButton("Cancelar");
            
            btnRegistrar.addActionListener(e -> {
                String user = txtUser.getText().trim();
                String pass = new String(txtPass.getPassword()).trim();
                
                if (user.isEmpty() || pass.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Complete todos los campos", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (usuarioo.registrarUsuario(user, pass, (String)comboRol.getSelectedItem())) {
                    JOptionPane.showMessageDialog(dialog, "Usuario temporal creado", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Error al registrar", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            
            btnCancelar.addActionListener(e -> dialog.dispose());
            
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            btnPanel.add(btnCancelar);
            btnPanel.add(btnRegistrar);
            
            dialog.getContentPane().add(panel, BorderLayout.CENTER);
            dialog.getContentPane().add(btnPanel, BorderLayout.SOUTH);
            dialog.setVisible(true);
        }
    
    private void showMessage(String title, String message, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
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
            
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(255, 255);
            }
        };
    }
    
    // Métodos públicos para el controlador
    public String getUsername() {
        return usernameField.getText().trim();
    }

    public String getPassword() {
        return new String(passwordField.getPassword()).trim();
    }

    public void clearPasswordField() {
        passwordField.setText("");
    }

    public void setLoginListener(ActionListener listener) {
        loginButton.addActionListener(listener);
    }

    // Métodos de estilo
    private void styleTextField(JComponent field) {
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
    }

    private JButton createButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(255, 30));
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    private void loginExitoso(Usuario usuario) {
        // Después de login exitoso, abrir el 'menuprincipal' pasando el 'usuario'
        new menuprincipal(usuario).setVisible(true);
        this.dispose();  // Cierra la ventana de Login
    }
}