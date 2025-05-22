package Controlador;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import Modelo.Venta;

public class PDFExporter implements PdfPageEvent {
    private Font footerFont;
    private ReportesControlador controlador;
    private static final String REPORTS_BASE_DIR = "Reportes";

    public PDFExporter(ReportesControlador controlador) {
        this.controlador = controlador;
        this.footerFont = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL);
    }

    // Método principal para exportar
    public void exportarAPDF(String filename, String tipoReporte, 
                           Date fechaInicio, Date fechaFin) throws DocumentException, IOException, SQLException {
        File file = new File(filename);
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));

        // Crear el directorio de reportes si no existe
        writer.setPageEvent(this);
        
        document.open();
        
        controlador.agregarEncabezadoReporte(document, tipoReporte, fechaInicio, fechaFin);
        
        if ("VENTAS".equals(tipoReporte)) {
            List<Venta> ventas = controlador.obtenerVentasDesdeBD(fechaInicio, fechaFin);
            controlador.agregarReporteVentas(document, ventas, writer);
            controlador.agregarGraficos(document, ventas, writer);
        } else {
            document.add(new Paragraph("Tipo de reporte no soportado para exportación"));
        }
        
        
        document.close();
        if (!file.exists() || file.length() == 0) {
            throw new IOException("No se pudo crear el archivo PDF");
        }
    }

    // Implementación de los métodos de PdfPageEvent
    @Override
    public void onOpenDocument(PdfWriter writer, Document document) {
        // No es necesario implementar
    }

    @Override
    public void onStartPage(PdfWriter writer, Document document) {
        // No es necesario implementar
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        try {
            agregarNumeroPagina(writer, document);
        } catch (DocumentException e) {
            System.err.println("Error al agregar número de página: " + e.getMessage());
        }
    }

    @Override
    public void onCloseDocument(PdfWriter writer, Document document) {
        // No es necesario implementar
    }

    @Override
    public void onParagraph(PdfWriter writer, Document document, float paragraphPosition) {
        // No es necesario implementar
    }

    @Override
    public void onParagraphEnd(PdfWriter writer, Document document, float paragraphPosition) {
        // No es necesario implementar
    }

    @Override
    public void onChapter(PdfWriter writer, Document document, float paragraphPosition, Paragraph title) {
        // No es necesario implementar
    }

    @Override
    public void onChapterEnd(PdfWriter writer, Document document, float paragraphPosition) {
        // No es necesario implementar
    }

    @Override
    public void onSection(PdfWriter writer, Document document, float paragraphPosition, int depth, Paragraph title) {
        // No es necesario implementar
    }

    @Override
    public void onSectionEnd(PdfWriter writer, Document document, float paragraphPosition) {
        // No es necesario implementar
    }

    @Override
    public void onGenericTag(PdfWriter writer, Document document, Rectangle rect, String text) {
        // No es necesario implementar
    }

    private void agregarNumeroPagina(PdfWriter writer, Document document) throws DocumentException {
        PdfContentByte cb = writer.getDirectContent();
        Phrase footer = new Phrase(
            String.format("Página %d", writer.getPageNumber()), 
            footerFont
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
}