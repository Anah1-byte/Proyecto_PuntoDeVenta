package Vista;

import Modelo.Usuario;
import Modelo.Proveedor;
import Controlador.ReportesControlador;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

public class ReporteProveedoresPanel extends JPanel {
    private final ReportesControlador controlador;
    private final CardLayout cardLayout;
    private final JPanel panelContenido;
    private JTable tablaProveedores;
    private JComboBox<String> comboFiltro;
    private JComboBox<String> comboExportar;
    private JLabel lblTotal, lblActivos, lblUltimaVisita;
    private ChartPanel chartPanelVisitas, chartPanelProductos;

    public ReporteProveedoresPanel(Usuario usuario, ReportesControlador controlador, 
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
        
        // 2. Panel central con tabla de proveedores y gráficas
        JPanel panelCentral = new JPanel(new BorderLayout(10, 10));
        
        // Tabla de proveedores
        JScrollPane scrollPane = crearTablaProveedores();
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
        panelFiltros.setBorder(BorderFactory.createTitledBorder("Filtrar Proveedores"));
        
        comboFiltro = new JComboBox<>(new String[]{"Todos", "Con Visita Reciente", "Sin Visita Reciente", "Por Producto"});
        comboFiltro.setPreferredSize(new Dimension(180, 30));
        
        JButton btnFiltrar = new JButton("Filtrar");
        btnFiltrar.setPreferredSize(new Dimension(100, 30));
        btnFiltrar.addActionListener(e -> controlador.filtrarProveedores(
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
        btnExportar.addActionListener(e -> controlador.exportarReporteProveedores(
            comboExportar.getSelectedItem().toString()));
        
        panelExportar.add(new JLabel("Formato:"));
        panelExportar.add(comboExportar);
        panelExportar.add(btnExportar);
        
        panel.add(panelFiltros);
        panel.add(panelExportar);
        
        return panel;
    }

    private JScrollPane crearTablaProveedores() {
        // Columnas de la tabla
        String[] columnNames = {"ID", "Nombre", "Teléfono", "Dirección", "Producto Suministrado", "Última Visita"};
        
        // Modelo de tabla vacío inicialmente
        tablaProveedores = new JTable(new Object[0][columnNames.length], columnNames);
        tablaProveedores.setFont(new Font("Arial", Font.PLAIN, 12));
        tablaProveedores.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        return new JScrollPane(tablaProveedores);
    }

    private JPanel crearPanelGraficas() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 0));
        panel.setBorder(BorderFactory.createTitledBorder("Estadísticas de Proveedores"));
        
        // Gráfica 1: Visitas por mes
        DefaultCategoryDataset datasetVisitas = new DefaultCategoryDataset();
        datasetVisitas.addValue(0, "Visitas", "Ene");
        datasetVisitas.addValue(0, "Visitas", "Feb");
        datasetVisitas.addValue(0, "Visitas", "Mar");
        
        JFreeChart chartVisitas = ChartFactory.createBarChart(
            "Visitas por Mes", 
            "Mes", 
            "Número de Visitas", 
            datasetVisitas
        );
        chartPanelVisitas = new ChartPanel(chartVisitas);
        panel.add(chartPanelVisitas);
        
        // Gráfica 2: Distribución por producto suministrado
        DefaultPieDataset datasetProductos = new DefaultPieDataset();
        datasetProductos.setValue("Sin datos", 1);
        
        JFreeChart chartProductos = ChartFactory.createPieChart(
            "Distribución por Producto", 
            datasetProductos, 
            true, true, false
        );
        chartPanelProductos = new ChartPanel(chartProductos);
        panel.add(chartPanelProductos);
        
        return panel;
    }

    private JPanel crearPanelInferior() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Estadísticas rápidas
        JPanel panelStats = new JPanel(new GridLayout(1, 3, 10, 0));
        panelStats.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        lblTotal = new JLabel("Total Proveedores: 0", SwingConstants.CENTER);
        lblActivos = new JLabel("Visitados este mes: 0", SwingConstants.CENTER);
        lblUltimaVisita = new JLabel("Última visita: N/A", SwingConstants.CENTER);
        
        panelStats.add(lblTotal);
        panelStats.add(lblActivos);
        panelStats.add(lblUltimaVisita);
        
        // Botón de regreso
        JButton btnRegresar = new JButton("Regresar al Menú de Reportes");
        btnRegresar.addActionListener(e -> mostrarMenuPrincipalReportes());
        
        panel.add(panelStats, BorderLayout.CENTER);
        panel.add(btnRegresar, BorderLayout.SOUTH);
        
        return panel;
    }

    private void cargarDatosIniciales() {
        controlador.cargarDatosProveedores();
    }

    public void mostrarMenuPrincipalReportes() {
        cardLayout.show(panelContenido, "menu_principal");
    }

    // Métodos para actualizar la vista desde el controlador
    public void actualizarTablaProveedores(List<Proveedor> proveedores) {
        String[] columnNames = {"ID", "Nombre", "Teléfono", "Dirección", "Producto Suministrado", "Última Visita"};
        Object[][] data = new Object[proveedores.size()][columnNames.length];
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        
        for (int i = 0; i < proveedores.size(); i++) {
            Proveedor p = proveedores.get(i);
            data[i][0] = p.getId();
            data[i][1] = p.getNombre();
            data[i][2] = p.getTelefono();
            data[i][3] = p.getDireccion();
            data[i][4] = p.getProductoSuministrado();
            data[i][5] = p.getUltimaVisita() != null ? sdf.format(p.getUltimaVisita()) : "Nunca";
        }
        
        tablaProveedores.setModel(new javax.swing.table.DefaultTableModel(data, columnNames));
    }

    public void actualizarEstadisticas(int totalProveedores, int visitadosEsteMes, String ultimaVisita) {
        lblTotal.setText("Total Proveedores: " + totalProveedores);
        lblActivos.setText("Visitados este mes: " + visitadosEsteMes);
        lblUltimaVisita.setText("Última visita: " + ultimaVisita);
    }
    
    public void actualizarGraficas(Map<String, Integer> visitasPorMes, Map<String, Integer> distribucionProductos) {
        // Actualizar gráfica de visitas por mes
        DefaultCategoryDataset datasetVisitas = new DefaultCategoryDataset();
        visitasPorMes.forEach((mes, cantidad) -> {
            datasetVisitas.addValue(cantidad, "Visitas", mes);
        });
        
        JFreeChart chartVisitas = ChartFactory.createBarChart(
            "Visitas por Mes", 
            "Mes", 
            "Número de Visitas", 
            datasetVisitas
        );
        chartPanelVisitas.setChart(chartVisitas);
        
        // Actualizar gráfica de distribución por producto
        DefaultPieDataset datasetProductos = new DefaultPieDataset();
        distribucionProductos.forEach(datasetProductos::setValue);
        
        JFreeChart chartProductos = ChartFactory.createPieChart(
            "Distribución por Producto", 
            datasetProductos, 
            true, true, false
        );
        chartPanelProductos.setChart(chartProductos);
    }
}