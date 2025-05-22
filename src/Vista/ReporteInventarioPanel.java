package Vista;

import Modelo.Usuario;
import Modelo.Producto;
import Controlador.ReportesControlador;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

public class ReporteInventarioPanel extends JPanel {
    private final ReportesControlador controlador;
    private final CardLayout cardLayout;
    private final JPanel panelContenido;
    private JTable tablaInventario;
    private JComboBox<String> comboFiltro;
    private JComboBox<String> comboExportar;
    private JLabel lblTotal, lblBajoStock, lblSinStock;
    private ChartPanel chartPanelStock, chartPanelCategorias;

    public ReporteInventarioPanel(Usuario usuario, ReportesControlador controlador, 
                                CardLayout cardLayout, JPanel panelContenido) {
        this.controlador = controlador;
        this.cardLayout = cardLayout;
        this.panelContenido = panelContenido;
        controlador.setPanelInventario(this);
        initUI();
        cargarDatosIniciales();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // 1. Panel de controles superiores (filtros y exportación)
        JPanel panelControles = crearPanelControles();
        add(panelControles, BorderLayout.NORTH);
        
        // 2. Panel central con tabla de inventario y gráficas
        JPanel panelCentral = new JPanel(new BorderLayout(10, 10));
        
        // Tabla de inventario
        JScrollPane scrollPane = crearTablaInventario();
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
        panelFiltros.setBorder(BorderFactory.createTitledBorder("Filtrar Inventario"));
        
        comboFiltro = new JComboBox<>(new String[]{"Todos", "Bajo Stock", "Sobre Stock", "Categoría", "Proveedor"});
        comboFiltro.setPreferredSize(new Dimension(150, 30));
        
        JButton btnFiltrar = new JButton("Filtrar");
        btnFiltrar.setPreferredSize(new Dimension(100, 30));
        btnFiltrar.addActionListener(e -> controlador.filtrarInventario(
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
        btnExportar.addActionListener(e -> controlador.exportarReporteInventario(
            comboExportar.getSelectedItem().toString()));
        
        panelExportar.add(new JLabel("Formato:"));
        panelExportar.add(comboExportar);
        panelExportar.add(btnExportar);
        
        panel.add(panelFiltros);
        panel.add(panelExportar);
        
        return panel;
    }

    private JScrollPane crearTablaInventario() {
        // Columnas de la tabla
        String[] columnNames = {"ID", "Producto", "Categoría", "Stock", "Stock Mín", "Stock Máx", "Precio", "Proveedor"};
        
        // Modelo de tabla vacío inicialmente
        tablaInventario = new JTable(new Object[0][columnNames.length], columnNames);
        tablaInventario.setFont(new Font("Arial", Font.PLAIN, 12));
        tablaInventario.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        return new JScrollPane(tablaInventario);
    }

    private JPanel crearPanelGraficas() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 0));
        panel.setBorder(BorderFactory.createTitledBorder("Estadísticas de Inventario"));
        
        // Gráfica 1: Niveles de stock
        DefaultCategoryDataset datasetStock = new DefaultCategoryDataset();
        datasetStock.addValue(0, "Stock", "Actual");
        datasetStock.addValue(0, "Stock", "Mínimo");
        datasetStock.addValue(0, "Stock", "Máximo");
        
        JFreeChart chartStock = ChartFactory.createBarChart(
            "Niveles de Stock Promedio", 
            "", 
            "Cantidad", 
            datasetStock
        );
        chartPanelStock = new ChartPanel(chartStock);
        panel.add(chartPanelStock);
        
        // Gráfica 2: Distribución por categoría
        DefaultPieDataset datasetCategorias = new DefaultPieDataset();
        datasetCategorias.setValue("Sin datos", 1);
        
        JFreeChart chartCategorias = ChartFactory.createPieChart(
            "Distribución por Categoría", 
            datasetCategorias, 
            true, true, false
        );
        chartPanelCategorias = new ChartPanel(chartCategorias);
        panel.add(chartPanelCategorias);
        
        return panel;
    }

    private JPanel crearPanelInferior() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Estadísticas rápidas
        JPanel panelStats = new JPanel(new GridLayout(1, 3, 10, 0));
        panelStats.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        lblTotal = new JLabel("Total Productos: 0", SwingConstants.CENTER);
        lblBajoStock = new JLabel("Bajo Stock: 0", SwingConstants.CENTER);
        lblSinStock = new JLabel("Sin Stock: 0", SwingConstants.CENTER);
        
        panelStats.add(lblTotal);
        panelStats.add(lblBajoStock);
        panelStats.add(lblSinStock);
        
        // Botón de regreso
        JButton btnRegresar = new JButton("Regresar al Menú de Reportes");
        btnRegresar.addActionListener(e -> mostrarMenuPrincipalReportes());
        
        panel.add(panelStats, BorderLayout.CENTER);
        panel.add(btnRegresar, BorderLayout.SOUTH);
        
        return panel;
    }

    private void cargarDatosIniciales() {
        controlador.cargarDatosInventario();
    }

    public void mostrarMenuPrincipalReportes() {
        cardLayout.show(panelContenido, "menu_principal");
    }

    // Métodos para actualizar la vista desde el controlador
    public void actualizarTablaInventario(List<Producto> productos) {
        String[] columnNames = {"ID", "Producto", "Categoría", "Stock", "Stock Mín", "Stock Máx", "Precio", "Proveedor"};
        Object[][] data = new Object[productos.size()][columnNames.length];
        
        for (int i = 0; i < productos.size(); i++) {
            Producto p = productos.get(i);
            data[i][0] = p.getId();
            data[i][1] = p.getNombre();
            data[i][2] = p.getCategoria();
            data[i][3] = p.getCantidadDisponible();
            data[i][4] = p.getStockMinimo();
            data[i][5] = p.getStockMaximo();
            data[i][6] = String.format("$%.2f", p.getPrecioVenta());
            data[i][7] = p.getProveedor();
        }
        
        tablaInventario.setModel(new javax.swing.table.DefaultTableModel(data, columnNames));
    }

    public void actualizarEstadisticas(int totalProductos, int bajoStock, int sinStock) {
        lblTotal.setText("Total Productos: " + totalProductos);
        lblBajoStock.setText("Bajo Stock: " + bajoStock);
        lblSinStock.setText("Sin Stock: " + sinStock);
    }
    
    public void actualizarGraficas(double stockPromedio, double stockMinPromedio, double stockMaxPromedio, 
                                 Map<String, Integer> distribucionCategorias) {
        // Actualizar gráfica de niveles de stock
        DefaultCategoryDataset datasetStock = new DefaultCategoryDataset();
        datasetStock.addValue(stockPromedio, "Stock", "Actual");
        datasetStock.addValue(stockMinPromedio, "Stock", "Mínimo");
        datasetStock.addValue(stockMaxPromedio, "Stock", "Máximo");
        
        JFreeChart chartStock = ChartFactory.createBarChart(
            "Niveles de Stock Promedio", 
            "", 
            "Cantidad", 
            datasetStock
        );
        chartPanelStock.setChart(chartStock);
        
        // Actualizar gráfica de distribución por categoría
        DefaultPieDataset datasetCategorias = new DefaultPieDataset();
        distribucionCategorias.forEach(datasetCategorias::setValue);
        
        JFreeChart chartCategorias = ChartFactory.createPieChart(
            "Distribución por Categoría", 
            datasetCategorias, 
            true, true, false
        );
        chartPanelCategorias.setChart(chartCategorias);
    }
}