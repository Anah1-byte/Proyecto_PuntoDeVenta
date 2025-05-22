package Vista;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.*;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import Modelo.Venta;
import Modelo.Producto;
import Modelo.Usuario;
import Modelo.Cliente;
import ConexionBD.ConexionAccess;
import Controlador.ReportesControlador;

public class ReimprimirDialog extends JDialog {
    private JComboBox<String> cmbTipoDocumento;
    private JTextField txtIdDocumento;
    private JButton btnBuscar;
    private JButton btnReimprimir;
    private ReportesControlador controlador;
    private Usuario usuario;
    
    public ReimprimirDialog(Usuario usuario,Frame parent, ReportesControlador controlador) {
        this.usuario = usuario;
    	super(parent, "Reimprimir Documento", true);
        this.controlador = controlador;
        initUI();
    }
    
    private void initUI() {
        // Configuración básica del diálogo
        setLayout(new BorderLayout(10, 10));
        setSize(450, 200);
        setResizable(false);
        getContentPane().setBackground(new Color(245, 245, 245));
        
        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout(10, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(245, 245, 245));
        
        // Panel de controles
        JPanel controlPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        controlPanel.setBackground(new Color(245, 245, 245));
        
        // Componentes
        JLabel lblTipo = new JLabel("Tipo de documento:");
        lblTipo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        cmbTipoDocumento = new JComboBox<>(new String[]{"Ticket de Venta", "Reporte de Ventas", 
            "Reporte de Inventario", "Reporte de Clientes", "Reporte de Proveedores"});
        styleComboBox(cmbTipoDocumento);
        
        JLabel lblId = new JLabel("ID o referencia:");
        lblId.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        txtIdDocumento = new JTextField();
        styleTextField(txtIdDocumento);
        
        controlPanel.add(lblTipo);
        controlPanel.add(cmbTipoDocumento);
        controlPanel.add(lblId);
        controlPanel.add(txtIdDocumento);
        
        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(new Color(245, 245, 245));
        
        btnBuscar = createStyledButton("Buscar", new Color(70, 130, 180));
        btnBuscar.addActionListener(e -> buscarDocumento());
        
        btnReimprimir = createStyledButton("Reimprimir", new Color(46, 125, 50));
        btnReimprimir.addActionListener(e -> reimprimirDocumento());
        
        buttonPanel.add(btnBuscar);
        buttonPanel.add(btnReimprimir);
        
        mainPanel.add(controlPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        setLocationRelativeTo(getParent());
    }
    
    private void styleComboBox(JComboBox<String> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        combo.setBackground(Color.WHITE);
        combo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
    }
    
    private void styleTextField(JTextField textField) {
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
    
    private void buscarDocumento() {
        String tipoSeleccionado = (String) cmbTipoDocumento.getSelectedItem();
        
        if (tipoSeleccionado.equals("Ticket de Venta")) {
            mostrarDialogoBusquedaTickets();
        } else {
            String tipoReporte = tipoSeleccionado.replace("Reporte de ", "").toUpperCase();
            mostrarDialogoBusquedaReportes(tipoReporte);
        }
    }

    private void mostrarDialogoBusquedaTickets() {
        JDialog searchDialog = new JDialog(this, "Buscar Tickets de Venta", true);
        searchDialog.setLayout(new BorderLayout());
        searchDialog.setSize(900, 600);
        searchDialog.getContentPane().setBackground(new Color(245, 245, 245));
        
        // Panel de filtros
        JPanel filterPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        filterPanel.setBackground(Color.WHITE);
        
        // Componentes de filtro
        JTextField txtId = new JTextField();
        styleTextField(txtId);
        
        JSpinner spinnerDesde = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editorDesde = new JSpinner.DateEditor(spinnerDesde, "dd/MM/yyyy");
        spinnerDesde.setEditor(editorDesde);
        spinnerDesde.setValue(new Date());
        
        JSpinner spinnerHasta = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editorHasta = new JSpinner.DateEditor(spinnerHasta, "dd/MM/yyyy");
        spinnerHasta.setEditor(editorHasta);
        spinnerHasta.setValue(new Date());
        
        JComboBox<String> cmbPayment = new JComboBox<>(new String[]{"Todos", "EFECTIVO", "TARJETA"});
        styleComboBox(cmbPayment);
        
        // Agregar componentes al panel
        filterPanel.add(createFilterLabel("ID Venta:"));
        filterPanel.add(txtId);
        filterPanel.add(createFilterLabel("Fecha Desde:"));
        filterPanel.add(spinnerDesde);
        filterPanel.add(createFilterLabel("Fecha Hasta:"));
        filterPanel.add(spinnerHasta);
        filterPanel.add(createFilterLabel("Método Pago:"));
        filterPanel.add(cmbPayment);
        
        // Tabla de resultados
        String[] columnas = {"ID Venta", "Fecha", "Total", "Método Pago", "Cliente", "Productos"};
        DefaultTableModel model = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable resultsTable = new JTable(model);
        configurarTabla(resultsTable);
        
        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton btnExport = createStyledButton("Exportar", new Color(56, 142, 60));
        btnExport.addActionListener(e -> exportarTablaACSV(resultsTable));
        
        JButton btnSearch = createStyledButton("Buscar", new Color(51, 103, 214));
        btnSearch.addActionListener(e -> realizarBusqueda(txtId, spinnerDesde, spinnerHasta, cmbPayment, resultsTable));
        
        JButton btnSelect = createStyledButton("Seleccionar", new Color(103, 58, 183));
        btnSelect.addActionListener(e -> seleccionarTicket(searchDialog, resultsTable));
        
        JButton btnCancel = createStyledButton("Cancelar", new Color(158, 158, 158));
        btnCancel.addActionListener(e -> searchDialog.dispose());
        
        buttonPanel.add(btnExport);
        buttonPanel.add(btnSearch);
        buttonPanel.add(btnSelect);
        buttonPanel.add(btnCancel);
        
        searchDialog.add(filterPanel, BorderLayout.NORTH);
        searchDialog.add(new JScrollPane(resultsTable), BorderLayout.CENTER);
        searchDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        searchDialog.setLocationRelativeTo(this);
        searchDialog.setVisible(true);
    }
    
    private JLabel createFilterLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return label;
    }
    
    private void configurarTabla(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(25);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setGridColor(new Color(240, 240, 240));
        table.setShowVerticalLines(false);
        
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                setBorder(noFocusBorder);
                if (isSelected) {
                    setBackground(new Color(220, 240, 255));
                } else {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 248, 248));
                }
                return this;
            }
        });
    }
    
    private void realizarBusqueda(JTextField txtId, JSpinner spinnerDesde, JSpinner spinnerHasta, 
                                 JComboBox<String> cmbPayment, JTable resultsTable) {
        DefaultTableModel model = (DefaultTableModel) resultsTable.getModel();
        model.setRowCount(0);
        
        try {
            String idVenta = txtId.getText().trim();
            Date fechaDesde = (Date) spinnerDesde.getValue();
            Date fechaHasta = (Date) spinnerHasta.getValue();
            String metodoPago = cmbPayment.getSelectedIndex() == 0 ? null : cmbPayment.getSelectedItem().toString();
            
            // Ajustar fecha hasta para incluir todo el día
            Calendar cal = Calendar.getInstance();
            cal.setTime(fechaHasta);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            fechaHasta = cal.getTime();
            
            List<Venta> ventas = buscarVentasEnBD(idVenta, fechaDesde, fechaHasta, metodoPago);
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            for (Venta venta : ventas) {
                String cliente = venta.getCliente() != null ? venta.getCliente().getNombre() : "N/A";
                String productos = venta.getProductos().stream()
                    .map(p -> p.getNombre())
                    .collect(Collectors.joining(", "));
                
                model.addRow(new Object[]{
                    venta.getId(),
                    sdf.format(venta.getFecha()),
                    String.format("$%,.2f", venta.getTotal()),
                    venta.getMetodoPago(),
                    cliente,
                    productos.length() > 50 ? productos.substring(0, 47) + "..." : productos
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error en la búsqueda: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private List<Venta> buscarVentasEnBD(String idVenta, Date fechaDesde, Date fechaHasta, String metodoPago) {
        List<Venta> ventas = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = ConexionAccess.conectar();
            
            StringBuilder sql = new StringBuilder(
                "SELECT v.id, v.fecha, v.total, v.metodo_pago, c.nombre as cliente " +
                "FROM Ventas v " +
                "LEFT JOIN Clientes c ON v.id_cliente = c.id " +
                "WHERE 1=1");
            
            if (!idVenta.isEmpty()) {
                sql.append(" AND v.id = ?");
            }
            if (fechaDesde != null) {
                sql.append(" AND v.fecha >= ?");
            }
            if (fechaHasta != null) {
                sql.append(" AND v.fecha <= ?");
            }
            if (metodoPago != null) {
                sql.append(" AND v.metodo_pago = ?");
            }
            sql.append(" ORDER BY v.fecha DESC");
            
            pstmt = conn.prepareStatement(sql.toString());
            
            int paramIndex = 1;
            if (!idVenta.isEmpty()) {
                pstmt.setString(paramIndex++, idVenta);
            }
            if (fechaDesde != null) {
                pstmt.setTimestamp(paramIndex++, new Timestamp(fechaDesde.getTime()));
            }
            if (fechaHasta != null) {
                pstmt.setTimestamp(paramIndex++, new Timestamp(fechaHasta.getTime()));
            }
            if (metodoPago != null) {
                pstmt.setString(paramIndex, metodoPago);
            }
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Venta venta = new Venta();
                venta.setId(rs.getInt("id"));
                venta.setFecha(rs.getTimestamp("fecha"));
                venta.setTotal(rs.getDouble("total"));
                venta.setMetodoPago(rs.getString("metodo_pago"));
                
                if (rs.getString("cliente") != null) {
                    Cliente cliente = new Cliente();
                    cliente.setNombre(rs.getString("cliente"));
                    venta.setCliente(cliente);
                }
                
                venta.setProductos(obtenerProductosVenta(conn, venta.getId()));
                ventas.add(venta);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return ventas;
    }

    private List<Producto> obtenerProductosVenta(Connection conn, int idVenta) throws SQLException {
        List<Producto> productos = new ArrayList<>();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            String sql = "SELECT p.id, p.nombre, dv.cantidad, dv.precio_unitario " +
                       "FROM DetalleVenta dv " +
                       "JOIN Productos p ON dv.id_producto = p.id " +
                       "WHERE dv.id_venta = ?";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, idVenta);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Producto producto = new Producto(usuario);
                producto.setId(rs.getString("id"));
                producto.setNombre(rs.getString("nombre"));
                producto.setCantidad(rs.getInt("cantidad"));
                producto.setPrecioUnitario(rs.getDouble("precio_unitario"));
                productos.add(producto);
            }
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
        }
        
        return productos;
    }
    
    private void exportarTablaACSV(JTable table) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar como CSV");
        fileChooser.setSelectedFile(new File("tickets_exportados.csv"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter pw = new PrintWriter(fileChooser.getSelectedFile())) {
                // Encabezados
                for (int i = 0; i < table.getColumnCount(); i++) {
                    pw.print(table.getColumnName(i));
                    if (i < table.getColumnCount() - 1) pw.print(",");
                }
                pw.println();
                
                // Datos
                for (int row = 0; row < table.getRowCount(); row++) {
                    for (int col = 0; col < table.getColumnCount(); col++) {
                        Object value = table.getValueAt(row, col);
                        pw.print(value != null ? value.toString().replace(",", ";") : "");
                        if (col < table.getColumnCount() - 1) pw.print(",");
                    }
                    pw.println();
                }
                
                JOptionPane.showMessageDialog(this, "Datos exportados exitosamente");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error al exportar: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void seleccionarTicket(JDialog parent, JTable table) {
        int row = table.getSelectedRow();
        if (row >= 0) {
            String id = table.getModel().getValueAt(row, 0).toString();
            txtIdDocumento.setText(id);
            parent.dispose();
        } else {
            JOptionPane.showMessageDialog(parent, 
                "Por favor seleccione un ticket de la lista", 
                "Advertencia", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void mostrarDialogoBusquedaReportes(String tipoReporte) {
        String idReporte = JOptionPane.showInputDialog(
            this, 
            "Ingrese el ID del reporte de " + tipoReporte,
            "Buscar Reporte",
            JOptionPane.QUESTION_MESSAGE
        );

        if (idReporte != null && !idReporte.trim().isEmpty()) {
            try {
                String reportType = "Reporte de " + tipoReporte; // Ej: "Reporte de Clientes"
                controlador.reimprimirReporte(reportType.toUpperCase(), idReporte.trim());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al buscar: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void reimprimirDocumento() {
        String type = (String) cmbTipoDocumento.getSelectedItem();
        String id = txtIdDocumento.getText().trim();
        
        System.out.println("[DEBUG] Intentando reimprimir: Tipo=" + type + ", ID=" + id); // Log

        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor ingrese un ID válido", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            if (type.equals("Ticket de Venta")) {
                System.out.println("[DEBUG] Reimprimiendo ticket...");
                controlador.reimprimirTicketVenta(id);
            } else {
                String reportType = type.replace("Reporte de ", "").toUpperCase();
                System.out.println("[DEBUG] Reimprimiendo reporte: " + reportType);
                controlador.reimprimirReporte(reportType, id);
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Fallo al reimprimir: " + e.getMessage()); // Log
            JOptionPane.showMessageDialog(this, "Error al reimprimir: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    
    }
}