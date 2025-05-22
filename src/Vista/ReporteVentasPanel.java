package Vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;

import com.toedter.calendar.JDateChooser;

import Controlador.ReportesControlador;
import Modelo.Producto;
import Modelo.Usuario;
import Modelo.Venta;
import Vista.reportes;

public class ReporteVentasPanel extends JPanel {
    private Usuario usuario;
    private ReportesControlador controlador;
    
    // Componentes para filtros
    private JComboBox<String> comboFiltro;
    private JDateChooser dateChooserInicio;
    private JDateChooser dateChooserFin;
    private JButton btnFiltrar;
    
    // Componentes para exportación
    private JComboBox<String> comboExportar;
    private JButton btnExportar;
    private JButton btnImprimir;
    
    // Componentes de datos
    private JTextArea txtAreaVentas;
    private JTable tablaResumen;
    private StatCard[] statsCards;         // Para el gráfico principal de resumen
    private ChartPanel graficoMetodosPanel; // Para el gráfico de métodos de pago
    private ChartPanel graficoProductosPanel;
    private ChartPanel graficoResumenPanel;
    private ChartPanel chartPanel;
    
    public ReporteVentasPanel(Usuario usuario, ReportesControlador controlador) {
        this.controlador = controlador;
        this.controlador.setPanelVentas(this); // ✅ ASÍ EVITAS EL NULL
        initUI();
        setControlador(controlador);
    }
    
    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JPanel panelControles = crearPanelControles();
        add(panelControles, BorderLayout.NORTH);
        
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));
        tabbedPane.addTab("Resumen", crearPanelResumen());
        tabbedPane.addTab("Detalle", crearPanelDetalle());
        tabbedPane.addTab("Gráficos", crearPanelGraficos());
        add(tabbedPane, BorderLayout.CENTER);

        this.chartPanel = new ChartPanel(null);
        this.chartPanel.setPreferredSize(new Dimension(700, 400));
        this.chartPanel.setBorder(BorderFactory.createEtchedBorder());

        add(crearPanelEstadisticas(), BorderLayout.SOUTH);

        // ✅ Al final, cuando todo está inicializado
        controlador.filtrarReporte();
        
    }

    private JPanel crearPanelControles() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // Panel de Filtros
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelFiltros.setBorder(BorderFactory.createTitledBorder("Filtrar Reporte"));
        
        comboFiltro = new JComboBox<>(new String[]{"Todos", "Hoy", "Esta semana", "Este mes", "Rango personalizado"});
        comboFiltro.setPreferredSize(new Dimension(150, 30));
        
        dateChooserInicio = new JDateChooser();
        dateChooserInicio.setPreferredSize(new Dimension(120, 30));
        dateChooserInicio.setEnabled(false);
        
        dateChooserFin = new JDateChooser();
        dateChooserFin.setPreferredSize(new Dimension(120, 30));
        dateChooserFin.setEnabled(false);
        
        comboFiltro.addActionListener(e -> {
            boolean rangoPersonalizado = "Rango personalizado".equals(comboFiltro.getSelectedItem());
            dateChooserInicio.setEnabled(rangoPersonalizado);
            dateChooserFin.setEnabled(rangoPersonalizado);
        });
        
        btnFiltrar = new JButton("Filtrar");
        btnFiltrar.setPreferredSize(new Dimension(100, 30));
        btnFiltrar.addActionListener(e -> controlador.filtrarReporte());
        
        panelFiltros.add(new JLabel("Período:"));
        panelFiltros.add(comboFiltro);
        panelFiltros.add(new JLabel("Desde:"));
        panelFiltros.add(dateChooserInicio);
        panelFiltros.add(new JLabel("Hasta:"));
        panelFiltros.add(dateChooserFin);
        panelFiltros.add(btnFiltrar);
        
        // Panel de Exportación
        JPanel panelExportar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        panelExportar.setBorder(BorderFactory.createTitledBorder("Exportar Reporte"));
        
        comboExportar = new JComboBox<>(new String[]{"PDF", "Excel", "HTML", "CSV"});
        comboExportar.setPreferredSize(new Dimension(100, 30));
        
        btnExportar = new JButton("Exportar");
        btnExportar.setPreferredSize(new Dimension(100, 30));
        btnExportar.addActionListener(e -> controlador.exportarReporte());
        
        btnImprimir = new JButton("Imprimir");
        btnImprimir.setPreferredSize(new Dimension(100, 30));
        btnImprimir.addActionListener(e -> controlador.imprimirReporte());
        
        panelExportar.add(new JLabel("Formato:"));
        panelExportar.add(comboExportar);
        panelExportar.add(btnExportar);
        panelExportar.add(btnImprimir);
        
        panel.add(panelFiltros);
        panel.add(panelExportar);
        
        return panel;
    }

    private JPanel crearPanelResumen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Crear dataset vacío inicial
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        // Crear gráfico inicial bien configurado
        JFreeChart chart = ChartFactory.createBarChart(
            "Resumen de Ventas",
            "Fecha",
            "Monto ($)",
            dataset,
            PlotOrientation.VERTICAL,
            true,  // Mostrar leyenda
            true,  // Mostrar tooltips
            false  // No URLs
        );
        
        // Configuración inicial del gráfico
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        
        // Configurar el renderizador
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(79, 129, 189));
        
        // Crear ChartPanel con el gráfico configurado
        this.chartPanel = new ChartPanel(chart) {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Solo mostrar mensaje si no hay datos
                if (((CategoryPlot)getChart().getPlot()).getDataset().getRowCount() == 0) {
                    drawPlaceholder(g, "Esperando datos para mostrar el gráfico...");
                }
            }
        };
        
        // Configuración del panel
        this.chartPanel.setPreferredSize(new Dimension(800, 500));
        this.chartPanel.setMinimumSize(new Dimension(400, 300));
        this.chartPanel.setBorder(BorderFactory.createEtchedBorder());
        this.chartPanel.setBackground(Color.WHITE);
        
        panel.add(this.chartPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
 // gráficos secundarios
    public ChartPanel getGraficoMetodosPago() {
        if (this.graficoMetodosPanel == null) {
            initializeMetodosPagoPanel();
        }
        return this.graficoMetodosPanel;
    }

    public ChartPanel getGraficoProductos() {
        if (this.graficoProductosPanel == null) {
            initializeProductosPanel();
        }
        return this.graficoProductosPanel;
    }
    private void initializeMetodosPagoPanel() {
        this.graficoMetodosPanel = new ChartPanel(null) {
            @Override
			public void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getChart() == null) {
                    drawPlaceholder(g, "Gráfico de Métodos de Pago");
                }
            }
        };
        this.graficoMetodosPanel.setPreferredSize(new Dimension(350, 300));
        this.graficoMetodosPanel.setBorder(BorderFactory.createTitledBorder("Métodos de Pago"));
    }

    private void initializeProductosPanel() {
        this.graficoProductosPanel = new ChartPanel(null) {
            @Override
			public void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getChart() == null) {
                    drawPlaceholder(g, "Gráfico de Productos");
                }
            }
        };
        this.graficoProductosPanel.setPreferredSize(new Dimension(350, 300));
        this.graficoProductosPanel.setBorder(BorderFactory.createTitledBorder("Productos Más Vendidos"));
    }
    // Método para actualizar el resumen
    public void actualizarResumen(List<Venta> ventas) {
        System.out.println("[DEBUG] Actualizando resumen con " + ventas.size() + " ventas");
        
        if (ventas == null || ventas.isEmpty()) {
            mostrarMensajeEnGrafico("No hay datos para mostrar");
            return;
        }

        try {
            // 1. Preparar dataset con estructura adecuada
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm");
            
            // 2. Procesar datos - usar fecha como categoría y "Ventas" como serie
            Map<String, Double> ventasPorFecha = new LinkedHashMap<>();
            for (Venta venta : ventas) {
                String fechaHora = sdf.format(venta.getFecha());
                ventasPorFecha.merge(fechaHora, venta.getTotal(), Double::sum);
            }
            
            // Agregar datos al dataset
            ventasPorFecha.forEach((fecha, total) -> {
                dataset.addValue(total, "Ventas", fecha);
            });
            
            System.out.println("[DEBUG] Dataset preparado con " + dataset.getRowCount() + 
                             " filas y " + dataset.getColumnCount() + " columnas");
            
            // 3. Obtener el gráfico existente o crear uno nuevo
            JFreeChart chart = chartPanel.getChart();
            if (chart == null) {
                chart = ChartFactory.createBarChart(
                    "Resumen de Ventas",
                    "Fecha y Hora",
                    "Monto ($)",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true, true, false
                );
                chartPanel.setChart(chart);
            }
            
            // 4. Actualizar el gráfico
            CategoryPlot plot = (CategoryPlot) chart.getPlot();
            plot.setDataset(dataset);
            
            // Personalización del gráfico
            BarRenderer renderer = (BarRenderer) plot.getRenderer();
            renderer.setSeriesPaint(0, new Color(79, 129, 189)); // Color azul
            renderer.setShadowVisible(true);
            renderer.setShadowPaint(new Color(100, 100, 100));
            
            // Configurar ejes para mejor visualización
            CategoryAxis domainAxis = plot.getDomainAxis();
            domainAxis.setCategoryLabelPositions(
                CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 4.0) // Rotación de 45°
            );
            
            NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
            rangeAxis.setNumberFormatOverride(NumberFormat.getCurrencyInstance());
            
            // Ajustar el rango del eje Y automáticamente
            rangeAxis.setAutoRange(true);
            
            // Forzar actualización visual
            chartPanel.revalidate();
            chartPanel.repaint();
            
        } catch (Exception e) {
            System.err.println("[ERROR] Al actualizar gráfico: " + e.getMessage());
            e.printStackTrace();
            mostrarError("Error al generar gráfico: " + e.getMessage());
        }
    }

    // Modificar drawPlaceholder() para que no interfiera con el gráfico
    private void drawPlaceholder(Graphics g, String message) {
        // Verificar que realmente no hay datos
        if (chartPanel != null && chartPanel.getChart() != null) {
            CategoryPlot plot = (CategoryPlot) chartPanel.getChart().getPlot();
            if (plot.getDataset() != null && plot.getDataset().getRowCount() > 0) {
                return; // Hay datos, no mostrar placeholder
            }
        }
        
        Graphics2D g2d = (Graphics2D) g.create();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                               RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setFont(new Font("Arial", Font.ITALIC, 16));
            g2d.setColor(new Color(100, 100, 100, 150)); // Gris semi-transparente
            
            FontMetrics fm = g2d.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(message)) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            
            g2d.drawString(message, x, y);
        } finally {
            g2d.dispose();
        }
    }
    private void mostrarMensajeEnGrafico(String mensaje) {
        if (chartPanel != null) {
            // Crear un gráfico vacío con el mensaje
            JFreeChart chart = ChartFactory.createBarChart(
                "Resumen de Ventas",
                "",
                "",
                new DefaultCategoryDataset(),
                PlotOrientation.VERTICAL,
                false, false, false
            );
            
            chart.addSubtitle(new TextTitle(mensaje, new Font("Arial", Font.ITALIC, 14)));
            chartPanel.setChart(chart);
            chartPanel.repaint();
        }
    }

    // Métodos auxiliares desglosados:

    private DefaultCategoryDataset crearDatasetVentas(List<Venta> ventas) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM");
        Map<String, Double> ventasPorDia = new TreeMap<>();  // TreeMap para ordenar por fecha

        for (Venta venta : ventas) {
            String dia = sdf.format(venta.getFecha());
            ventasPorDia.merge(dia, venta.getTotal(), Double::sum);
        }

        ventasPorDia.forEach((dia, total) -> {
            dataset.addValue(total, "Ventas", dia);
        });

        return dataset;
    }

    private JFreeChart obtenerGraficoResumen(DefaultCategoryDataset dataset) {
        if (graficoResumenPanel == null || graficoResumenPanel.getChart() == null) {
            return ChartFactory.createBarChart(
                "Resumen de Ventas Diarias",
                "Fecha", 
                "Monto ($)",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
            );
        }
        
        // Reutilizar gráfico existente
        JFreeChart chart = graficoResumenPanel.getChart();
        chart.getCategoryPlot().setDataset(dataset);
        return chart;
    }

    private void personalizarGraficoResumen(JFreeChart chart) {
        chart.setBackgroundPaint(Color.WHITE);
        
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        
        // Personalizar barras
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(79, 129, 189)); // Azul corporativo
        renderer.setShadowVisible(true);
        renderer.setShadowPaint(new Color(200, 200, 200));
        
        // Personalizar ejes
        plot.getDomainAxis().setCategoryLabelPositions(
            CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0) // Rotar etiquetas 30°
        );
        
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setNumberFormatOverride(NumberFormat.getCurrencyInstance());
    }

    private void actualizarPanelGrafico(JFreeChart chart) {
        if (graficoResumenPanel == null) {
            graficoResumenPanel = new ChartPanel(chart) {
                @Override
				public void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    if (getChart() == null) {
                        drawPlaceholder(g, "Esperando datos...");
                    }
                }
            };
            graficoResumenPanel.setPreferredSize(new Dimension(700, 400));
            graficoResumenPanel.setBorder(BorderFactory.createEtchedBorder());
        } else {
            graficoResumenPanel.setChart(chart);
        }
        
        graficoResumenPanel.repaint();
    }

    private void mostrarErrorEnGraficoResumen(String mensaje) {
        DefaultCategoryDataset emptyDataset = new DefaultCategoryDataset();
        JFreeChart errorChart = ChartFactory.createBarChart(
            "Error en Resumen de Ventas",
            "",
            "",
            emptyDataset,
            PlotOrientation.VERTICAL,
            false, false, false
        );
        
        errorChart.addSubtitle(new TextTitle(mensaje, 
            new Font("Arial", Font.PLAIN, 12)));
        
        if (graficoResumenPanel != null) {
            graficoResumenPanel.setChart(errorChart);
        } else {
            System.err.println("Error: graficoResumenPanel no está inicializado");
        }
    }

    private JFreeChart createEmptyChart() {
        JFreeChart chart = ChartFactory.createBarChart(
            "Resumen de Ventas - Sin Datos",
            "Período",
            "Monto ($)",
            new DefaultCategoryDataset()
        );
        
        TextTitle message = new TextTitle("No hay datos disponibles para el período seleccionado",
            new Font("Arial", Font.ITALIC, 14));
        chart.addSubtitle(message);
        
        return chart;
    }

    private Map<String, Double> agruparVentasPorPeriodo(List<Venta> ventas) {
        Map<String, Double> ventasAgrupadas = new LinkedHashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM");
        
        for (Venta venta : ventas) {
            String periodo = sdf.format(venta.getFecha());
            ventasAgrupadas.merge(periodo, venta.getTotal(), Double::sum);
        }
        
        return ventasAgrupadas;
    }

    private void customizeChart(JFreeChart chart) {
        CategoryPlot plot = chart.getCategoryPlot();
        
        // Gradiente para el fondo
        plot.setBackgroundPaint(new GradientPaint(
            0, 0, new Color(240, 240, 240), 
            0, 100, new Color(200, 200, 200))
        );
        
        // Sombras en las barras
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setShadowVisible(true);
        renderer.setShadowPaint(Color.GRAY);
        renderer.setSeriesPaint(0, new Color(44, 160, 44)); // Verde moderno
        
        // Eje Y con formato monetario
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setNumberFormatOverride(NumberFormat.getCurrencyInstance());
        
        // Rotar etiquetas del eje X
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(
            CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0)
        );
    }
    
    private JPanel crearPanelDetalle() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        txtAreaVentas = new JTextArea();
        txtAreaVentas.setEditable(false);
        txtAreaVentas.setFont(new Font("Monospaced", Font.PLAIN, 14));
        
        JScrollPane scrollPane = new JScrollPane(txtAreaVentas);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Mensaje inicial
        txtAreaVentas.setText("Seleccione un período y haga clic en 'Filtrar' para ver el detalle de ventas");
        
        return panel;
    }
    
    private JPanel crearPanelGraficos() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Inicializar paneles de gráficos
        this.graficoMetodosPanel = new ChartPanel(null);
        this.graficoProductosPanel = new ChartPanel(null);
        
        // Configurar gráfico de métodos de pago
        graficoMetodosPanel.setPreferredSize(new Dimension(350, 300));
        graficoMetodosPanel.setBorder(BorderFactory.createTitledBorder("Métodos de Pago"));
        
        // Configurar gráfico de productos
        graficoProductosPanel.setPreferredSize(new Dimension(350, 300));
        graficoProductosPanel.setBorder(BorderFactory.createTitledBorder("Productos Más Vendidos"));
        
        panel.add(graficoMetodosPanel);
        panel.add(graficoProductosPanel);
        
        return panel;
    }
    
    private JPanel crearPanelEstadisticas() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 10, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        statsCards = new StatCard[4];
        statsCards[0] = new StatCard("Total Ventas", "$0.00", new Color(144, 238, 144));
        statsCards[1] = new StatCard("Ventas Hoy", "0", new Color(135, 206, 250));
        statsCards[2] = new StatCard("Productos Vendidos", "0", new Color(255, 182, 193));
        statsCards[3] = new StatCard("Ticket Promedio", "$0.00", new Color(221, 160, 221));
        
        for (StatCard card : statsCards) {
            card.setPreferredSize(new Dimension(250, 100));
            panel.add(card);
        }
        
        return panel;
    }
    
    // Métodos para actualizar datos desde el controlador
    public void mostrarDetalleVentas(List<Venta> ventas) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        StringBuilder sb = new StringBuilder();
        
        sb.append("══════════════════════════════════════════════════════════════════════════════════\n");
        sb.append("                            DETALLE DE VENTAS\n");
        sb.append("══════════════════════════════════════════════════════════════════════════════════\n");
        sb.append(String.format("%-10s %-20s %-30s %-10s %-10s\n", 
            "ID Venta", "Fecha", "Producto", "Cantidad", "Total"));
        sb.append("──────────────────────────────────────────────────────────────────────────────────\n");
        
        for (Venta venta : ventas) {
            for (Producto producto : venta.getProductos()) {
                sb.append(String.format("%-10d %-20s %-30s %-10d $%-10.2f\n",
                    venta.getId(),
                    sdf.format(venta.getFecha()),
                    producto.getNombre(),
                    producto.getCantidad(),
                    producto.getPrecioUnitario() * producto.getCantidad()));
            }
        }
        
        sb.append("══════════════════════════════════════════════════════════════════════════════════\n");
        sb.append(String.format("%62s $%-10.2f\n", "TOTAL VENTAS:", 
            ventas.stream().mapToDouble(Venta::getTotal).sum()));
        
        txtAreaVentas.setText(sb.toString());
        txtAreaVentas.setCaretPosition(0);
    }
    
    public void actualizarEstadisticas(String totalVentas, int ventasHoy, int productosVendidos, String ticketPromedio) {
        statsCards[0].setValue(totalVentas);
        statsCards[1].setValue(String.valueOf(ventasHoy));
        statsCards[2].setValue(String.valueOf(productosVendidos));
        statsCards[3].setValue(ticketPromedio);
        
        for (StatCard card : statsCards) {
            card.highlightChange();
        }
    }
    
    public Date getFechaInicio() {
        if ("Rango personalizado".equals(comboFiltro.getSelectedItem())) {
            return dateChooserInicio.getDate();
        } else {
            Calendar cal = Calendar.getInstance();
            switch (comboFiltro.getSelectedItem().toString()) {
                case "Hoy": return cal.getTime();
                case "Esta semana": cal.add(Calendar.DAY_OF_YEAR, -7); return cal.getTime();
                case "Este mes": cal.add(Calendar.MONTH, -1); return cal.getTime();
                default: return null; // "Todos"
            }
        }
    }
    
    public Date getFechaFin() {
        if ("Rango personalizado".equals(comboFiltro.getSelectedItem())) {
            return dateChooserFin.getDate();
        }
        return new Date(); // Fecha actual
    }
    
    public String getFormatoExportacion() {
        return comboExportar.getSelectedItem().toString();
    }
    
    // Clase interna para las tarjetas de estadísticas
    private class StatCard extends JPanel {
        private JLabel valueLabel;
        private Color originalColor;

        public StatCard(String title, String initialValue, Color color) {
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
            ));
            setBackground(color.brighter());
            this.originalColor = color.brighter();

            JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
            
            valueLabel = new JLabel(initialValue, SwingConstants.CENTER);
            valueLabel.setFont(new Font("Arial", Font.BOLD, 18));
            
            add(titleLabel, BorderLayout.NORTH);
            add(valueLabel, BorderLayout.CENTER);
        }

        public void setValue(String value) {
            valueLabel.setText(value);
        }

        public void highlightChange() {
            Timer timer = new Timer(300, e -> setBackground(originalColor));
            setBackground(Color.YELLOW);
            timer.setRepeats(false);
            timer.start();
        }
    }
    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }
    public void setControlador(ReportesControlador controlador) {
        btnExportar.addActionListener(e -> controlador.exportarReporte());
        btnImprimir.addActionListener(e -> controlador.imprimirReporte());
        btnFiltrar.addActionListener(e -> controlador.filtrarReporte());
    }
    
}
