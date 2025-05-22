package Vista;

import Modelo.Cliente;
import Modelo.Usuario;
import Controlador.ReportesControlador;
import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

public class ReporteClientePanel extends JPanel {
    private final ReportesControlador controlador;
    private final CardLayout cardLayout;
    private final JPanel panelContenido;
    private JTable tablaClientes;
    private JComboBox<String> comboFiltro;
    private JComboBox<String> comboExportar;
    private JLabel lblTotal, lblActivos, lblInactivos;
    private ChartPanel chartPanelRegistros, chartPanelPuntos;

    public ReporteClientePanel(Usuario usuario, ReportesControlador controlador, 
                              CardLayout cardLayout, JPanel panelContenido) {
        this.controlador = controlador;
        this.cardLayout = cardLayout;
        this.panelContenido = panelContenido;
        
        initUI();
        cargarDatosIniciales();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // 1. Panel de controles superiores (filtros y exportación)
        JPanel panelControles = crearPanelControles();
        add(panelControles, BorderLayout.NORTH);
        
        // 2. Panel central con tabla de clientes y gráficas
        JPanel panelCentral = new JPanel(new BorderLayout(10, 10));
        
        // Tabla de clientes
        JScrollPane scrollPane = crearTablaClientes();
        panelCentral.add(scrollPane, BorderLayout.CENTER);
        
        // Panel de gráficas
        JPanel panelGraficas = crearPanelGraficas();
        panelCentral.add(panelGraficas, BorderLayout.SOUTH);
        
        add(panelCentral, BorderLayout.CENTER);
        
        // 3. Panel inferior con botones
        JPanel panelInferior = crearPanelInferior();
        add(panelInferior, BorderLayout.SOUTH);
    }

    private JPanel crearPanelControles() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // Panel de Filtros
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelFiltros.setBorder(BorderFactory.createTitledBorder("Filtrar Clientes"));
        
        comboFiltro = new JComboBox<>(new String[]{"Todos", "Activos", "Inactivos", "Con Puntos", "Sin Puntos"});
        comboFiltro.setPreferredSize(new Dimension(150, 30));
        
        JButton btnFiltrar = new JButton("Filtrar");
        btnFiltrar.setPreferredSize(new Dimension(100, 30));
        btnFiltrar.addActionListener(e -> controlador.filtrarClientes(
            comboFiltro.getSelectedItem().toString()));
        
        panelFiltros.add(new JLabel("Filtro:"));
        panelFiltros.add(comboFiltro);
        panelFiltros.add(btnFiltrar);
        
        // Panel de Exportación
        JPanel panelExportar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        panelExportar.setBorder(BorderFactory.createTitledBorder("Exportar Reporte"));
        
        comboExportar = new JComboBox<>(new String[]{"PDF", "Excel", "HTML"});
        comboExportar.setPreferredSize(new Dimension(100, 30));
        
        JButton btnExportar = new JButton("Exportar");
        btnExportar.setPreferredSize(new Dimension(100, 30));
        btnExportar.addActionListener(e -> controlador.exportarReporteClientes(
            comboExportar.getSelectedItem().toString()));
        
        panelExportar.add(new JLabel("Formato:"));
        panelExportar.add(comboExportar);
        panelExportar.add(btnExportar);
        
        panel.add(panelFiltros);
        panel.add(panelExportar);
        
        return panel;
    }

    private JScrollPane crearTablaClientes() {
        // Columnas de la tabla
        String[] columnNames = {"ID", "Teléfono", "Nombre", "Última Compra", "Puntos", "Fecha Registro"};
        
        // Modelo de tabla vacío inicialmente
        tablaClientes = new JTable(new Object[0][columnNames.length], columnNames);
        tablaClientes.setFont(new Font("Arial", Font.PLAIN, 12));
        tablaClientes.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        return new JScrollPane(tablaClientes);
    }

    private JPanel crearPanelGraficas() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 0));
        panel.setBorder(BorderFactory.createTitledBorder("Estadísticas de Clientes"));
        
        // Gráfica 1: Registros por mes
        DefaultCategoryDataset datasetRegistros = new DefaultCategoryDataset();
        datasetRegistros.addValue(0, "Registros", "Ene");
        datasetRegistros.addValue(0, "Registros", "Feb");
        // ... otros meses
        
        JFreeChart chartRegistros = ChartFactory.createBarChart(
            "Registros de Clientes por Mes", 
            "Mes", 
            "Cantidad", 
            datasetRegistros
        );
        chartPanelRegistros = new ChartPanel(chartRegistros);
        panel.add(chartPanelRegistros);
        
        // Gráfica 2: Distribución de puntos
        DefaultPieDataset datasetPuntos = new DefaultPieDataset();
        datasetPuntos.setValue("0-100 pts", 0);
        datasetPuntos.setValue("101-500 pts", 0);
        datasetPuntos.setValue("501+ pts", 0);
        
        JFreeChart chartPuntos = ChartFactory.createPieChart(
            "Distribución de Puntos", 
            datasetPuntos, 
            true, true, false
        );
        chartPanelPuntos = new ChartPanel(chartPuntos);
        panel.add(chartPanelPuntos);
        
        return panel;
    }

    private JPanel crearPanelInferior() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Estadísticas rápidas
        JPanel panelStats = new JPanel(new GridLayout(1, 3, 10, 0));
        panelStats.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        lblTotal = new JLabel("Total Clientes: 0", SwingConstants.CENTER);
        lblActivos = new JLabel("Activos: 0", SwingConstants.CENTER);
        lblInactivos = new JLabel("Inactivos: 0", SwingConstants.CENTER);
        
        panelStats.add(lblTotal);
        panelStats.add(lblActivos);
        panelStats.add(lblInactivos);
        
        // Botón de regreso
        JButton btnRegresar = new JButton("Regresar al Menú de Reportes");
        btnRegresar.addActionListener(e -> mostrarMenuPrincipalReportes());
        
        panel.add(panelStats, BorderLayout.CENTER);
        panel.add(btnRegresar, BorderLayout.SOUTH);
        
        return panel;
    }

    private void cargarDatosIniciales() {
        controlador.cargarDatosClientes();
    }

    public void mostrarMenuPrincipalReportes() {
        cardLayout.show(panelContenido, "menu_principal");
    }

    // Métodos para actualizar la vista desde el controlador
    public void actualizarTablaClientes(List<Cliente> clientes) {
        String[] columnNames = {"ID", "Teléfono", "Nombre", "Última Compra", "Puntos", "Fecha Registro"};
        Object[][] data = new Object[clientes.size()][columnNames.length];
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        
        for (int i = 0; i < clientes.size(); i++) {
            Cliente c = clientes.get(i);
            data[i][0] = c.getId();
            data[i][1] = c.getTelefono();
            data[i][2] = c.getNombre();
            data[i][3] = c.getUltimaCompra();
            data[i][4] = c.getPuntos();
            data[i][5] = c.getFechaRegistro() != null ? sdf.format(c.getFechaRegistro()) : "";
        }
        
        tablaClientes.setModel(new javax.swing.table.DefaultTableModel(data, columnNames));
    }

    public void actualizarEstadisticas(int totalClientes, int activos, int inactivos) {
        lblTotal.setText("Total Clientes: " + totalClientes);
        lblActivos.setText("Activos: " + activos);
        lblInactivos.setText("Inactivos: " + inactivos);
    }
    
    public void actualizarGraficas(Map<String, Integer> registrosPorMes, 
                                 Map<String, Integer> distribucionPuntos) {
        // Actualizar gráfica de registros por mes
        DefaultCategoryDataset datasetRegistros = new DefaultCategoryDataset();
        registrosPorMes.forEach((mes, cantidad) -> {
            datasetRegistros.addValue(cantidad, "Registros", mes);
        });
        
        JFreeChart chartRegistros = ChartFactory.createBarChart(
            "Registros de Clientes por Mes", 
            "Mes", 
            "Cantidad", 
            datasetRegistros
        );
        chartPanelRegistros.setChart(chartRegistros);
        
        // Actualizar gráfica de distribución de puntos
        DefaultPieDataset datasetPuntos = new DefaultPieDataset();
        distribucionPuntos.forEach(datasetPuntos::setValue);
        
        JFreeChart chartPuntos = ChartFactory.createPieChart(
            "Distribución de Puntos", 
            datasetPuntos, 
            true, true, false
        );
        chartPanelPuntos.setChart(chartPuntos);
    }
    
    public void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}