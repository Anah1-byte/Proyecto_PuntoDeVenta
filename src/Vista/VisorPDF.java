package Vista;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPrintable;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;

public class VisorPDF extends JFrame {

    private PDDocument document;
    private PDFRenderer renderer;
    private JLabel pdfLabel;
    private int currentPage = 0;
    private double zoomFactor = 1.0;
    private final double ZOOM_STEP = 0.25;
    private final double MIN_ZOOM = 0.5;
    private final double MAX_ZOOM = 3.0;

    public VisorPDF(File archivo) {
        try {
            document = PDDocument.load(archivo);
            renderer = new PDFRenderer(document);

            setTitle("Visor de PDF - " + archivo.getName());
            setSize(900, 700);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setLayout(new BorderLayout());

            // Panel central con la imagen del PDF
            pdfLabel = new JLabel("", SwingConstants.CENTER);
            JScrollPane scrollPane = new JScrollPane(pdfLabel);
            add(scrollPane, BorderLayout.CENTER);

            // Panel de botones
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            
            // Botón Anterior
            JButton btnAnterior = new JButton("Anterior");
            btnAnterior.addActionListener(e -> {
                if (currentPage > 0) {
                    currentPage--;
                    mostrarPagina(currentPage);
                }
            });
            
            // Botón Siguiente
            JButton btnSiguiente = new JButton("Siguiente");
            btnSiguiente.addActionListener(e -> {
                if (currentPage < document.getNumberOfPages() - 1) {
                    currentPage++;
                    mostrarPagina(currentPage);
                }
            });
            
            // Botón Zoom +
            JButton btnZoomIn = new JButton("Acercar (+)");
            btnZoomIn.addActionListener(e -> {
                zoomFactor = Math.min(zoomFactor + ZOOM_STEP, MAX_ZOOM);
                mostrarPagina(currentPage);
            });
            
            // Botón Zoom -
            JButton btnZoomOut = new JButton("Alejar (-)");
            btnZoomOut.addActionListener(e -> {
                zoomFactor = Math.max(zoomFactor - ZOOM_STEP, MIN_ZOOM);
                mostrarPagina(currentPage);
            });
            
            // Botón Imprimir
            JButton btnImprimir = new JButton("Imprimir");
            btnImprimir.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    imprimirDocumento();
                }
            });
            
            // Botón Tamaño Original
            JButton btnResetZoom = new JButton("Tamaño Original");
            btnResetZoom.addActionListener(e -> {
                zoomFactor = 1.0;
                mostrarPagina(currentPage);
            });

            // Agregar botones al panel
            buttonPanel.add(btnAnterior);
            buttonPanel.add(btnSiguiente);
            buttonPanel.add(btnZoomIn);
            buttonPanel.add(btnZoomOut);
            buttonPanel.add(btnResetZoom);
            buttonPanel.add(btnImprimir);
            
            add(buttonPanel, BorderLayout.SOUTH);

            // Mostrar la primera página
            mostrarPagina(currentPage);
            setVisible(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar PDF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }

    private void mostrarPagina(int pagina) {
        try {
            // Renderizar la página con DPI ajustado según el zoom
            double dpi = 150 * zoomFactor;
            BufferedImage image = renderer.renderImageWithDPI(pagina, (float)dpi);
            
            // Actualizar el título con la página actual y el zoom
            setTitle(String.format("Visor de PDF (Página %d de %d, Zoom: %.0f%%)", 
                    pagina + 1, document.getNumberOfPages(), zoomFactor * 100));
            
            // Mostrar la imagen en el JLabel
            pdfLabel.setIcon(new ImageIcon(image));
            pdfLabel.revalidate();
            pdfLabel.repaint();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al mostrar página: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void imprimirDocumento() {
        if (document == null) return;
        
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Imprimir PDF");
        
        // Configurar las opciones de impresión
        if (job.printDialog()) {
            try {
                // Usar PDFPrintable para imprimir el documento con calidad
                job.setPrintable(new PDFPrintable(document));
                job.print();
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error al imprimir: " + e.getMessage(), 
                    "Error de impresión", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void dispose() {
        try {
            if (document != null) {
                document.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.dispose();
    }

    public static void mostrar(File rutaPDF) {
        SwingUtilities.invokeLater(() -> new VisorPDF(rutaPDF));
    }
}