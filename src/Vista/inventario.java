package Vista;

import Controlador.ControladorInventario;
import Controlador.ReportesControlador;
import Modelo.ClienteImpl;
import Modelo.Devolucion;
import Modelo.Producto;
import Modelo.Proveedor;
import Modelo.Proveedorr;
import Modelo.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class inventario extends JFrame {
    private ControladorInventario controlador;
    private Usuario usuario;
    private Proveedorr proveedorr;
    private JPanel panelProductos;
    private JDialog detallesDialog;
    private JLabel lblImagen;
    private JLabel lblNombre;
    private JLabel lblId;
    private JLabel lblDescripcion;
    private JLabel lblCategoria;
    private JLabel lblProveedor;
    private JLabel lblStock;
    private JLabel lblStockMin;
    private JLabel lblStockMax;
    private JLabel lblPrecioVenta;
    private JLabel lblPrecioCompra;
    private JLabel lblIVA;
    private JLabel lblDescuento;
    private JLabel lblFecha;
    private JLabel lblEstado;
    private JButton btnEditar;
    private JButton btnEliminar;
    private JButton btnContactarProveedor;
    private Producto productoSeleccionado;
	private Component panel;
	private JTextField txtBusqueda;
	private JButton btnBusquedaAvanzada;
	
	
    public inventario(Usuario usuario) {
        this.usuario = usuario;
        this.controlador = new ControladorInventario(this);
        this.proveedorr = new Proveedorr();
        initUI();
    }

    private void initUI() {
        setTitle("El Habanerito - Inventario");
        setSize(1517, 903);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        // Panel principal con BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // 1. Panel superior con logo y usuario (NORTH)
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // 2. Menú horizontal justo debajo del header (en su propio panel)
        JPanel menuContainer = new JPanel(new BorderLayout());
        JPanel menuPanel = crearMenuHorizontal();
        menuContainer.add(menuPanel, BorderLayout.NORTH);
        
        // 3. Panel central con SplitPane para filtros y productos
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        
        // Panel izquierdo con filtros (botones rosas)
        JPanel filterPanel = createCategoryFilterPanel();
        filterPanel.setPreferredSize(new Dimension(220, 0));
        splitPane.setLeftComponent(filterPanel);
        
        // Panel derecho con productos
        panelProductos = new JPanel(new GridLayout(0, 3, 15, 15));
        JScrollPane scrollProductos = new JScrollPane(panelProductos);
        scrollProductos.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        splitPane.setRightComponent(scrollProductos);
        
        splitPane.setDividerLocation(220);
        splitPane.setResizeWeight(0);
        
        menuContainer.add(splitPane, BorderLayout.CENTER);
        
        // 4. Panel inferior con búsqueda y botones (SOUTH)
        JPanel bottomSearchPanel = new JPanel(new BorderLayout());
        bottomSearchPanel.setBackground(new Color(255, 182, 193));
        bottomSearchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel de búsqueda (izquierda)
        JPanel searchPanel = createSearchPanel();
        searchPanel.setBackground(new Color(255, 182, 193));
        bottomSearchPanel.add(searchPanel, BorderLayout.CENTER);
        
        // Panel de botones (derecha)
        JPanel buttonPanel = createBottomPanel();
        buttonPanel.setBackground(new Color(255, 182, 193));
        bottomSearchPanel.add(buttonPanel, BorderLayout.EAST);
        
        menuContainer.add(bottomSearchPanel, BorderLayout.SOUTH);
        
        mainPanel.add(menuContainer, BorderLayout.CENTER);
        
        getContentPane().add(mainPanel);
        
        // Cargar productos iniciales
        mostrarProductos(controlador.getTodosProductos());
    }

    private JPanel createTopPanel() {
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        
        // Panel de encabezado
        JPanel headerPanel = createHeaderPanel();
        topContainer.add(headerPanel);
        
        // Menú horizontal
        JPanel horizontalMenu = crearMenuHorizontal();
        topContainer.add(horizontalMenu);
        
        // Panel de búsqueda rápida
        JPanel searchPanel = createSearchPanel();
        topContainer.add(searchPanel);
        
        return topContainer;
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

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        panel.setBackground(new Color(240, 240, 240));
        
        // Panel contenedor
        JPanel searchContainer = new JPanel(new BorderLayout());
        searchContainer.setBackground(Color.WHITE);
        searchContainer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(2, 10, 2, 2)
        ));
        
        // Campo de búsqueda
        txtBusqueda = new JTextField();
        txtBusqueda.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        txtBusqueda.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Botón de búsqueda ovalado
        JButton btnBuscar = createOvalSearchButton();
        
        searchContainer.add(txtBusqueda, BorderLayout.CENTER);
        searchContainer.add(btnBuscar, BorderLayout.EAST);
        panel.add(searchContainer, BorderLayout.CENTER);
        
        return panel;
    }

    private JButton createOvalSearchButton() {
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Fondo ovalado
                g2.setColor(new Color(80, 80, 80));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                
                super.paintComponent(g);
            }
        };
        
        // Cargar imagen (versión segura)
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("imagen\\buscar.png"));
            if (icon.getImage() != null) {
                // Escalar imagen
                Image img = icon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                button.setIcon(new ImageIcon(img));
            }
        } catch (Exception e) {
            // Dibujar lupa manualmente si no hay imagen
            button.setIcon(null);
        }
        
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(40, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(e -> realizarBusqueda());
        
        // Efecto hover
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(100, 100, 100));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(80, 80, 80));
            }
        });
        
        return button;
    }
    
    private void realizarBusqueda() {
        // Ignorar si es el texto de placeholder
        if (txtBusqueda.getText().equals("Buscar productos...")) {
            mostrarProductos(controlador.getTodosProductos());
            return;
        }
        
        String texto = txtBusqueda.getText().trim();
        
        if (texto.isEmpty()) {
            mostrarProductos(controlador.getTodosProductos());
            return;
        }

        // Mostrar animación de carga (opcional)
        JLabel loading = new JLabel("Buscando...", SwingConstants.CENTER);
        JOptionPane.showMessageDialog(this, loading, "", JOptionPane.PLAIN_MESSAGE);
        
        SwingWorker<List<Producto>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Producto> doInBackground() throws Exception {
                return controlador.buscarProductosMultiCriterio(texto);
            }
            
            @Override
            protected void done() {
                try {
                    List<Producto> resultados = get();
                    if (resultados != null && !resultados.isEmpty()) {
                        mostrarProductos(resultados);
                    } else {
                        JOptionPane.showMessageDialog(inventario.this, 
                            "No se encontraron productos", 
                            "Búsqueda", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(inventario.this, 
                        "Error en la búsqueda: " + e.getMessage(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
  
    private JButton createActionButton(String text) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(150, 30));
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setBackground(new Color(70, 130, 180)); // Azul
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(50, 110, 160));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(70, 130, 180));
            }
        });
        
        return btn;
    }

    public void mostrarProductos(List<Producto> productos) {
        panelProductos.removeAll();
        
        for (Producto producto : productos) {
            JPanel card = createProductCard(producto);
            panelProductos.add(card);
        }
        
        panelProductos.revalidate();
        panelProductos.repaint();
    }

    private JPanel createProductCard(Producto producto) {
        // Panel principal de la tarjeta
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(240, 300));
        
        // Estilo del borde basado en el estado del stock
        if (producto.necesitaReposicion()) {
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 80, 80), 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
        } else {
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
        }

        // Panel para la imagen del producto
        JPanel imagePanel = new JPanel();
        imagePanel.setBackground(Color.WHITE);
        imagePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel imgLabel = new JLabel();
        imgLabel.setPreferredSize(new Dimension(150, 150));
        imgLabel.setHorizontalAlignment(JLabel.CENTER);
        
        // Cargar imagen del producto o usar placeholder
        if (producto.getImagenPath() != null && !producto.getImagenPath().isEmpty()) {
            try {
                ImageIcon icon = new ImageIcon(producto.getImagenPath());
                Image img = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                imgLabel.setIcon(new ImageIcon(img));
            } catch (Exception e) {
                imgLabel.setIcon(new ImageIcon(createPlaceholderImage(producto.getNombre(), 150, 150)));
            }
        } else {
            imgLabel.setIcon(new ImageIcon(createPlaceholderImage(producto.getNombre(), 150, 150)));
        }
        
        imagePanel.add(imgLabel);
        card.add(imagePanel);
        card.add(Box.createVerticalStrut(10));

        // Panel para la información del producto
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Nombre del producto
        JLabel nameLabel = new JLabel(producto.getNombre());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setForeground(new Color(50, 50, 50));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(5));

        // ID del producto
        JLabel idLabel = new JLabel("ID: " + producto.getId());
        idLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        idLabel.setForeground(new Color(100, 100, 100));
        idLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoPanel.add(idLabel);
        infoPanel.add(Box.createVerticalStrut(8));

        // Precio y descuento
        JPanel pricePanel = new JPanel();
        pricePanel.setBackground(Color.WHITE);
        pricePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel priceLabel = new JLabel(String.format("$%.2f", producto.getPrecioVenta()));
        priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        priceLabel.setForeground(new Color(0, 100, 0));
        
        // Mostrar descuento si aplica
        if (producto.getDescuento() > 0) {
            JLabel discountLabel = new JLabel(String.format(" (%.0f%% OFF)", producto.getDescuento()));
            discountLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
            discountLabel.setForeground(new Color(200, 0, 0));
            pricePanel.add(discountLabel);
        }
        
        pricePanel.add(priceLabel);
        infoPanel.add(pricePanel);
        infoPanel.add(Box.createVerticalStrut(8));

        // Stock y unidad de medida
        JLabel stockLabel = new JLabel();
        stockLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        stockLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        if (producto.necesitaReposicion()) {
            stockLabel.setText(String.format("¡BAJO STOCK! (%d %s)", 
                producto.getCantidadDisponible(), 
                producto.getUnidadMedida()));
            stockLabel.setForeground(Color.RED);
            stockLabel.setFont(stockLabel.getFont().deriveFont(Font.BOLD));
        } else {
            stockLabel.setText(String.format("Disponible: %d %s", 
                producto.getCantidadDisponible(), 
                producto.getUnidadMedida()));
            stockLabel.setForeground(new Color(70, 70, 70));
        }
        
        infoPanel.add(stockLabel);
        infoPanel.add(Box.createVerticalStrut(5));

        // Estado del producto
        JLabel statusLabel = new JLabel("Estado: " + producto.getEstado());
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Color según estado
        switch(producto.getEstado().toLowerCase()) {
            case "activo":
                statusLabel.setForeground(new Color(0, 120, 0));
                break;
            case "descontinuado":
                statusLabel.setForeground(new Color(150, 150, 150));
                break;
            case "dañado":
                statusLabel.setForeground(new Color(200, 0, 0));
                break;
            default:
                statusLabel.setForeground(new Color(70, 70, 70));
        }
        
        infoPanel.add(statusLabel);
        card.add(infoPanel);

        // Evento para mostrar detalles al hacer clic
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mostrarDetalleProducto(producto);
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(245, 245, 245));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(Color.WHITE);
            }
        });

        return card;
    }

    public void mostrarDetalleProducto(Producto producto) {
        this.productoSeleccionado = producto;
        
        // Configuración del diálogo
        detallesDialog = new JDialog(this, "Detalles del Producto", true);
        detallesDialog.setSize(700, 800);
        detallesDialog.setLocationRelativeTo(this);
        detallesDialog.setLayout(new BorderLayout());
        detallesDialog.getContentPane().setBackground(new Color(240, 245, 250));

        // Panel principal con margenes
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(240, 245, 250));

        // 1. Panel de imagen
        JPanel imagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        imagePanel.setBackground(Color.WHITE);
        imagePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 230), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        lblImagen = new JLabel();
        lblImagen.setPreferredSize(new Dimension(250, 250));
        
        // Cargar imagen o placeholder
        if (producto.getImagenPath() != null && !producto.getImagenPath().isEmpty()) {
            try {
                ImageIcon icon = new ImageIcon(producto.getImagenPath());
                Image img = icon.getImage().getScaledInstance(250, 250, Image.SCALE_SMOOTH);
                lblImagen.setIcon(new ImageIcon(img));
            } catch (Exception e) {
                lblImagen.setIcon(new ImageIcon(createPlaceholderImage("Imagen no disponible", 250, 250)));
            }
        } else {
            lblImagen.setIcon(new ImageIcon(createPlaceholderImage(producto.getNombre(), 250, 250)));
        }
        
        imagePanel.add(lblImagen);
        mainPanel.add(imagePanel);
        mainPanel.add(Box.createVerticalStrut(20));

        // 2. Panel de pestañas
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Pestaña de Información General
        JPanel infoPanel = createInfoPanel(producto);
        tabbedPane.addTab("Información General", infoPanel);

        // Pestaña de Stock y Precios
        JPanel stockPanel = createStockPanel(producto);
        tabbedPane.addTab("Stock y Precios", stockPanel);

        // Pestaña de Proveedor (si existe)
        if (producto.getProveedor() != null && !producto.getProveedor().isEmpty()) {
            JPanel supplierPanel = createSupplierPanel(producto);
            tabbedPane.addTab("Proveedor", supplierPanel);
        }

        mainPanel.add(tabbedPane);
        mainPanel.add(Box.createVerticalStrut(20));

        // 3. Panel de botones
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(240, 245, 250));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        // Botón Editar
        btnEditar = new JButton("Editar");
        styleButton(btnEditar, new Color(70, 130, 180));
        btnEditar.addActionListener(e -> editarProducto());
        
        // Botón Eliminar
        btnEliminar = new JButton("Eliminar");
        styleButton(btnEliminar, new Color(180, 70, 70));
        btnEliminar.addActionListener(e -> eliminarProducto());
        
        // Botón Contactar Proveedor (solo si hay proveedor)
        if (producto.getProveedor() != null && !producto.getProveedor().isEmpty()) {
            btnContactarProveedor = new JButton("Contactar Proveedor");
            styleButton(btnContactarProveedor, new Color(90, 150, 90));
            btnContactarProveedor.addActionListener(e -> {
                Proveedor proveedor = proveedorr.buscarProveedorPorId(producto.getProveedor());
                mostrarInfoProveedor(proveedor);
            });
            buttonPanel.add(btnContactarProveedor);
            buttonPanel.add(Box.createHorizontalStrut(15));
        }
        
        // Botón Cerrar
        JButton btnCerrar = new JButton("Cerrar");
        styleButton(btnCerrar, new Color(100, 100, 100));
        btnCerrar.addActionListener(e -> detallesDialog.dispose());
        
        buttonPanel.add(btnEditar);
        buttonPanel.add(Box.createHorizontalStrut(15));
        buttonPanel.add(btnEliminar);
        buttonPanel.add(Box.createHorizontalStrut(15));
        buttonPanel.add(btnCerrar);
        
        mainPanel.add(buttonPanel);

        detallesDialog.add(mainPanel, BorderLayout.CENTER);
        detallesDialog.setVisible(true);
    }

    // Métodos auxiliares para crear los paneles de cada pestaña
    private JPanel createInfoPanel(Producto producto) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setBackground(Color.WHITE);
        
        addDetailRow(panel, "Nombre:", producto.getNombre());
        addDetailRow(panel, "ID:", producto.getId());
        addDetailRow(panel, "Descripción:", producto.getDescripcion());
        addDetailRow(panel, "Categoría:", producto.getCategoria());
        addDetailRow(panel, "Unidad de Medida:", producto.getUnidadMedida());
        addDetailRow(panel, "Estado:", producto.getEstado());
        addDetailRow(panel, "Fecha de Ingreso:", 
            new SimpleDateFormat("dd/MM/yyyy").format(producto.getFechaIngreso()));
        
        return panel;
    }

    private JPanel createStockPanel(Producto producto) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setBackground(Color.WHITE);
        
        addDetailRow(panel, "Precio de Compra:", String.format("$%.2f", producto.getPrecioCompra()));
        addDetailRow(panel, "Precio de Venta:", String.format("$%.2f", producto.getPrecioVenta()));
        
        if (producto.getDescuento() > 0) {
            addDetailRow(panel, "Descuento:", String.format("%.0f%%", producto.getDescuento()));
            addDetailRow(panel, "Precio con Descuento:", 
                String.format("$%.2f", producto.getPrecioConDescuento()));
        }
        
        addDetailRow(panel, "IVA:", producto.isTieneIVA() ? "Sí (16%)" : "No");
        
        if (producto.isTieneIVA()) {
            addDetailRow(panel, "Precio con IVA:", 
                String.format("$%.2f", producto.getPrecioConIVA()));
        }
        
        addDetailRow(panel, "Stock Actual:", 
            formatStockValue(producto.getCantidadDisponible(), producto.getUnidadMedida(), 
            producto.necesitaReposicion()));
        addDetailRow(panel, "Stock Mínimo:", 
            String.valueOf(producto.getStockMinimo()) + " " + producto.getUnidadMedida());
        addDetailRow(panel, "Stock Máximo:", 
            String.valueOf(producto.getStockMaximo()) + " " + producto.getUnidadMedida());
        
        // Añadir alerta si es necesario
        if (producto.necesitaReposicion()) {
            JLabel alertLabel = new JLabel("¡NECESITA REPOSICIÓN!");
            alertLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            alertLabel.setForeground(Color.RED);
            alertLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(Box.createVerticalStrut(15));
            panel.add(alertLabel);
        } else if (producto.tieneExcesoStock()) {
            JLabel alertLabel = new JLabel("¡EXCESO DE STOCK!");
            alertLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            alertLabel.setForeground(new Color(255, 140, 0));
            alertLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(Box.createVerticalStrut(15));
            panel.add(alertLabel);
        }
        
        return panel;
    }

    private JPanel createSupplierPanel(Producto producto) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setBackground(Color.WHITE);
        
        Proveedor proveedor = proveedorr.buscarProveedorPorId(producto.getProveedor());
        
        if (proveedor != null) {
            addDetailRow(panel, "Proveedor:", proveedor.getNombre());
            addDetailRow(panel, "ID Proveedor:", proveedor.getId());
            addDetailRow(panel, "Teléfono:", proveedor.getTelefono());
            addDetailRow(panel, "Dirección:", 
                proveedor.getDireccion() != null ? proveedor.getDireccion() : "No disponible");
            addDetailRow(panel, "Productos Suministrados:", 
                proveedor.getProductoSuministrado() != null ? proveedor.getProductoSuministrado() : "No disponible");
            addDetailRow(panel, "Última Visita:", 
                proveedor.getUltimaVisita() != null ? 
                new SimpleDateFormat("dd/MM/yyyy").format(proveedor.getUltimaVisita()) : "No registrada");
        } else {
            JLabel noInfoLabel = new JLabel("No se encontró información del proveedor");
            noInfoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            noInfoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(noInfoLabel);
        }
        
        return panel;
    }

    // Métodos auxiliares
    private void addDetailRow(JPanel panel, String label, String value) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        row.setBackground(Color.WHITE);
        
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setPreferredSize(new Dimension(150, 20));
        
        JLabel val = new JLabel(value != null ? value : "N/A");
        val.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        row.add(lbl);
        row.add(val);
        panel.add(row);
        panel.add(Box.createVerticalStrut(5));
    }

    private String formatStockValue(int cantidad, String unidad, boolean bajoStock) {
        String texto = cantidad + " " + unidad;
        if (bajoStock) {
            return "<html><font color='red'>" + texto + " (BAJO STOCK)</font></html>";
        }
        return texto;
    }

    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.darker());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });
    }

    private BufferedImage createPlaceholderImage(String text, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // Fondo
        g2d.setColor(new Color(240, 240, 240));
        g2d.fillRect(0, 0, width, height);
        
        // Rectángulo central
        g2d.setColor(new Color(220, 230, 240));
        g2d.fillRoundRect(10, 10, width-20, height-20, 20, 20);
        
        // Texto
        g2d.setColor(new Color(80, 80, 80));
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Centrar texto
        FontMetrics fm = g2d.getFontMetrics();
        String[] lines = text.split("\n");
        int lineHeight = fm.getHeight();
        int y = (height - (lines.length * lineHeight)) / 2 + fm.getAscent();
        
        for (String line : lines) {
            int textWidth = fm.stringWidth(line);
            int x = (width - textWidth) / 2;
            g2d.drawString(line, x, y);
            y += lineHeight;
        }
        
        g2d.dispose();
        return image;
    }

    private void mostrarInfoProveedor(Proveedor proveedor) {
        JDialog proveedorDialog = new JDialog(this, "Información del Proveedor", true);
        proveedorDialog.setSize(500, 300);
        proveedorDialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridLayout(0, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        if (proveedor == null) {
            panel.add(new JLabel("No hay información disponible del proveedor", JLabel.CENTER));
        } else {
            panel.add(new JLabel("Nombre: " + proveedor.getNombre()));
            panel.add(new JLabel("Teléfono: " + (proveedor.getTelefono() != null ? proveedor.getTelefono() : "No disponible")));
            panel.add(new JLabel("Dirección: " + (proveedor.getDireccion() != null ? proveedor.getDireccion() : "No disponible")));
            panel.add(new JLabel("Productos suministrados: " + (proveedor.getProductoSuministrado() != null ? proveedor.getProductoSuministrado() : "No disponible")));
            panel.add(new JLabel("Última visita: " + (proveedor.getUltimaVisita() != null ? proveedor.getUltimaVisita().toString() : "No disponible")));
        }
        
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> proveedorDialog.dispose());
        panel.add(btnCerrar);
        
        proveedorDialog.getContentPane().add(panel);
        proveedorDialog.setVisible(true);
    }

    private void editarProducto() {
        if (productoSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto primero", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            agregarinventario editarDialog = new agregarinventario(usuario, this);
            editarDialog.setTitle("Editar Producto: " + productoSeleccionado.getNombre());
            editarDialog.setSize(900, 750);
            editarDialog.setLocationRelativeTo(this);
            
            editarDialog.cargarDatosProducto(productoSeleccionado);
            
            editarDialog.setGuardarListener(e -> {
                try {
                    Producto productoEditado = editarDialog.obtenerProductoDelFormulario();
                    productoEditado.setId(productoSeleccionado.getId());
                    productoEditado.setFechaIngreso(productoSeleccionado.getFechaIngreso());
                    
                    // Validación adicional
                    if (productoEditado.getPrecioVenta() <= productoEditado.getPrecioCompra()) {
                        throw new IllegalArgumentException("El precio de venta debe ser mayor al de compra");
                    }
                    
                    if (controlador.actualizarProducto(productoEditado)) {
                        JOptionPane.showMessageDialog(editarDialog, "Producto actualizado", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                        editarDialog.dispose();
                        if (detallesDialog != null) detallesDialog.dispose();
                        mostrarProductos(controlador.getTodosProductos());
                    } else {
                        throw new Exception("No se pudo actualizar el producto en la base de datos");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(editarDialog, 
                        "Error al guardar: " + ex.getMessage(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            });
            
            editarDialog.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error al iniciar edición: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void eliminarProducto() {
        if (productoSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto primero", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(
            this, 
            "¿Está seguro que desea eliminar el producto " + productoSeleccionado.getNombre() + "?", 
            "Confirmar Eliminación", 
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (controlador.eliminarProducto(productoSeleccionado.getId())) {
                JOptionPane.showMessageDialog(this, "Producto eliminado correctamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                detallesDialog.dispose();
                mostrarProductos(controlador.getTodosProductos());
            } else {
                JOptionPane.showMessageDialog(this, "Error al eliminar el producto", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private Image createSampleProductImage() {
        int width = 150, height = 150;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Fondo blanco
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        // Dibuja un rectángulo con el nombre como placeholder
        g2d.setColor(new Color(200, 200, 255));
        g2d.fillRoundRect(10, 10, width - 20, height - 20, 20, 20);
        g2d.setColor(Color.BLUE);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("Imagen", 50, 80);

        g2d.dispose();
        return image;
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
        	JOptionPane.showMessageDialog(this, "Ya estás en la ventana de Inventario.");
            break;
        case "Cliente":
        	this.dispose();
        	ClienteImpl clienteDAO = new ClienteImpl();
        	new clientes(usuario, clienteDAO).setVisible(true);          
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

    private void agregarProducto() {
        agregarinventario agregar = new agregarinventario(usuario, this);
        agregar.setVisible(true);
        
        // Actualizar la vista después de cerrar el diálogo
        mostrarProductos(controlador.getTodosProductos());
    }


    private void generarReporte() {
        // Implementar generación de reportes
        JOptionPane.showMessageDialog(this, "Generando reporte...", "Reporte", JOptionPane.INFORMATION_MESSAGE);
    }

    public void setControlador(ControladorInventario controlador) {
        this.controlador = controlador;
    }
    


    private JButton createCategoryButton(String text) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(180, 40));
        button.setPreferredSize(new Dimension(180, 40));
        
        // Estilo del botón
        button.setBackground(new Color(255, 182, 193)); // Rosa claro
        button.setForeground(Color.BLACK);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 120, 150)), // Borde rosado oscuro
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        // Efecto hover
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(255, 160, 180)); // Rosa más intenso
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(255, 182, 193)); // Volver al color original
            }
        });
        
        return button;
    }
    
 // Método para  botones con estilo moderno
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFont(new Font("Arial", Font.BOLD, 12));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }

    // Método modificado para el panel de categorías
    private JPanel createCategoryFilterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(245, 245, 245));

        // Título
        JLabel lblTitle = new JLabel("CATEGORÍAS:");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblTitle);
        panel.add(Box.createVerticalStrut(10));

        // Panel de categorías con scroll (solo si es necesario)
        JPanel categoriesPanel = new JPanel();
        categoriesPanel.setLayout(new BoxLayout(categoriesPanel, BoxLayout.Y_AXIS));
        categoriesPanel.setBackground(new Color(245, 245, 245));

        JScrollPane scrollPane = new JScrollPane(categoriesPanel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        panel.add(scrollPane);

        // Botón "Todas"
        JButton btnAll = createRoundButton("TODAS LAS CATEGORÍAS", Color.PINK);
        btnAll.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnAll.addActionListener(e -> mostrarProductos(controlador.getTodosProductos()));
        categoriesPanel.add(btnAll);
        categoriesPanel.add(Box.createVerticalStrut(10));

        // Cargar categorías desde BD
        loadCategoriesFromDB(categoriesPanel);

        // Panel para agregar (solo admin)
        if (usuario.esAdministrador()) {
            panel.add(Box.createVerticalStrut(15));
            JPanel addPanel = createAddCategoryPanel(categoriesPanel);
            panel.add(addPanel);
        }

        return panel;
    }

    // Método para cargar categorías desde BD
    private void loadCategoriesFromDB(JPanel container) {
        container.removeAll(); // Limpiar primero
        
        List<String> categorias = controlador.getCategorias();
        System.out.println("Categorías cargadas desde BD: " + categorias); // Debug
        
        if (categorias.isEmpty()) {
            JLabel lblEmpty = new JLabel("No hay categorías registradas");
            lblEmpty.setAlignmentX(Component.CENTER_ALIGNMENT);
            container.add(lblEmpty);
        } else {
            for (String categoria : categorias) {
                JButton btnCat = createRoundButton(categoria, new Color(120, 120, 120));
                btnCat.setAlignmentX(Component.CENTER_ALIGNMENT);
                btnCat.addActionListener(e -> mostrarProductos(controlador.buscarPorCategoria(categoria)));
                container.add(btnCat);
                container.add(Box.createVerticalStrut(8));
            }
        }
        
        container.revalidate();
        container.repaint();
    }

    // Método para crear botones redondeados (como en tu diseño anterior)
    private JButton createRoundButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                super.paintComponent(g2);
                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground().darker());
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 25, 25);
                g2.dispose();
            }
        };
        
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setBackground(Color.PINK);
        button.setForeground(Color.BLACK);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(180, 40));
        button.setMaximumSize(new Dimension(180, 40));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(button.getBackground().brighter());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(Color.PINK);
            }
        });
        
        return button;
    }

    // Panel para agregar nuevas categorías (conservando tu diseño)
    private JPanel createAddCategoryPanel(JPanel categoriesPanel) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));

        JTextField txtNewCat = new JTextField();
        txtNewCat.setMaximumSize(new Dimension(150, 30));
        txtNewCat.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JButton btnAdd = new JButton("+");
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAdd.setBackground(new Color(80, 180, 80));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        btnAdd.addActionListener(e -> {
            String newCategory = txtNewCat.getText().trim();
            if (!newCategory.isEmpty()) {
                if (controlador.agregarCategoria(newCategory, "")) {
                    txtNewCat.setText("");
                    loadCategoriesFromDB(categoriesPanel); // Recargar categorías
                } else {
                    JOptionPane.showMessageDialog(panel, "Error al agregar categoría");
                }
            }
        });

        panel.add(txtNewCat);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(btnAdd);

        return panel;
    }

    // Método optimizado para cargar categorías
    private void loadCategories(JPanel container) {
        container.removeAll();
        
        // Botón "Todas"
        JButton btnAll = createCategoryButton("TODAS", Color.PINK);
        btnAll.addActionListener(e -> mostrarProductos(controlador.getTodosProductos()));
        container.add(btnAll);

        // Categorías existentes
        for (String categoria : controlador.getCategorias()) {
            JButton btnCat = createCategoryButton(categoria, Color.PINK);
            btnCat.addActionListener(e -> mostrarProductos(controlador.buscarPorCategoria(categoria)));
            container.add(btnCat);
        }

        container.revalidate();
        container.repaint();
    }

    // Botón de categoría optimizado
    private JButton createCategoryButton(String text, Color bgColor) {
        JButton button = new JButton(text.length() > 12 ? text.substring(0, 10) + "..." : text);
        button.setFont(new Font("Arial", Font.PLAIN, 11));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        button.setToolTipText(text); // Muestra texto completo al pasar el mouse
        button.setPreferredSize(new Dimension(100, 30));
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.brighter());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }

    // Método para agregar nueva categoría
    private void addNewCategory(JTextField textField, JPanel categoriesPanel) {
        String newCat = textField.getText().trim();
        if (!newCat.isEmpty()) {
            if (controlador.agregarCategoria(newCat, "")) {
                textField.setText("");
                loadCategories(categoriesPanel);
            } else {
                JOptionPane.showMessageDialog(panel, "Error al agregar categoría");
            }
        }
    }

    // Clase WrapLayout para flujo automático (añadir al proyecto)
    public class WrapLayout extends FlowLayout {
        public WrapLayout() { super(); }
        public WrapLayout(int align) { super(align); }
        public WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }
        
        @Override
        public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target, true);
        }
        
        @Override
        public Dimension minimumLayoutSize(Container target) {
            return layoutSize(target, false);
        }
        
        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getSize().width;
                if (targetWidth == 0) targetWidth = Integer.MAX_VALUE;
                
                int hgap = getHgap();
                int vgap = getVgap();
                Insets insets = target.getInsets();
                int maxWidth = targetWidth - (insets.left + insets.right + hgap * 2);
                
                Dimension dim = new Dimension(0, 0);
                int rowWidth = 0, rowHeight = 0;
                
                for (Component m : target.getComponents()) {
                    if (m.isVisible()) {
                        Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                        if (rowWidth + d.width > maxWidth) {
                            dim.width = Math.max(dim.width, rowWidth);
                            dim.height += rowHeight + vgap;
                            rowWidth = 0;
                            rowHeight = 0;
                        }
                        rowWidth += d.width + hgap;
                        rowHeight = Math.max(rowHeight, d.height);
                    }
                }
                
                dim.width = Math.max(dim.width, rowWidth);
                dim.height += rowHeight;
                dim.width += insets.left + insets.right + hgap * 2;
                dim.height += insets.top + insets.bottom + vgap * 2;
                
                return dim;
            }
        }
    }

    private void cargarCategorias(JPanel container) {
    	 container.removeAll();
    	    container.setLayout(new GridBagLayout()); // Cambia a GridBagLayout
    	    GridBagConstraints gbc = new GridBagConstraints();
    	    gbc.gridwidth = GridBagConstraints.REMAINDER;
    	    gbc.fill = GridBagConstraints.HORIZONTAL;
    	    gbc.insets = new Insets(5, 5, 5, 5);
        
        List<String> categorias = controlador.getCategorias();
        for (String categoria : categorias) {
            JPanel categoriaPanel = new JPanel(new BorderLayout());
            categoriaPanel.setBackground(new Color(240, 240, 240));
            
            // Botón para filtrar
            JButton btnCategoria = createStyledButton(categoria, new Color(120, 120, 120));  btnCategoria.addActionListener(e -> 
                mostrarProductos(controlador.buscarPorCategoria(categoria)));
            
            // Botón eliminar (solo admin)
            if (usuario.esAdministrador()) {
                JButton btnEliminar = createStyledButton("X", new Color(200, 80, 80));
                btnEliminar.setPreferredSize(new Dimension(30, 20));
                btnEliminar.addActionListener(e -> {
                    if (JOptionPane.showConfirmDialog(container, 
                        "¿Eliminar categoría " + categoria + "?", 
                        "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        
                        if (controlador.eliminarCategoria(categoria)) {
                            cargarCategorias(container); // Recargar
                        }
                    }
                });
                
                container.add(btnCategoria, gbc);
                categoriaPanel.add(btnEliminar, BorderLayout.EAST);
            } else {
            	 container.add(btnCategoria, gbc);
            }
            
            container.add(categoriaPanel);
            container.add(Box.createVerticalStrut(5));
        }
        
        container.revalidate();
        container.repaint();
    }

    // Método para la barra inferior
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setBackground(new Color(240, 240, 240));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 15, 20));

        // Botón Agregar
        JButton btnAgregar = createRoundButton("AGREGAR", new Color(120, 120, 120), 25);
        btnAgregar.addActionListener(e -> agregarProducto());
        
        // Botón Búsqueda Avanzada
        JButton btnAvanzada = createRoundButton("BÚSQUEDA AVANZADA", new Color(120, 120, 120), 25);
        btnAvanzada.addActionListener(e -> abrirBusquedaAvanzada());
        
        // Botón Reporte
        JButton btnReporte = createRoundButton("REPORTE", new Color(120, 120, 120), 25);
        btnReporte.addActionListener(e -> abrirReporteInventario());
        
        panel.add(btnAgregar);
        panel.add(btnAvanzada);
        panel.add(btnReporte);
        
        return panel;
    }

    private JButton createRoundButton(String text, Color bgColor, int arc) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Fondo redondeado
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
                
                // Texto
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                Rectangle2D r = fm.getStringBounds(text, g2);
                int x = (getWidth() - (int) r.getWidth()) / 2;
                int y = (getHeight() - (int) r.getHeight()) / 2 + fm.getAscent();
                g2.drawString(text, x, y);
                
                g2.dispose();
            }
            
            @Override
            protected void paintBorder(Graphics g) {
                // Sin borde
            }
        };
        
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setPreferredSize(new Dimension(150, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Efecto hover
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.brighter());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }

    private JButton createModernButton(String text, Color bgColor, Color textColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(bgColor);
        button.setForeground(textColor);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bgColor.darker(), 1),
            BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Efecto hover
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.brighter());
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(bgColor.darker(), 1),
                    BorderFactory.createEmptyBorder(8, 20, 8, 20)
                ));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(bgColor.darker(), 1),
                    BorderFactory.createEmptyBorder(8, 20, 8, 20)
                ));
            }
        });
        
        return button;
    }
    
    private void abrirBusquedaAvanzada() {
        JDialog dialog = new JDialog(this, "Búsqueda Avanzada de Productos", true);
        dialog.setSize(900, 700);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(new Color(240, 245, 250));
        
        // Panel principal con pestañas
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        // 1. Pestaña de Búsqueda
        tabbedPane.addTab("🔍 Búsqueda", crearPanelBusquedaAvanzada());
        
        // 2. Pestaña de Movimientos
        tabbedPane.addTab("📦 Movimientos", crearPanelMovimientos());
        
        // 3. Pestaña de Historial
        tabbedPane.addTab("📜 Historial", crearPanelHistorial());
        
        // 4.  pestaña de Devoluciones
        tabbedPane.addTab("🔄 Devoluciones", crearPanelDevoluciones());
        
        // Panel inferior con botones
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 15, 0));
        bottomPanel.setBackground(new Color(240, 245, 250));
        
        JButton btnBuscar = createModernButton("Buscar", new Color(70, 130, 180), Color.WHITE);
        btnBuscar.setPreferredSize(new Dimension(120, 35));
        btnBuscar.addActionListener(e -> realizarBusquedaAvanzada());
        
        JButton btnLimpiar = createModernButton("Limpiar", new Color(120, 120, 120), Color.WHITE);
        btnLimpiar.setPreferredSize(new Dimension(120, 35));
        btnLimpiar.addActionListener(e -> limpiarCamposBusqueda());
        
        JButton btnCerrar = createModernButton("Cerrar", new Color(192, 57, 43), Color.WHITE);
        btnCerrar.setPreferredSize(new Dimension(120, 35));
        btnCerrar.addActionListener(e -> dialog.dispose());
        
        bottomPanel.add(btnBuscar);
        bottomPanel.add(btnLimpiar);
        bottomPanel.add(btnCerrar);
        
        dialog.add(tabbedPane, BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JPanel crearPanelBusquedaAvanzada() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // 1. Fila - Nombre
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(createStyledLabel("Nombre:"), gbc);
        
        gbc.gridx = 1;
        JTextField txtNombre = new JTextField(20);
        txtNombre.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(txtNombre, gbc);
        
        // 2. Fila - Categoría
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(createStyledLabel("Categoría:"), gbc);
        
        gbc.gridx = 1;
        JComboBox<String> cbCategorias = new JComboBox<>();
        cbCategorias.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbCategorias.addItem("Todas las categorías");
        controlador.getCategorias().forEach(cbCategorias::addItem);
        panel.add(cbCategorias, gbc);
        
        // 3. Fila - Proveedor
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(createStyledLabel("Proveedor:"), gbc);
        
        gbc.gridx = 1;
        JComboBox<String> cbProveedores = new JComboBox<>();
        cbProveedores.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbProveedores.addItem("Todos los proveedores");
        proveedorr.obtenerTodosProveedores().forEach(p -> cbProveedores.addItem(p.getNombre()));
        panel.add(cbProveedores, gbc);
        
        // 4. Fila - Estado
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(createStyledLabel("Estado:"), gbc);
        
        gbc.gridx = 1;
        JComboBox<String> cbEstado = new JComboBox<>(new String[]{"Todos", "Activo", "Descontinuado", "Dañado"});
        cbEstado.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(cbEstado, gbc);
        
        // 5. Fila - Rango de Precios
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(createStyledLabel("Rango de Precio:"), gbc);
        
        gbc.gridx = 1;
        JPanel panelPrecio = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panelPrecio.setBackground(Color.WHITE);
        
        JFormattedTextField txtPrecioMin = new JFormattedTextField(NumberFormat.getCurrencyInstance());
        txtPrecioMin.setColumns(8);
        txtPrecioMin.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JLabel lblHasta = new JLabel(" hasta ");
        lblHasta.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JFormattedTextField txtPrecioMax = new JFormattedTextField(NumberFormat.getCurrencyInstance());
        txtPrecioMax.setColumns(8);
        txtPrecioMax.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        panelPrecio.add(txtPrecioMin);
        panelPrecio.add(lblHasta);
        panelPrecio.add(txtPrecioMax);
        panel.add(panelPrecio, gbc);
        
        // 6. Fila - Rango de Stock
        gbc.gridx = 0;
        gbc.gridy = 5;
        panel.add(createStyledLabel("Rango de Stock:"), gbc);
        
        gbc.gridx = 1;
        JPanel panelStock = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panelStock.setBackground(Color.WHITE);
        
        JSpinner spnStockMin = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
        spnStockMin.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JLabel lblHastaStock = new JLabel(" hasta ");
        lblHastaStock.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JSpinner spnStockMax = new JSpinner(new SpinnerNumberModel(100, 0, 10000, 1));
        spnStockMax.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        panelStock.add(spnStockMin);
        panelStock.add(lblHastaStock);
        panelStock.add(spnStockMax);
        panel.add(panelStock, gbc);
        
        // 7. Fila - Fechas
        gbc.gridx = 0;
        gbc.gridy = 6;
        panel.add(createStyledLabel("Fecha de Ingreso:"), gbc);
        
        gbc.gridx = 1;
        JPanel panelFechas = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panelFechas.setBackground(Color.WHITE);
        
        // Crear JSpinner para fecha desde
        JSpinner spnFechaDesde = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor fechaEditorDesde = new JSpinner.DateEditor(spnFechaDesde, "dd/MM/yyyy");
        spnFechaDesde.setEditor(fechaEditorDesde);
        spnFechaDesde.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JLabel lblHastaFecha = new JLabel(" hasta ");
        lblHastaFecha.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Crear JSpinner para fecha hasta
        JSpinner spnFechaHasta = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor fechaEditorHasta = new JSpinner.DateEditor(spnFechaHasta, "dd/MM/yyyy");
        spnFechaHasta.setEditor(fechaEditorHasta);
        spnFechaHasta.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        panelFechas.add(spnFechaDesde);
        panelFechas.add(lblHastaFecha);
        panelFechas.add(spnFechaHasta);
        panel.add(panelFechas, gbc);
        
        // 8. Fila - Checkboxes adicionales
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        
        JPanel panelChecks = new JPanel(new GridLayout(1, 3, 10, 0));
        panelChecks.setBackground(Color.WHITE);
        
        JCheckBox chkNecesitaReposicion = new JCheckBox("Necesita reposición");
        styleCheckbox(chkNecesitaReposicion);
        
        JCheckBox chkTieneDescuento = new JCheckBox("Tiene descuento");
        styleCheckbox(chkTieneDescuento);
        
        JCheckBox chkTieneIVA = new JCheckBox("Aplica IVA");
        styleCheckbox(chkTieneIVA);
        
        panelChecks.add(chkNecesitaReposicion);
        panelChecks.add(chkTieneDescuento);
        panelChecks.add(chkTieneIVA);
        panel.add(panelChecks, gbc);
        
        return panel;
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(new Color(70, 70, 70));
        return label;
    }

    private void styleCheckbox(JCheckBox checkbox) {
        checkbox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        checkbox.setBackground(Color.WHITE);
        checkbox.setFocusPainted(false);
    }

    private void realizarBusquedaAvanzada() {
        // Implementar lógica de búsqueda avanzada aquí
        // Recopilar todos los criterios de búsqueda y llamar al controlador
        
        JOptionPane.showMessageDialog(this, 
            "Búsqueda avanzada realizada con los criterios seleccionados", 
            "Resultados", 
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void limpiarCamposBusqueda() {
        // Implementar lógica para limpiar todos los campos de búsqueda
        JOptionPane.showMessageDialog(this, 
            "Todos los campos de búsqueda han sido limpiados", 
            "Campos limpiados", 
            JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel crearPanelMovimientos() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JTabbedPane tabsMovimientos = new JTabbedPane();
        tabsMovimientos.addTab("➕ Entradas", crearPanelEntradas());
        tabsMovimientos.addTab("➖ Salidas", crearPanelSalidas());
        tabsMovimientos.addTab("🔄 Ajustes", crearPanelAjustes());
        
        panel.add(tabsMovimientos, BorderLayout.CENTER);
        return panel;
    }

	private JPanel crearPanelHistorial() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Panel de búsqueda
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.add(new JLabel("Teléfono cliente:"));
        JTextField txtTelefono = new JTextField(15);
        searchPanel.add(txtTelefono);
        
        JButton btnBuscar = createStyledButton("Buscar", new Color(70, 130, 180));
        btnBuscar.addActionListener(e -> {
            // Implementar búsqueda de historial
        });
        searchPanel.add(btnBuscar);
        
        // Tabla de resultados
        String[] columnas = {"Fecha", "Producto", "Cantidad", "Total", "Puntos"};
        Object[][] datos = {}; // Datos reales irían aquí
        JTable tabla = new JTable(datos, columnas);
        
        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        
        return panel;
    }
	
	private JPanel crearPanelEntradas() {
	    JPanel panel = new JPanel(new BorderLayout(10, 10));
	    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

	    // Panel de formulario
	    JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
	    
	    // Campo ID Producto
	    JTextField txtIdProducto = new JTextField();
	    JButton btnBuscarProducto = createStyledButton("Buscar", new Color(70, 130, 180));
	    JPanel panelIdProducto = new JPanel(new BorderLayout(5, 5));
	    panelIdProducto.add(txtIdProducto, BorderLayout.CENTER);
	    panelIdProducto.add(btnBuscarProducto, BorderLayout.EAST);
	    
	    formPanel.add(new JLabel("ID Producto:"));
	    formPanel.add(panelIdProducto);
	    
	    // Campo Nombre Producto (solo lectura)
	    JTextField txtNombreProducto = new JTextField();
	    txtNombreProducto.setEditable(false);
	    formPanel.add(new JLabel("Nombre Producto:"));
	    formPanel.add(txtNombreProducto);
	    
	    // Campo Cantidad
	    JSpinner spnCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
	    formPanel.add(new JLabel("Cantidad:"));
	    formPanel.add(spnCantidad);
	    
	    // Campo Fecha
	    JSpinner spnFecha = new JSpinner(new SpinnerDateModel());
	    JSpinner.DateEditor fechaEditor = new JSpinner.DateEditor(spnFecha, "dd/MM/yyyy");
	    spnFecha.setEditor(fechaEditor);
	    spnFecha.setValue(new Date());
	    formPanel.add(new JLabel("Fecha:"));
	    formPanel.add(spnFecha);
	    
	    // Botón Registrar
	    JButton btnRegistrar = createStyledButton("Registrar Entrada", new Color(63, 142, 77));
	    btnRegistrar.addActionListener(e -> {
	        if (validarEntrada(txtIdProducto.getText(), (Integer)spnCantidad.getValue())) {
	            registrarEntrada(
	                txtIdProducto.getText(),
	                txtNombreProducto.getText(),
	                (Integer)spnCantidad.getValue(),
	                (Date)spnFecha.getValue()
	            );
	        }
	    });
	    
	    // Configurar acción del botón buscar
	    btnBuscarProducto.addActionListener(e -> {
	        Producto producto = buscarProducto(txtIdProducto.getText());
	        if (producto != null) {
	            txtNombreProducto.setText(producto.getNombre());
	        }
	    });
	    
	    // Panel principal
	    panel.add(formPanel, BorderLayout.CENTER);
	    panel.add(btnRegistrar, BorderLayout.SOUTH);
	    
	    return panel;
	}
	private JPanel crearPanelSalidas() {
	    JPanel panel = new JPanel(new BorderLayout(10, 10));
	    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

	    // Panel de formulario
	    JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
	    
	    // Campo ID Producto
	    JTextField txtIdProducto = new JTextField();
	    JButton btnBuscarProducto = createStyledButton("Buscar", new Color(70, 130, 180));
	    JPanel panelIdProducto = new JPanel(new BorderLayout(5, 5));
	    panelIdProducto.add(txtIdProducto, BorderLayout.CENTER);
	    panelIdProducto.add(btnBuscarProducto, BorderLayout.EAST);
	    
	    formPanel.add(new JLabel("ID Producto:"));
	    formPanel.add(panelIdProducto);
	    
	    // Campo Nombre Producto
	    JTextField txtNombreProducto = new JTextField();
	    txtNombreProducto.setEditable(false);
	    formPanel.add(new JLabel("Nombre Producto:"));
	    formPanel.add(txtNombreProducto);
	    
	    // Campo Cantidad
	    JSpinner spnCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
	    formPanel.add(new JLabel("Cantidad:"));
	    formPanel.add(spnCantidad);
	    
	    // Campo Motivo
	    JComboBox<String> cbMotivo = new JComboBox<>(new String[]{
	        "Venta directa", 
	        "Venta en línea", 
	        "Muestra comercial", 
	        "Donación", 
	        "Otro"
	    });
	    formPanel.add(new JLabel("Motivo:"));
	    formPanel.add(cbMotivo);
	    
	    // Campo Observaciones
	    JTextField txtObservaciones = new JTextField();
	    formPanel.add(new JLabel("Observaciones:"));
	    formPanel.add(txtObservaciones);
	    
	    // Botón Registrar
	    JButton btnRegistrar = createStyledButton("Registrar Salida", new Color(192, 57, 43));
	    btnRegistrar.addActionListener(e -> {
	        if (validarSalida(txtIdProducto.getText(), (Integer)spnCantidad.getValue())) {
	            registrarSalida(
	                txtIdProducto.getText(),
	                txtNombreProducto.getText(),
	                (Integer)spnCantidad.getValue(),
	                (String)cbMotivo.getSelectedItem(),
	                txtObservaciones.getText()
	            );
	        }
	    });
	    
	    // Configurar acción del botón buscar
	    btnBuscarProducto.addActionListener(e -> {
	        Producto producto = buscarProducto(txtIdProducto.getText());
	        if (producto != null) {
	            txtNombreProducto.setText(producto.getNombre());
	            // Mostrar stock actual como máximo en el spinner
	            spnCantidad.setModel(new SpinnerNumberModel(
	                1, 1, producto.getCantidadDisponible(), 1));
	        }
	    });
	    
	    // Panel principal
	    panel.add(formPanel, BorderLayout.CENTER);
	    panel.add(btnRegistrar, BorderLayout.SOUTH);
	    
	    return panel;
	}
	
	private JPanel crearPanelAjustes() {
	    JPanel panel = new JPanel(new BorderLayout(10, 10));
	    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

	    // Panel de formulario
	    JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
	    
	    // Campo ID Producto
	    JTextField txtIdProducto = new JTextField();
	    JButton btnBuscarProducto = createStyledButton("Buscar", new Color(70, 130, 180));
	    JPanel panelIdProducto = new JPanel(new BorderLayout(5, 5));
	    panelIdProducto.add(txtIdProducto, BorderLayout.CENTER);
	    panelIdProducto.add(btnBuscarProducto, BorderLayout.EAST);
	    
	    formPanel.add(new JLabel("ID Producto:"));
	    formPanel.add(panelIdProducto);
	    
	    // Campo Nombre Producto
	    JTextField txtNombreProducto = new JTextField();
	    txtNombreProducto.setEditable(false);
	    formPanel.add(new JLabel("Nombre Producto:"));
	    formPanel.add(txtNombreProducto);
	    
	    // Campo Tipo de Ajuste
	    JComboBox<String> cbTipoAjuste = new JComboBox<>(new String[]{
	        "Merma", 
	        "Daño", 
	        "Robo", 
	        "Error de inventario", 
	        "Caducidad", 
	        "Otro"
	    });
	    formPanel.add(new JLabel("Tipo de ajuste:"));
	    formPanel.add(cbTipoAjuste);
	    
	    // Campo Cantidad (puede ser positivo o negativo)
	    JSpinner spnCantidad = new JSpinner(new SpinnerNumberModel(0, -1000, 1000, 1));
	    formPanel.add(new JLabel("Cantidad:"));
	    formPanel.add(spnCantidad);
	    
	    // Campo Fecha
	    JSpinner spnFecha = new JSpinner(new SpinnerDateModel());
	    JSpinner.DateEditor fechaEditor = new JSpinner.DateEditor(spnFecha, "dd/MM/yyyy");
	    spnFecha.setEditor(fechaEditor);
	    spnFecha.setValue(new Date());
	    formPanel.add(new JLabel("Fecha:"));
	    formPanel.add(spnFecha);
	    
	    // Campo Descripción
	    JTextField txtDescripcion = new JTextField();
	    formPanel.add(new JLabel("Descripción:"));
	    formPanel.add(txtDescripcion);
	    
	    // Botón Registrar
	    JButton btnRegistrar = createStyledButton("Registrar Ajuste", new Color(243, 156, 18));
	    btnRegistrar.addActionListener(e -> {
	        if (validarAjuste(txtIdProducto.getText(), (Integer)spnCantidad.getValue())) {
	            registrarAjuste(
	                txtIdProducto.getText(),
	                txtNombreProducto.getText(),
	                (String)cbTipoAjuste.getSelectedItem(),
	                (Integer)spnCantidad.getValue(),
	                (Date)spnFecha.getValue(),
	                txtDescripcion.getText()
	            );
	        }
	    });
	    
	    // Configurar acción del botón buscar
	    btnBuscarProducto.addActionListener(e -> {
	        Producto producto = buscarProducto(txtIdProducto.getText());
	        if (producto != null) {
	            txtNombreProducto.setText(producto.getNombre());
	        }
	    });
	    
	    // Panel principal
	    panel.add(formPanel, BorderLayout.CENTER);
	    panel.add(btnRegistrar, BorderLayout.SOUTH);
	    
	    return panel;
	}
	
	private Producto buscarProducto(String idProducto) {
	    if (idProducto == null || idProducto.trim().isEmpty()) {
	        JOptionPane.showMessageDialog(this, "Ingrese un ID de producto", "Error", JOptionPane.ERROR_MESSAGE);
	        return null;
	    }
	    
	    Producto producto = controlador.getProductoPorId(idProducto);
	    if (producto == null) {
	        JOptionPane.showMessageDialog(this, "Producto no encontrado", "Error", JOptionPane.ERROR_MESSAGE);
	    }
	    return producto;
	}

	private boolean validarEntrada(String idProducto, int cantidad) {
	    if (idProducto == null || idProducto.trim().isEmpty()) {
	        JOptionPane.showMessageDialog(this, "Ingrese un ID de producto", "Error", JOptionPane.ERROR_MESSAGE);
	        return false;
	    }
	    if (cantidad <= 0) {
	        JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor a cero", "Error", JOptionPane.ERROR_MESSAGE);
	        return false;
	    }
	    return true;
	}

	private void registrarEntrada(String idProducto, String nombreProducto, int cantidad, Date fecha) {
	    // Implementar lógica para registrar entrada en la base de datos
	    JOptionPane.showMessageDialog(this, 
	        String.format("Entrada registrada:\nProducto: %s\nCantidad: %d", nombreProducto, cantidad),
	        "Éxito", JOptionPane.INFORMATION_MESSAGE);
	}

	// Similarmente implementar los métodos:
	// validarSalida(), registrarSalida(), validarAjuste(), registrarAjuste()
	private boolean validarSalida(String idProducto, int cantidad) {
		if (idProducto == null || idProducto.trim().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Ingrese un ID de producto", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if (cantidad <= 0) {
			JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor a cero", "Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	private void registrarSalida(String idProducto, String nombreProducto, int cantidad, String motivo,
			String observaciones) {
		// Implementar lógica para registrar salida en la base de datos
		JOptionPane.showMessageDialog(this, String.format("Salida registrada:\nProducto: %s\nCantidad: %d\nMotivo: %s",
				nombreProducto, cantidad, motivo), "Éxito", JOptionPane.INFORMATION_MESSAGE);
	}

	private boolean validarAjuste(String idProducto, int cantidad) {
		if (idProducto == null || idProducto.trim().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Ingrese un ID de producto", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if (cantidad == 0) {
			JOptionPane.showMessageDialog(this, "La cantidad no puede ser cero", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	private void registrarAjuste(String idProducto, String nombreProducto, String tipoAjuste, int cantidad, Date fecha,
			String descripcion) {
		// Implementar lógica para registrar ajuste en la base de datos
		JOptionPane.showMessageDialog(this, String.format("Ajuste registrado:\nProducto: %s\nTipo: %s\nCantidad: %d",
				nombreProducto, tipoAjuste, cantidad), "Éxito", JOptionPane.INFORMATION_MESSAGE);
	}
  
	// Método para crear el panel de devoluciones
	private JPanel crearPanelDevoluciones() {
	    JPanel panel = new JPanel(new BorderLayout(10, 10));
	    panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
	    
	    // Panel de pestañas para los tipos de devolución
	    JTabbedPane tabsDevoluciones = new JTabbedPane();
	    tabsDevoluciones.addTab("Devolución de Cliente", crearPanelDevolucionCliente());
	    tabsDevoluciones.addTab("Devolución a Proveedor", crearPanelDevolucionProveedor());
	    tabsDevoluciones.addTab("Historial", crearPanelHistorialDevoluciones());
	    
	    panel.add(tabsDevoluciones, BorderLayout.CENTER);
	    return panel;
	}
	
	private JPanel crearPanelDevolucionCliente() {
	    JPanel panel = new JPanel(new BorderLayout(10, 10));
	    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

	    // Panel de formulario
	    JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
	    
	    // Campo ID Producto
	    JTextField txtIdProducto = new JTextField();
	    JButton btnBuscarProducto = createStyledButton("Buscar", new Color(70, 130, 180));
	    JPanel panelIdProducto = new JPanel(new BorderLayout(5, 5));
	    panelIdProducto.add(txtIdProducto, BorderLayout.CENTER);
	    panelIdProducto.add(btnBuscarProducto, BorderLayout.EAST);
	    
	    formPanel.add(new JLabel("ID Producto:"));
	    formPanel.add(panelIdProducto);
	    
	    // Campo Nombre Producto (solo lectura)
	    JTextField txtNombreProducto = new JTextField();
	    txtNombreProducto.setEditable(false);
	    formPanel.add(new JLabel("Nombre Producto:"));
	    formPanel.add(txtNombreProducto);
	    
	    // Campo Cantidad
	    JSpinner spnCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
	    formPanel.add(new JLabel("Cantidad:"));
	    formPanel.add(spnCantidad);
	    
	    // Campo Motivo
	    JComboBox<String> cbMotivo = new JComboBox<>(new String[]{
	        "Producto defectuoso", 
	        "Producto no solicitado", 
	        "Producto vencido", 
	        "Producto incorrecto", 
	        "Otro"
	    });
	    formPanel.add(new JLabel("Motivo:"));
	    formPanel.add(cbMotivo);
	    
	    // Campo ID Transacción Original
	    JTextField txtIdTransaccion = new JTextField();
	    formPanel.add(new JLabel("ID Venta Original:"));
	    formPanel.add(txtIdTransaccion);
	    
	    // Campo Observaciones
	    JTextField txtObservaciones = new JTextField();
	    formPanel.add(new JLabel("Observaciones:"));
	    formPanel.add(txtObservaciones);
	    
	    // Botón Registrar
	    JButton btnRegistrar = createStyledButton("Registrar Devolución", new Color(63, 142, 77));
	    btnRegistrar.addActionListener(e -> {
	        if (validarDevolucion(txtIdProducto.getText(), (Integer)spnCantidad.getValue())) {
	            registrarDevolucionCliente(
	                txtIdProducto.getText(),
	                txtNombreProducto.getText(),
	                (Integer)spnCantidad.getValue(),
	                (String)cbMotivo.getSelectedItem(),
	                txtIdTransaccion.getText(),
	                txtObservaciones.getText()
	            );
	        }
	    });
	    
	    // Configurar acción del botón buscar
	    btnBuscarProducto.addActionListener(e -> {
	        Producto producto = buscarProducto(txtIdProducto.getText());
	        if (producto != null) {
	            txtNombreProducto.setText(producto.getNombre());
	            spnCantidad.setModel(new SpinnerNumberModel(
	                1, 1, producto.getCantidadDisponible(), 1));
	        }
	    });
	    
	    // Panel principal
	    panel.add(formPanel, BorderLayout.CENTER);
	    panel.add(btnRegistrar, BorderLayout.SOUTH);
	    
	    return panel;
	}
	
	private JPanel crearPanelDevolucionProveedor() {
	    JPanel panel = new JPanel(new BorderLayout(10, 10));
	    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

	    // Panel de formulario
	    JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
	    
	    // Campo ID Producto
	    JTextField txtIdProducto = new JTextField();
	    JButton btnBuscarProducto = createStyledButton("Buscar", new Color(70, 130, 180));
	    JPanel panelIdProducto = new JPanel(new BorderLayout(5, 5));
	    panelIdProducto.add(txtIdProducto, BorderLayout.CENTER);
	    panelIdProducto.add(btnBuscarProducto, BorderLayout.EAST);
	    
	    formPanel.add(new JLabel("ID Producto:"));
	    formPanel.add(panelIdProducto);
	    
	    // Campo Nombre Producto (solo lectura)
	    JTextField txtNombreProducto = new JTextField();
	    txtNombreProducto.setEditable(false);
	    formPanel.add(new JLabel("Nombre Producto:"));
	    formPanel.add(txtNombreProducto);
	    
	    // Campo Cantidad
	    JSpinner spnCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
	    formPanel.add(new JLabel("Cantidad:"));
	    formPanel.add(spnCantidad);
	    
	    // Campo Motivo
	    JComboBox<String> cbMotivo = new JComboBox<>(new String[]{
	        "Producto defectuoso", 
	        "Producto vencido", 
	        "Producto incorrecto", 
	        "Exceso de entrega", 
	        "Otro"
	    });
	    formPanel.add(new JLabel("Motivo:"));
	    formPanel.add(cbMotivo);
	    
	    // Campo ID Transacción Original
	    JTextField txtIdTransaccion = new JTextField();
	    formPanel.add(new JLabel("ID Compra Original:"));
	    formPanel.add(txtIdTransaccion);
	    
	    // Campo Observaciones
	    JTextField txtObservaciones = new JTextField();
	    formPanel.add(new JLabel("Observaciones:"));
	    formPanel.add(txtObservaciones);
	    
	    // Botón Registrar
	    JButton btnRegistrar = createStyledButton("Registrar Devolución", new Color(192, 57, 43));
	    btnRegistrar.addActionListener(e -> {
	        if (validarDevolucion(txtIdProducto.getText(), (Integer)spnCantidad.getValue())) {
	            registrarDevolucionProveedor(
	                txtIdProducto.getText(),
	                txtNombreProducto.getText(),
	                (Integer)spnCantidad.getValue(),
	                (String)cbMotivo.getSelectedItem(),
	                txtIdTransaccion.getText(),
	                txtObservaciones.getText()
	            );
	        }
	    });
	    
	    // Configurar acción del botón buscar
	    btnBuscarProducto.addActionListener(e -> {
	        Producto producto = buscarProducto(txtIdProducto.getText());
	        if (producto != null) {
	            txtNombreProducto.setText(producto.getNombre());
	            spnCantidad.setModel(new SpinnerNumberModel(
	                1, 1, producto.getCantidadDisponible(), 1));
	        }
	    });
	    
	    // Panel principal
	    panel.add(formPanel, BorderLayout.CENTER);
	    panel.add(btnRegistrar, BorderLayout.SOUTH);
	    
	    return panel;
	}

	private JPanel crearPanelHistorialDevoluciones() {
	    JPanel panel = new JPanel(new BorderLayout(10, 10));
	    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
	    
	    // Modelo de tabla para las devoluciones
	    String[] columnas = {"ID", "Producto", "Cantidad", "Tipo", "Motivo", "Fecha", "Estado"};
	    Object[][] datos = obtenerDatosDevoluciones();
	    
	    JTable tabla = new JTable(datos, columnas);
	    JScrollPane scrollPane = new JScrollPane(tabla);
	    
	    // Panel de botones
	    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
	    
	    // Botón para actualizar
	    JButton btnActualizar = createStyledButton("Actualizar", new Color(70, 130, 180));
	    btnActualizar.addActionListener(e -> actualizarTablaDevoluciones(tabla));
	    
	    // Botón para generar reporte
	    JButton btnReporte = createStyledButton("Generar Reporte", new Color(63, 142, 77));
	    btnReporte.addActionListener(e -> generarReporteDevoluciones());
	    
	    buttonPanel.add(btnActualizar);
	    buttonPanel.add(btnReporte);
	    
	    panel.add(scrollPane, BorderLayout.CENTER);
	    panel.add(buttonPanel, BorderLayout.SOUTH);
	    
	    return panel;
	}

	private Object[][] obtenerDatosDevoluciones() {
	    List<Devolucion> devoluciones = controlador.obtenerDevoluciones();
	    Object[][] datos = new Object[devoluciones.size()][7];
	    
	    for (int i = 0; i < devoluciones.size(); i++) {
	        Devolucion d = devoluciones.get(i);
	        datos[i][0] = d.getId();
	        datos[i][1] = d.getNombreProducto();
	        datos[i][2] = d.getCantidad();
	        datos[i][3] = d.getTipo();
	        datos[i][4] = d.getMotivo();
	        datos[i][5] = new SimpleDateFormat("dd/MM/yyyy").format(d.getFecha());
	        datos[i][6] = d.getEstado();
	    }
	    
	    return datos;
	}

	private void actualizarTablaDevoluciones(JTable tabla) {
	    Object[][] nuevosDatos = obtenerDatosDevoluciones();
	    DefaultTableModel model = new DefaultTableModel(nuevosDatos, 
	        new String[]{"ID", "Producto", "Cantidad", "Tipo", "Motivo", "Fecha", "Estado"});
	    tabla.setModel(model);
	}

	private boolean validarDevolucion(String idProducto, int cantidad) {
	    if (idProducto == null || idProducto.trim().isEmpty()) {
	        JOptionPane.showMessageDialog(this, "Ingrese un ID de producto", "Error", JOptionPane.ERROR_MESSAGE);
	        return false;
	    }
	    if (cantidad <= 0) {
	        JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor a cero", "Error", JOptionPane.ERROR_MESSAGE);
	        return false;
	    }
	    return true;
	}

	private void registrarDevolucionCliente(String idProducto, String nombreProducto, int cantidad, 
	                                      String motivo, String idTransaccion, String observaciones) {
	    Devolucion devolucion = new Devolucion(
	        "DEV-" + System.currentTimeMillis(),
	        idProducto,
	        nombreProducto,
	        cantidad,
	        "CLIENTE",
	        motivo,
	        new Date(),
	        "PENDIENTE",
	        observaciones,
	        idTransaccion,
	        usuario.getUsername()
	    );
	    
	    if (controlador.registrarDevolucion(devolucion)) {
	        JOptionPane.showMessageDialog(this, 
	            "Devolución de cliente registrada exitosamente", 
	            "Éxito", JOptionPane.INFORMATION_MESSAGE);
	    } else {
	        JOptionPane.showMessageDialog(this, 
	            "Error al registrar la devolución", 
	            "Error", JOptionPane.ERROR_MESSAGE);
	    }
	}

	private void registrarDevolucionProveedor(String idProducto, String nombreProducto, int cantidad, 
	                                        String motivo, String idTransaccion, String observaciones) {
	    Devolucion devolucion = new Devolucion(
	        "DEV-" + System.currentTimeMillis(),
	        idProducto,
	        nombreProducto,
	        cantidad,
	        "PROVEEDOR",
	        motivo,
	        new Date(),
	        "PENDIENTE",
	        observaciones,
	        idTransaccion,
	        usuario.getUsername()
	    );
	    
	    if (controlador.registrarDevolucion(devolucion)) {
	        JOptionPane.showMessageDialog(this, 
	            "Devolución a proveedor registrada exitosamente", 
	            "Éxito", JOptionPane.INFORMATION_MESSAGE);
	    } else {
	        JOptionPane.showMessageDialog(this, 
	            "Error al registrar la devolución", 
	            "Error", JOptionPane.ERROR_MESSAGE);
	    }
	}

	private void generarReporteDevoluciones() {
	    JDialog reporteDialog = new JDialog(this, "Generar Reporte de Devoluciones", true);
	    reporteDialog.setSize(400, 300);
	    reporteDialog.setLocationRelativeTo(this);
	    reporteDialog.setLayout(new GridLayout(0, 1, 10, 10));
	    reporteDialog.getContentPane().setBackground(new Color(240, 245, 250));
	    
	    // Filtros
	    JPanel filtroPanel = new JPanel(new GridLayout(3, 2, 5, 5));
	    filtroPanel.setBorder(BorderFactory.createTitledBorder("Filtros"));
	    
	    // Tipo de devolución
	    JComboBox<String> cbTipo = new JComboBox<>(new String[]{"Todas", "CLIENTE", "PROVEEDOR"});
	    filtroPanel.add(new JLabel("Tipo:"));
	    filtroPanel.add(cbTipo);
	    
	    // Estado
	    JComboBox<String> cbEstado = new JComboBox<>(new String[]{"Todos", "PENDIENTE", "PROCESADA", "RECHAZADA"});
	    filtroPanel.add(new JLabel("Estado:"));
	    filtroPanel.add(cbEstado);
	    
	    // Rango de fechas
	    JSpinner spnFechaDesde = new JSpinner(new SpinnerDateModel());
	    JSpinner.DateEditor fechaEditorDesde = new JSpinner.DateEditor(spnFechaDesde, "dd/MM/yyyy");
	    spnFechaDesde.setEditor(fechaEditorDesde);
	    
	    JSpinner spnFechaHasta = new JSpinner(new SpinnerDateModel());
	    JSpinner.DateEditor fechaEditorHasta = new JSpinner.DateEditor(spnFechaHasta, "dd/MM/yyyy");
	    spnFechaHasta.setEditor(fechaEditorHasta);
	    
	    JPanel fechaPanel = new JPanel(new GridLayout(1, 2, 5, 5));
	    fechaPanel.add(spnFechaDesde);
	    fechaPanel.add(spnFechaHasta);
	    
	    filtroPanel.add(new JLabel("Rango Fechas:"));
	    filtroPanel.add(fechaPanel);
	    
	    // Formatos de reporte
	    JPanel formatoPanel = new JPanel(new GridLayout(2, 2, 5, 5));
	    formatoPanel.setBorder(BorderFactory.createTitledBorder("Formato"));
	    
	    JRadioButton rbPDF = new JRadioButton("PDF", true);
	    JRadioButton rbExcel = new JRadioButton("Excel");
	    JRadioButton rbCSV = new JRadioButton("CSV");
	    JRadioButton rbHTML = new JRadioButton("HTML");
	    
	    ButtonGroup bgFormato = new ButtonGroup();
	    bgFormato.add(rbPDF);
	    bgFormato.add(rbExcel);
	    bgFormato.add(rbCSV);
	    bgFormato.add(rbHTML);
	    
	    formatoPanel.add(rbPDF);
	    formatoPanel.add(rbExcel);
	    formatoPanel.add(rbCSV);
	    formatoPanel.add(rbHTML);
	    
	    // Botones
	    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
	    
	    JButton btnGenerar = createStyledButton("Generar Reporte", new Color(70, 130, 180));
	    btnGenerar.addActionListener(e -> {
	        String tipo = (String)cbTipo.getSelectedItem();
	        String estado = (String)cbEstado.getSelectedItem();
	        Date desde = (Date)spnFechaDesde.getValue();
	        Date hasta = (Date)spnFechaHasta.getValue();
	        
	        String formato = "";
	        if (rbPDF.isSelected()) formato = "PDF";
	        else if (rbExcel.isSelected()) formato = "Excel";
	        else if (rbCSV.isSelected()) formato = "CSV";
	        else if (rbHTML.isSelected()) formato = "HTML";
	        
	        generarReporte(tipo, estado, desde, hasta, formato);
	        reporteDialog.dispose();
	    });
	    
	    JButton btnCancelar = createStyledButton("Cancelar", new Color(192, 57, 43));
	    btnCancelar.addActionListener(e -> reporteDialog.dispose());
	    
	    buttonPanel.add(btnGenerar);
	    buttonPanel.add(btnCancelar);
	    
	    reporteDialog.add(filtroPanel);
	    reporteDialog.add(formatoPanel);
	    reporteDialog.add(buttonPanel);
	    
	    reporteDialog.setVisible(true);
	}

	private void generarReporte(String tipo, String estado, Date desde, Date hasta, String formato) {
	    // Filtrar las devoluciones según los parámetros
	    List<Devolucion> devoluciones = controlador.obtenerDevoluciones();
	    List<Devolucion> devolucionesFiltradas = new ArrayList<>();
	    
	    for (Devolucion d : devoluciones) {
	        // Filtrar por tipo
	        if (!tipo.equals("Todas") && !d.getTipo().equals(tipo)) {
	            continue;
	        }
	        
	        // Filtrar por estado
	        if (!estado.equals("Todos") && !d.getEstado().equals(estado)) {
	            continue;
	        }
	        
	        // Filtrar por fecha
	        if (desde != null && hasta != null && 
	            (d.getFecha().before(desde) || d.getFecha().after(hasta))) {
	            continue;
	        }
	        
	        devolucionesFiltradas.add(d);
	    }
	    
	    // Generar el reporte según el formato seleccionado
	    switch (formato) {
	        case "PDF":
	            generarReportePDF(devolucionesFiltradas);
	            break;
	        case "Excel":
	            generarReporteExcel(devolucionesFiltradas);
	            break;
	        case "CSV":
	            generarReporteCSV(devolucionesFiltradas);
	            break;
	        case "HTML":
	            generarReporteHTML(devolucionesFiltradas);
	            break;
	    }
	    
	    JOptionPane.showMessageDialog(this, 
	        "Reporte generado exitosamente en formato " + formato, 
	        "Éxito", JOptionPane.INFORMATION_MESSAGE);
	}

	private void generarReportePDF(List<Devolucion> devoluciones) {
	    // Implementación para generar PDF (usando librería como iText)
	    try {
	        // Crear directorio de reportes si no existe
	        File dir = new File("reportes");
	        if (!dir.exists()) {
	            dir.mkdir();
	        }
	        
	        String ruta = "reportes/devoluciones_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".pdf";
	        
	        // Crear documento PDF
	        Document document = new Document();
	        PdfWriter.getInstance(document, new FileOutputStream(ruta));
	        document.open();
	        
	        // Título
	        com.itextpdf.text.Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
	        Paragraph titulo = new Paragraph("Reporte de Devoluciones", fontTitulo);
	        titulo.setAlignment(Element.ALIGN_CENTER);
	        document.add(titulo);
	        
	        // Fecha de generación
	        com.itextpdf.text.Font fontFecha = FontFactory.getFont(FontFactory.HELVETICA, 10);
	        Paragraph fecha = new Paragraph("Generado el: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()), fontFecha);
	        fecha.setAlignment(Element.ALIGN_CENTER);
	        document.add(fecha);
	        
	        document.add(new Paragraph(" ")); // Espacio
	        
	        // Crear tabla
	        PdfPTable table = new PdfPTable(7); // 7 columnas
	        table.setWidthPercentage(100);
	        
	        // Encabezados de tabla
	        String[] headers = {"ID", "Producto", "Cantidad", "Tipo", "Motivo", "Fecha", "Estado"};
	        for (String header : headers) {
	            PdfPCell cell = new PdfPCell(new Phrase(header));
	            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
	            table.addCell(cell);
	        }
	        
	        // Datos
	        for (Devolucion d : devoluciones) {
	            table.addCell(d.getId());
	            table.addCell(d.getNombreProducto());
	            table.addCell(String.valueOf(d.getCantidad()));
	            table.addCell(d.getTipo());
	            table.addCell(d.getMotivo());
	            table.addCell(new SimpleDateFormat("dd/MM/yyyy").format(d.getFecha()));
	            table.addCell(d.getEstado());
	        }
	        
	        document.add(table);
	        document.close();
	        
	        // Abrir el archivo generado
	        Desktop.getDesktop().open(new File(ruta));
	    } catch (Exception e) {
	        JOptionPane.showMessageDialog(this, 
	            "Error al generar PDF: " + e.getMessage(), 
	            "Error", JOptionPane.ERROR_MESSAGE);
	    }
	}

	private void generarReporteExcel(List<Devolucion> devoluciones) {
	    // Implementación para generar Excel (usando librería como Apache POI)
	    try {
	        // Crear directorio de reportes si no existe
	        File dir = new File("reportes");
	        if (!dir.exists()) {
	            dir.mkdir();
	        }
	        
	        String ruta = "reportes/devoluciones_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".xlsx";
	        
	        XSSFWorkbook workbook = new XSSFWorkbook();
	        Sheet sheet = workbook.createSheet("Devoluciones");
	        
	        // Crear fila de encabezados
	        Row headerRow = sheet.createRow(0);
	        String[] headers = {"ID", "Producto", "Cantidad", "Tipo", "Motivo", "Fecha", "Estado"};
	        for (int i = 0; i < headers.length; i++) {
	            Cell cell = headerRow.createCell(i);
	            cell.setCellValue(headers[i]);
	        }
	        
	        // Llenar datos
	        int rowNum = 1;
	        for (Devolucion d : devoluciones) {
	            Row row = sheet.createRow(rowNum++);
	            row.createCell(0).setCellValue(d.getId());
	            row.createCell(1).setCellValue(d.getNombreProducto());
	            row.createCell(2).setCellValue(d.getCantidad());
	            row.createCell(3).setCellValue(d.getTipo());
	            row.createCell(4).setCellValue(d.getMotivo());
	            row.createCell(5).setCellValue(new SimpleDateFormat("dd/MM/yyyy").format(d.getFecha()));
	            row.createCell(6).setCellValue(d.getEstado());
	        }
	        
	        // Autoajustar columnas
	        for (int i = 0; i < headers.length; i++) {
	            sheet.autoSizeColumn(i);
	        }
	        
	        // Escribir archivo
	        FileOutputStream outputStream = new FileOutputStream(ruta);
	        workbook.write(outputStream);
	        workbook.close();
	        outputStream.close();
	        
	        // Abrir el archivo generado
	        Desktop.getDesktop().open(new File(ruta));
	    } catch (Exception e) {
	        JOptionPane.showMessageDialog(this, 
	            "Error al generar Excel: " + e.getMessage(), 
	            "Error", JOptionPane.ERROR_MESSAGE);
	    }
	}

	private void generarReporteCSV(List<Devolucion> devoluciones) {
	    try {
	        // Crear directorio de reportes si no existe
	        File dir = new File("reportes");
	        if (!dir.exists()) {
	            dir.mkdir();
	        }
	        
	        String ruta = "reportes/devoluciones_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".csv";
	        
	        FileWriter writer = new FileWriter(ruta);
	        
	        // Escribir encabezados
	        writer.append("ID,Producto,Cantidad,Tipo,Motivo,Fecha,Estado\n");
	        
	        // Escribir datos
	        for (Devolucion d : devoluciones) {
	            writer.append(d.getId()).append(",");
	            writer.append(d.getNombreProducto()).append(",");
	            writer.append(String.valueOf(d.getCantidad())).append(",");
	            writer.append(d.getTipo()).append(",");
	            writer.append(d.getMotivo()).append(",");
	            writer.append(new SimpleDateFormat("dd/MM/yyyy").format(d.getFecha())).append(",");
	            writer.append(d.getEstado()).append("\n");
	        }
	        
	        writer.close();
	        
	        // Abrir el archivo generado
	        Desktop.getDesktop().open(new File(ruta));
	    } catch (Exception e) {
	        JOptionPane.showMessageDialog(this, 
	            "Error al generar CSV: " + e.getMessage(), 
	            "Error", JOptionPane.ERROR_MESSAGE);
	    }
	}

	private void generarReporteHTML(List<Devolucion> devoluciones) {
	    try {
	        // Crear directorio de reportes si no existe
	        File dir = new File("reportes");
	        if (!dir.exists()) {
	            dir.mkdir();
	        }
	        
	        String ruta = "reportes/devoluciones_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".html";
	        
	        FileWriter writer = new FileWriter(ruta);
	        
	        // Escribir encabezado HTML
	        writer.write("<!DOCTYPE html>\n");
	        writer.write("<html>\n");
	        writer.write("<head>\n");
	        writer.write("<title>Reporte de Devoluciones</title>\n");
	        writer.write("<style>\n");
	        writer.write("body { font-family: Arial, sans-serif; }\n");
	        writer.write("h1 { text-align: center; color: #333; }\n");
	        writer.write(".fecha { text-align: center; color: #666; margin-bottom: 20px; }\n");
	        writer.write("table { width: 100%; border-collapse: collapse; margin-top: 20px; }\n");
	        writer.write("th { background-color: #f2f2f2; text-align: left; padding: 8px; }\n");
	        writer.write("td { padding: 8px; border-bottom: 1px solid #ddd; }\n");
	        writer.write("tr:nth-child(even) { background-color: #f9f9f9; }\n");
	        writer.write("</style>\n");
	        writer.write("</head>\n");
	        writer.write("<body>\n");
	        writer.write("<h1>Reporte de Devoluciones</h1>\n");
	        writer.write("<div class=\"fecha\">Generado el: " + 
	            new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()) + "</div>\n");
	        writer.write("<table>\n");
	        writer.write("<tr>\n");
	        writer.write("<th>ID</th>\n");
	        writer.write("<th>Producto</th>\n");
	        writer.write("<th>Cantidad</th>\n");
	        writer.write("<th>Tipo</th>\n");
	        writer.write("<th>Motivo</th>\n");
	        writer.write("<th>Fecha</th>\n");
	        writer.write("<th>Estado</th>\n");
	        writer.write("</tr>\n");
	        
	        // Escribir datos
	        for (Devolucion d : devoluciones) {
	            writer.write("<tr>\n");
	            writer.write("<td>" + d.getId() + "</td>\n");
	            writer.write("<td>" + d.getNombreProducto() + "</td>\n");
	            writer.write("<td>" + d.getCantidad() + "</td>\n");
	            writer.write("<td>" + d.getTipo() + "</td>\n");
	            writer.write("<td>" + d.getMotivo() + "</td>\n");
	            writer.write("<td>" + new SimpleDateFormat("dd/MM/yyyy").format(d.getFecha()) + "</td>\n");
	            writer.write("<td>" + d.getEstado() + "</td>\n");
	            writer.write("</tr>\n");
	        }
	        
	        writer.write("</table>\n");
	        writer.write("</body>\n");
	        writer.write("</html>\n");
	        writer.close();
	        
	        // Abrir el archivo generado
	        Desktop.getDesktop().open(new File(ruta));
	    } catch (Exception e) {
	        JOptionPane.showMessageDialog(this, 
	            "Error al generar HTML: " + e.getMessage(), 
	            "Error", JOptionPane.ERROR_MESSAGE);
	    }
	}
	private void abrirReporteInventario() {
	    try {
	        // Crear el panel de contenido principal con CardLayout
	        JPanel mainContentPanel = new JPanel();
	        CardLayout cardLayout = new CardLayout();
	        mainContentPanel.setLayout(cardLayout);
	        
	        // Crear el panel contenedor que tendrá el CardLayout
	        JPanel containerPanel = new JPanel(new BorderLayout());
	        containerPanel.add(mainContentPanel, BorderLayout.CENTER);
	        
	        // Crear el controlador de reportes
	        ReportesControlador reportesControlador = new ReportesControlador(null, usuario);
	        
	        // Crear el panel de reportes de inventario
	        ReporteInventarioPanel reportePanel = new ReporteInventarioPanel(
	            usuario, 
	            reportesControlador, 
	            cardLayout, 
	            mainContentPanel
	        );
	        
	        // Agregar el panel de reportes al contenedor principal
	        mainContentPanel.add(reportePanel, "reporteInventario");
	        
	        // Crear el diálogo para mostrar el reporte
	        JDialog reportDialog = new JDialog(this, "Reportes de Inventario", true);
	        reportDialog.setSize(1000, 700);
	        reportDialog.setLocationRelativeTo(this);
	        reportDialog.setLayout(new BorderLayout());
	        reportDialog.add(containerPanel, BorderLayout.CENTER);
	        
	        // Mostrar el panel de reportes
	        cardLayout.show(mainContentPanel, "reporteInventario");
	        reportDialog.setVisible(true);
	        
	    } catch (Exception e) {
	        JOptionPane.showMessageDialog(this, 
	            "Error al abrir reportes: " + e.getMessage(), 
	            "Error", JOptionPane.ERROR_MESSAGE);
	        e.printStackTrace();
	    }
	}
}