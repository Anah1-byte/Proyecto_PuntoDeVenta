package Vista;

import Controlador.ControladorInventario;
import Modelo.Producto;
import Modelo.Proveedor;
import Modelo.Proveedorr;
import Modelo.Usuario;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import javax.swing.filechooser.FileNameExtensionFilter;

import ConexionBD.ConexionAccess;

import java.util.List;

public class agregarinventario extends JDialog {
    private ControladorInventario controlador;
    private Usuario usuario;
    private Proveedorr proveedorr;
    private ActionListener guardarListener;
    private boolean esEdicion = false;
    private JTextField txtNombre, txtDescripcion;
    private JComboBox<String> cbProveedor, cbCategoria, cbEstado;
    private JSpinner spnStockMin, spnStockMax, spnCantidad;
    private JSpinner spnPrecioCompra, spnPrecioVenta, spnDescuento;
    private JCheckBox chkIVA;
    private JButton btnSeleccionarImagen, btnAgregarProveedor;
    private JLabel lblImagen;
    private String imagenPath = "";
	private JComboBox<String> cbUnidadMedida;

    public agregarinventario(Usuario usuario, JFrame parent) {
        super(parent, "Agregar Producto", true);
        this.usuario = usuario;
        this.proveedorr = new Proveedorr();
        this.controlador = new ControladorInventario(null);
        initUI();
    }
    
    public void setGuardarListener(ActionListener listener) {
        this.guardarListener = listener;
        this.esEdicion = true; // Indicar que estamos en modo edición
    }


    private void initUI() {
        setSize(800, 700);
        setLocationRelativeTo(getParent());
        setResizable(false);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Panel de título
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(255, 198, 144)); // Naranja claro
        JLabel titleLabel = new JLabel("AGREGAR NUEVO PRODUCTO");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titlePanel.add(titleLabel);
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        
        // Panel central con pestañas
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Pestaña 1: Información Básica
        JPanel basicPanel = createBasicInfoPanel();
        tabbedPane.addTab("Información Básica", basicPanel);
        
        // Pestaña 2: Stock y Precios
        JPanel stockPanel = createStockPricePanel();
        tabbedPane.addTab("Stock y Precios", stockPanel);
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton btnGuardar = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");
        
        btnGuardar.addActionListener(e -> guardarProducto());
        btnCancelar.addActionListener(e -> dispose());
        
        buttonPanel.add(btnGuardar);
        buttonPanel.add(btnCancelar);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        getContentPane().add(mainPanel);
    }

    private JPanel createBasicInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        // Sección de imagen
        JPanel imagePanel = new JPanel();
        imagePanel.setBorder(BorderFactory.createTitledBorder("Imagen del Producto"));
        imagePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        lblImagen = new JLabel();
        lblImagen.setPreferredSize(new Dimension(200, 200));
        lblImagen.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        btnSeleccionarImagen = new JButton("Seleccionar Imagen");
        btnSeleccionarImagen.addActionListener(e -> seleccionarImagen());
        
        imagePanel.add(lblImagen);
        imagePanel.add(Box.createRigidArea(new Dimension(10, 0)));
        imagePanel.add(btnSeleccionarImagen);
        
        panel.add(imagePanel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Sección de datos básicos
        JPanel basicInfoPanel = new JPanel();
        basicInfoPanel.setLayout(new GridLayout(0, 2, 10, 10));
        basicInfoPanel.setBorder(BorderFactory.createTitledBorder("Datos Básicos"));
        basicInfoPanel.setBackground(new Color(230, 230, 230)); // Gris claro
        
        // Nombre
        basicInfoPanel.add(new JLabel("Nombre:"));
        txtNombre = new JTextField();
        basicInfoPanel.add(txtNombre);
        
        // Descripción
        basicInfoPanel.add(new JLabel("Descripción:"));
        txtDescripcion = new JTextField();
        basicInfoPanel.add(txtDescripcion);
        
        // Categoría
        basicInfoPanel.add(new JLabel("Categoría:"));
        cbCategoria = new JComboBox<>(controlador.getCategorias().toArray(new String[0]));
        cbCategoria.removeItem("TODOS");
        basicInfoPanel.add(cbCategoria);
        
        // Proveedor
        basicInfoPanel.add(new JLabel("Proveedor:"));
        JPanel proveedorPanel = new JPanel(new BorderLayout());
        cbProveedor = new JComboBox<>(cargarProveedores());
        btnAgregarProveedor = new JButton("+");
        btnAgregarProveedor.addActionListener(e -> agregarNuevoProveedor());
        
        proveedorPanel.add(cbProveedor, BorderLayout.CENTER);
        proveedorPanel.add(btnAgregarProveedor, BorderLayout.EAST);
        basicInfoPanel.add(proveedorPanel);
        
        // Estado
        basicInfoPanel.add(new JLabel("Estado:"));
        cbEstado = new JComboBox<>(new String[]{"Activo", "Descontinuado", "Dañado"});
        basicInfoPanel.add(cbEstado);
        
        basicInfoPanel.add(new JLabel("Unidad de Medida:"));
        String[] unidades = {"Pieza", "Kg", "Litro", "Metro", "Caja", "Paquete"};
        cbUnidadMedida = new JComboBox<>(unidades); // Usar la variable de instancia
        basicInfoPanel.add(cbUnidadMedida);
        
        panel.add(basicInfoPanel);
        
        return panel;
    
    }

    private JPanel createStockPricePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        // Sección de stock
        JPanel stockPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        stockPanel.setBorder(BorderFactory.createTitledBorder("Stock"));
        stockPanel.setBackground(new Color(255, 182, 193)); // Rosa
        
        stockPanel.add(new JLabel("Cantidad disponible:"));
        spnCantidad = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
        stockPanel.add(spnCantidad);
        
        stockPanel.add(new JLabel("Stock mínimo:"));
        spnStockMin = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
        stockPanel.add(spnStockMin);
        
        stockPanel.add(new JLabel("Stock máximo:"));
        spnStockMax = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
        stockPanel.add(spnStockMax);
        
        panel.add(stockPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Sección financiera
        JPanel financePanel = new JPanel(new GridLayout(0, 2, 10, 10));
        financePanel.setBorder(BorderFactory.createTitledBorder("Datos Financieros"));
        financePanel.setBackground(new Color(216, 237, 88)); // Amarillo claro
        
        // Precio compra (formato moneda)
        financePanel.add(new JLabel("Precio compra:"));
        spnPrecioCompra = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100000.0, 0.5));
        JSpinner.NumberEditor editorCompra = new JSpinner.NumberEditor(spnPrecioCompra, "$#,##0.00");
        spnPrecioCompra.setEditor(editorCompra);
        financePanel.add(spnPrecioCompra);
        
        // Precio venta (formato moneda)
        financePanel.add(new JLabel("Precio venta:"));
        spnPrecioVenta = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100000.0, 0.5));
        JSpinner.NumberEditor editorVenta = new JSpinner.NumberEditor(spnPrecioVenta, "$#,##0.00");
        spnPrecioVenta.setEditor(editorVenta);
        financePanel.add(spnPrecioVenta);
        
        // IVA
        financePanel.add(new JLabel("IVA:"));
        chkIVA = new JCheckBox("Aplica IVA (16%)");
        financePanel.add(chkIVA);
        
        // Descuento
        financePanel.add(new JLabel("Descuento (%):"));
        spnDescuento = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100.0, 0.5));
        financePanel.add(spnDescuento);
        
        panel.add(financePanel);
        
        return panel;
    }

    private String[] cargarProveedores() {
        return proveedorr.obtenerTodosProveedores().stream()
                .map(Proveedor::getNombre)
                .toArray(String[]::new);
    }

    private void seleccionarImagen() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar imagen del producto");
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(
            new FileNameExtensionFilter("Imágenes", "jpg", "jpeg", "png", "gif"));
        
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                // Crear directorio de imágenes si no existe
                File imgDir = new File("imagenes_productos");
                if (!imgDir.exists()) {
                    imgDir.mkdir();
                }
                
                // Copiar imagen al directorio de la aplicación
                File destino = new File(imgDir, selectedFile.getName());
                Files.copy(selectedFile.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
                
                // Mostrar imagen en el label
                ImageIcon icon = new ImageIcon(destino.getAbsolutePath());
                Image img = icon.getImage().getScaledInstance(
                    lblImagen.getWidth(), lblImagen.getHeight(), Image.SCALE_SMOOTH);
                lblImagen.setIcon(new ImageIcon(img));
                
                // Guardar ruta para la base de datos
                imagenPath = destino.getAbsolutePath();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error al cargar la imagen: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void agregarNuevoProveedor() {
        // Crear diálogo para agregar proveedor rápido
        JDialog dialog = new JDialog(this, "Agregar Proveedor Rápido", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JTextField txtNombre = new JTextField();
        JTextField txtTelefono = new JTextField();
        
        panel.add(new JLabel("Nombre:"));
        panel.add(txtNombre);
        panel.add(new JLabel("Teléfono:"));
        panel.add(txtTelefono);
        
        JButton btnGuardar = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");
        
        btnGuardar.addActionListener(e -> {
            if (txtNombre.getText().trim().isEmpty() || txtTelefono.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, 
                    "Nombre y teléfono son obligatorios", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Proveedor nuevo = new Proveedor(
                "PRV-" + System.currentTimeMillis(),
                txtNombre.getText(),
                txtTelefono.getText(),
                "",
                "",
                new Timestamp(System.currentTimeMillis())
            );
            
            try {
                if (proveedorr.agregarProveedor(nuevo)) {
                    cbProveedor.addItem(nuevo.getNombre());
                    cbProveedor.setSelectedItem(nuevo.getNombre());
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, 
                        "Error al guardar proveedor", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "Error al guardar proveedor: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
            });
        
        btnCancelar.addActionListener(e -> dialog.dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(btnCancelar);
        buttonPanel.add(btnGuardar);
        
        dialog.getContentPane().add(panel, BorderLayout.CENTER);
        dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
 // Método para obtener los datos del formulario como Producto
    public Producto obtenerProductoDelFormulario() {
        // Obtener el proveedor seleccionado
        String nombreProveedor = (String) cbProveedor.getSelectedItem();
        Proveedor proveedor = proveedorr.obtenerTodosProveedores().stream()
                .filter(p -> p.getNombre().equals(nombreProveedor))
                .findFirst()
                .orElse(null);
        
        if (proveedor == null) {
            throw new IllegalStateException("Proveedor no válido");
        }
        
        return new Producto(
        	    generarId(), // Esto se sobrescribirá con el ID original en edición
        	    txtNombre.getText(),
        	    txtDescripcion.getText(),
        	    (String) cbCategoria.getSelectedItem(),
        	    proveedor.getId(),
        	    (Integer) spnCantidad.getValue(),
        	    (Integer) spnStockMin.getValue(),
        	    (Integer) spnStockMax.getValue(),
        	    (Double) spnPrecioCompra.getValue(),
        	    (Double) spnPrecioVenta.getValue(),
        	    chkIVA.isSelected(),
        	    (Double) spnDescuento.getValue(),
        	    new Date(), // Usar fecha actual o mantener la original?
        	    (String) cbEstado.getSelectedItem(),
        	    imagenPath,
        	    (String) cbUnidadMedida.getSelectedItem() // Unidad de medida agregada
        	);
    }

    private void guardarProducto() {
        if (esEdicion && guardarListener != null) {
            guardarListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "guardar"));
            return;
        }
        // Validaciones adicionales
        if (txtNombre.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre del producto es obligatorio", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if ((Integer)spnStockMin.getValue() > (Integer)spnStockMax.getValue()) {
            JOptionPane.showMessageDialog(this, "El stock mínimo no puede ser mayor al máximo", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if ((Double)spnPrecioCompra.getValue() <= 0 || (Double)spnPrecioVenta.getValue() <= 0) {
            JOptionPane.showMessageDialog(this, "Los precios deben ser mayores a cero", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
     // Validar que el precio de venta sea mayor que el de compra
        if ((Double)spnPrecioVenta.getValue() <= (Double)spnPrecioCompra.getValue()) {
            JOptionPane.showMessageDialog(this, 
                "El precio de venta debe ser mayor al precio de compra", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validar que el stock actual esté entre mínimo y máximo
        int stockActual = (Integer)spnCantidad.getValue();
        int stockMin = (Integer)spnStockMin.getValue();
        int stockMax = (Integer)spnStockMax.getValue();

        if (stockActual < 0 || stockMin < 0 || stockMax < 0) {
            JOptionPane.showMessageDialog(this, 
                "Los valores de stock no pueden ser negativos", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (stockMin >= stockMax) {
            JOptionPane.showMessageDialog(this, 
                "El stock mínimo debe ser menor al stock máximo", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Obtener el proveedor seleccionado
        String nombreProveedor = (String) cbProveedor.getSelectedItem();
        Proveedor proveedor = proveedorr.obtenerTodosProveedores().stream()
                .filter(p -> p.getNombre().equals(nombreProveedor))
                .findFirst()
                .orElse(null);
        
        if (proveedor == null) {
            JOptionPane.showMessageDialog(this, 
                "Seleccione un proveedor válido", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Crear el producto
        Producto producto = new Producto(
        			    generarId(), // Esto se sobrescribirá con el ID original en edición
        			    txtNombre.getText(),
        			    txtDescripcion.getText(),
        			    (String) cbCategoria.getSelectedItem(),
        			    proveedor.getId(),
        			    (Integer) spnCantidad.getValue(),
        			    (Integer) spnStockMin.getValue(),
        			    (Integer) spnStockMax.getValue(),
        			    (Double) spnPrecioCompra.getValue(),
        			    (Double) spnPrecioVenta.getValue(),
        			    chkIVA.isSelected(),
        			    (Double) spnDescuento.getValue(),
        			    new Date(), // Usar fecha actual o mantener la original?
        			    (String) cbEstado.getSelectedItem(),
        			    imagenPath,
        			    (String) cbUnidadMedida.getSelectedItem() // Unidad de medida agregada
        			);
        
        try {
            if (controlador.agregarProducto(producto)) {
                JOptionPane.showMessageDialog(this, "Producto agregado correctamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Error al agregar producto", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al guardar: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    private String generarId() {
        return "PROD-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }

    public void cargarDatosProducto(Producto producto) {
        // Cargar datos básicos
        txtNombre.setText(producto.getNombre());
        txtDescripcion.setText(producto.getDescripcion());
        cbCategoria.setSelectedItem(producto.getCategoria());
        
        // Cargar proveedor
        Proveedor prov = proveedorr.buscarProveedorPorId(producto.getProveedor());
        if (prov != null) {
            cbProveedor.setSelectedItem(prov.getNombre());
        }
        
        cbEstado.setSelectedItem(producto.getEstado());
        
        // Cargar stock
        spnCantidad.setValue(producto.getCantidadDisponible());
        spnStockMin.setValue(producto.getStockMinimo());
        spnStockMax.setValue(producto.getStockMaximo());
        
        // Cargar datos financieros
        spnPrecioCompra.setValue(producto.getPrecioCompra());
        spnPrecioVenta.setValue(producto.getPrecioVenta());
        chkIVA.setSelected(producto.isTieneIVA());
        spnDescuento.setValue(producto.getDescuento());
        
     // Cargar imagen si existe
        if (producto.getImagenPath() != null && !producto.getImagenPath().isEmpty()) {
            try {
                ImageIcon icon = new ImageIcon(producto.getImagenPath());
                // Verificar dimensiones antes de escalar
                int width = lblImagen.getWidth() > 0 ? lblImagen.getWidth() : 200;
                int height = lblImagen.getHeight() > 0 ? lblImagen.getHeight() : 200;
                
                Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                lblImagen.setIcon(new ImageIcon(img));
                imagenPath = producto.getImagenPath();
            } catch (Exception e) {
                System.err.println("Error al cargar imagen: " + e.getMessage());
                lblImagen.setIcon(null);
            }
        } else {
            lblImagen.setIcon(null);
        }
    }
    
    public List<String> getCategorias() {
        List<String> categorias = new ArrayList<>();
        String sql = "SELECT DISTINCT categoria FROM Productos WHERE categoria IS NOT NULL";
        
        try (Connection conn = ConexionAccess.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String categoria = rs.getString("categoria");
                if (categoria != null && !categoria.trim().isEmpty()) {
                    categorias.add(categoria.trim());
                }
            }
            
            // Si no hay categorías, agregar algunas básicas
            if (categorias.isEmpty()) {
                categorias.add("Abarrotes");
                categorias.add("Bebidas");
                categorias.add("Lácteos");
            }
            
        } catch (SQLException e) {
            System.err.println("Error al obtener categorías: " + e.getMessage());
            // Retornar categorías predeterminadas en caso de error
            categorias.add("General");
        }
        
        Collections.sort(categorias);
        return categorias;
    }
}