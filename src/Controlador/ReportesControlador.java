package Controlador;

import Modelo.*;
import Vista.reportes;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.List;
import ConexionBD.ConexionAccess;
import Vista.ReporteClientePanel;
import Vista.ReporteInventarioPanel;
import Vista.ReporteProveedoresPanel;
import Vista.ReporteVentasPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.Date;
import java.sql.*;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import Vista.*;


public class ReportesControlador {
    private reportes vista;
    private ReporteVentasPanel panelVentas;
    private final Reportes modelo;
    private String tipoReporteActual = "VENTAS"; // Por defecto
    private Document document;
    private PDFExporter pdfExporter;
    private Inventarioo inventarioo;
    private ReporteInventarioPanel panelInventario;
    private Clientee clientee;
    private ReporteClientePanel panelClientes;
    private Proveedorr proveedorr;
    private ReporteProveedoresPanel panelProveedores;
	private Usuario usuario;
    
    public ReportesControlador(reportes vista, Usuario usuario) {
    	this.usuario = usuario;
        this.vista = vista;
        this.modelo = new Reportes(usuario.getUsername());
        this.pdfExporter = new PDFExporter(this);
        this.inventarioo = new Inventarioo();
        this.clientee = new ClienteImpl();
        this.proveedorr = new Proveedorr();
    }
    
    public void setPanelVentas(ReporteVentasPanel panelVentas) {
        this.panelVentas = panelVentas;
        System.out.println("[DEBUG] Panel de ventas asignado al controlador");
    }
    
    public void setPanelInventario(ReporteInventarioPanel panelInventario) {
        this.panelInventario = panelInventario;
        System.out.println("[DEBUG] Panel de inventario asignado al controlador");
    }
    
    public void setPanelClientes(ReporteClientePanel panelClientes) {
        this.panelClientes = panelClientes;
        System.out.println("[DEBUG] Panel de clientes asignado al controlador");
    }
    public void setPanelProveedores(ReporteProveedoresPanel panelProveedores) {
        this.panelProveedores = panelProveedores;
        System.out.println("[DEBUG] Panel de proveedores asignado al controlador");
    }
    
    public void inicializarPaneles(Usuario usuario) {
        System.out.println("[DEBUG] Inicializando paneles - Fase 2");
        this.panelVentas = new ReporteVentasPanel(usuario, this);
    }

    private void verificarConexionBD() {
        try (Connection testConn = ConexionAccess.conectar()) {
            System.out.println("[DEBUG] Conexión a BD exitosa");
        } catch (SQLException e) {
            System.err.println("[ERROR] No se pudo conectar a la BD: " + e.getMessage());
            panelVentas.mostrarError("Error de conexión a la base de datos");
        }
    }

    public void filtrarReporte() {
        try {
            // 1. Obtener fechas del panel
            Date fechaInicio = panelVentas.getFechaInicio();
            Date fechaFin = panelVentas.getFechaFin();
            
            // Si no hay fecha de inicio, usar fecha muy antigua
            if (fechaInicio == null) {
                Calendar cal = Calendar.getInstance();
                cal.set(2000, 0, 1); // 1 de enero del 2000
                fechaInicio = cal.getTime();
                System.out.println("[DEBUG] Fecha inicio ajustada a valor por defecto: " + fechaInicio);
            }
            
            // 2. Ajustar fecha fin para incluir todo el día
            if (fechaFin != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(fechaFin);
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                fechaFin = cal.getTime();
            } else {
                fechaFin = new Date(); // Fecha actual
            }
            
            System.out.println("[DEBUG] Fechas seleccionadas - Inicio: " + fechaInicio + " - Fin: " + fechaFin);
            
            // 3. Obtener datos de la base de datos
            List<Venta> ventas = obtenerVentasDesdeBD(fechaInicio, fechaFin);
            System.out.println("[DEBUG] Ventas obtenidas: " + ventas.size());
            
            // 4. Actualizar vista con los datos
            actualizarVistaConDatos(ventas);
            
        } catch (SQLException e) {
            System.err.println("[ERROR] Al obtener ventas: " + e.getMessage());
            panelVentas.mostrarError("Error al cargar datos: " + e.getMessage());
        }
    }

    public void actualizarVistaConDatos(List<Venta> ventas) {
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("[DEBUG] Actualizando gráfico con " + ventas.size() + " ventas");
                
                // Actualizar componentes principales
                panelVentas.actualizarResumen(ventas);
                panelVentas.mostrarDetalleVentas(ventas);
                
                // Calcular y mostrar estadísticas
                Map<String, Object> stats = generarEstadisticasVentas(ventas);
                panelVentas.actualizarEstadisticas(
                    String.format("$%,.2f", stats.get("totalVentas")),
                    ventas.size(),
                    ((Number)stats.get("totalProductos")).intValue(),
                    String.format("$%,.2f", (double)stats.get("totalVentas")/ventas.size())
                );
                
                // Actualizar gráficos secundarios
                actualizarGraficosSecundarios(ventas, stats);
                
            } catch (Exception e) {
                System.err.println("[ERROR] Al actualizar vista: " + e.getMessage());
                panelVentas.mostrarError("Error al mostrar datos: " + e.getMessage());
            }
        });
    }

    private boolean esMismoDia(Date fecha1, Date fecha2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(fecha1);
        cal2.setTime(fecha2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
               cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }
    
    private void ajustarFechaFin(Date fechaFin) {
        if (fechaFin != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(fechaFin);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            fechaFin = cal.getTime();
        }
    }

    private String formatCurrency(double value) {
        return NumberFormat.getCurrencyInstance().format(value);
    }

    private void actualizarGraficosSecundarios(List<Venta> ventas, Map<String, Object> stats) {
        // Actualizar gráfico de métodos de pago
        if (panelVentas.getGraficoMetodosPago() != null) {
            DefaultPieDataset datasetMetodos = new DefaultPieDataset();
            ((Map<String, Double>) stats.get("metodosPago")).forEach(datasetMetodos::setValue);
            
            JFreeChart chartMetodos = ChartFactory.createPieChart(
                "Métodos de Pago",
                datasetMetodos,
                true, true, false
            );
            
            panelVentas.getGraficoMetodosPago().setChart(chartMetodos);
        }
        
        // Actualizar gráfico de productos más vendidos
        if (panelVentas.getGraficoProductos() != null) {
            DefaultCategoryDataset datasetProductos = new DefaultCategoryDataset();
            ((Map<String, Long>) stats.get("productosVendidos")).entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .forEach(e -> datasetProductos.addValue(e.getValue(), "Ventas", e.getKey()));
            
            JFreeChart chartProductos = ChartFactory.createBarChart(
                "Top 10 Productos",
                "Productos",
                "Unidades Vendidas",
                datasetProductos,
                PlotOrientation.VERTICAL,
                true, true, false
            );
            
            panelVentas.getGraficoProductos().setChart(chartProductos);
        }
    }

    public List<Venta> obtenerVentasDesdeBD(Date fechaInicio, Date fechaFin) throws SQLException {
        List<Venta> ventas = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmtVentas = null;
        PreparedStatement pstmtDetalles = null;
        ResultSet rsVentas = null;
        ResultSet rsDetalles = null;

        try {
            // 1. Obtener conexión
            conn = ConexionAccess.conectar();
            conn.setAutoCommit(false); // Usamos transacción

            // 2. Consulta base de datos para ventas principales
            String sqlVentas = construirQueryVentas(fechaInicio, fechaFin);
            pstmtVentas = conn.prepareStatement(sqlVentas);
            
            // 3. Establecer parámetros de fecha
            establecerParametrosFecha(pstmtVentas, fechaInicio, fechaFin);
            
            // 4. Ejecutar consulta de ventas
            rsVentas = pstmtVentas.executeQuery();
            
            // 5. Procesar resultados
            while (rsVentas.next()) {
                Venta venta = mapearVentaDesdeResultSet(rsVentas);
                
                // 6. Obtener detalles de la venta
                String sqlDetalles = "SELECT p.id, p.nombre, p.precio_venta, dv.cantidad " +
                                    "FROM DetalleVenta dv " +
                                    "JOIN Productos p ON dv.id_producto = p.id " +
                                    "WHERE dv.id_venta = ?";
                
                pstmtDetalles = conn.prepareStatement(sqlDetalles);
                pstmtDetalles.setLong(1, venta.getId());
                rsDetalles = pstmtDetalles.executeQuery();
                
                // 7. Procesar detalles
                while (rsDetalles.next()) {
                    Producto producto = new Producto(
                        rsDetalles.getString("id"),
                        rsDetalles.getString("nombre"),
                        "", // descripción
                        "", // categoría
                        "", // proveedor
                        rsDetalles.getInt("cantidad"),
                        0,  // stock mínimo
                        0,  // stock máximo
                        0.0, // precio compra
                        rsDetalles.getDouble("precio_venta"),
                        false, // tiene_iva
                        0.0, // porcentaje_iva
                        null, // fecha_vencimiento
                        "",  // codigo_barras
                        "",  // unidad_medida
                        ""   // notas
                    );
                    venta.agregarProducto(producto);
                }
                
                ventas.add(venta);
                cerrarResultSetYStatement(rsDetalles, pstmtDetalles);
            }
            
            conn.commit();
            System.out.println("[BD] Ventas obtenidas: " + ventas.size());
            return ventas;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("[BD] Error al hacer rollback: " + ex.getMessage());
                }
            }
            System.err.println("[BD] Error al obtener ventas: " + e.getMessage());
            throw e;
        } finally {
            cerrarResultSetYStatement(rsVentas, pstmtVentas);
            cerrarResultSetYStatement(rsDetalles, pstmtDetalles);
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Restaurar autocommit
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("[BD] Error al cerrar conexión: " + e.getMessage());
                }
            }
        }
    }

    private String construirQueryVentas(Date fechaInicio, Date fechaFin) {
        StringBuilder sql = new StringBuilder(
            "SELECT id, fecha, total, metodo_pago, descuento FROM Ventas WHERE 1=1"
        );
        
        if (fechaInicio != null) {
            sql.append(" AND fecha >= ?");
        }
        if (fechaFin != null) {
            sql.append(" AND fecha <= ?");
        }
        sql.append(" ORDER BY fecha DESC");
        
        return sql.toString();
    }

    private void establecerParametrosFecha(PreparedStatement pstmt, Date fechaInicio, Date fechaFin) 
        throws SQLException {
        int paramIndex = 1;
        
        if (fechaInicio != null) {
            pstmt.setTimestamp(paramIndex++, new Timestamp(fechaInicio.getTime()));
        }
        if (fechaFin != null) {
            pstmt.setTimestamp(paramIndex, new Timestamp(fechaFin.getTime()));
        }
    }

    private Venta mapearVentaDesdeResultSet(ResultSet rs) throws SQLException {
        Venta venta = new Venta(
            rs.getString("id"),
            rs.getTimestamp("fecha"),
            rs.getDouble("total"),
            rs.getString("metodo_pago"),
            new ArrayList<>() // Productos se agregarán después
        );
        venta.setDescuento(rs.getDouble("descuento"));
        return venta;
    }

    private void cerrarResultSetYStatement(ResultSet rs, Statement stmt) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        } catch (SQLException e) {
            System.err.println("[BD] Error al cerrar recursos: " + e.getMessage());
        }
    }
    
    public void cargarProductosParaVenta(Venta venta) throws SQLException {
        String sql = "SELECT p.id, p.nombre, p.precio_venta, dv.cantidad " +
                     "FROM DetalleVenta dv " +
                     "JOIN Productos p ON dv.id_producto = p.id " +
                     "WHERE dv.id_venta = ?";
        
        try (Connection conn = ConexionAccess.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, venta.getId());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Producto producto = new Producto(
                        rs.getString("id"),
                        rs.getString("nombre"),
                        "", "", "",
                        rs.getInt("cantidad"),
                        0, 0, 0.0,
                        rs.getDouble("precio_venta"),
                        false, 0.0, null, "", "", ""
                    );
                    venta.getProductos().add(producto);
                }
            }
        }
    }

    public Map<String, Object> generarEstadisticasVentas(List<Venta> ventas) {
        Map<String, Object> stats = new HashMap<>();
        
        if (ventas == null || ventas.isEmpty()) {
            return stats;
        }
        
        // Totales básicos
        double totalVentas = ventas.stream().mapToDouble(Venta::getTotal).sum();
        int totalTransacciones = ventas.size();
        
        // Productos vendidos (con protección contra null)
        long totalProductos = ventas.stream()
            .filter(v -> v.getProductos() != null)
            .flatMap(v -> v.getProductos().stream())
            .count();
        
        stats.put("totalVentas", totalVentas);
        stats.put("totalTransacciones", totalTransacciones);
        stats.put("totalProductos", totalProductos);
        
        // Métodos de pago
        Map<String, Double> metodosPago = ventas.stream()
            .filter(v -> v.getMetodoPago() != null)
            .collect(Collectors.groupingBy(
                Venta::getMetodoPago,
                Collectors.summingDouble(Venta::getTotal)
            ));
        stats.put("metodosPago", metodosPago);
        
        // Productos más vendidos
        Map<String, Long> productosVendidos = ventas.stream()
            .filter(v -> v.getProductos() != null)
            .flatMap(v -> v.getProductos().stream())
            .filter(p -> p.getNombre() != null)
            .collect(Collectors.groupingBy(
                Producto::getNombre,
                Collectors.summingLong(Producto::getCantidad)
            ));
        stats.put("productosVendidos", productosVendidos);
        
        return stats;
    }

    public Map<String, Double> prepararDatosGrafica(List<Venta> ventas, String tipo) {
        Map<String, Double> datos = new LinkedHashMap<>();
        SimpleDateFormat sdf = obtenerFormatoFechaParaTipo(tipo);
        
        if ("CATEGORIA".equals(tipo)) {
            procesarDatosPorCategoria(ventas, datos);
        } else {
            procesarDatosPorFecha(ventas, datos, sdf);
        }
        
        return datos;
    }
    
    private SimpleDateFormat obtenerFormatoFechaParaTipo(String tipo) {
        switch (tipo) {
            case "DIARIO": return new SimpleDateFormat("dd/MM");
            case "MENSUAL": return new SimpleDateFormat("MM/yyyy");
            case "ANUAL": return new SimpleDateFormat("yyyy");
            default: return new SimpleDateFormat("dd/MM/yyyy");
        }
    }
    
    private void procesarDatosPorCategoria(List<Venta> ventas, Map<String, Double> datos) {
        for (Venta venta : ventas) {
            for (Producto producto : venta.getProductos()) {
                String categoria = producto.getCategoria() != null ? producto.getCategoria() : "Sin categoría";
                double valor = producto.getPrecioUnitario() * producto.getCantidad();
                datos.put(categoria, datos.getOrDefault(categoria, 0.0) + valor);
            }
        }
    }
    
    private void procesarDatosPorFecha(List<Venta> ventas, Map<String, Double> datos, SimpleDateFormat sdf) {
        for (Venta venta : ventas) {
            String clave = sdf.format(venta.getFecha());
            datos.put(clave, datos.getOrDefault(clave, 0.0) + venta.getTotal());
        }
    }

    public void exportarReporte() {
        try {
            // 1. Obtener parámetros básicos
            String formato = panelVentas.getFormatoExportacion().toUpperCase();
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String nombreBase = "reporte_" + tipoReporteActual.toLowerCase() + "_" + timestamp;
            
            // 2. Determinar la ubicación para guardar
            File carpetaTipoReporte = crearEstructuraCarpetas();
            
            // 3. Crear archivo con la ruta completa
            String extension = obtenerExtension(formato);
            File archivoReporte = new File(carpetaTipoReporte, nombreBase + extension);
            
            // 4. Exportar según el formato
            switch (formato) {
                case "PDF":
                    exportarAPDF(archivoReporte.getAbsolutePath());
                    break;
                    
                case "EXCEL":
                    exportarAExcel(archivoReporte);
                    break;
                    
                case "HTML":
                    exportarAHTML(archivoReporte);
                    break;
                    
                case "CSV":
                    exportarACSV(archivoReporte);
                    break;
                    
                case "JSON":
                    exportarAJSON(archivoReporte);
                    break;
                    
                case "XML":
                    exportarAXML(archivoReporte);
                    break;
                    
                default:
                    mostrarErrorEnPanel("Formato no soportado: " + formato);
                    return;
            }
            
            // 5. Mostrar resultado y abrir archivo
            manejarPostExportacion(archivoReporte, formato);
            
        } catch (Exception e) {
			mostrarErrorEnPanel("Error al exportar reporte: " + e.getMessage());
			e.printStackTrace();
        }
    }
    
    private File crearEstructuraCarpetas() throws IOException {
        // Obtener la ruta del proyecto actual
        String projectPath = System.getProperty("\"C:\\Users\\Anahi\\eclipse-workspace\\punto_venta_2\"");
        File carpetaReportes = new File(projectPath, "Reportes");
        
        if (!carpetaReportes.exists() && !carpetaReportes.mkdir()) {
            throw new IOException("No se pudo crear la carpeta Reportes en: " + projectPath);
        }
        
        File carpetaTipoReporte = new File(carpetaReportes, tipoReporteActual);
        
        if (!carpetaTipoReporte.exists() && !carpetaTipoReporte.mkdir()) {
            throw new IOException("No se pudo crear la carpeta para " + tipoReporteActual);
        }
        
        return carpetaTipoReporte;
    }

    private String obtenerExtension(String formato) {
        switch (formato.toUpperCase()) {
            case "PDF": return ".pdf";
            case "EXCEL": return ".xlsx";
            case "HTML": return ".html";
            case "CSV": return ".csv";
            case "JSON": return ".json";
            case "XML": return ".xml";
            default: return ".txt";
        }
    }
    
    private void exportarAJSON(File archivo) throws Exception {
        List<Venta> ventas = obtenerVentasDesdeBD(panelVentas.getFechaInicio(), panelVentas.getFechaFin());
        Map<String, Object> datosReporte = generarDatosReporte(ventas);
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        try (FileWriter writer = new FileWriter(archivo)) {
            mapper.writeValue(writer, datosReporte);
        }
    }

    private Map<String, Object> generarDatosReporte(List<Venta> ventas) {
        Map<String, Object> reporte = new LinkedHashMap<>();
        
        // Metadatos
        reporte.put("tipo_reporte", tipoReporteActual);
        reporte.put("fecha_generacion", new Date());
        reporte.put("rango_fechas", Map.of(
            "inicio", panelVentas.getFechaInicio(),
            "fin", panelVentas.getFechaFin()
        ));
        
        // Datos de ventas
        List<Map<String, Object>> ventasData = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        
        for (Venta venta : ventas) {
            Map<String, Object> ventaMap = new LinkedHashMap<>();
            ventaMap.put("id", venta.getId());
            ventaMap.put("fecha", sdf.format(venta.getFecha()));
            ventaMap.put("total", venta.getTotal());
            ventaMap.put("metodo_pago", venta.getMetodoPago());
            
            List<Map<String, Object>> productos = new ArrayList<>();
            for (Producto producto : venta.getProductos()) {
                productos.add(Map.of(
                    "nombre", producto.getNombre(),
                    "cantidad", producto.getCantidad(),
                    "precio_unitario", producto.getPrecioUnitario(),
                    "total", producto.getPrecioUnitario() * producto.getCantidad()
                ));
            }
            
            ventaMap.put("productos", productos);
            ventasData.add(ventaMap);
        }
        
        reporte.put("ventas", ventasData);
        
        // Estadísticas
        Map<String, Object> stats = generarEstadisticasVentas(ventas);
        reporte.put("estadisticas", stats);
        
        return reporte;
    }
    
    private void exportarAXML(File archivo) throws Exception {
        List<Venta> ventas = obtenerVentasDesdeBD(panelVentas.getFechaInicio(), panelVentas.getFechaFin());
        
        try (PrintWriter writer = new PrintWriter(new FileOutputStream(archivo))) {
            writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.println("<reporte tipo=\"" + tipoReporteActual + "\">");
            writer.println("  <fechaGeneracion>" + new Date() + "</fechaGeneracion>");
            writer.println("  <rango>");
            writer.println("    <inicio>" + panelVentas.getFechaInicio() + "</inicio>");
            writer.println("    <fin>" + panelVentas.getFechaFin() + "</fin>");
            writer.println("  </rango>");
            
            if (!ventas.isEmpty()) {
                Map<String, Object> stats = generarEstadisticasVentas(ventas);
                writer.println("  <resumen>");
                writer.println("    <totalVentas>" + stats.get("totalVentas") + "</totalVentas>");
                writer.println("    <cantidadVentas>" + ventas.size() + "</cantidadVentas>");
                writer.println("    <productosVendidos>" + stats.get("totalProductos") + "</productosVendidos>");
                writer.println("    <ticketPromedio>" + ((double)stats.get("totalVentas"))/ventas.size() + "</ticketPromedio>");
                writer.println("  </resumen>");
                
                writer.println("  <ventas>");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                for (Venta venta : ventas) {
                    writer.println("    <venta id=\"" + venta.getId() + "\" fecha=\"" + sdf.format(venta.getFecha()) + 
                                 "\" total=\"" + venta.getTotal() + "\">");
                    
                    for (Producto producto : venta.getProductos()) {
                        writer.println("      <producto nombre=\"" + escapeXML(producto.getNombre()) + "\"" +
                                     " cantidad=\"" + producto.getCantidad() + "\"" +
                                     " precioUnitario=\"" + producto.getPrecioUnitario() + "\"" +
                                     " total=\"" + (producto.getPrecioUnitario() * producto.getCantidad()) + "\"/>");
                    }
                    
                    writer.println("    </venta>");
                }
                writer.println("  </ventas>");
            } else {
                writer.println("  <mensaje>No hay datos para el período seleccionado</mensaje>");
            }
            
            writer.println("</reporte>");
        }
    }
    
    private void exportarACSV(File archivo) throws Exception {
        List<Venta> ventas = obtenerVentasDesdeBD(panelVentas.getFechaInicio(), panelVentas.getFechaFin());
        
        try (PrintWriter writer = new PrintWriter(new FileOutputStream(archivo))) {
            // Encabezados
            writer.println("ID_Venta,Fecha,Producto,Cantidad,Precio_Unitario,Total,Metodo_Pago");
            
            // Datos
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (Venta venta : ventas) {
                for (Producto producto : venta.getProductos()) {
                    writer.println(String.format("%d,%s,\"%s\",%d,%.2f,%.2f,%s",
                        venta.getId(),
                        sdf.format(venta.getFecha()),
                        producto.getNombre().replace("\"", "\"\""),
                        producto.getCantidad(),
                        producto.getPrecioUnitario(),
                        producto.getPrecioUnitario() * producto.getCantidad(),
                        venta.getMetodoPago()
                    ));
                }
            }
        }
    }

    public void exportarAExcel(File archivo) throws Exception {
        List<Venta> ventas = obtenerVentasDesdeBD(panelVentas.getFechaInicio(), panelVentas.getFechaFin());

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Reporte de Ventas");

            // Estilos
            CellStyle headerStyle = crearEstiloEncabezado(workbook);
            CellStyle titleStyle = crearEstiloTitulo(workbook);
            CellStyle currencyStyle = crearEstiloMoneda(workbook);
            CellStyle dateStyle = crearEstiloFecha(workbook);

            int currentRow = 0;

            // Logo e información
            Row logoRow = sheet.createRow(currentRow++);
            crearCeldaConMergedRegion(sheet, logoRow, 0, 6, "EL HABANERITO - REPORTE DE VENTAS", titleStyle);

            Row infoRow = sheet.createRow(currentRow++);
            crearCeldaConMergedRegion(sheet, infoRow, 0, 6, "Atendido por: " + modelo.getUsuarioActual(), null);

            Row fechaRow = sheet.createRow(currentRow++);
            crearCeldaConMergedRegion(sheet, fechaRow, 0, 6, "Fecha: " + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()), null);

            currentRow++;

            // Encabezados
            Row headerRow = sheet.createRow(currentRow++);
            String[] headers = {"ID Venta", "Fecha", "Producto", "Cantidad", "Precio Unitario", "Total", "Método Pago"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            // Mapa para totales por método de pago
            Map<String, Double> totalesPorMetodo = new HashMap<>();

            // Datos de ventas
            for (Venta venta : ventas) {
                if (venta.getProductos() == null || venta.getProductos().isEmpty()) continue;

                for (Producto producto : venta.getProductos()) {
                    if (producto == null) continue;

                    Row row = sheet.createRow(currentRow++);

                    crearCelda(row, 0, venta.getId(), null);
                    crearCelda(row, 1, sdf.format(venta.getFecha()), dateStyle);

                    String nombreProducto = producto.getNombre() != null ? producto.getNombre() : "Sin nombre";
                    crearCelda(row, 2, nombreProducto, null);

                    crearCelda(row, 3, producto.getCantidad(), null);
                    crearCelda(row, 4, producto.getPrecioUnitario(), currencyStyle);

                    double total = producto.getPrecioUnitario() * producto.getCantidad();
                    crearCelda(row, 5, total, currencyStyle);

                    String metodoPago = venta.getMetodoPago() != null ? venta.getMetodoPago() : "No especificado";
                    crearCelda(row, 6, metodoPago, null);

                    // Acumulamos total por método de pago
                    totalesPorMetodo.merge(metodoPago, total, Double::sum);
                }
            }

            // Autoajustar columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, Math.max(sheet.getColumnWidth(i), 3000));
            }

            // Espacio extra antes del resumen
            currentRow++;
            currentRow++;

            // Título del resumen
            Row resumenTitulo = sheet.createRow(currentRow++);
            crearCeldaConMergedRegion(sheet, resumenTitulo, 0, 6, "Resumen por Método de Pago", titleStyle);

            // Encabezados de resumen
            Row resumenHeader = sheet.createRow(currentRow++);
            crearCelda(resumenHeader, 0, "Método de Pago", headerStyle);
            crearCelda(resumenHeader, 1, "Total Ventas", headerStyle);

            // Filas del resumen
            for (Map.Entry<String, Double> entry : totalesPorMetodo.entrySet()) {
                Row rowResumen = sheet.createRow(currentRow++);
                crearCelda(rowResumen, 0, entry.getKey(), null);
                crearCelda(rowResumen, 1, entry.getValue(), currencyStyle);
            }

            // Guardar archivo
            try (FileOutputStream outputStream = new FileOutputStream(archivo)) {
                workbook.write(outputStream);
            }
        }
    }


    // Métodos auxiliares para estilos y creación de celdas
    private CellStyle crearEstiloEncabezado(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle crearEstiloTitulo(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short)14);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle crearEstiloMoneda(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("$#,##0.00"));
        return style;
    }

    private CellStyle crearEstiloFecha(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("dd/mm/yyyy hh:mm"));
        return style;
    }

    private void crearCeldaConMergedRegion(Sheet sheet, Row row, int colInicio, int colFin, String valor, CellStyle estilo) {
        Cell cell = row.createCell(colInicio);
        cell.setCellValue(valor);
        if (estilo != null) {
            cell.setCellStyle(estilo);
        }
        sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), colInicio, colFin));
    }

    private void crearCelda(Row row, int columna, Object valor, CellStyle estilo) {
        Cell cell = row.createCell(columna);
        
        if (valor == null) {
            cell.setCellValue("");
        } else if (valor instanceof Number) {
            cell.setCellValue(((Number) valor).doubleValue());
        } else if (valor instanceof Date) {
            cell.setCellValue((Date) valor);
        } else {
            cell.setCellValue(valor.toString());
        }
        
        if (estilo != null) {
            cell.setCellStyle(estilo);
        }
    }
    
    private void exportarAHTML(File archivo) throws Exception {
        List<Venta> ventas = obtenerVentasDesdeBD(panelVentas.getFechaInicio(), panelVentas.getFechaFin());
        
        try (PrintWriter writer = new PrintWriter(new FileOutputStream(archivo))) {
            writer.println("<!DOCTYPE html>");
            writer.println("<html lang='es'>");
            writer.println("<head>");
            writer.println("<meta charset='UTF-8'>");
            writer.println("<title>Reporte de " + tipoReporteActual + "</title>");
            writer.println("<style>");
            writer.println("body { font-family: Arial, sans-serif; margin: 20px; }");
            writer.println("h1 { color: #2c3e50; }");
            writer.println("table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }");
            writer.println("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
            writer.println("th { background-color: #f2f2f2; }");
            writer.println(".resumen { background-color: #f8f9fa; padding: 15px; margin-bottom: 20px; }");
            writer.println("</style>");
            writer.println("</head>");
            writer.println("<body>");
            
            writer.println("<h1>Reporte de " + tipoReporteActual + "</h1>");
            writer.println("<div class='resumen'>");
            writer.println("<p><strong>Generado:</strong> " + new Date() + "</p>");
            writer.println("<p><strong>Rango:</strong> " + panelVentas.getFechaInicio() + " - " + panelVentas.getFechaFin() + "</p>");
            
            if (!ventas.isEmpty()) {
                Map<String, Object> stats = generarEstadisticasVentas(ventas);
                writer.println("<p><strong>Total Ventas:</strong> $" + String.format("%,.2f", stats.get("totalVentas")) + "</p>");
                writer.println("<p><strong>Cantidad de Ventas:</strong> " + ventas.size() + "</p>");
                writer.println("<p><strong>Productos Vendidos:</strong> " + stats.get("totalProductos") + "</p>");
                writer.println("<p><strong>Ticket Promedio:</strong> $" + 
                             String.format("%,.2f", ((double)stats.get("totalVentas"))/ventas.size()) + "</p>");
            }
            
            writer.println("</div>");
            
            if (!ventas.isEmpty()) {
                writer.println("<h2>Detalle de Ventas</h2>");
                writer.println("<table>");
                writer.println("<tr><th>ID Venta</th><th>Fecha</th><th>Producto</th><th>Cantidad</th><th>Precio Unitario</th><th>Total</th><th>Método Pago</th></tr>");
                
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                for (Venta venta : ventas) {
                    for (Producto producto : venta.getProductos()) {
                        writer.println(String.format(
                            "<tr><td>%d</td><td>%s</td><td>%s</td><td>%d</td><td>$%.2f</td><td>$%.2f</td><td>%s</td></tr>",
                            venta.getId(),
                            sdf.format(venta.getFecha()),
                            producto.getNombre(),
                            producto.getCantidad(),
                            producto.getPrecioUnitario(),
                            producto.getPrecioUnitario() * producto.getCantidad(),
                            venta.getMetodoPago()
                        ));
                    }
                }
                
                writer.println("</table>");
            } else {
                writer.println("<p>No hay datos para el período seleccionado</p>");
            }
            
            writer.println("</body>");
            writer.println("</html>");
        }
    }
    
    private void manejarPostExportacion(File archivo, String formato) throws IOException {
        // Verificar que el archivo se creó
        if (!archivo.exists() || archivo.length() == 0) {
            throw new IOException("El archivo no se generó correctamente");
        }
        
        // Mostrar mensaje de éxito
        String mensajeExito = String.format(
            "<html><b>Reporte exportado exitosamente</b><br>" +
            "<b>Tipo:</b> %s<br>" +
            "<b>Formato:</b> %s<br>" +
            "<b>Ubicación:</b> %s</html>",
            tipoReporteActual,
            formato,
            archivo.getAbsolutePath()
        );
        
        panelVentas.mostrarMensaje(mensajeExito);
        
        // Intentar abrir el archivo o carpeta
        abrirArchivoExportado(archivo, formato);
    }

    private void abrirArchivoExportado(File archivo, String formato) {
        try {
            String nombreArchivo = archivo.getName().toLowerCase();

            if (nombreArchivo.endsWith(".pdf")) {
                VisorPDF visor = new VisorPDF(archivo);
                visor.setVisible(true);
            } else {
                Desktop.getDesktop().open(archivo);  // Abre el archivo con la aplicación predeterminada
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "No se pudo abrir el archivo con la aplicación predeterminada.");
            e.printStackTrace();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al mostrar el archivo: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void mostrarMensajeEnPanel(String mensaje) {
        if (panelVentas != null) {
            panelVentas.mostrarMensaje(mensaje);
        } else if (vista != null) {
            vista.mostrarMensaje(mensaje);
        } else {
            System.out.println(mensaje);
        }
    }

    private void mostrarErrorEnPanel(String mensaje) {
        if (panelVentas != null) {
            panelVentas.mostrarError(mensaje);
        } else if (vista != null) {
            vista.mostrarError(mensaje);
        } else {
            System.err.println(mensaje);
        }
    }
    
    private void exportarAXML(String filename) throws Exception {
        try (PrintWriter writer = new PrintWriter(new FileOutputStream(filename))) {
            writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.println("<reporte tipo=\"" + tipoReporteActual + "\">");
            writer.println("  <fechaGeneracion>" + new Date() + "</fechaGeneracion>");
            writer.println("  <rango>");
            writer.println("    <inicio>" + panelVentas.getFechaInicio() + "</inicio>");
            writer.println("    <fin>" + panelVentas.getFechaFin() + "</fin>");
            writer.println("  </rango>");
            
            if ("VENTAS".equals(tipoReporteActual)) {
                List<Venta> ventas = obtenerVentasDesdeBD(panelVentas.getFechaInicio(), panelVentas.getFechaFin());
                Map<String, Object> stats = generarEstadisticasVentas(ventas);
                
                writer.println("  <resumen>");
                writer.println(String.format("    <totalVentas>%,.2f</totalVentas>", stats.get("totalVentas")));
                writer.println(String.format("    <cantidadVentas>%d</cantidadVentas>", ventas.size()));
                writer.println(String.format("    <productosVendidos>%d</productosVendidos>", stats.get("totalProductos")));
                writer.println(String.format("    <ticketPromedio>%,.2f</ticketPromedio>", 
                    ((double)stats.get("totalVentas"))/ventas.size()));
                writer.println("  </resumen>");
                
                writer.println("  <ventas>");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                for (Venta venta : ventas) {
                    writer.println(String.format("    <venta id=\"%d\" fecha=\"%s\" total=\"%.2f\">",
                        venta.getId(),
                        sdf.format(venta.getFecha()),
                        venta.getTotal()));
                    
                    for (Producto producto : venta.getProductos()) {
                        writer.println(String.format(
                            "      <producto nombre=\"%s\" cantidad=\"%d\" precioUnitario=\"%.2f\" total=\"%.2f\"/>",
                            producto.getNombre(),
                            producto.getCantidad(),
                            producto.getPrecioUnitario(),
                            producto.getPrecioUnitario() * producto.getCantidad()
                        ));
                    }
                    
                    writer.println("    </venta>");
                }
                writer.println("  </ventas>");
            } else {
                writer.println("  <mensaje>Tipo de reporte no soportado para exportación XML</mensaje>");
            }
            
            writer.println("</reporte>");
        }
    }

    private void exportarAHTML(String filename) throws Exception {
        try (PrintWriter writer = new PrintWriter(new FileOutputStream(filename))) {
            writer.println("<!DOCTYPE html>");
            writer.println("<html>");
            writer.println("<head>");
            writer.println("<title>Reporte de " + tipoReporteActual + "</title>");
            writer.println("<style>");
            writer.println("body { font-family: Arial, sans-serif; margin: 20px; }");
            writer.println("h1 { color: #2c3e50; }");
            writer.println("table { width: 100%; border-collapse: collapse; }");
            writer.println("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
            writer.println("th { background-color: #f2f2f2; }");
            writer.println("</style>");
            writer.println("</head>");
            writer.println("<body>");
            
            writer.println("<h1>Reporte de " + tipoReporteActual + "</h1>");
            writer.println("<p>Generado: " + new Date() + "</p>");
            writer.println("<p>Rango: " + panelVentas.getFechaInicio() + " - " + panelVentas.getFechaFin() + "</p>");
            
            if ("VENTAS".equals(tipoReporteActual)) {
                List<Venta> ventas = obtenerVentasDesdeBD(panelVentas.getFechaInicio(), panelVentas.getFechaFin());
                
                // Tabla de resumen
                writer.println("<h2>Resumen de Ventas</h2>");
                writer.println("<table>");
                writer.println("<tr><th>Total Ventas</th><th>Cantidad Ventas</th><th>Productos Vendidos</th><th>Ticket Promedio</th></tr>");
                
                Map<String, Object> stats = generarEstadisticasVentas(ventas);
                writer.println(String.format("<tr><td>$%,.2f</td><td>%d</td><td>%d</td><td>$%,.2f</td></tr>",
                    stats.get("totalVentas"),
                    ventas.size(),
                    stats.get("totalProductos"),
                    ((double)stats.get("totalVentas"))/ventas.size()));
                
                writer.println("</table>");
                
                // Tabla de detalle
                writer.println("<h2>Detalle de Ventas</h2>");
                writer.println("<table>");
                writer.println("<tr><th>ID Venta</th><th>Fecha</th><th>Producto</th><th>Cantidad</th><th>Precio Unitario</th><th>Total</th></tr>");
                
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                for (Venta venta : ventas) {
                    for (Producto producto : venta.getProductos()) {
                        writer.println(String.format(
                            "<tr><td>%d</td><td>%s</td><td>%s</td><td>%d</td><td>$%.2f</td><td>$%.2f</td></tr>",
                            venta.getId(),
                            sdf.format(venta.getFecha()),
                            producto.getNombre(),
                            producto.getCantidad(),
                            producto.getPrecioUnitario(),
                            producto.getPrecioUnitario() * producto.getCantidad()
                        ));
                    }
                }
                writer.println("</table>");
            } else {
                writer.println("<p>Tipo de reporte no soportado para exportación HTML</p>");
            }
            
            writer.println("</body>");
            writer.println("</html>");
        }
    }

    private void exportarAPDF(String filename) throws Exception {
        pdfExporter.exportarAPDF(
            filename,
            tipoReporteActual,
            panelVentas.getFechaInicio(),
            panelVentas.getFechaFin()
        );
    }
    
    public void agregarReporteVentas(Document document, List<Venta> ventas, PdfWriter writer) throws DocumentException {
        // Resumen estadístico
        Map<String, Object> stats = generarEstadisticasVentas(ventas);
        
        PdfPTable summaryTable = new PdfPTable(4);
        summaryTable.setWidthPercentage(100);
        summaryTable.setSpacingBefore(10f);
        summaryTable.setSpacingAfter(10f);
        
        // Encabezado de la tabla de resumen
        addSummaryHeaderCell(summaryTable, "Total Ventas");
        addSummaryHeaderCell(summaryTable, "Cantidad Ventas");
        addSummaryHeaderCell(summaryTable, "Productos Vendidos");
        addSummaryHeaderCell(summaryTable, "Ticket Promedio");
        
        // Datos del resumen
        addSummaryDataCell(summaryTable, String.format("$%,.2f", stats.get("totalVentas")));
        addSummaryDataCell(summaryTable, String.valueOf(ventas.size()));
        addSummaryDataCell(summaryTable, String.valueOf(stats.get("totalProductos")));
        addSummaryDataCell(summaryTable, String.format("$%,.2f", ((double)stats.get("totalVentas"))/ventas.size()));
        
        document.add(summaryTable);
        
        // Tabla de detalle de ventas
        Paragraph detalleTitle = new Paragraph("Detalle de Ventas", 
            new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD));
        detalleTitle.setSpacingBefore(15f);
        document.add(detalleTitle);
        
        PdfPTable detailTable = new PdfPTable(6);
        detailTable.setWidthPercentage(100);
        detailTable.setSpacingBefore(5f);
        
        // Configurar anchos de columnas
        float[] columnWidths = {1f, 2f, 3f, 1f, 1.5f, 1.5f};
        detailTable.setWidths(columnWidths);
        
        // Encabezados de la tabla de detalle
        addDetailHeaderCell(detailTable, "ID Venta");
        addDetailHeaderCell(detailTable, "Fecha");
        addDetailHeaderCell(detailTable, "Producto");
        addDetailHeaderCell(detailTable, "Cantidad");
        addDetailHeaderCell(detailTable, "Precio Unitario");
        addDetailHeaderCell(detailTable, "Total");
        
        // Llenar datos
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        for (Venta venta : ventas) {
            for (Producto producto : venta.getProductos()) {
                addDetailDataCell(detailTable, String.valueOf(venta.getId()));
                addDetailDataCell(detailTable, sdf.format(venta.getFecha()));
                addDetailDataCell(detailTable, producto.getNombre());
                addDetailDataCell(detailTable, String.valueOf(producto.getCantidad()));
                addDetailDataCell(detailTable, String.format("$%.2f", producto.getPrecioUnitario()));
                addDetailDataCell(detailTable, String.format("$%.2f", 
                    producto.getPrecioUnitario() * producto.getCantidad()));
            }
        }
        
        document.add(detailTable);
        
        // Pie de página
        Paragraph footer = new Paragraph("Fin del reporte", 
            new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC));
        footer.setSpacingBefore(20f);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }

    private void addSummaryHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, 
            new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD)));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(new BaseColor(220, 220, 220));
        cell.setPadding(5f);
        table.addCell(cell);
    }

    private void addSummaryDataCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, 
            new Font(Font.FontFamily.HELVETICA, 10)));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5f);
        table.addCell(cell);
    }

    private void addDetailHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, 
            new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD)));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(new BaseColor(220, 220, 220));
        cell.setPadding(3f);
        table.addCell(cell);
    }

    private void addDetailDataCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, 
            new Font(Font.FontFamily.HELVETICA, 8)));
        cell.setPadding(3f);
        table.addCell(cell);
    }
    
    public void agregarEncabezadoReporte(Document document, String tipoReporte, Date fechaInicio, Date fechaFin) throws DocumentException {
    	  // Si no se proporcionan fechas, usar null
        if (fechaInicio == null && fechaFin == null) {
            // Lógica del encabezado sin fechas
        } else {
    	// Crear tabla para el encabezado
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new int[]{1, 3});
        
        // Logo (si está disponible)
        try {
            Image logo = Image.getInstance("imagen/logo.png");
            logo.scaleToFit(100, 100);
            PdfPCell logoCell = new PdfPCell(logo);
            logoCell.setBorder(Rectangle.NO_BORDER);
            headerTable.addCell(logoCell);
        } catch (Exception e) {
            PdfPCell emptyCell = new PdfPCell(new Phrase(""));
            emptyCell.setBorder(Rectangle.NO_BORDER);
            headerTable.addCell(emptyCell);
        }
        
        // Información de la empresa
        PdfPCell infoCell = new PdfPCell();
        infoCell.setBorder(Rectangle.NO_BORDER);
        
        Paragraph empresa = new Paragraph("El Habanerito", 
            new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD));
        empresa.setAlignment(Element.ALIGN_RIGHT);
        
        Paragraph reporte = new Paragraph("Reporte de " + tipoReporteActual, 
            new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD));
        reporte.setAlignment(Element.ALIGN_RIGHT);
        
        Paragraph fecha = new Paragraph("Generado: " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()), 
            new Font(Font.FontFamily.HELVETICA, 10));
        fecha.setAlignment(Element.ALIGN_RIGHT);
        
        infoCell.addElement(empresa);
        infoCell.addElement(reporte);
        infoCell.addElement(fecha);
        
        headerTable.addCell(infoCell);
        document.add(headerTable);
        
        // Línea separadora
        Paragraph separator = new Paragraph();
        separator.add(new Chunk(new Chunk()));
        document.add(separator);
        
        // Información del rango de fechas
        if (panelVentas.getFechaInicio() != null || panelVentas.getFechaFin() != null) {
            Paragraph rangoFechas = new Paragraph();
            rangoFechas.add("Período: ");
            if (panelVentas.getFechaInicio() != null) {
                rangoFechas.add(new SimpleDateFormat("dd/MM/yyyy").format(panelVentas.getFechaInicio()));
            }
            rangoFechas.add(" - ");
            if (panelVentas.getFechaFin() != null) {
                rangoFechas.add(new SimpleDateFormat("dd/MM/yyyy").format(panelVentas.getFechaFin()));
            }
            document.add(rangoFechas);
        }
        
        document.add(Chunk.NEWLINE);}
    }

    public void imprimirReporte() {
        exportarReporte(); // usamos exportar como impresión
    }

    private void abrirArchivoGenerado(File archivo) {
        if (!archivo.exists() || archivo.length() == 0) {
            mostrarErrorEnPanel("No se encontró el archivo generado: " + archivo.getAbsolutePath());
            return;
        }
        
        if (!verificarIntegridadArchivo(archivo)) {
            mostrarErrorEnPanel("El archivo está dañado o no es válido: " + archivo.getName());
            return;
        }
        
        String nombre = archivo.getName().toLowerCase();
        
        try {
            if (nombre.endsWith(".pdf")) {
                mostrarDocumentoPDF(archivo);
            } else if (nombre.endsWith(".xlsx") || nombre.endsWith(".xls")) {
                mostrarDocumentoExcel(archivo);
            } else if (nombre.endsWith(".html")) {
                mostrarDocumentoHTML(archivo);
            } else {
                mostrarErrorEnPanel("Tipo de documento no soportado para visualización interna: " + nombre);
            }
        } catch (Exception e) {
            mostrarErrorEnPanel("Error al abrir el documento: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void mostrarDocumentoPDF(File archivoPDF) {
        JDialog visorDialog = new JDialog();
        visorDialog.setTitle("Visor PDF - " + archivoPDF.getName());
        visorDialog.setSize(900, 700);
        visorDialog.setLocationRelativeTo(vista);
        
        // Configurar el visor PDF de icepdf
        SwingController controller = new SwingController();
        SwingViewBuilder factory = new SwingViewBuilder(controller);
        JPanel viewerComponentPanel = factory.buildViewerPanel();
        
        controller.openDocument(archivoPDF.getAbsolutePath());
        
        // Botón para exportar
        JButton btnExportar = new JButton("Exportar...");
        btnExportar.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Guardar copia del PDF");
            fileChooser.setSelectedFile(new File(archivoPDF.getName()));
            
            if (fileChooser.showSaveDialog(visorDialog) == JFileChooser.APPROVE_OPTION) {
                File destino = fileChooser.getSelectedFile();
                if (!destino.getName().toLowerCase().endsWith(".pdf")) {
                    destino = new File(destino.getAbsolutePath() + ".pdf");
                }
                
                try {
                    Files.copy(archivoPDF.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    mostrarMensajeEnPanel("PDF exportado exitosamente a: " + destino.getAbsolutePath());
                } catch (IOException ex) {
                    mostrarErrorEnPanel("Error al exportar PDF: " + ex.getMessage());
                }
            }
        });
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(viewerComponentPanel, BorderLayout.CENTER);
        
        JPanel panelBoton = new JPanel();
        panelBoton.add(btnExportar);
        panel.add(panelBoton, BorderLayout.SOUTH);
        
        visorDialog.setContentPane(panel);
        visorDialog.setVisible(true);
    }

    private void mostrarDocumentoExcel(File archivoExcel) {
        try (Workbook workbook = WorkbookFactory.create(archivoExcel)) {
            JDialog visorDialog = new JDialog();
            visorDialog.setTitle("Visor Excel - " + archivoExcel.getName());
            visorDialog.setSize(900, 700);
            visorDialog.setLocationRelativeTo(vista);
            
            JTabbedPane tabbedPane = new JTabbedPane();
            
            // Crear una pestaña por cada hoja
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                DefaultTableModel model = new DefaultTableModel();
                JTable table = new JTable(model);
                
                // Leer encabezados
                Row headerRow = sheet.getRow(0);
                for (Cell cell : headerRow) {
                    model.addColumn(cell.getStringCellValue());
                }
                
                // Leer datos
                for (int j = 1; j <= sheet.getLastRowNum(); j++) {
                    Row row = sheet.getRow(j);
                    if (row != null) {
                        Object[] rowData = new Object[row.getLastCellNum()];
                        for (int k = 0; k < row.getLastCellNum(); k++) {
                            Cell cell = row.getCell(k);
                            rowData[k] = (cell != null) ? getCellValue(cell) : "";
                        }
                        model.addRow(rowData);
                    }
                }
                
                tabbedPane.addTab(sheet.getSheetName(), new JScrollPane(table));
            }
            
            // Botón para exportar
            JButton btnExportar = new JButton("Exportar...");
            btnExportar.addActionListener(e -> {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Guardar copia del Excel");
                fileChooser.setSelectedFile(new File(archivoExcel.getName()));
                
                if (fileChooser.showSaveDialog(visorDialog) == JFileChooser.APPROVE_OPTION) {
                    File destino = fileChooser.getSelectedFile();
                    if (!destino.getName().toLowerCase().endsWith(".xlsx")) {
                        destino = new File(destino.getAbsolutePath() + ".xlsx");
                    }
                    
                    try {
                        Files.copy(archivoExcel.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        mostrarMensajeEnPanel("Excel exportado exitosamente a: " + destino.getAbsolutePath());
                    } catch (IOException ex) {
                        mostrarErrorEnPanel("Error al exportar Excel: " + ex.getMessage());
                    }
                }
            });
            
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(tabbedPane, BorderLayout.CENTER);
            
            JPanel panelBoton = new JPanel();
            panelBoton.add(btnExportar);
            panel.add(panelBoton, BorderLayout.SOUTH);
            
            visorDialog.setContentPane(panel);
            visorDialog.setVisible(true);
        } catch (Exception e) {
            mostrarErrorEnPanel("Error al leer archivo Excel: " + e.getMessage());
        }
    }

    private Object getCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return cell.getNumericCellValue();
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    private void mostrarDocumentoHTML(File archivoHTML) {
        JDialog visorDialog = new JDialog();
        visorDialog.setTitle("Visor HTML - " + archivoHTML.getName());
        visorDialog.setSize(900, 700);
        visorDialog.setLocationRelativeTo(vista);
        
        JEditorPane editorPane = new JEditorPane();
        editorPane.setEditable(false);
        
        try {
            editorPane.setPage(archivoHTML.toURI().toURL());
        } catch (Exception e) {
            editorPane.setText("Error al cargar el archivo HTML");
        }
        
        // Botón para exportar
        JButton btnExportar = new JButton("Exportar...");
        btnExportar.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Guardar copia del HTML");
            fileChooser.setSelectedFile(new File(archivoHTML.getName()));
            
            if (fileChooser.showSaveDialog(visorDialog) == JFileChooser.APPROVE_OPTION) {
                File destino = fileChooser.getSelectedFile();
                if (!destino.getName().toLowerCase().endsWith(".html")) {
                    destino = new File(destino.getAbsolutePath() + ".html");
                }
                
                try {
                    Files.copy(archivoHTML.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    mostrarMensajeEnPanel("HTML exportado exitosamente a: " + destino.getAbsolutePath());
                } catch (IOException ex) {
                    mostrarErrorEnPanel("Error al exportar HTML: " + ex.getMessage());
                }
            }
        });
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(editorPane), BorderLayout.CENTER);
        
        JPanel panelBoton = new JPanel();
        panelBoton.add(btnExportar);
        panel.add(panelBoton, BorderLayout.SOUTH);
        
        visorDialog.setContentPane(panel);
        visorDialog.setVisible(true);
    }

    public void onEndPage(PdfWriter writer, Document document) {
        try {
            agregarNumeroPagina(writer, document);
        } catch (DocumentException e) {
            panelVentas.mostrarError("Error al agregar número de página: " + e.getMessage());
        }
    }

    private void agregarNumeroPagina(PdfWriter writer, Document document) throws DocumentException {
        PdfContentByte cb = writer.getDirectContent();
        Phrase footer = new Phrase(
            String.format("Página %d", writer.getPageNumber()), 
            new Font(Font.FontFamily.HELVETICA, 8)
        );
        
        ColumnText.showTextAligned(
            cb,
            Element.ALIGN_CENTER,
            footer,
            (document.right() - document.left()) / 2 + document.leftMargin(),
            document.bottom() - 15,
            0
        );
    }

    public void agregarGraficos(Document document, List<Venta> ventas, PdfWriter writer) 
            throws DocumentException, IOException {
            
            if (ventas == null || ventas.isEmpty()) {
                return;
            }

            // 1. Gráfico de métodos de pago (Pie Chart)
            DefaultPieDataset paymentDataset = new DefaultPieDataset();
            Map<String, Double> metodosPago = ventas.stream()
                .filter(v -> v.getMetodoPago() != null)
                .collect(Collectors.groupingBy(
                    Venta::getMetodoPago,
                    Collectors.summingDouble(Venta::getTotal)
              )  );
            
            metodosPago.forEach(paymentDataset::setValue);
            
            JFreeChart paymentChart = ChartFactory.createPieChart(
                "Distribución por Método de Pago", 
                paymentDataset, 
                true, true, false);
            
            // 2. Gráfico de productos más vendidos (Bar Chart)
            DefaultCategoryDataset productDataset = new DefaultCategoryDataset();
            Map<String, Long> productosVendidos = ventas.stream()
                .flatMap(v -> v.getProductos().stream())
                .collect(Collectors.groupingBy(
                    Producto::getNombre,
                    Collectors.summingLong(Producto::getCantidad)
                ));
            
            // Ordenar y tomar los top 10 productos
            productosVendidos.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .forEach(e -> productDataset.addValue(e.getValue(), "Ventas", e.getKey()));
            
            JFreeChart productChart = ChartFactory.createBarChart(
                "Top 10 Productos Más Vendidos",
                "Productos",
                "Cantidad Vendida",
                productDataset,
                PlotOrientation.VERTICAL,
                true, true, false);
            
            // Configuración de tamaño y calidad
            int width = 500;
            int height = 300;
            float quality = 1.0f;
            
            // Agregar gráficos al documento
            Paragraph chartTitle = new Paragraph("Análisis Gráfico", 
                new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD));
            chartTitle.setSpacingBefore(20f);
            document.add(chartTitle);
            
            // Gráfico de métodos de pago
            BufferedImage paymentImage = paymentChart.createBufferedImage(width, height);
            Image paymentPdfImage = Image.getInstance(writer, paymentImage, quality);
            paymentPdfImage.setAlignment(Image.MIDDLE);
            document.add(paymentPdfImage);
            
            // Espacio entre gráficos
            document.add(Chunk.NEWLINE);
            
            // Gráfico de productos
            BufferedImage productImage = productChart.createBufferedImage(width, height);
            Image productPdfImage = Image.getInstance(writer, productImage, quality);
            productPdfImage.setAlignment(Image.MIDDLE);
            document.add(productPdfImage);
        }

    public void cargarDatosInventario() {
        try {
            List<Producto> productos = inventarioo.obtenerTodosProductos();
            actualizarVistaInventario(productos);
        } catch (Exception e) {
            mostrarErrorEnPanel("Error al cargar inventario: " + e.getMessage());
        }
    }

    private void actualizarVistaInventario(List<Producto> productos) {
        System.out.println("[UI] Actualizando vista con " + productos.size() + " productos");
        
        if (panelInventario == null) {
            System.err.println("[ERROR] No se puede actualizar vista: panelInventario es null");
            return;
        }
        
        // Calcular estadísticas
        int total = productos.size();
        int bajoStock = (int) productos.stream()
            .filter(p -> p.getCantidadDisponible() <= p.getStockMinimo())
            .count();
        int sinStock = (int) productos.stream()
            .filter(p -> p.getCantidadDisponible() == 0)
            .count();
        
        System.out.println("[UI] Estadísticas - Total: " + total + 
                          ", Bajo stock: " + bajoStock + 
                          ", Sin stock: " + sinStock);
        
        // Calcular promedios de stock
        double stockPromedio = productos.stream()
            .mapToInt(Producto::getCantidadDisponible)
            .average()
            .orElse(0);
        
        double stockMinPromedio = productos.stream()
            .mapToInt(Producto::getStockMinimo)
            .average()
            .orElse(0);
        
        double stockMaxPromedio = productos.stream()
            .mapToInt(Producto::getStockMaximo)
            .average()
            .orElse(0);
        
        System.out.println("[UI] Promedios - Stock: " + stockPromedio + 
                          ", Mín: " + stockMinPromedio + 
                          ", Máx: " + stockMaxPromedio);
        
        // Distribución por categoría
        Map<String, Integer> distribucionCategorias = productos.stream()
            .collect(Collectors.groupingBy(
                Producto::getCategoria,
                Collectors.summingInt(p -> 1)
            ));
        
        System.out.println("[UI] Distribución por categoría: " + distribucionCategorias);
        
        // Actualizar la vista en el hilo de EDT
        SwingUtilities.invokeLater(() -> {
            System.out.println("[UI] Actualizando componentes en EDT...");
            try {
                panelInventario.actualizarTablaInventario(productos);
                panelInventario.actualizarEstadisticas(total, bajoStock, sinStock);
                panelInventario.actualizarGraficas(stockPromedio, stockMinPromedio, stockMaxPromedio, distribucionCategorias);
                System.out.println("[UI] Componentes actualizados correctamente");
            } catch (Exception e) {
                System.err.println("[UI] Error al actualizar componentes: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    public void filtrarInventario(String filtro) {
        System.out.println("[DEBUG] Filtrando inventario por: " + filtro);
        
        try {
            List<Producto> productosFiltrados;
            
            switch (filtro) {
                case "Bajo Stock":
                    productosFiltrados = inventarioo.buscarProductosNecesitanReposicion();
                    System.out.println("[DEBUG] Productos bajo stock encontrados: " + productosFiltrados.size());
                    break;
                case "Sobre Stock":
                    productosFiltrados = inventarioo.buscarProductosConExcesoStock();
                    System.out.println("[DEBUG] Productos sobre stock encontrados: " + productosFiltrados.size());
                    break;
                case "Categoría":
                    String categoria = JOptionPane.showInputDialog("Ingrese la categoría:");
                    if (categoria == null || categoria.isEmpty()) {
                        System.out.println("[DEBUG] Filtro por categoría cancelado");
                        return;
                    }
                    productosFiltrados = inventarioo.buscarPorCategoria(categoria);
                    System.out.println("[DEBUG] Productos por categoría '" + categoria + "' encontrados: " + productosFiltrados.size());
                    break;
                case "Proveedor":
                    String proveedor = JOptionPane.showInputDialog("Ingrese el proveedor:");
                    if (proveedor == null || proveedor.isEmpty()) {
                        System.out.println("[DEBUG] Filtro por proveedor cancelado");
                        return;
                    }
                    productosFiltrados = inventarioo.buscarPorProveedor(proveedor);
                    System.out.println("[DEBUG] Productos por proveedor '" + proveedor + "' encontrados: " + productosFiltrados.size());
                    break;
                default: // "Todos"
                    productosFiltrados = inventarioo.obtenerTodosProductos();
                    System.out.println("[DEBUG] Todos los productos obtenidos: " + productosFiltrados.size());
                    break;
            }
            
            if (panelInventario == null) {
                System.err.println("[ERROR] Panel de inventario no está inicializado");
                return;
            }
            
            actualizarVistaInventario(productosFiltrados);
        } catch (Exception e) {
            System.err.println("[ERROR] Al filtrar inventario: " + e.getMessage());
            e.printStackTrace();
            mostrarErrorEnPanel("Error al filtrar inventario: " + e.getMessage());
        }
    }

    public void exportarReporteInventario(String formato) {
        try {
            List<Producto> productos = inventarioo.obtenerTodosProductos();
            int totalProductos = productos.size();
            int bajoStock = (int) productos.stream().filter(p -> p.getCantidadDisponible() <= p.getStockMinimo()).count();
            int sinStock = (int) productos.stream().filter(p -> p.getCantidadDisponible() == 0).count();
            double stockPromedio = productos.stream().mapToInt(Producto::getCantidadDisponible).average().orElse(0);
            Map<String, Integer> distribucionCategorias = productos.stream()
                .collect(Collectors.groupingBy(Producto::getCategoria, Collectors.summingInt(p -> 1)));

            switch (formato.toUpperCase()) {
                case "PDF":
                    exportarInventarioAPDF(productos, totalProductos, bajoStock, sinStock, stockPromedio, distribucionCategorias);
                    break;
                case "EXCEL":
                    exportarInventarioAExcel(productos, totalProductos, bajoStock, sinStock, stockPromedio, distribucionCategorias);
                    break;
                case "HTML":
                    exportarInventarioAHTML(productos, totalProductos, bajoStock, sinStock, stockPromedio, distribucionCategorias);
                    break;
                case "JSON":
                    exportarInventarioAJSON(productos, totalProductos, bajoStock, sinStock, stockPromedio, distribucionCategorias);
                    break;
                case "XML":
                    exportarInventarioAXML(productos, totalProductos, bajoStock, sinStock, stockPromedio, distribucionCategorias);
                    break;
                default:
                    mostrarErrorEnPanel("Formato no soportado: " + formato);
            }
        } catch (Exception e) {
            mostrarErrorEnPanel("Error al exportar reporte: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void exportarInventarioAPDF(List<Producto> productos, int totalProductos, int bajoStock, 
            int sinStock, double stockPromedio, 
            Map<String, Integer> distribucionCategorias) throws Exception {
        String filename = "reporte_inventario_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".pdf";
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
        document.open();

// Encabezado
agregarEncabezadoReporte(document, "INVENTARIO", null, null);

// Resumen estadístico
PdfPTable summaryTable = new PdfPTable(4);
summaryTable.setWidthPercentage(100);
summaryTable.setSpacingBefore(10f);

addSummaryHeaderCell(summaryTable, "Total Productos");
addSummaryHeaderCell(summaryTable, "Bajo Stock");
addSummaryHeaderCell(summaryTable, "Sin Stock");
addSummaryHeaderCell(summaryTable, "Stock Promedio");

addSummaryDataCell(summaryTable, String.valueOf(totalProductos));
addSummaryDataCell(summaryTable, String.valueOf(bajoStock));
addSummaryDataCell(summaryTable, String.valueOf(sinStock));
addSummaryDataCell(summaryTable, String.format("%.2f", stockPromedio));

document.add(summaryTable);

// Tabla de detalle
Paragraph detalleTitle = new Paragraph("Detalle de Inventario", 
new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD));
detalleTitle.setSpacingBefore(15f);
document.add(detalleTitle);

PdfPTable table = new PdfPTable(8);
table.setWidthPercentage(100);
table.setWidths(new float[]{1f, 3f, 2f, 1f, 1f, 1f, 1.5f, 2f});

// Encabezados
addDetailHeaderCell(table, "ID");
addDetailHeaderCell(table, "Producto");
addDetailHeaderCell(table, "Categoría");
addDetailHeaderCell(table, "Stock");
addDetailHeaderCell(table, "Mín");
addDetailHeaderCell(table, "Máx");
addDetailHeaderCell(table, "Precio");
addDetailHeaderCell(table, "Proveedor");

// Datos
for (Producto producto : productos) {
addDetailDataCell(table, producto.getId());
addDetailDataCell(table, producto.getNombre());
addDetailDataCell(table, producto.getCategoria());
addDetailDataCell(table, String.valueOf(producto.getCantidadDisponible()));
addDetailDataCell(table, String.valueOf(producto.getStockMinimo()));
addDetailDataCell(table, String.valueOf(producto.getStockMaximo()));
addDetailDataCell(table, String.format("$%.2f", producto.getPrecioVenta()));
addDetailDataCell(table, producto.getProveedor());
}

document.add(table);

// Gráficas
agregarGraficasInventario(document, writer, stockPromedio, distribucionCategorias);

document.close();

// Abrir el archivo generado
abrirArchivoExportado(new File(filename), "PDF");
}

private void agregarGraficasInventario(Document document, PdfWriter writer, 
               double stockPromedio, 
               Map<String, Integer> distribucionCategorias) 
               throws IOException, DocumentException {
if (distribucionCategorias.isEmpty()) return;

// 1. Gráfico de niveles de stock
DefaultCategoryDataset datasetStock = new DefaultCategoryDataset();
datasetStock.addValue(stockPromedio, "Stock", "Actual");

JFreeChart chartStock = ChartFactory.createBarChart(
"Nivel de Stock Promedio", 
"", 
"Cantidad", 
datasetStock
);

// 2. Gráfico de distribución por categoría
DefaultPieDataset datasetCategorias = new DefaultPieDataset();
distribucionCategorias.forEach(datasetCategorias::setValue);

JFreeChart chartCategorias = ChartFactory.createPieChart(
"Distribución por Categoría", 
datasetCategorias, 
true, true, false
);

// Configuración de tamaño y calidad
int width = 500;
int height = 300;
float quality = 1.0f;

// Agregar gráficos al documento
Paragraph chartTitle = new Paragraph("Análisis Gráfico", 
new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD));
chartTitle.setSpacingBefore(20f);
document.add(chartTitle);

// Gráfico de stock
BufferedImage stockImage = chartStock.createBufferedImage(width, height);
Image stockPdfImage = Image.getInstance(writer, stockImage, quality);
stockPdfImage.setAlignment(Image.MIDDLE);
document.add(stockPdfImage);

// Espacio entre gráficos
document.add(Chunk.NEWLINE);

// Gráfico de categorías
BufferedImage categoriasImage = chartCategorias.createBufferedImage(width, height);
Image categoriasPdfImage = Image.getInstance(writer, categoriasImage, quality);
categoriasPdfImage.setAlignment(Image.MIDDLE);
document.add(categoriasPdfImage);
}

private void exportarInventarioAExcel(List<Producto> productos, int totalProductos, int bajoStock, 
              int sinStock, double stockPromedio, 
              Map<String, Integer> distribucionCategorias) throws Exception {
String filename = "reporte_inventario_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".xlsx";

try (Workbook workbook = new XSSFWorkbook()) {
Sheet sheet = workbook.createSheet("Inventario");

// Estilos
CellStyle headerStyle = crearEstiloEncabezado(workbook);
CellStyle titleStyle = crearEstiloTitulo(workbook);
CellStyle currencyStyle = crearEstiloMoneda(workbook);

int currentRow = 0;

// Título
Row titleRow = sheet.createRow(currentRow++);
crearCeldaConMergedRegion(sheet, titleRow, 0, 7, "REPORTE DE INVENTARIO", titleStyle);

// Fecha
Row dateRow = sheet.createRow(currentRow++);
crearCeldaConMergedRegion(sheet, dateRow, 0, 7, 
"Generado: " + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()), null);

currentRow++;

// Resumen estadístico
Row statsTitleRow = sheet.createRow(currentRow++);
crearCeldaConMergedRegion(sheet, statsTitleRow, 0, 7, "RESUMEN ESTADÍSTICO", titleStyle);

Row statsHeaderRow = sheet.createRow(currentRow++);
crearCelda(statsHeaderRow, 0, "Total Productos", headerStyle);
crearCelda(statsHeaderRow, 1, "Bajo Stock", headerStyle);
crearCelda(statsHeaderRow, 2, "Sin Stock", headerStyle);
crearCelda(statsHeaderRow, 3, "Stock Promedio", headerStyle);

Row statsDataRow = sheet.createRow(currentRow++);
crearCelda(statsDataRow, 0, totalProductos, null);
crearCelda(statsDataRow, 1, bajoStock, null);
crearCelda(statsDataRow, 2, sinStock, null);
crearCelda(statsDataRow, 3, stockPromedio, null);

currentRow++;

// Encabezados de la tabla
Row headerRow = sheet.createRow(currentRow++);
String[] headers = {"ID", "Producto", "Categoría", "Stock", "Stock Mín", "Stock Máx", "Precio", "Proveedor"};
for (int i = 0; i < headers.length; i++) {
crearCelda(headerRow, i, headers[i], headerStyle);
}

// Datos de productos
for (Producto producto : productos) {
Row row = sheet.createRow(currentRow++);

crearCelda(row, 0, producto.getId(), null);
crearCelda(row, 1, producto.getNombre(), null);
crearCelda(row, 2, producto.getCategoria(), null);
crearCelda(row, 3, producto.getCantidadDisponible(), null);
crearCelda(row, 4, producto.getStockMinimo(), null);
crearCelda(row, 5, producto.getStockMaximo(), null);
crearCelda(row, 6, producto.getPrecioVenta(), currencyStyle);
crearCelda(row, 7, producto.getProveedor(), null);
}

// Autoajustar columnas
for (int i = 0; i < headers.length; i++) {
sheet.autoSizeColumn(i);
}

// Guardar archivo
try (FileOutputStream outputStream = new FileOutputStream(filename)) {
workbook.write(outputStream);
}
}

abrirArchivoExportado(new File(filename), "PDF");
}

private void exportarInventarioAHTML(List<Producto> productos, int totalProductos, int bajoStock, 
             int sinStock, double stockPromedio, 
             Map<String, Integer> distribucionCategorias) throws Exception {
String filename = "reporte_inventario_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".html";

try (PrintWriter writer = new PrintWriter(new FileOutputStream(filename))) {
writer.println("<!DOCTYPE html>");
writer.println("<html lang='es'>");
writer.println("<head>");
writer.println("<meta charset='UTF-8'>");
writer.println("<title>Reporte de Inventario</title>");
writer.println("<style>");
writer.println("body { font-family: Arial, sans-serif; margin: 20px; }");
writer.println("h1 { color: #2c3e50; }");
writer.println("table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }");
writer.println("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
writer.println("th { background-color: #f2f2f2; }");
writer.println(".resumen { background-color: #f8f9fa; padding: 15px; margin-bottom: 20px; }");
writer.println("</style>");
writer.println("</head>");
writer.println("<body>");

writer.println("<h1>Reporte de Inventario</h1>");
writer.println("<div class='resumen'>");
writer.println("<p><strong>Generado:</strong> " + new Date() + "</p>");

writer.println("<p><strong>Total Productos:</strong> " + totalProductos + "</p>");
writer.println("<p><strong>Productos bajo stock:</strong> " + bajoStock + "</p>");
writer.println("<p><strong>Productos sin stock:</strong> " + sinStock + "</p>");
writer.println("<p><strong>Stock promedio:</strong> " + String.format("%.2f", stockPromedio) + "</p>");

writer.println("</div>");

writer.println("<h2>Detalle de Productos</h2>");
writer.println("<table>");
writer.println("<tr><th>ID</th><th>Producto</th><th>Categoría</th><th>Stock</th><th>Stock Mín</th><th>Stock Máx</th><th>Precio</th><th>Proveedor</th></tr>");

for (Producto producto : productos) {
writer.println(String.format(
"<tr><td>%s</td><td>%s</td><td>%s</td><td>%d</td><td>%d</td><td>%d</td><td>$%.2f</td><td>%s</td></tr>",
producto.getId(),
producto.getNombre(),
producto.getCategoria(),
producto.getCantidadDisponible(),
producto.getStockMinimo(),
producto.getStockMaximo(),
producto.getPrecioVenta(),
producto.getProveedor()
));
}

writer.println("</table>");

writer.println("<h2>Distribución por Categoría</h2>");
writer.println("<ul>");
distribucionCategorias.forEach((categoria, cantidad) -> {
writer.println(String.format("<li><strong>%s:</strong> %d productos (%.1f%%)</li>",
categoria,
cantidad,
(cantidad * 100.0 / totalProductos)
));
});
writer.println("</ul>");

writer.println("</body>");
writer.println("</html>");
}

abrirArchivoExportado(new File(filename), "HTML");
}

public void cargarDatosClientes() {
    try {
        List<Cliente> clientes = clientee.obtenerTodos();
        actualizarVistaClientes(clientes);
    } catch (Exception e) {
        mostrarErrorEnPanel("Error al cargar clientes: " + e.getMessage());
    }
}

private void actualizarVistaClientes(List<Cliente> clientes) {
    System.out.println("[UI] Actualizando vista con " + clientes.size() + " clientes");
    
    if (panelClientes == null) {
        System.err.println("[ERROR] No se puede actualizar vista: panelClientes es null");
        return;
    }
    
    // Calcular estadísticas
    int total = clientes.size();
    int activos = (int) clientes.stream()
        .filter(c -> c.getFechaEliminacion() == null)
        .count();
    int inactivos = total - activos;
    
    // Registros por mes
    Map<String, Integer> registrosPorMes = clientes.stream()
        .collect(Collectors.groupingBy(
            c -> new SimpleDateFormat("MMM").format(c.getFechaRegistro()),
            Collectors.summingInt(c -> 1)
        ));
    
    // Distribución de puntos
    Map<String, Integer> distribucionPuntos = Map.of(
        "0-100 pts", (int) clientes.stream().filter(c -> c.getPuntos() <= 100).count(),
        "101-500 pts", (int) clientes.stream().filter(c -> c.getPuntos() > 100 && c.getPuntos() <= 500).count(),
        "501+ pts", (int) clientes.stream().filter(c -> c.getPuntos() > 500).count()
    );
    
    // Actualizar la vista en el hilo de EDT
    SwingUtilities.invokeLater(() -> {
        try {
            panelClientes.actualizarTablaClientes(clientes);
            panelClientes.actualizarEstadisticas(total, activos, inactivos);
            panelClientes.actualizarGraficas(registrosPorMes, distribucionPuntos);
        } catch (Exception e) {
            System.err.println("[UI] Error al actualizar componentes: " + e.getMessage());
            e.printStackTrace();
        }
    });
}

public void filtrarClientes(String filtro) {
    System.out.println("[DEBUG] Filtrando clientes por: " + filtro);
    
    try {
        List<Cliente> clientesFiltrados;
        
        switch (filtro) {
            case "Activos":
                clientesFiltrados = clientee.obtenerTodos().stream()
                    .filter(c -> c.getFechaEliminacion() == null)
                    .collect(Collectors.toList());
                break;
            case "Inactivos":
                clientesFiltrados = clientee.obtenerTodos().stream()
                    .filter(c -> c.getFechaEliminacion() != null)
                    .collect(Collectors.toList());
                break;
            case "Con Puntos":
                clientesFiltrados = clientee.obtenerTodos().stream()
                    .filter(c -> c.getPuntos() > 0)
                    .collect(Collectors.toList());
                break;
            case "Sin Puntos":
                clientesFiltrados = clientee.obtenerTodos().stream()
                    .filter(c -> c.getPuntos() == 0)
                    .collect(Collectors.toList());
                break;
            default: // "Todos"
                clientesFiltrados = clientee.obtenerTodos();
                break;
        }
        
        actualizarVistaClientes(clientesFiltrados);
    } catch (Exception e) {
        mostrarErrorEnPanel("Error al filtrar clientes: " + e.getMessage());
    }
}

public void exportarReporteClientes(String formato) {
    try {
        List<Cliente> clientes = clientee.obtenerTodos();
        int totalClientes = clientes.size();
        int activos = (int) clientes.stream().filter(c -> c.getFechaEliminacion() == null).count();
        Map<String, Integer> registrosPorMes = clientes.stream()
            .collect(Collectors.groupingBy(
                c -> new SimpleDateFormat("MMM").format(c.getFechaRegistro()),
                Collectors.summingInt(c -> 1)
            ));

        switch (formato.toUpperCase()) {
            case "PDF":
                exportarClientesAPDF(clientes, totalClientes, activos, registrosPorMes);
                break;
            case "EXCEL":
                exportarClientesAExcel(clientes, totalClientes, activos, registrosPorMes);
                break;
            case "HTML":
                exportarClientesAHTML(clientes, totalClientes, activos, registrosPorMes);
                break;
            case "JSON":
                exportarClientesAJSON(clientes, totalClientes, activos, registrosPorMes);
                break;
            case "XML":
                exportarClientesAXML(clientes, totalClientes, activos, registrosPorMes);
                break;
            default:
                mostrarErrorEnPanel("Formato no soportado: " + formato);
        }
    } catch (Exception e) {
        mostrarErrorEnPanel("Error al exportar reporte: " + e.getMessage());
    }
}

private void exportarClientesAPDF(List<Cliente> clientes, int totalClientes, int activos, 
        Map<String, Integer> registrosPorMes) throws Exception {
String filename = "reporte_clientes_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".pdf";
Document document = new Document(PageSize.A4.rotate());
PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));

document.open();

// Encabezado
agregarEncabezadoReporte(document, "CLIENTES", null, null);

// Resumen estadístico
PdfPTable summaryTable = new PdfPTable(3);
summaryTable.setWidthPercentage(100);
summaryTable.setSpacingBefore(10f);

addSummaryHeaderCell(summaryTable, "Total Clientes");
addSummaryHeaderCell(summaryTable, "Clientes Activos");
addSummaryHeaderCell(summaryTable, "Clientes Inactivos");

addSummaryDataCell(summaryTable, String.valueOf(totalClientes));
addSummaryDataCell(summaryTable, String.valueOf(activos));
addSummaryDataCell(summaryTable, String.valueOf(totalClientes - activos));

document.add(summaryTable);

// Tabla de detalle
Paragraph detalleTitle = new Paragraph("Detalle de Clientes", 
new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD));
detalleTitle.setSpacingBefore(15f);
document.add(detalleTitle);

PdfPTable table = new PdfPTable(6);
table.setWidthPercentage(100);
table.setWidths(new float[]{1f, 1.5f, 3f, 2f, 1f, 2f});

// Encabezados
addDetailHeaderCell(table, "ID");
addDetailHeaderCell(table, "Teléfono");
addDetailHeaderCell(table, "Nombre");
addDetailHeaderCell(table, "Última Compra");
addDetailHeaderCell(table, "Puntos");
addDetailHeaderCell(table, "Fecha Registro");

// Datos
SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
for (Cliente cliente : clientes) {
addDetailDataCell(table, cliente.getId());
addDetailDataCell(table, cliente.getTelefono());
addDetailDataCell(table, cliente.getNombre());
addDetailDataCell(table, cliente.getUltimaCompra() != null ? cliente.getUltimaCompra() : "N/A");
addDetailDataCell(table, String.valueOf(cliente.getPuntos()));
addDetailDataCell(table, cliente.getFechaRegistro() != null ? sdf.format(cliente.getFechaRegistro()) : "N/A");
}

document.add(table);

// Gráficas
agregarGraficasClientes(document, writer, registrosPorMes);

document.close();

// Abrir el archivo generado
abrirArchivoExportado(new File(filename), "PDF");

}

private void agregarGraficasClientes(Document document, PdfWriter writer, 
           Map<String, Integer> registrosPorMes) 
           throws IOException, DocumentException {
if (registrosPorMes.isEmpty()) return;

// 1. Gráfico de registros por mes
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

// 2. Gráfico de distribución de puntos (ejemplo)
DefaultPieDataset datasetPuntos = new DefaultPieDataset();
datasetPuntos.setValue("0-100 pts", 30);
datasetPuntos.setValue("101-500 pts", 45);
datasetPuntos.setValue("501+ pts", 25);

JFreeChart chartPuntos = ChartFactory.createPieChart(
"Distribución de Puntos", 
datasetPuntos, 
true, true, false
);

// Configuración de tamaño y calidad
int width = 500;
int height = 300;
float quality = 1.0f;

// Agregar gráficos al documento
Paragraph chartTitle = new Paragraph("Análisis Gráfico", 
new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD));
chartTitle.setSpacingBefore(20f);
document.add(chartTitle);

// Gráfico de registros
BufferedImage registrosImage = chartRegistros.createBufferedImage(width, height);
Image registrosPdfImage = Image.getInstance(writer, registrosImage, quality);
registrosPdfImage.setAlignment(Image.MIDDLE);
document.add(registrosPdfImage);

// Espacio entre gráficos
document.add(Chunk.NEWLINE);

// Gráfico de puntos
BufferedImage puntosImage = chartPuntos.createBufferedImage(width, height);
Image puntosPdfImage = Image.getInstance(writer, puntosImage, quality);
puntosPdfImage.setAlignment(Image.MIDDLE);
document.add(puntosPdfImage);
}

private void exportarClientesAHTML(List<Cliente> clientes, int totalClientes, int activos, 
        Map<String, Integer> registrosPorMes) throws Exception {
String filename = "reporte_clientes_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".html";

try (PrintWriter writer = new PrintWriter(new FileOutputStream(filename))) {
writer.println("<!DOCTYPE html>");
writer.println("<html lang='es'>");
writer.println("<head>");
writer.println("<meta charset='UTF-8'>");
writer.println("<title>Reporte de Clientes</title>");
writer.println("<style>");
writer.println("body { font-family: Arial, sans-serif; margin: 20px; }");
writer.println("h1 { color: #2c3e50; }");
writer.println("table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }");
writer.println("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
writer.println("th { background-color: #f2f2f2; }");
writer.println(".resumen { background-color: #f8f9fa; padding: 15px; margin-bottom: 20px; }");
writer.println(".chart-container { display: flex; justify-content: space-around; margin: 20px 0; }");
writer.println(".chart { width: 45%; border: 1px solid #ddd; padding: 10px; }");
writer.println("</style>");
writer.println("</head>");
writer.println("<body>");

writer.println("<h1>Reporte de Clientes</h1>");
writer.println("<div class='resumen'>");
writer.println("<p><strong>Generado:</strong> " + new Date() + "</p>");
writer.println("<p><strong>Total Clientes:</strong> " + totalClientes + "</p>");
writer.println("<p><strong>Clientes activos:</strong> " + activos + "</p>");
writer.println("<p><strong>Clientes inactivos:</strong> " + (totalClientes - activos) + "</p>");
writer.println("</div>");

writer.println("<h2>Detalle de Clientes</h2>");
writer.println("<table>");
writer.println("<tr><th>ID</th><th>Teléfono</th><th>Nombre</th><th>Última Compra</th><th>Puntos</th><th>Fecha Registro</th></tr>");

SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
for (Cliente cliente : clientes) {
writer.println(String.format(
"<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%d</td><td>%s</td></tr>",
cliente.getId(),
cliente.getTelefono(),
cliente.getNombre(),
cliente.getUltimaCompra() != null ? cliente.getUltimaCompra() : "N/A",
cliente.getPuntos(),
cliente.getFechaRegistro() != null ? sdf.format(cliente.getFechaRegistro()) : "N/A"
));
}

writer.println("</table>");

// Sección de gráficos (simulada con HTML básico)
writer.println("<div class='chart-container'>");
writer.println("<div class='chart'>");
writer.println("<h3>Registros por Mes</h3>");
writer.println("<ul>");
registrosPorMes.forEach((mes, cantidad) -> {
writer.println(String.format("<li><strong>%s:</strong> %d clientes</li>", mes, cantidad));
});
writer.println("</ul>");
writer.println("</div>");

writer.println("<div class='chart'>");
writer.println("<h3>Distribución de Puntos</h3>");
writer.println("<ul>");
writer.println("<li><strong>0-100 pts:</strong> " + 
clientes.stream().filter(c -> c.getPuntos() <= 100).count() + " clientes</li>");
writer.println("<li><strong>101-500 pts:</strong> " + 
clientes.stream().filter(c -> c.getPuntos() > 100 && c.getPuntos() <= 500).count() + " clientes</li>");
writer.println("<li><strong>501+ pts:</strong> " + 
clientes.stream().filter(c -> c.getPuntos() > 500).count() + " clientes</li>");
writer.println("</ul>");
writer.println("</div>");
writer.println("</div>");

writer.println("</body>");
writer.println("</html>");
}

abrirArchivoExportado(new File(filename), "HTML");
}

private void exportarClientesAExcel(List<Cliente> clientes, int totalClientes, int activos, 
        Map<String, Integer> registrosPorMes) throws Exception {
String filename = "reporte_clientes_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".xlsx";

try (Workbook workbook = new XSSFWorkbook()) {
Sheet sheet = workbook.createSheet("Clientes");

// Estilos
CellStyle headerStyle = crearEstiloEncabezado(workbook);
CellStyle titleStyle = crearEstiloTitulo(workbook);
CellStyle dateStyle = crearEstiloFecha(workbook);

int currentRow = 0;

// Título
Row titleRow = sheet.createRow(currentRow++);
crearCeldaConMergedRegion(sheet, titleRow, 0, 5, "REPORTE DE CLIENTES", titleStyle);

// Fecha
Row dateRow = sheet.createRow(currentRow++);
crearCeldaConMergedRegion(sheet, dateRow, 0, 5, 
"Generado: " + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()), null);

currentRow++;

// Resumen estadístico
Row statsTitleRow = sheet.createRow(currentRow++);
crearCeldaConMergedRegion(sheet, statsTitleRow, 0, 5, "RESUMEN ESTADÍSTICO", titleStyle);

Row statsHeaderRow = sheet.createRow(currentRow++);
crearCelda(statsHeaderRow, 0, "Total Clientes", headerStyle);
crearCelda(statsHeaderRow, 1, "Clientes Activos", headerStyle);
crearCelda(statsHeaderRow, 2, "Clientes Inactivos", headerStyle);

Row statsDataRow = sheet.createRow(currentRow++);
crearCelda(statsDataRow, 0, totalClientes, null);
crearCelda(statsDataRow, 1, activos, null);
crearCelda(statsDataRow, 2, totalClientes - activos, null);

currentRow++;

// Encabezados de la tabla
Row headerRow = sheet.createRow(currentRow++);
String[] headers = {"ID", "Teléfono", "Nombre", "Última Compra", "Puntos", "Fecha Registro"};
for (int i = 0; i < headers.length; i++) {
crearCelda(headerRow, i, headers[i], headerStyle);
}

// Datos de clientes
SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
for (Cliente cliente : clientes) {
Row row = sheet.createRow(currentRow++);

crearCelda(row, 0, cliente.getId(), null);
crearCelda(row, 1, cliente.getTelefono(), null);
crearCelda(row, 2, cliente.getNombre(), null);
crearCelda(row, 3, cliente.getUltimaCompra() != null ? cliente.getUltimaCompra() : "N/A", null);
crearCelda(row, 4, cliente.getPuntos(), null);

if (cliente.getFechaRegistro() != null) {
Cell fechaCell = row.createCell(5);
fechaCell.setCellValue(cliente.getFechaRegistro());
fechaCell.setCellStyle(dateStyle);
} else {
crearCelda(row, 5, "N/A", null);
}
}

// Autoajustar columnas
for (int i = 0; i < headers.length; i++) {
sheet.autoSizeColumn(i);
}

// Hoja adicional para gráficos (requiere Apache POI 4.0+)
Sheet chartSheet = workbook.createSheet("Gráficos");
currentRow = 0;

// Datos para gráfico de registros por mes
Row chartTitleRow = chartSheet.createRow(currentRow++);
crearCeldaConMergedRegion(chartSheet, chartTitleRow, 0, 1, "Registros por Mes", titleStyle);

Row chartHeaderRow = chartSheet.createRow(currentRow++);
crearCelda(chartHeaderRow, 0, "Mes", headerStyle);
crearCelda(chartHeaderRow, 1, "Cantidad", headerStyle);

for (Map.Entry<String, Integer> entry : registrosPorMes.entrySet()) {
Row dataRow = chartSheet.createRow(currentRow++);
crearCelda(dataRow, 0, entry.getKey(), null);
crearCelda(dataRow, 1, entry.getValue(), null);
}

// Guardar archivo
try (FileOutputStream outputStream = new FileOutputStream(filename)) {
workbook.write(outputStream);
}
}

abrirArchivoExportado(new File(filename), "Excel");

}

public Map<String, Object> generarEstadisticasClientes(List<Cliente> clientes) {
    Map<String, Object> stats = new HashMap<>();
    
    if (clientes == null || clientes.isEmpty()) {
        return stats;
    }
    
    // Estadísticas básicas
    int total = clientes.size();
    int activos = (int) clientes.stream().filter(c -> c.getFechaEliminacion() == null).count();
    int conPuntos = (int) clientes.stream().filter(c -> c.getPuntos() > 0).count();
    
    // Registros por mes
    Map<String, Integer> registrosPorMes = clientes.stream()
        .collect(Collectors.groupingBy(
            c -> new SimpleDateFormat("MMM yyyy").format(c.getFechaRegistro()),
            Collectors.summingInt(c -> 1)
        ));
    
    // Distribución de puntos
    Map<String, Integer> distribucionPuntos = new LinkedHashMap<>();
    distribucionPuntos.put("0-100 pts", (int) clientes.stream().filter(c -> c.getPuntos() <= 100).count());
    distribucionPuntos.put("101-500 pts", (int) clientes.stream().filter(c -> c.getPuntos() > 100 && c.getPuntos() <= 500).count());
    distribucionPuntos.put("501+ pts", (int) clientes.stream().filter(c -> c.getPuntos() > 500).count());
    
    // Clientes con más puntos (Top 5)
    List<Cliente> topClientes = clientes.stream()
        .sorted((c1, c2) -> Integer.compare(c2.getPuntos(), c1.getPuntos()))
        .limit(5)
        .collect(Collectors.toList());
    
    stats.put("totalClientes", total);
    stats.put("clientesActivos", activos);
    stats.put("clientesInactivos", total - activos);
    stats.put("clientesConPuntos", conPuntos);
    stats.put("registrosPorMes", registrosPorMes);
    stats.put("distribucionPuntos", distribucionPuntos);
    stats.put("topClientes", topClientes);
    
    return stats;
}

public void actualizarGraficasClientes() {
    List<Cliente> clientes = clientee.obtenerTodos();
    Map<String, Object> stats = generarEstadisticasClientes(clientes);
    
    if (panelClientes != null) {
        panelClientes.actualizarGraficas(
            (Map<String, Integer>) stats.get("registrosPorMes"),
            (Map<String, Integer>) stats.get("distribucionPuntos")
        );
    }
}

public List<Cliente> filtrarClientesPorRangoPuntos(int min, int max) {
    return clientee.obtenerTodos().stream()
        .filter(c -> c.getPuntos() >= min && c.getPuntos() <= max)
        .collect(Collectors.toList());
}

public List<Cliente> filtrarClientesPorPeriodo(Date fechaInicio, Date fechaFin) {
    return clientee.obtenerTodos().stream()
        .filter(c -> c.getFechaRegistro() != null &&
                    !c.getFechaRegistro().before(fechaInicio) &&
                    !c.getFechaRegistro().after(fechaFin))
        .collect(Collectors.toList());
}

public void cargarDatosProveedores() {
    try {
        if (proveedorr == null) {
            proveedorr = new Proveedorr(); // Inicializa si es null
        }
        List<Proveedor> proveedores = proveedorr.obtenerTodosProveedores();
        actualizarVistaProveedores(proveedores);
    } catch (Exception e) {
        mostrarErrorEnPanel("Error al cargar proveedores: " + e.getMessage());
    }
}

private void actualizarVistaProveedores(List<Proveedor> proveedores) {
    System.out.println("[UI] Actualizando vista con " + proveedores.size() + " proveedores");
    
    if (panelProveedores == null) {
        System.err.println("[ERROR] No se puede actualizar vista: panelProveedores es null");
        return;
    }
    
    // Calcular estadísticas
    int total = proveedores.size();
    
    // Proveedores visitados este mes
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.DAY_OF_MONTH, 1);
    Date inicioMes = new Date(cal.getTimeInMillis());
    
    int visitadosEsteMes = (int) proveedores.stream()
        .filter(p -> p.getUltimaVisita() != null && 
                    p.getUltimaVisita().after(inicioMes))
        .count();
    
    // Última visita registrada
    String ultimaVisita = proveedores.stream()
        .filter(p -> p.getUltimaVisita() != null)
        .map(Proveedor::getUltimaVisita)
        .max(Timestamp::compareTo)
        .map(t -> new SimpleDateFormat("dd/MM/yyyy").format(t))
        .orElse("N/A");
    
    // Distribución por producto suministrado
    Map<String, Integer> distribucionProductos = proveedores.stream()
        .collect(Collectors.groupingBy(
            p -> p.getProductoSuministrado() != null && !p.getProductoSuministrado().isEmpty() ? 
                 p.getProductoSuministrado() : "No especificado",
            Collectors.summingInt(p -> 1)
        ));
    
    // Visitas por mes (últimos 6 meses)
    Map<String, Integer> visitasPorMes = new LinkedHashMap<>();
    SimpleDateFormat sdfMes = new SimpleDateFormat("MMM");
    
    for (int i = 5; i >= 0; i--) {
        cal.setTime(new Date());
        cal.add(Calendar.MONTH, -i);
        String mes = sdfMes.format(cal.getTime());
        visitasPorMes.put(mes, 0);
    }
    
    proveedores.stream()
        .filter(p -> p.getUltimaVisita() != null)
        .forEach(p -> {
            String mes = sdfMes.format(p.getUltimaVisita());
            visitasPorMes.merge(mes, 1, Integer::sum);
        });
    
    // Actualizar la vista en el hilo de EDT
    SwingUtilities.invokeLater(() -> {
        try {
            panelProveedores.actualizarTablaProveedores(proveedores);
            panelProveedores.actualizarEstadisticas(total, visitadosEsteMes, ultimaVisita);
            panelProveedores.actualizarGraficas(visitasPorMes, distribucionProductos);
        } catch (Exception e) {
            System.err.println("[UI] Error al actualizar componentes: " + e.getMessage());
            e.printStackTrace();
        }
    });
}

public void filtrarProveedores(String filtro) {
    System.out.println("[DEBUG] Filtrando proveedores por: " + filtro);
    
    try {
        List<Proveedor> proveedoresFiltrados;
        
        switch (filtro) {
            case "Con Visita Reciente":
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.MONTH, -1);
                Date haceUnMes = new Date(cal.getTimeInMillis());
                
                proveedoresFiltrados = proveedorr.obtenerTodosProveedores().stream()
                    .filter(p -> p.getUltimaVisita() != null && 
                                p.getUltimaVisita().after(haceUnMes))
                    .collect(Collectors.toList());
                break;
                
            case "Sin Visita Reciente":
                cal = Calendar.getInstance();
                cal.add(Calendar.MONTH, -1);
                haceUnMes = new Date(cal.getTimeInMillis());
                
                proveedoresFiltrados = proveedorr.obtenerTodosProveedores().stream()
                    .filter(p -> p.getUltimaVisita() == null || 
                                p.getUltimaVisita().before(haceUnMes))
                    .collect(Collectors.toList());
                break;
                
            case "Por Producto":
                String producto = JOptionPane.showInputDialog("Ingrese el producto:");
                if (producto == null || producto.isEmpty()) {
                    System.out.println("[DEBUG] Filtro por producto cancelado");
                    return;
                }
                
                proveedoresFiltrados = proveedorr.obtenerTodosProveedores().stream()
                    .filter(p -> p.getProductoSuministrado() != null && 
                                p.getProductoSuministrado().toLowerCase().contains(producto.toLowerCase()))
                    .collect(Collectors.toList());
                break;
                
            default: // "Todos"
                proveedoresFiltrados = proveedorr.obtenerTodosProveedores();
                break;
        }
        
        actualizarVistaProveedores(proveedoresFiltrados);
    } catch (Exception e) {
        mostrarErrorEnPanel("Error al filtrar proveedores: " + e.getMessage());
    }
}

public void exportarReporteProveedores(String formato) {
    try {
        List<Proveedor> proveedores = proveedorr.obtenerTodosProveedores();
        int totalProveedores = proveedores.size();
        
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date inicioMes = new Date(cal.getTimeInMillis());
        
        int visitadosEsteMes = (int) proveedores.stream()
            .filter(p -> p.getUltimaVisita() != null && p.getUltimaVisita().after(inicioMes))
            .count();
        
        Map<String, Integer> distribucionProductos = proveedores.stream()
            .collect(Collectors.groupingBy(
                p -> p.getProductoSuministrado() != null && !p.getProductoSuministrado().isEmpty() ? 
                     p.getProductoSuministrado() : "No especificado",
                Collectors.summingInt(p -> 1)
            ));
        
        Map<String, Integer> visitasPorMes = new LinkedHashMap<>();
        SimpleDateFormat sdfMes = new SimpleDateFormat("MMM");
        
        for (int i = 5; i >= 0; i--) {
            cal.setTime(new Date());
            cal.add(Calendar.MONTH, -i);
            String mes = sdfMes.format(cal.getTime());
            visitasPorMes.put(mes, 0);
        }
        
        proveedores.stream()
            .filter(p -> p.getUltimaVisita() != null)
            .forEach(p -> {
                String mes = sdfMes.format(p.getUltimaVisita());
                visitasPorMes.merge(mes, 1, Integer::sum);
            });

        switch (formato.toUpperCase()) {
            case "PDF":
                exportarProveedoresAPDF(proveedores, totalProveedores, visitadosEsteMes, visitasPorMes, distribucionProductos);
                break;
            case "EXCEL":
                exportarProveedoresAExcel(proveedores, totalProveedores, visitadosEsteMes, visitasPorMes, distribucionProductos);
                break;
            case "HTML":
                exportarProveedoresAHTML(proveedores, totalProveedores, visitadosEsteMes, visitasPorMes, distribucionProductos);
                break;
            case "JSON":
                exportarProveedoresAJSON(proveedores, totalProveedores, visitadosEsteMes, visitasPorMes, distribucionProductos);
                break;
            case "XML":
                exportarProveedoresAXML(proveedores, totalProveedores, visitadosEsteMes, visitasPorMes, distribucionProductos);
                break;
            default:
                mostrarErrorEnPanel("Formato no soportado: " + formato);
        }
    } catch (Exception e) {
        mostrarErrorEnPanel("Error al exportar reporte: " + e.getMessage());
        e.printStackTrace();
    }
}
private void exportarProveedoresAPDF(List<Proveedor> proveedores, int totalProveedores, int visitadosEsteMes, 
        Map<String, Integer> visitasPorMes, 
        Map<String, Integer> distribucionProductos) throws Exception {
String filename = "reporte_proveedores_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".pdf";
Document document = new Document(PageSize.A4.rotate());
PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));

document.open();

// Encabezado
agregarEncabezadoReporte(document, "PROVEEDORES", null, null);

// Resumen estadístico
PdfPTable summaryTable = new PdfPTable(3);
summaryTable.setWidthPercentage(100);
summaryTable.setSpacingBefore(10f);

addSummaryHeaderCell(summaryTable, "Total Proveedores");
addSummaryHeaderCell(summaryTable, "Visitados este mes");
addSummaryHeaderCell(summaryTable, "Por visitar este mes");

addSummaryDataCell(summaryTable, String.valueOf(totalProveedores));
addSummaryDataCell(summaryTable, String.valueOf(visitadosEsteMes));
addSummaryDataCell(summaryTable, String.valueOf(totalProveedores - visitadosEsteMes));

document.add(summaryTable);

// Tabla de detalle
Paragraph detalleTitle = new Paragraph("Detalle de Proveedores", 
new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD));
detalleTitle.setSpacingBefore(15f);
document.add(detalleTitle);

PdfPTable table = new PdfPTable(6);
table.setWidthPercentage(100);
table.setWidths(new float[]{1f, 2f, 1.5f, 2f, 2f, 2f});

// Encabezados
addDetailHeaderCell(table, "ID");
addDetailHeaderCell(table, "Nombre");
addDetailHeaderCell(table, "Teléfono");
addDetailHeaderCell(table, "Dirección");
addDetailHeaderCell(table, "Producto Suministrado");
addDetailHeaderCell(table, "Última Visita");

// Datos
SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
for (Proveedor proveedor : proveedores) {
addDetailDataCell(table, proveedor.getId());
addDetailDataCell(table, proveedor.getNombre());
addDetailDataCell(table, proveedor.getTelefono());
addDetailDataCell(table, proveedor.getDireccion());
addDetailDataCell(table, proveedor.getProductoSuministrado());
addDetailDataCell(table, proveedor.getUltimaVisita() != null ? 
sdf.format(proveedor.getUltimaVisita()) : "Nunca");
}

document.add(table);

// Gráficas
agregarGraficasProveedores(document, writer, visitasPorMes, distribucionProductos);

document.close();

// Abrir el archivo generado
abrirArchivoExportado(new File(filename), "PDF");

}

private void agregarGraficasProveedores(Document document, PdfWriter writer, 
           Map<String, Integer> visitasPorMes, 
           Map<String, Integer> distribucionProductos) 
           throws IOException, DocumentException {
// 1. Gráfico de visitas por mes
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

// 2. Gráfico de distribución por producto
DefaultPieDataset datasetProductos = new DefaultPieDataset();
distribucionProductos.forEach(datasetProductos::setValue);

JFreeChart chartProductos = ChartFactory.createPieChart(
"Distribución por Producto", 
datasetProductos, 
true, true, false
);

// Configuración de tamaño y calidad
int width = 500;
int height = 300;
float quality = 1.0f;

// Agregar gráficos al documento
Paragraph chartTitle = new Paragraph("Análisis Gráfico", 
new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD));
chartTitle.setSpacingBefore(20f);
document.add(chartTitle);

// Gráfico de visitas
BufferedImage visitasImage = chartVisitas.createBufferedImage(width, height);
Image visitasPdfImage = Image.getInstance(writer, visitasImage, quality);
visitasPdfImage.setAlignment(Image.MIDDLE);
document.add(visitasPdfImage);

// Espacio entre gráficos
document.add(Chunk.NEWLINE);

// Gráfico de productos
BufferedImage productosImage = chartProductos.createBufferedImage(width, height);
Image productosPdfImage = Image.getInstance(writer, productosImage, quality);
productosPdfImage.setAlignment(Image.MIDDLE);
document.add(productosPdfImage);
}

private void exportarProveedoresAExcel(List<Proveedor> proveedores, int totalProveedores, int visitadosEsteMes, 
          Map<String, Integer> visitasPorMes, 
          Map<String, Integer> distribucionProductos) throws Exception {
String filename = "reporte_proveedores_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".xlsx";

try (Workbook workbook = new XSSFWorkbook()) {
Sheet sheet = workbook.createSheet("Proveedores");

// Estilos
CellStyle headerStyle = crearEstiloEncabezado(workbook);
CellStyle titleStyle = crearEstiloTitulo(workbook);
CellStyle dateStyle = crearEstiloFecha(workbook);

int currentRow = 0;

// Título
Row titleRow = sheet.createRow(currentRow++);
crearCeldaConMergedRegion(sheet, titleRow, 0, 5, "REPORTE DE PROVEEDORES", titleStyle);

// Fecha
Row dateRow = sheet.createRow(currentRow++);
crearCeldaConMergedRegion(sheet, dateRow, 0, 5, 
"Generado: " + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()), null);

currentRow++;

// Resumen estadístico
Row statsTitleRow = sheet.createRow(currentRow++);
crearCeldaConMergedRegion(sheet, statsTitleRow, 0, 5, "RESUMEN ESTADÍSTICO", titleStyle);

Row statsHeaderRow = sheet.createRow(currentRow++);
crearCelda(statsHeaderRow, 0, "Total Proveedores", headerStyle);
crearCelda(statsHeaderRow, 1, "Visitados este mes", headerStyle);
crearCelda(statsHeaderRow, 2, "Por visitar este mes", headerStyle);

Row statsDataRow = sheet.createRow(currentRow++);
crearCelda(statsDataRow, 0, totalProveedores, null);
crearCelda(statsDataRow, 1, visitadosEsteMes, null);
crearCelda(statsDataRow, 2, totalProveedores - visitadosEsteMes, null);

currentRow++;

// Encabezados de la tabla
Row headerRow = sheet.createRow(currentRow++);
String[] headers = {"ID", "Nombre", "Teléfono", "Dirección", "Producto Suministrado", "Última Visita"};
for (int i = 0; i < headers.length; i++) {
crearCelda(headerRow, i, headers[i], headerStyle);
}

// Datos de proveedores
SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
for (Proveedor proveedor : proveedores) {
Row row = sheet.createRow(currentRow++);

crearCelda(row, 0, proveedor.getId(), null);
crearCelda(row, 1, proveedor.getNombre(), null);
crearCelda(row, 2, proveedor.getTelefono(), null);
crearCelda(row, 3, proveedor.getDireccion(), null);
crearCelda(row, 4, proveedor.getProductoSuministrado(), null);

if (proveedor.getUltimaVisita() != null) {
Cell fechaCell = row.createCell(5);
fechaCell.setCellValue(proveedor.getUltimaVisita());
fechaCell.setCellStyle(dateStyle);
} else {
crearCelda(row, 5, "Nunca", null);
}
}

// Autoajustar columnas
for (int i = 0; i < headers.length; i++) {
sheet.autoSizeColumn(i);
}

// Hoja adicional para gráficos
Sheet chartSheet = workbook.createSheet("Gráficos");
currentRow = 0;

// Datos para gráfico de visitas por mes
Row chartTitleRow = chartSheet.createRow(currentRow++);
crearCeldaConMergedRegion(chartSheet, chartTitleRow, 0, 1, "Visitas por Mes", titleStyle);

Row chartHeaderRow = chartSheet.createRow(currentRow++);
crearCelda(chartHeaderRow, 0, "Mes", headerStyle);
crearCelda(chartHeaderRow, 1, "Visitas", headerStyle);

for (Map.Entry<String, Integer> entry : visitasPorMes.entrySet()) {
Row dataRow = chartSheet.createRow(currentRow++);
crearCelda(dataRow, 0, entry.getKey(), null);
crearCelda(dataRow, 1, entry.getValue(), null);
}

// Datos para gráfico de distribución por producto
currentRow += 2;
Row chartTitleRow2 = chartSheet.createRow(currentRow++);
crearCeldaConMergedRegion(chartSheet, chartTitleRow2, 0, 1, "Distribución por Producto", titleStyle);

Row chartHeaderRow2 = chartSheet.createRow(currentRow++);
crearCelda(chartHeaderRow2, 0, "Producto", headerStyle);
crearCelda(chartHeaderRow2, 1, "Cantidad", headerStyle);

for (Map.Entry<String, Integer> entry : distribucionProductos.entrySet()) {
Row dataRow = chartSheet.createRow(currentRow++);
crearCelda(dataRow, 0, entry.getKey(), null);
crearCelda(dataRow, 1, entry.getValue(), null);
}

// Guardar archivo
try (FileOutputStream outputStream = new FileOutputStream(filename)) {
workbook.write(outputStream);
}
}

abrirArchivoExportado(new File(filename), "Excel");
}

private void exportarProveedoresAHTML(List<Proveedor> proveedores, int totalProveedores, int visitadosEsteMes, 
         Map<String, Integer> visitasPorMes, 
         Map<String, Integer> distribucionProductos) throws Exception {
String filename = "reporte_proveedores_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".html";

try (PrintWriter writer = new PrintWriter(new FileOutputStream(filename))) {
writer.println("<!DOCTYPE html>");
writer.println("<html lang='es'>");
writer.println("<head>");
writer.println("<meta charset='UTF-8'>");
writer.println("<title>Reporte de Proveedores</title>");
writer.println("<style>");
writer.println("body { font-family: Arial, sans-serif; margin: 20px; }");
writer.println("h1 { color: #2c3e50; }");
writer.println("table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }");
writer.println("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
writer.println("th { background-color: #f2f2f2; }");
writer.println(".resumen { background-color: #f8f9fa; padding: 15px; margin-bottom: 20px; }");
writer.println(".chart-container { display: flex; justify-content: space-around; margin: 20px 0; }");
writer.println(".chart { width: 45%; border: 1px solid #ddd; padding: 10px; }");
writer.println("</style>");
writer.println("</head>");
writer.println("<body>");

writer.println("<h1>Reporte de Proveedores</h1>");
writer.println("<div class='resumen'>");
writer.println("<p><strong>Generado:</strong> " + new Date() + "</p>");
writer.println("<p><strong>Total Proveedores:</strong> " + totalProveedores + "</p>");
writer.println("<p><strong>Visitados este mes:</strong> " + visitadosEsteMes + "</p>");
writer.println("<p><strong>Por visitar este mes:</strong> " + (totalProveedores - visitadosEsteMes) + "</p>");
writer.println("</div>");

writer.println("<h2>Detalle de Proveedores</h2>");
writer.println("<table>");
writer.println("<tr><th>ID</th><th>Nombre</th><th>Teléfono</th><th>Dirección</th><th>Producto Suministrado</th><th>Última Visita</th></tr>");

SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
for (Proveedor proveedor : proveedores) {
writer.println(String.format(
"<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>",
proveedor.getId(),
proveedor.getNombre(),
proveedor.getTelefono(),
proveedor.getDireccion(),
proveedor.getProductoSuministrado(),
proveedor.getUltimaVisita() != null ? sdf.format(proveedor.getUltimaVisita()) : "Nunca"
));
}
writer.println("</table>");

// Sección de gráficos
writer.println("<div class='chart-container'>");
writer.println("<div class='chart'>");
writer.println("<h3>Visitas por Mes</h3>");
writer.println("<ul>");
visitasPorMes.forEach((mes, cantidad) -> {
writer.println(String.format("<li><strong>%s:</strong> %d visitas</li>", mes, cantidad));
});
writer.println("</ul>");
writer.println("</div>");

writer.println("<div class='chart'>");
writer.println("<h3>Distribución por Producto</h3>");
writer.println("<ul>");
distribucionProductos.forEach((producto, cantidad) -> {
writer.println(String.format("<li><strong>%s:</strong> %d proveedores</li>", producto, cantidad));
});
writer.println("</ul>");
writer.println("</div>");
writer.println("</div>");

writer.println("</body>");
writer.println("</html>");
}

abrirArchivoExportado(new File(filename), "HTML");

}


public void reimprimirReporte(String tipoReporte, String idReporte) {
    try {
        File reporte = buscarArchivoReporte(tipoReporte, idReporte);
        
        if (reporte == null) {
            // Intenta regenerar el reporte si no se encuentra
            regenerarReporte(tipoReporte, idReporte);
            reporte = buscarArchivoReporte(tipoReporte, idReporte);
            
            if (reporte == null) {
                mostrarErrorEnPanel("No se encontró el reporte " + idReporte + 
                                 " de tipo " + tipoReporte);
                return;
            }
        }
        
        // Abrir el archivo
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(reporte);
        } else {
            mostrarErrorEnPanel("No se puede abrir el reporte automáticamente");
        }
    } catch (Exception e) {
        mostrarErrorEnPanel("Error al reimprimir: " + e.getMessage());
        e.printStackTrace();
    }
}

private void regenerarReporte(String tipoReporte, String idReporte) {
    switch (tipoReporte.toUpperCase()) {
        case "VENTA":
        try {
        reimprimirTicketVenta(idReporte);
        } catch (Exception e) {
        }
        break;
       
        case "TICKET":
			try {
				reimprimirTicketVenta(idReporte);
			} catch (Exception e) {
				mostrarErrorEnPanel("Error al regenerar el ticket: " + e.getMessage());
			}
            break;
        case "INVENTARIO":
            exportarReporteInventario("PDF");
            break;
        case "CLIENTES":
            exportarReporteClientes("PDF");
            break;
        case "PROVEEDORES":
            exportarReporteProveedores("PDF");
            break;
        default:
            mostrarErrorEnPanel("Tipo de reporte no soportado: " + tipoReporte);
    }
}

public void reimprimirTicketVenta(String idVenta) throws Exception {
 // Obtener los datos de la venta desde la BD
 Venta venta = obtenerVentaPorId(idVenta);
 
 if (venta == null) {
     throw new Exception("No se encontró la venta con ID: " + idVenta);
 }
 
 // Generar el ticket nuevamente
 VentaContro ventaControl = new VentaContro(usuario);
 ventaControl.generarTicketVenta(venta);
}

private Venta obtenerVentaPorId(String idVenta) throws SQLException {
 Connection conn = null;
 PreparedStatement pstmtVenta = null;
 PreparedStatement pstmtDetalles = null;
 ResultSet rsVenta = null;
 ResultSet rsDetalles = null;
 
 try {
     conn = ConexionAccess.conectar();
     
     // Consulta para obtener la venta principal
     String sqlVenta = "SELECT id, fecha, total, metodo_pago, descuento, monto_recibido FROM Ventas WHERE id = ?";
     pstmtVenta = conn.prepareStatement(sqlVenta);
     pstmtVenta.setString(1, idVenta);
     rsVenta = pstmtVenta.executeQuery();
     
     if (!rsVenta.next()) {
         return null;
     }
     
     Venta venta = new Venta();
     venta.setId(rsVenta.getInt("id"));
     venta.setFecha(rsVenta.getTimestamp("fecha"));
     venta.setTotal(rsVenta.getDouble("total"));
     venta.setMetodoPago(rsVenta.getString("metodo_pago"));
     venta.setDescuento(rsVenta.getDouble("descuento"));
     venta.setMontoRecibido(rsVenta.getDouble("monto_recibido"));
     
     // Obtener detalles de la venta
     String sqlDetalles = "SELECT p.id, p.nombre, p.precio_venta, dv.cantidad " +
                        "FROM DetalleVenta dv " +
                        "JOIN Productos p ON dv.id_producto = p.id " +
                        "WHERE dv.id_venta = ?";
     pstmtDetalles = conn.prepareStatement(sqlDetalles);
     pstmtDetalles.setString(1, idVenta);
     rsDetalles = pstmtDetalles.executeQuery();
     
     List<Producto> productos = new ArrayList<>();
     while (rsDetalles.next()) {
         Producto producto = new Producto(
             rsDetalles.getString("id"),
             rsDetalles.getString("nombre"),
             "", "", "",
             rsDetalles.getInt("cantidad"),
             0, 0, 0.0,
             rsDetalles.getDouble("precio_venta"),
             false, 0.0, null, "", "", ""
         );
         productos.add(producto);
     }
     
     venta.setProductos(productos);
     return venta;
     
 } finally {
     // Cerrar recursos
     if (rsDetalles != null) rsDetalles.close();
     if (rsVenta != null) rsVenta.close();
     if (pstmtDetalles != null) pstmtDetalles.close();
     if (pstmtVenta != null) pstmtVenta.close();
     if (conn != null) conn.close();
 }
}

private void reimprimirReporteGenerado(String tipoReporte, String idReporte) throws Exception {
 // Buscar el archivo del reporte en la estructura de carpetas
 File reporte = buscarArchivoReporte(tipoReporte, idReporte);
 
 if (reporte == null || !reporte.exists()) {
     throw new Exception("No se encontró el archivo del reporte");
 }
 
 // Abrir el archivo con el visor PDF predeterminado
 if (Desktop.isDesktopSupported()) {
     Desktop.getDesktop().open(reporte);
 } else {
     throw new Exception("No se puede abrir el reporte automáticamente en este sistema");
 }
}
public boolean existeReporte(String tipoReporte, String idReporte) {
    return buscarArchivoReporte(tipoReporte, idReporte) != null;
}

private File buscarArchivoReporte(String tipoReporte, String idReporte) {
    // Ruta base donde se guardan los reportes
    String basePath = "C:\\Users\\Anahi\\eclipse-workspace\\punto_venta_2\\Reportes";
    
    // Primero buscar en la subcarpeta específica del tipo de reporte
    File carpetaTipo = new File(basePath, tipoReporte);
    if (carpetaTipo.exists() && carpetaTipo.isDirectory()) {
        File[] archivos = buscarArchivosEnCarpeta(carpetaTipo, idReporte);
        if (archivos != null && archivos.length > 0) {
            return archivos[0]; // Devolver el más reciente
        }
    }
    
    // Si no se encontró en la subcarpeta, buscar en la carpeta principal de Reportes
    File carpetaPrincipal = new File(basePath);
    if (carpetaPrincipal.exists() && carpetaPrincipal.isDirectory()) {
        File[] archivos = buscarArchivosEnCarpeta(carpetaPrincipal, idReporte);
        if (archivos != null && archivos.length > 0) {
            return archivos[0];
        }
    }
    
    return null;
}

private File[] buscarArchivosEnCarpeta(File carpeta, String idReporte) {
    // Patrones de nombres de archivo a buscar
    String[] patrones = {
        "reporte_" + tipoReporteActual.toLowerCase() + "_" + idReporte + ".*",
        tipoReporteActual.toLowerCase() + "_" + idReporte + ".*",
        idReporte + ".*"
    };
    
    // Filtrar archivos que coincidan con los patrones
    File[] archivos = carpeta.listFiles((dir, name) -> {
        for (String patron : patrones) {
            if (name.toLowerCase().matches(patron.toLowerCase().replace(".", "\\.").replace("*", ".*"))) {
                return true;
            }
        }
        return false;
    });
    
    // Ordenar por fecha de modificación (más reciente primero)
    if (archivos != null && archivos.length > 0) {
        Arrays.sort(archivos, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
        
        // Verificar integridad de los archivos
        for (File archivo : archivos) {
            if (verificarIntegridadArchivo(archivo)) {
                return new File[]{archivo}; // Devolver solo el primero válido
            }
        }
    }
    
    return null;
}

private boolean verificarIntegridadArchivo(File archivo) {
    try {
        // Verificaciones básicas
        if (!archivo.exists() || archivo.length() == 0) {
            return false;
        }
        
        // Verificación específica por tipo de archivo
        String nombre = archivo.getName().toLowerCase();
        
        if (nombre.endsWith(".pdf")) {
            // Verificar si es un PDF válido
            try (RandomAccessFile raf = new RandomAccessFile(archivo, "r")) {
                byte[] buffer = new byte[4];
                raf.read(buffer);
                return new String(buffer).equals("%PDF");
            }
        } else if (nombre.endsWith(".xlsx")) {
            // Verificación básica para Excel
            return archivo.length() > 100; // Tamaño mínimo razonable
        } else if (nombre.endsWith(".html")) {
            // Verificación básica para HTML
            try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
                String primeraLinea = reader.readLine();
                return primeraLinea != null && primeraLinea.trim().toLowerCase().startsWith("<!doctype html");
            }
        }
        
        return true; // Para otros tipos de archivo, asumir que son válidos
    } catch (Exception e) {
        return false;
    }
}
private boolean coincideConReporte(String nombreArchivo, String tipoReporte, String idReporte) {
    String nombreLower = nombreArchivo.toLowerCase();
    String tipoLower = tipoReporte.toLowerCase();
    String idLower = idReporte.toLowerCase();
    
    // Patrones que puede tener un nombre de reporte
    boolean patron1 = nombreLower.contains(tipoLower) && nombreLower.contains(idLower);
    boolean patron2 = nombreLower.startsWith(tipoLower) && nombreLower.contains(idLower);
    boolean patron3 = nombreLower.endsWith(idLower + ".pdf");
    
    // Formatos aceptados
    boolean formatoValido = nombreLower.endsWith(".pdf") || 
                          nombreLower.endsWith(".html") || 
                          nombreLower.endsWith(".xlsx");
    
    return (patron1 || patron2 || patron3) && formatoValido;
}

private void reimprimirReporteInventario(String idReporte) throws Exception {
    // Buscar el archivo del reporte en la estructura de carpetas
    File reporte = buscarArchivoReporte("INVENTARIO", idReporte);
    
    if (reporte == null || !reporte.exists()) {
        // Si no existe el archivo, generamos un nuevo reporte PDF con los datos actuales
        String filename = "reporte_inventario_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".pdf";
        exportarInventarioAPDF(
            inventarioo.obtenerTodosProductos(),
            inventarioo.obtenerTodosProductos().size(),
            inventarioo.buscarProductosNecesitanReposicion().size(),
            inventarioo.buscarProductosSinStock().size(),
            calcularStockPromedio(),
            obtenerDistribucionCategorias()
        );
        reporte = new File(filename);
    }
    
    // Abrir el archivo con el visor predeterminado
    if (Desktop.isDesktopSupported()) {
        Desktop.getDesktop().open(reporte);
    } else {
        throw new Exception("No se puede abrir el reporte automáticamente en este sistema");
    }
}

private double calcularStockPromedio() {
    List<Producto> productos = inventarioo.obtenerTodosProductos();
    return productos.stream()
        .mapToInt(Producto::getCantidadDisponible)
        .average()
        .orElse(0);
}

private Map<String, Integer> obtenerDistribucionCategorias() {
    return inventarioo.obtenerTodosProductos().stream()
        .collect(Collectors.groupingBy(
            Producto::getCategoria,
            Collectors.summingInt(p -> 1)
        ));
}
private void reimprimirReporteClientes(String idReporte) throws Exception {
    // Buscar el archivo del reporte en la estructura de carpetas
    File reporte = buscarArchivoReporte("CLIENTES", idReporte);
    
    if (reporte == null || !reporte.exists()) {
        // Si no existe el archivo, generamos un nuevo reporte PDF con los datos actuales
        String filename = "reporte_clientes_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".pdf";
        
        List<Cliente> clientes = clientee.obtenerTodos();
        int totalClientes = clientes.size();
        int activos = (int) clientes.stream().filter(c -> c.getFechaEliminacion() == null).count();
        
        Map<String, Integer> registrosPorMes = clientes.stream()
            .collect(Collectors.groupingBy(
                c -> new SimpleDateFormat("MMM").format(c.getFechaRegistro()),
                Collectors.summingInt(c -> 1)
            ));
        
        exportarClientesAPDF(clientes, totalClientes, activos, registrosPorMes);
        reporte = new File(filename);
    }
    
    // Abrir el archivo con el visor predeterminado
    if (Desktop.isDesktopSupported()) {
        Desktop.getDesktop().open(reporte);
    } else {
        throw new Exception("No se puede abrir el reporte automáticamente en este sistema");
    }
}

private void mostrarDocumentoEnVisor(File documento, String tipoDocumento, String tituloVentana) {
    JDialog dialogo = new JDialog(); // JDialog para una ventana modal flotante
    dialogo.setTitle(tituloVentana + " - " + documento.getName());
    dialogo.setModal(true); // Bloquea la interacción con la ventana principal hasta que se cierre este diálogo
    dialogo.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    dialogo.setPreferredSize(new Dimension(900, 700)); // Tamaño preferido

    JPanel panelContenido = new JPanel(new BorderLayout()); // Panel principal del diálogo
    JButton botonExportar = new JButton("Exportar / Guardar Como...");
    botonExportar.addActionListener(e -> {
        JFileChooser selectorArchivos = new JFileChooser();
        selectorArchivos.setDialogTitle("Guardar copia de " + documento.getName());
        selectorArchivos.setFileSelectionMode(JFileChooser.FILES_ONLY);
        selectorArchivos.setSelectedFile(new File(documento.getName()));

        int seleccionUsuario = selectorArchivos.showSaveDialog(dialogo);
        if (seleccionUsuario == JFileChooser.APPROVE_OPTION) {
            File archivoAGuardar = selectorArchivos.getSelectedFile();
            // Añadir extensión si el usuario no la puso
            String extensionDestino = "";
            if (tipoDocumento.equalsIgnoreCase("PDF")) {
                extensionDestino = ".pdf";
            } else if (tipoDocumento.equalsIgnoreCase("EXCEL")) {
                extensionDestino = ".xlsx"; // Asumiendo XLSX, puedes ajustar para .xls si es necesario
            }

            String nombreActual = archivoAGuardar.getName();
            if (!nombreActual.toLowerCase().endsWith(extensionDestino)) {
                archivoAGuardar = new File(archivoAGuardar.getAbsolutePath() + extensionDestino);
            }

            try {
                // Utilizar Files.copy para copiar el archivo
                Files.copy(documento.toPath(), archivoAGuardar.toPath(),
                                      StandardCopyOption.REPLACE_EXISTING);
                JOptionPane.showMessageDialog(dialogo, "Archivo guardado exitosamente en:\n" + archivoAGuardar.getAbsolutePath(), "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialogo, "Error al guardar el archivo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    });

    JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    panelBoton.add(botonExportar);
    panelContenido.add(panelBoton, BorderLayout.SOUTH);

    if (tipoDocumento.equalsIgnoreCase("PDF")) {
        PanelVisorPdf visorPdf = new PanelVisorPdf();
        visorPdf.cargarDocumento(documento.getAbsolutePath());
        panelContenido.add(visorPdf, BorderLayout.CENTER);
    } else if (tipoDocumento.equalsIgnoreCase("EXCEL")) {
        PanelVisorExcel visorExcel = new PanelVisorExcel();
        visorExcel.cargarDocumento(documento.getAbsolutePath());
        panelContenido.add(visorExcel, BorderLayout.CENTER);
        // El botón de exportar ya se maneja genéricamente arriba
    } else {
        JOptionPane.showMessageDialog(vista, "Tipo de documento no soportado para visualización interna.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    dialogo.setContentPane(panelContenido);
    dialogo.pack();
    dialogo.setLocationRelativeTo(vista); 
    dialogo.setVisible(true);
}

private void exportarInventarioAJSON(List<Producto> productos, int totalProductos, int bajoStock, 
        int sinStock, double stockPromedio, 
        Map<String, Integer> distribucionCategorias) throws Exception {
String filename = "reporte_inventario_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".json";

// Crear estructura de datos para el JSON
Map<String, Object> reporte = new LinkedHashMap<>();
reporte.put("tipo_reporte", "INVENTARIO");
reporte.put("fecha_generacion", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
reporte.put("total_productos", totalProductos);
reporte.put("productos_bajo_stock", bajoStock);
reporte.put("productos_sin_stock", sinStock);
reporte.put("stock_promedio", stockPromedio);
reporte.put("distribucion_categorias", distribucionCategorias);

// Lista de productos
List<Map<String, Object>> productosData = new ArrayList<>();
for (Producto producto : productos) {
Map<String, Object> productoMap = new LinkedHashMap<>();
productoMap.put("id", producto.getId());
productoMap.put("nombre", producto.getNombre());
productoMap.put("categoria", producto.getCategoria());
productoMap.put("stock_actual", producto.getCantidadDisponible());
productoMap.put("stock_minimo", producto.getStockMinimo());
productoMap.put("stock_maximo", producto.getStockMaximo());
productoMap.put("precio_venta", producto.getPrecioVenta());
productoMap.put("proveedor", producto.getProveedor());
productosData.add(productoMap);
}
reporte.put("productos", productosData);

// Escribir JSON
ObjectMapper mapper = new ObjectMapper();
mapper.enable(SerializationFeature.INDENT_OUTPUT);
mapper.writeValue(new File(filename), reporte);

abrirArchivoExportado(new File(filename), "JSON");
}

private void exportarInventarioAXML(List<Producto> productos, int totalProductos, int bajoStock, 
       int sinStock, double stockPromedio, 
       Map<String, Integer> distribucionCategorias) throws Exception {
String filename = "reporte_inventario_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".xml";

try (PrintWriter writer = new PrintWriter(new FileOutputStream(filename))) {
writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
writer.println("<reporte tipo=\"INVENTARIO\">");
writer.println("  <fechaGeneracion>" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "</fechaGeneracion>");
writer.println("  <estadisticas>");
writer.println("    <totalProductos>" + totalProductos + "</totalProductos>");
writer.println("    <productosBajoStock>" + bajoStock + "</productosBajoStock>");
writer.println("    <productosSinStock>" + sinStock + "</productosSinStock>");
writer.println("    <stockPromedio>" + stockPromedio + "</stockPromedio>");
writer.println("  </estadisticas>");

writer.println("  <distribucionCategorias>");
for (Map.Entry<String, Integer> entry : distribucionCategorias.entrySet()) {
writer.println("    <categoria nombre=\"" + entry.getKey() + "\" cantidad=\"" + entry.getValue() + "\"/>");
}
writer.println("  </distribucionCategorias>");

writer.println("  <productos>");
for (Producto producto : productos) {
writer.println("    <producto>");
writer.println("      <id>" + producto.getId() + "</id>");
writer.println("      <nombre>" + escapeXML(producto.getNombre()) + "</nombre>");
writer.println("      <categoria>" + escapeXML(producto.getCategoria()) + "</categoria>");
writer.println("      <stockActual>" + producto.getCantidadDisponible() + "</stockActual>");
writer.println("      <stockMinimo>" + producto.getStockMinimo() + "</stockMinimo>");
writer.println("      <stockMaximo>" + producto.getStockMaximo() + "</stockMaximo>");
writer.println("      <precioVenta>" + producto.getPrecioVenta() + "</precioVenta>");
writer.println("      <proveedor>" + escapeXML(producto.getProveedor()) + "</proveedor>");
writer.println("    </producto>");
}
writer.println("  </productos>");
writer.println("</reporte>");
}

abrirArchivoExportado(new File(filename), "XML");
}

private void exportarClientesAJSON(List<Cliente> clientes, int totalClientes, int activos, 
        Map<String, Integer> registrosPorMes) throws Exception {
String filename = "reporte_clientes_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".json";

// Crear estructura de datos para el JSON
Map<String, Object> reporte = new LinkedHashMap<>();
reporte.put("tipo_reporte", "CLIENTES");
reporte.put("fecha_generacion", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
reporte.put("total_clientes", totalClientes);
reporte.put("clientes_activos", activos);
reporte.put("clientes_inactivos", totalClientes - activos);
reporte.put("registros_por_mes", registrosPorMes);

// Lista de clientes
List<Map<String, Object>> clientesData = new ArrayList<>();
for (Cliente cliente : clientes) {
Map<String, Object> clienteMap = new LinkedHashMap<>();
clienteMap.put("id", cliente.getId());
clienteMap.put("nombre", cliente.getNombre());
clienteMap.put("telefono", cliente.getTelefono());
clienteMap.put("puntos", cliente.getPuntos());
clienteMap.put("ultima_compra", cliente.getUltimaCompra());
clienteMap.put("fecha_registro", cliente.getFechaRegistro());
clientesData.add(clienteMap);
}
reporte.put("clientes", clientesData);

// Escribir JSON
ObjectMapper mapper = new ObjectMapper();
mapper.enable(SerializationFeature.INDENT_OUTPUT);
mapper.writeValue(new File(filename), reporte);

abrirArchivoExportado(new File(filename), "JSON");
}

private void exportarClientesAXML(List<Cliente> clientes, int totalClientes, int activos, 
       Map<String, Integer> registrosPorMes) throws Exception {
String filename = "reporte_clientes_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".xml";

try (PrintWriter writer = new PrintWriter(new FileOutputStream(filename))) {
writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
writer.println("<reporte tipo=\"CLIENTES\">");
writer.println("  <fechaGeneracion>" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "</fechaGeneracion>");
writer.println("  <estadisticas>");
writer.println("    <totalClientes>" + totalClientes + "</totalClientes>");
writer.println("    <clientesActivos>" + activos + "</clientesActivos>");
writer.println("    <clientesInactivos>" + (totalClientes - activos) + "</clientesInactivos>");
writer.println("  </estadisticas>");

writer.println("  <registrosPorMes>");
for (Map.Entry<String, Integer> entry : registrosPorMes.entrySet()) {
writer.println("    <mes nombre=\"" + entry.getKey() + "\" cantidad=\"" + entry.getValue() + "\"/>");
}
writer.println("  </registrosPorMes>");

writer.println("  <clientes>");
for (Cliente cliente : clientes) {
writer.println("    <cliente>");
writer.println("      <id>" + cliente.getId() + "</id>");
writer.println("      <nombre>" + escapeXML(cliente.getNombre()) + "</nombre>");
writer.println("      <telefono>" + escapeXML(cliente.getTelefono()) + "</telefono>");
writer.println("      <puntos>" + cliente.getPuntos() + "</puntos>");
writer.println("      <ultimaCompra>" + cliente.getUltimaCompra() + "</ultimaCompra>");
writer.println("      <fechaRegistro>" + cliente.getFechaRegistro() + "</fechaRegistro>");
writer.println("    </cliente>");
}
writer.println("  </clientes>");
writer.println("</reporte>");
}

abrirArchivoExportado(new File(filename), "XML");
}

private void exportarProveedoresAJSON(List<Proveedor> proveedores, int totalProveedores, int visitadosEsteMes, 
        Map<String, Integer> visitasPorMes, 
        Map<String, Integer> distribucionProductos) throws Exception {
String filename = "reporte_proveedores_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".json";

// Crear estructura de datos para el JSON
Map<String, Object> reporte = new LinkedHashMap<>();
reporte.put("tipo_reporte", "PROVEEDORES");
reporte.put("fecha_generacion", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
reporte.put("total_proveedores", totalProveedores);
reporte.put("visitados_este_mes", visitadosEsteMes);
reporte.put("por_visitar_este_mes", totalProveedores - visitadosEsteMes);
reporte.put("visitas_por_mes", visitasPorMes);
reporte.put("distribucion_productos", distribucionProductos);

// Lista de proveedores
List<Map<String, Object>> proveedoresData = new ArrayList<>();
for (Proveedor proveedor : proveedores) {
Map<String, Object> proveedorMap = new LinkedHashMap<>();
proveedorMap.put("id", proveedor.getId());
proveedorMap.put("nombre", proveedor.getNombre());
proveedorMap.put("telefono", proveedor.getTelefono());
proveedorMap.put("direccion", proveedor.getDireccion());
proveedorMap.put("producto_suministrado", proveedor.getProductoSuministrado());
proveedorMap.put("ultima_visita", proveedor.getUltimaVisita());
proveedoresData.add(proveedorMap);
}
reporte.put("proveedores", proveedoresData);

// Escribir JSON
ObjectMapper mapper = new ObjectMapper();
mapper.enable(SerializationFeature.INDENT_OUTPUT);
mapper.writeValue(new File(filename), reporte);

abrirArchivoExportado(new File(filename), "JSON");
}

private void exportarProveedoresAXML(List<Proveedor> proveedores, int totalProveedores, int visitadosEsteMes, 
       Map<String, Integer> visitasPorMes, 
       Map<String, Integer> distribucionProductos) throws Exception {
String filename = "reporte_proveedores_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".xml";

try (PrintWriter writer = new PrintWriter(new FileOutputStream(filename))) {
writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
writer.println("<reporte tipo=\"PROVEEDORES\">");
writer.println("  <fechaGeneracion>" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "</fechaGeneracion>");
writer.println("  <estadisticas>");
writer.println("    <totalProveedores>" + totalProveedores + "</totalProveedores>");
writer.println("    <visitadosEsteMes>" + visitadosEsteMes + "</visitadosEsteMes>");
writer.println("    <porVisitarEsteMes>" + (totalProveedores - visitadosEsteMes) + "</porVisitarEsteMes>");
writer.println("  </estadisticas>");

writer.println("  <visitasPorMes>");
for (Map.Entry<String, Integer> entry : visitasPorMes.entrySet()) {
writer.println("    <mes nombre=\"" + entry.getKey() + "\" cantidad=\"" + entry.getValue() + "\"/>");
}
writer.println("  </visitasPorMes>");

writer.println("  <distribucionProductos>");
for (Map.Entry<String, Integer> entry : distribucionProductos.entrySet()) {
writer.println("    <producto nombre=\"" + escapeXML(entry.getKey()) + "\" cantidad=\"" + entry.getValue() + "\"/>");
}
writer.println("  </distribucionProductos>");

writer.println("  <proveedores>");
for (Proveedor proveedor : proveedores) {
writer.println("    <proveedor>");
writer.println("      <id>" + proveedor.getId() + "</id>");
writer.println("      <nombre>" + escapeXML(proveedor.getNombre()) + "</nombre>");
writer.println("      <telefono>" + escapeXML(proveedor.getTelefono()) + "</telefono>");
writer.println("      <direccion>" + escapeXML(proveedor.getDireccion()) + "</direccion>");
writer.println("      <productoSuministrado>" + escapeXML(proveedor.getProductoSuministrado()) + "</productoSuministrado>");
writer.println("      <ultimaVisita>" + proveedor.getUltimaVisita() + "</ultimaVisita>");
writer.println("    </proveedor>");
}
writer.println("  </proveedores>");
writer.println("</reporte>");
}

abrirArchivoExportado(new File(filename), "XML");
}
private String escapeXML(String input) {
    if (input == null) return "";
    return input.replace("&", "&amp;")
               .replace("<", "&lt;")
               .replace(">", "&gt;")
               .replace("\"", "&quot;")
               .replace("'", "&apos;");
}


}