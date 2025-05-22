package Controlador;

import Modelo.Carrito;
import Modelo.Cliente;
import Modelo.Producto;
import Modelo.Reportes;
import Modelo.Usuario;
import Modelo.Clientee;
import Modelo.ClienteImpl;
import Modelo.Inventarioo;
import javax.swing.*;
import javax.swing.JOptionPane;
import Modelo.Venta;
import Vista.TicketVistaPrevia;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Phrase;
import ConexionBD.ConexionAccess;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Window;

public class VentaContro {
    private Carrito carrito;
    private Inventarioo inventarioDAO = new Inventarioo();
    private Clientee clienteDAO = new ClienteImpl();
    private Usuario usuarioActual; // Usuario actual
	private PdfPTable totalesTable;
    
    public VentaContro(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }
        this.carrito = new Carrito();
        this.usuarioActual = usuario;
        this.inventarioDAO = new Inventarioo();
        this.clienteDAO = new ClienteImpl();
        
    }


    public void agregarProducto(String id, String nombre, double precio, int cantidad) {
        carrito.agregarProducto(id, nombre, precio, cantidad);
    }

    public void cancelarVenta() {
        carrito.cancelarVenta();
    }

    public double getTotalVenta() {
        return carrito.getTotalVenta();
    }

    public List<Producto> getProductosEnCarrito() {
        return carrito.getCarrito();
    }

    public boolean procesarPago(String metodoPago, String montoTexto, String telefono) throws Exception {
        // Validaciones iniciales

        if (metodoPago == null || metodoPago.trim().isEmpty()) {
            throw new Exception("Seleccione un método de pago");
        }

        // Corregir posible typo en "tarjeta"
        metodoPago = metodoPago.toUpperCase().replace("TARGETA", "TARJETA");

        // Procesar monto
        double montoRecibido;
        double total = carrito.getTotalVenta();
        double descuento = calcularDescuento();
        double totalConDescuento = total - descuento;
        double descuentoPorPuntos = 0;

        try {
            montoRecibido = Double.parseDouble(montoTexto);
        } catch (NumberFormatException e) {
            throw new Exception("Monto inválido. Ingrese una cantidad numérica válida");
        }

        // ============ PROCESAR CLIENTE Y PUNTOS ============
        Cliente cliente = null;
        if (!telefono.trim().isEmpty()) {
            cliente = clienteDAO.buscarPorTelefono(telefono);
            
            if (cliente != null) {
            	int maxPuntosUsables = (int) (totalConDescuento * 5);
            	int puntosParaUsar = Math.min(maxPuntosUsables,  cliente.getPuntos());
            	double maxDescuento = puntosParaUsar / 5.0;
            	
            	int opcion = JOptionPane.showConfirmDialog(null,
            			"Cliente: "	+ cliente.getNombre() + "\n" +
            	"Puntos disponibles: " + cliente.getPuntos() + "(" +(cliente.getPuntos() / 5) +
            	"$ de descuento)\n" +
            	"¿Desea aplicar el descuento por puntos?", "Descuento por puntos", JOptionPane.YES_NO_OPTION);
               
            	if (opcion == JOptionPane.YES_OPTION) {
            		descuentoPorPuntos = maxDescuento;
            		totalConDescuento -= descuentoPorPuntos;
            	
            		cliente.setPuntos(cliente.getPuntos() - puntosParaUsar);
                	clienteDAO.actualizarCliente(cliente);
                	
                	if(metodoPago.equals("EFECTIVO") && montoRecibido > totalConDescuento) {
                		montoRecibido = totalConDescuento;
                	}
            	}

            }
            int puntosGanados = (int) (totalConDescuento / 20);
            cliente.setPuntos(cliente.getPuntos()+ puntosGanados);
            clienteDAO.actualizarCliente(cliente);
        }
        // Validaciones por método de pago
        if (metodoPago.equals("EFECTIVO")) {
            if (montoRecibido < totalConDescuento) {
                double faltante = totalConDescuento - montoRecibido;
                throw new Exception(String.format("Monto insuficiente. Faltan $%,.2f", faltante));
            }
        } else if (metodoPago.equals("TARJETA")) {
            double diferencia = Math.abs(montoRecibido - totalConDescuento);
            if (diferencia > 0.50) { // Permitir hasta 50 centavos de diferencia
                throw new Exception(String.format(
                    "Para pagos con tarjeta, el monto debe ser aproximadamente $%,.2f", 
                    totalConDescuento
                ));
            }
            montoRecibido = totalConDescuento; // Usar el monto exacto
        } else {
            throw new Exception("Método de pago no válido");
        }

        // ============ VERIFICACIÓN DE STOCK ============
        Connection connStock = null;
        try {
            connStock = ConexionAccess.conectar();
            connStock.setAutoCommit(false);
            
            // Verificar stock para todos los productos
            String sqlVerificar = "SELECT p.id, p.nombre, p.cantidad_disponible " +
                                "FROM Productos p " +
                                "WHERE p.id = ?";
            
            try (PreparedStatement pstmt = connStock.prepareStatement(sqlVerificar)) {
                for (Producto producto : carrito.getProductos()) {
                    pstmt.setString(1, producto.getId());
                    ResultSet rs = pstmt.executeQuery();
                    
                    if (!rs.next()) {
                        throw new Exception("Producto no encontrado: " + producto.getNombre());
                    }
                    
                    int stockDisponible = rs.getInt("cantidad_disponible");
                    if (stockDisponible < producto.getCantidad()) {
                        throw new Exception("Stock insuficiente para " + rs.getString("nombre") + 
                                         ". Disponible: " + stockDisponible + 
                                         ", Solicitado: " + producto.getCantidad());
                    }
                }
            }
            connStock.commit();
        } catch (SQLException e) {
            if (connStock != null) {
                try { connStock.rollback(); } catch (SQLException ex) {}
            }
            throw new Exception("Error al verificar disponibilidad: " + e.getMessage());
        } finally {
            if (connStock != null) {
                try { connStock.close(); } catch (SQLException e) {}
            }
        }

        // ============ CREAR OBJETO VENTA CON TODOS LOS DATOS ============
        Venta venta = new Venta();
        venta.setFecha(new Date());
        venta.setTotal(totalConDescuento);
        venta.setMetodoPago(metodoPago);
        venta.setMontoRecibido(montoRecibido); // Asegurar que tiene el monto correcto
        venta.setDescuento(descuento + descuentoPorPuntos);
        venta.setProductos(new ArrayList<>(carrito.getProductos()));
        
		if (cliente != null) {
			venta.setCliente(cliente);
		}        // Procesar puntos del cliente

        // Registrar en BD
        try {
            registrarVentaEnBD(venta);
            generarTicketVenta(venta);
            carrito.cancelarVenta();
            return true;
        } catch (SQLException e) {
        	if (cliente != null && descuentoPorPuntos > 0) {
        		cliente.setPuntos(cliente.getPuntos()+ (int) (descuentoPorPuntos * 5));
        		clienteDAO.actualizarCliente(cliente);
        	}
            throw new Exception("Error al registrar la venta: " + e.getMessage());
        }
    }

    // Método auxiliar para calcular descuentos
    private double calcularDescuento() {
        double descuento = 0;
        
        // Ejemplo: 10% de descuento para compras mayores a $1000
        double total = carrito.getTotalVenta();
        if (total > 1000) {
            descuento = total * 0.10;
        }
        
        return descuento;
    }
    
    public void generarTicketVenta(Venta venta) {
        try {
            // Crear la carpeta tickets si no existe
            File carpetaTickets = new File("tickets");
            if (!carpetaTickets.exists()) {
                boolean carpetaCreada = carpetaTickets.mkdirs();
                if (!carpetaCreada) {
                    throw new IOException("No se pudo crear la carpeta 'tickets'");
                }
            }
            
            // Configurar nombre de archivo
            String filename = "tickets/ticket_" + System.currentTimeMillis() + ".pdf";
            File pdfFile = new File(filename);
            pdfFile.getParentFile().mkdirs();
            
            // Configuración del documento
            Document document = new Document(new Rectangle(226f, 800f), 5, 5, 5, 5);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.open();
            
            // Fuentes personalizadas
            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
            Font fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
            Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.BLACK);
            Font fontNegrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, BaseColor.BLACK);
            Font fontImporte = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.BLACK);
            Font fontPequeno = FontFactory.getFont(FontFactory.HELVETICA, 7, BaseColor.BLACK);
            
            // Logo del negocio (opcional)
            try {
                Image logo = Image.getInstance("imagen/logo.png");
                logo.scaleToFit(100, 60);
                logo.setAlignment(Element.ALIGN_CENTER);
                document.add(logo);
            } catch (Exception e) {
                System.out.println("Logo no encontrado, continuando sin él");
            }
            
            // Encabezado
            Paragraph titulo = new Paragraph("EL HABANERITO", fontTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);
            
            Paragraph subtitulo = new Paragraph("Ticket de Venta #" + venta.getId(), fontSubtitulo);
            subtitulo.setAlignment(Element.ALIGN_CENTER);
            document.add(subtitulo);
            
            // Información de la venta
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{1, 2});
            
            addCell(infoTable, "Fecha y Hora:", fontNegrita);
            addCell(infoTable, new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(venta.getFecha()), fontNormal);
            
            addCell(infoTable, "Atendió:", fontNegrita);
            addCell(infoTable, usuarioActual.getUsername(), fontNormal);
            
            // Información del cliente y puntos (si existe)
            if (venta.getCliente() != null) {
                addCell(infoTable, "Cliente:", fontNegrita);
                addCell(infoTable, venta.getCliente().getNombre(), fontNormal);
                
                addCell(infoTable, "Teléfono:", fontNegrita);
                addCell(infoTable, venta.getCliente().getTelefono(), fontNormal);
                
                // Calcular puntos antes de la compra (puntos actuales + puntos ganados en esta compra)
                int puntosGanados = (int)(venta.getTotal() / 20);
                int puntosAntes = venta.getCliente().getPuntos() - puntosGanados;
                
                // Calcular puntos usados (si aplica)
                double descuentoNormal = carrito.getTotalVenta() - (venta.getTotal() + venta.getDescuento());
                double descuentoPuntos = venta.getDescuento() - descuentoNormal;
                
                addCell(infoTable, "Puntos antes:", fontNegrita);
                addCell(infoTable, String.valueOf(puntosAntes), fontNormal);
                
                if (descuentoPuntos > 0) {
                    addCell(infoTable, "Puntos usados:", fontNegrita);
                    addCell(infoTable, String.valueOf((int)(descuentoPuntos * 5)), fontNormal);
                }
                
                addCell(infoTable, "Puntos ganados:", fontNegrita);
                addCell(infoTable, String.valueOf(puntosGanados), fontNormal);
                
                addCell(infoTable, "Puntos actuales:", fontNegrita);
                addCell(infoTable, String.valueOf(venta.getCliente().getPuntos()), fontNormal);
            }
            
            document.add(infoTable);
            document.add(Chunk.NEWLINE);
            
            // Tabla de productos
            PdfPTable productosTable = new PdfPTable(5);
            productosTable.setWidthPercentage(100);
            productosTable.setWidths(new float[]{1, 3, 1, 1, 1}); 
            
            // Encabezados de productos
            addCell(productosTable, "CÓDIGO", fontNegrita);
            addCell(productosTable, "NOMBRE", fontNegrita);
            addCell(productosTable, "CANT.", fontNegrita);
            addCell(productosTable, "P.UNIT.", fontNegrita);
            addCell(productosTable, "IMPORTE", fontNegrita);
            
            // Productos
            double subtotal = 0;
            for (Producto producto : venta.getProductos()) {
                addCell(productosTable, producto.getId(), fontNormal);
                addCell(productosTable, producto.getNombre(), fontNormal);
                addCell(productosTable, String.valueOf(producto.getCantidad()), fontNormal);
                addCell(productosTable, String.format("$%,.2f", producto.getPrecioUnitario()), fontNormal);
                addCell(productosTable, String.format("$%,.2f", producto.getPrecioUnitario() * producto.getCantidad()), fontNormal);
                subtotal += producto.getPrecioUnitario() * producto.getCantidad();
            }
            
            document.add(productosTable);
            document.add(Chunk.NEWLINE);
            
            // Totales
            PdfPTable totalesTable = new PdfPTable(2);
            totalesTable.setWidthPercentage(100);
            totalesTable.setWidths(new float[]{1, 1});
            
            addCell(totalesTable, "Subtotal:", fontNegrita);
            addCell(totalesTable, String.format("$%,.2f", subtotal), fontImporte);
            
            addCell(totalesTable, "IVA (16%):", fontNegrita);
            addCell(totalesTable, String.format("$%,.2f", venta.getTotal() - subtotal), fontImporte);
            
            // Mostrar descuento normal si existe
            double descuentoNormal = carrito.getTotalVenta() - (venta.getTotal() + venta.getDescuento());
            if (descuentoNormal > 0) {
                addCell(totalesTable, "Descuento:", fontNegrita);
                addCell(totalesTable, String.format("-$%,.2f", descuentoNormal), fontImporte);
            }
            
            // Mostrar descuento por puntos si existe
            double descuentoPuntos = venta.getDescuento() - descuentoNormal;
            if (descuentoPuntos > 0) {
                addCell(totalesTable, "Descuento por puntos:", fontNegrita);
                addCell(totalesTable, String.format("-$%,.2f", descuentoPuntos), fontImporte);
            }
            
            addCell(totalesTable, "Total:", fontNegrita);
            addCell(totalesTable, String.format("$%,.2f", venta.getTotal()), fontImporte);
            
            addCell(totalesTable, "Método de pago:", fontNegrita);
            addCell(totalesTable, venta.getMetodoPago(), fontImporte);
            
            // Sección de pago
            if (venta.getMetodoPago().equalsIgnoreCase("EFECTIVO")) {
                addCell(totalesTable, "Monto recibido:", fontNegrita);
                addCell(totalesTable, String.format("$%,.2f", venta.getMontoRecibido()), fontImporte);
                
                addCell(totalesTable, "Cambio:", fontNegrita);
                double cambio = venta.getMontoRecibido() - venta.getTotal();
                addCell(totalesTable, String.format("$%,.2f", cambio), fontImporte);
            } else if (venta.getMetodoPago().equalsIgnoreCase("TARJETA")) {
                addCell(totalesTable, "Forma de pago:", fontNegrita);
                addCell(totalesTable, "Tarjeta - Pago exacto", fontImporte);
            }
            
            document.add(totalesTable);
            document.add(Chunk.NEWLINE);
            
            // Mensaje de agradecimiento
            Paragraph gracias = new Paragraph("¡Gracias por su preferencia!", fontSubtitulo);
            gracias.setAlignment(Element.ALIGN_CENTER);
            document.add(gracias);
            
            // Datos del negocio
            Paragraph datosNegocio = new Paragraph(
                "Av. Principal 123, Centro\n" +
                "Tel: 555-123-4567\n" +
                "RFC: HAB123456ABC\n" +
                "Horario: L-V 9:00 a 20:00", 
                fontPequeno);
            datosNegocio.setAlignment(Element.ALIGN_CENTER);
            document.add(datosNegocio);
            
            document.close();
            
            // 4. Mostrar el visor de vista previa
            SwingUtilities.invokeLater(() -> {
                TicketVistaPrevia visor = new TicketVistaPrevia((JFrame)null, pdfFile);
                visor.setVisible(true);
            });
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, 
                "Error al generar ticket: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String generarNombreArchivoTicket(Venta venta) {
        // Usar rutas absolutas para mayor confiabilidad
        String basePath = new File("").getAbsolutePath();
        File ticketsDir = new File(basePath, "tickets");
        
        if (!ticketsDir.exists()) {
            ticketsDir.mkdirs();
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String fileName = "TICKET_" + venta.getId() + "_" + sdf.format(new Date()) + ".pdf";
        
        return new File(ticketsDir, fileName).getAbsolutePath();
    }

    // Método para reimprimir un ticket existente
    public void reimprimirTicket(String idVenta) throws Exception {
        // 1. Buscar el archivo PDF original
        File archivoTicket = buscarArchivoTicket(idVenta);
        
        if (archivoTicket != null && archivoTicket.exists()) {
            System.out.println("[DEBUG] Encontrado archivo existente: " + archivoTicket.getAbsolutePath());
         //   abrirArchivoPDF(archivoTicket.getAbsolutePath());
            return;
        }
        
        // 2. Si no existe, buscar en la BD y regenerar
        System.out.println("[DEBUG] No se encontró archivo, generando desde BD...");
        Venta venta = obtenerVentaDesdeBD(idVenta);
        
        if (venta == null) {
            throw new Exception("No se encontró la venta con ID: " + idVenta);
        }
        
        // 3. Generar el ticket y guardarlo
        generarTicketVenta(venta);
        
        // 4. Verificar que se creó correctamente
        File nuevoTicket = new File(generarNombreArchivoTicket(venta));
        if (!nuevoTicket.exists()) {
            throw new Exception("No se pudo generar el archivo del ticket");
        }
        
       // abrirArchivoPDF(nuevoTicket.getAbsolutePath());
    }

    private File buscarArchivoTicket(String idVenta) {
        // Buscar en la carpeta tickets con diferentes patrones por si cambió el formato
        File carpetaTickets = new File("tickets");
        
        if (!carpetaTickets.exists()) {
            return null;
        }
        
        // Patrones de búsqueda alternativos
        String[] patrones = {
            "TICKET_" + idVenta + "_*.pdf",
            "Ticket_" + idVenta + "_*.pdf",
            idVenta + "_*.pdf",
            "*" + idVenta + "*.pdf"
        };
        
        for (String patron : patrones) {
            File[] archivos = carpetaTickets.listFiles((dir, name) -> name.matches(patron.replace("*", ".*")));
            if (archivos != null && archivos.length > 0) {
                return archivos[0]; // Devuelve el primer archivo que coincida
            }
        }
        
        return null;
    }

    // Método auxiliar para agregar celdas a las tablas
    private void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(3);
        table.addCell(cell);
    }
    
    private Venta obtenerVentaDesdeBD(String idVenta) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmtVenta = null;
        PreparedStatement pstmtDetalles = null;
        ResultSet rsVenta = null;
        ResultSet rsDetalles = null;
        
        try {
            conn = ConexionAccess.conectar();
            
            // 1. Consulta mejorada para obtener más datos de la venta
            String sqlVenta = "SELECT v.*, u.username as nombre_usuario " +
                             "FROM Ventas v " +
                             "LEFT JOIN Usuarios u ON v.id_usuario = u.username " +
                             "WHERE v.id = ?";
            pstmtVenta = conn.prepareStatement(sqlVenta);
            pstmtVenta.setString(1, idVenta);
            rsVenta = pstmtVenta.executeQuery();
            
            if (!rsVenta.next()) {
                throw new SQLException("No se encontró la venta con ID: " + idVenta);
            }
            
            Venta venta = new Venta();
            venta.setId(rsVenta.getInt("id"));
            venta.setFecha(rsVenta.getTimestamp("fecha"));
            venta.setTotal(rsVenta.getDouble("total"));
            venta.setMetodoPago(rsVenta.getString("metodo_pago"));
            venta.setDescuento(rsVenta.getDouble("descuento"));
            venta.setMontoRecibido(rsVenta.getDouble("monto_recibido"));
            
            // Establecer el usuario que realizó la venta
            if (usuarioActual == null) {
                usuarioActual = new Usuario(sqlVenta, sqlVenta, sqlVenta);
                usuarioActual.setUsername(rsVenta.getString("nombre_usuario"));
            }
            
            // 2. Obtener cliente si existe
            String idCliente = rsVenta.getString("id_cliente");
            if (idCliente != null && !idCliente.trim().isEmpty()) {
                Cliente cliente = clienteDAO.buscarPorId(idCliente);
                venta.setCliente(cliente);
            }
            
            // 3. Consulta mejorada para obtener detalles
            String sqlDetalles = "SELECT dv.*, p.nombre, p.precio_venta " +
                               "FROM DetalleVenta dv " +
                               "JOIN Productos p ON dv.id_producto = p.id " +
                               "WHERE dv.id_venta = ?";
            pstmtDetalles = conn.prepareStatement(sqlDetalles);
            pstmtDetalles.setString(1, idVenta);
            rsDetalles = pstmtDetalles.executeQuery();
            
            List<Producto> productos = new ArrayList<>();
            while (rsDetalles.next()) {
                Producto producto = new Producto(usuarioActual);
                producto.setId(rsDetalles.getString("id_producto"));
                producto.setNombre(rsDetalles.getString("nombre"));
                producto.setPrecioUnitario(rsDetalles.getDouble("precio_venta"));
                producto.setCantidad(rsDetalles.getInt("cantidad"));
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

    private Venta obtenerVentaRecienRegistrada() {
        Connection conn = null;
        PreparedStatement pstmtVenta = null;
        PreparedStatement pstmtDetalle = null;
        ResultSet rsVenta = null;
        ResultSet rsDetalle = null;
        
        try {
            conn = ConexionAccess.conectar();
            
            // 1. Obtener la última venta registrada (la más reciente)
            String sqlVenta = "SELECT TOP 1 id, fecha, total, metodo_pago FROM Ventas ORDER BY id DESC";
            pstmtVenta = conn.prepareStatement(sqlVenta);
            rsVenta = pstmtVenta.executeQuery();
            
            if (!rsVenta.next()) {
                throw new SQLException("No se pudo obtener la venta recién registrada");
            }
            
            int idVenta = rsVenta.getInt("id");
            Date fecha = rsVenta.getDate("fecha");
            double total = rsVenta.getDouble("total");
            String metodoPago = rsVenta.getString("metodo_pago");
            
            // 2. Obtener los productos de esta venta
            String sqlDetalle = "SELECT p.id, p.nombre, p.precio_venta, dv.cantidad " +
                               "FROM DetalleVenta dv " +
                               "JOIN Productos p ON dv.id_producto = p.id " +
                               "WHERE dv.id_venta = ?";
            pstmtDetalle = conn.prepareStatement(sqlDetalle);
            pstmtDetalle.setInt(1, idVenta);
            rsDetalle = pstmtDetalle.executeQuery();
            
            List<Producto> productos = new ArrayList<>();
            while (rsDetalle.next()) {
                Producto producto = new Producto(
                    rsDetalle.getString("id"),
                    rsDetalle.getString("nombre"),
                    "", // descripción
                    "", // categoría
                    "", // proveedor
                    rsDetalle.getInt("cantidad"), // cantidad vendida
                    0,  // stock mínimo
                    0,  // stock máximo
                    0.0,// precio compra
                    rsDetalle.getDouble("precio_venta"),
                    false, // tiene IVA
                    0.0,   // descuento
                    null,   // fecha ingreso
                    "",     // estado
                    "",     // imagen path
                    ""      // unidad medida
                );
                producto.setCantidad(rsDetalle.getInt("cantidad"));
                productos.add(producto);
            }
            
            return new Venta(idVenta, fecha, total, metodoPago, productos);
            
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al obtener la venta recién registrada: " + e.getMessage());
        } finally {
            // Cerrar todos los recursos
            try {
                if (rsDetalle != null) rsDetalle.close();
                if (rsVenta != null) rsVenta.close();
                if (pstmtDetalle != null) pstmtDetalle.close();
                if (pstmtVenta != null) pstmtVenta.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void registrarVentaEnBD(Venta venta) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmtVenta = null;
        PreparedStatement pstmtDetalle = null;
        ResultSet generatedKeys = null;
        
        try {
            // 1. Establecer conexión
            conn = ConexionAccess.conectar();
            
            // Verificar modo de solo lectura
            if (conn.isReadOnly()) {
                throw new SQLException("La base de datos está en modo solo lectura");
            }
            
            // Iniciar transacción (Access soporta transacciones básicas)
            conn.setAutoCommit(false);
            
            // 2. Verificar stock disponible para todos los productos
            verificarStockDisponible(conn, venta.getProductos());
            
            // 3. Registrar la venta principal
            String sqlVenta = "INSERT INTO Ventas (fecha, total, metodo_pago, id_usuario, descuento, monto_recibido) " +
                             "VALUES (?, ?, ?, ?, ?, ?)";
            pstmtVenta = conn.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS);
            
            pstmtVenta.setTimestamp(1, new java.sql.Timestamp(venta.getFecha().getTime()));
            pstmtVenta.setDouble(2, venta.getTotal());
            pstmtVenta.setString(3, venta.getMetodoPago());
            pstmtVenta.setString(4, usuarioActual.getUsername());
            pstmtVenta.setDouble(5, venta.getDescuento());
            pstmtVenta.setDouble(6, venta.getMontoRecibido());
            
            int affectedRows = pstmtVenta.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("No se pudo registrar la venta principal");
            }
            
            // 4. Obtener el ID generado (Access usa un enfoque especial)
            generatedKeys = pstmtVenta.getGeneratedKeys();
            if (!generatedKeys.next()) {
                throw new SQLException("No se pudo obtener el ID de la venta generada");
            }
            int idVenta = generatedKeys.getInt(1);
            venta.setId(idVenta);
            
            // 5. Registrar detalles de venta
            String sqlDetalle = "INSERT INTO DetalleVenta (id_venta, id_producto, cantidad, precio_unitario, subtotal) " +
                              "VALUES (?, ?, ?, ?, ?)";
            pstmtDetalle = conn.prepareStatement(sqlDetalle);
            
            for (Producto producto : venta.getProductos()) {
                if (producto.getId() == null || producto.getId().trim().isEmpty()) {
                    throw new SQLException("ID de producto inválido: " + producto.getNombre());
                }
                
                pstmtDetalle.setInt(1, idVenta);
                pstmtDetalle.setString(2, producto.getId());
                pstmtDetalle.setInt(3, producto.getCantidad());
                pstmtDetalle.setDouble(4, producto.getPrecioUnitario());
                pstmtDetalle.setDouble(5, producto.getPrecioUnitario() * producto.getCantidad());
                pstmtDetalle.addBatch();
            }
            
            // Ejecutar todos los detalles en lote
            int[] resultadosDetalle = pstmtDetalle.executeBatch();
            for (int resultado : resultadosDetalle) {
                if (resultado == PreparedStatement.EXECUTE_FAILED) {
                    throw new SQLException("Error al registrar detalles de venta");
                }
            }
            
            // 6. Actualizar inventario
            actualizarInventario(conn, venta.getProductos());
            
            // 7. Confirmar transacción si todo fue exitoso
            conn.commit();
            
        } catch (SQLException e) {
            // Revertir transacción en caso de error
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    throw new SQLException("Error al revertir transacción: " + ex.getMessage(), e);
                }
            }
            throw new SQLException("Error al registrar la venta: " + e.getMessage(), e);
        } finally {
            // Cerrar recursos en orden inverso
            if (generatedKeys != null) {
                try { generatedKeys.close(); } catch (SQLException e) { /* ignorar */ }
            }
            if (pstmtDetalle != null) {
                try { pstmtDetalle.close(); } catch (SQLException e) { /* ignorar */ }
            }
            if (pstmtVenta != null) {
                try { pstmtVenta.close(); } catch (SQLException e) { /* ignorar */ }
            }
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Restaurar autocommit
                    conn.close();
                } catch (SQLException e) { /* ignorar */ }
            }
        }
    }

    private void verificarStockDisponible(Connection conn, List<Producto> productos) throws SQLException {
        // Consulta modificada para Access (eliminando WITH (ROWLOCK, UPDLOCK))
        String sql = "SELECT id, nombre, cantidad_disponible FROM Productos WHERE id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Producto producto : productos) {
                pstmt.setString(1, producto.getId());
                ResultSet rs = pstmt.executeQuery();
                
                if (!rs.next()) {
                    throw new SQLException("Producto no encontrado: " + producto.getId());
                }
                
                int stockDisponible = rs.getInt("cantidad_disponible");
                if (stockDisponible < producto.getCantidad()) {
                    throw new SQLException(String.format(
                        "Stock insuficiente para %s. Disponible: %d, Solicitado: %d",
                        rs.getString("nombre"),
                        stockDisponible,
                        producto.getCantidad()
                    ));
                }
            }
        }
    }
    private void actualizarInventario(Connection conn, List<Producto> productos) throws SQLException {
        String sql = "UPDATE Productos SET cantidad_disponible = cantidad_disponible - ? WHERE id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Producto producto : productos) {
                pstmt.setInt(1, producto.getCantidad());
                pstmt.setString(2, producto.getId());
                pstmt.addBatch();
            }
            
            int[] resultados = pstmt.executeBatch();
            for (int i = 0; i < resultados.length; i++) {
                if (resultados[i] == PreparedStatement.EXECUTE_FAILED) {
                    throw new SQLException("Error al actualizar inventario para producto: " + productos.get(i).getId());
                }
            }
        }
    }
    private void crearTablaVentasSiNoExiste(Connection conn) throws SQLException {
        if (!tablaExiste(conn, "Ventas")) {
            try (Statement stmt = conn.createStatement()) {
                // Crear tabla Ventas con estructura básica
                stmt.execute("CREATE TABLE Ventas (" +
                    "ID AUTOINCREMENT PRIMARY KEY, " +
                    "fecha DATETIME NOT NULL, " +
                    "total CURRENCY NOT NULL, " +
                    "descuento CURRENCY NOT NULL, " +
                    "metodo_pago TEXT(50) NOT NULL, " +
                    "id_usuario TEXT(50) NOT NULL)");
                
                // Crear tabla DetalleVenta relacionada
                stmt.execute("CREATE TABLE DetalleVenta (" +
                    "ID AUTOINCREMENT PRIMARY KEY, " +
                    "id_venta INTEGER NOT NULL, " +
                    "id_producto TEXT(50) NOT NULL, " +
                    "cantidad INTEGER NOT NULL, " +
                    "precio_unitario CURRENCY NOT NULL, " +
                    "subtotal CURRENCY NOT NULL)");
                
                // Crear relación entre tablas
                stmt.execute("ALTER TABLE DetalleVenta ADD CONSTRAINT FK_DetalleVenta_Ventas " +
                    "FOREIGN KEY (id_venta) REFERENCES Ventas (ID)");
                
                System.out.println("Tablas Ventas y DetalleVenta creadas exitosamente");
            }
        }
    }

    private boolean tablaExiste(Connection conn, String nombreTabla) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getTables(null, null, nombreTabla, new String[] {"TABLE"})) {
            return rs.next();
        }
    }
    private void procesarPuntosCliente(String telefono, double total) throws Exception {
        if (!telefono.trim().isEmpty()) {
            Cliente cliente = clienteDAO.buscarPorTelefono(telefono);
            int puntosGanados = (int)(total / 20); // 1 punto por cada $20
            
            if (cliente != null) {
                cliente.setPuntos(cliente.getPuntos() + puntosGanados);
                clienteDAO.actualizarCliente(cliente);
            }
        }
    }

    public String generarResumenVenta() {
        StringBuilder resumen = new StringBuilder();
        resumen.append("Productos:\n");
        
        for (Producto p : carrito.getProductos()) {
            resumen.append(String.format("- %s x%d: $%.2f\n", 
                p.getNombre(), p.getCantidad(), p.getPrecioUnitario()));
        }
        
        resumen.append(String.format("\nTotal: $%.2f", carrito.getTotalVenta()));
        return resumen.toString();
    }
    
    public void setTotalVenta(double total) {
        carrito.setTotalVenta(total);
    }


	public Carrito getCarrito() {
		return carrito;
    }
	
	private void verificarStockDisponible() throws Exception {
	    try (Connection conn = ConexionAccess.conectar()) {
	        conn.setAutoCommit(false);
	        
	        for (Producto producto : carrito.getProductos()) {
	            String sql = "SELECT cantidad_disponible FROM Productos WHERE id = ? WITH (ROWLOCK, UPDLOCK)";
	            
	            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
	                pstmt.setString(1, producto.getId());
	                ResultSet rs = pstmt.executeQuery();
	                
	                if (!rs.next()) {
	                    throw new Exception("Producto no encontrado: " + producto.getNombre());
	                }
	                
	                int stockDisponible = rs.getInt("cantidad_disponible");
	                if (stockDisponible < producto.getCantidad()) {
	                    throw new Exception("Stock insuficiente para el producto " + producto.getNombre() + 
	                                     ". Disponible: " + stockDisponible + 
	                                     ", Solicitado: " + producto.getCantidad());
	                }
	            }
	        }
	        conn.commit();
	    } catch (SQLException e) {
	        throw new Exception("Error al verificar stock: " + e.getMessage());
	    }
	}
	
	private boolean verificarYReservarStock(List<Producto> productos) {
	    Connection conn = null;
	    try {
	        conn = ConexionAccess.conectar();
	        conn.setAutoCommit(false);
	        
	        // Primero verificar
	        for (Producto p : productos) {
	            String sqlCheck = "SELECT cantidad_disponible FROM Productos WHERE id = ?";
	            try (PreparedStatement pstmt = conn.prepareStatement(sqlCheck)) {
	                pstmt.setString(1, p.getId());
	                ResultSet rs = pstmt.executeQuery();
	                if (!rs.next() || rs.getInt(1) < p.getCantidad()) {
	                    conn.rollback();
	                    return false;
	                }
	            }
	        }
	        
	        // Luego reservar (actualizar)
	        String sqlUpdate = "UPDATE Productos SET cantidad_disponible = cantidad_disponible - ? WHERE id = ?";
	        try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdate)) {
	            for (Producto p : productos) {
	                pstmt.setInt(1, p.getCantidad());
	                pstmt.setString(2, p.getId());
	                pstmt.addBatch();
	            }
	            int[] results = pstmt.executeBatch();
	            for (int r : results) {
	                if (r <= 0) {
	                    conn.rollback();
	                    return false;
	                }
	            }
	        }
	        
	        conn.commit();
	        return true;
	    } catch (SQLException e) {
	        if (conn != null) {
	            try { conn.rollback(); } catch (SQLException ex) {}
	        }
	        return false;
	    } finally {
	        if (conn != null) {
	            try { conn.close(); } catch (SQLException e) {}
	        }
	    }
	}
	
	public Producto eliminarProductoDelCarrito(String nombreProducto) throws SQLException {
	    // Buscar el producto en el carrito
	    Producto producto = getProductoEnCarrito(nombreProducto);
	    
	    if (producto != null) {
	        // 1. Eliminar del carrito
	        carrito.getProductos().remove(producto);
	        
	        // 2. Recalcular total
	        carrito.actualizarTotal();
	        
	        // 3. Devolver stock al inventario
	        inventarioDAO.actualizarStock(producto.getId(), producto.getCantidad());
	        
	        return producto;
	    }
	    return null;
	}
	
	public Producto getProductoEnCarrito(String nombre) {
	    if (nombre == null || nombre.trim().isEmpty()) {
	        return null;
	    }
	    
	    return carrito.getProductos().stream()
	        .filter(p -> p.getNombre().equalsIgnoreCase(nombre.trim()))
	        .findFirst()
	        .orElse(null);
	}
	}
    