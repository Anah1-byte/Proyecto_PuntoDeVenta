package Vista;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import Controlador.ControladorInventario;

public class GestionCategorias extends JDialog {
    private ControladorInventario controlador;
    private JTable tablaCategorias;
    
    public GestionCategorias(JFrame parent, ControladorInventario controlador) {
        super(parent, "Gestión de Categorías", true);
        this.controlador = controlador;
        initUI();
    }
    
    private void initUI() {
        setSize(500, 400);
        
        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Tabla de categorías
        String[] columnNames = {"Nombre", "Descripción"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        tablaCategorias = new JTable(model);
        actualizarTabla();
        
        mainPanel.add(new JScrollPane(tablaCategorias), BorderLayout.CENTER);
        
        // Panel de botones
        JPanel buttonPanel = new JPanel();
        
        JButton btnAgregar = new JButton("Agregar");
        btnAgregar.addActionListener(e -> agregarCategoria());
        
        JButton btnEliminar = new JButton("Eliminar");
        btnEliminar.addActionListener(e -> eliminarCategoria());
        
        buttonPanel.add(btnAgregar);
        buttonPanel.add(btnEliminar);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        getContentPane().add(mainPanel);
    }
    
    private void actualizarTabla() {
        DefaultTableModel model = (DefaultTableModel) tablaCategorias.getModel();
        model.setRowCount(0);
        
        List<String> categorias = controlador.getCategorias();
        for (String categoria : categorias) {
            model.addRow(new Object[]{categoria, ""}); // Puedes cargar descripción si la muestras
        }
    }
    
    private void agregarCategoria() {
        JTextField txtNombre = new JTextField();
        JTextField txtDescripcion = new JTextField();
        
        Object[] fields = {
            "Nombre:", txtNombre,
            "Descripción:", txtDescripcion
        };
        
        int option = JOptionPane.showConfirmDialog(
            this,
            fields,
            "Nueva Categoría",
            JOptionPane.OK_CANCEL_OPTION
        );
        
        if (option == JOptionPane.OK_OPTION) {
            if (controlador.agregarCategoria(txtNombre.getText(), txtDescripcion.getText())) {
                actualizarTabla();
            } else {
                JOptionPane.showMessageDialog(this, "Error al agregar categoría");
            }
        }
    }
    
    private void eliminarCategoria() {
        // Implementar lógica de eliminación
    	        int selectedRow = tablaCategorias.getSelectedRow();
    	                if (selectedRow != -1) {
    	                	String nombreCategoria = (String) tablaCategorias.getValueAt(selectedRow, 0);
							if (controlador.eliminarCategoria(nombreCategoria)) {
								((DefaultTableModel) tablaCategorias.getModel()).removeRow(selectedRow);
							} else {
								JOptionPane.showMessageDialog(this, "Error al eliminar categoría");
							}
    	                }
    }
}