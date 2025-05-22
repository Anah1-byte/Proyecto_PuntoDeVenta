
package Vista;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.printing.PDFPageable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterJob;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;

public class TicketVistaPrevia extends JDialog {
	private static final long serialVersionUID = 1L;
	private final File archivoPDF;

	public TicketVistaPrevia(Frame owner, File archivoPDF) {
		super(owner, "Vista Previa del Ticket", true);
		this.archivoPDF = archivoPDF;

		setSize(320, 500);
		setLocationRelativeTo(owner);
		setLayout(new BorderLayout());

		JScrollPane scrollPane = new JScrollPane();
		JPanel panelPaginas = new JPanel();
		panelPaginas.setLayout(new BoxLayout(panelPaginas, BoxLayout.Y_AXIS));
		scrollPane.setViewportView(panelPaginas);

		try {
			PDDocument documento = PDDocument.load(archivoPDF);
			PDFRenderer renderer = new PDFRenderer(documento);
			int totalPaginas = documento.getNumberOfPages();

			for (int i = 0; i < totalPaginas; i++) {
				Image image = renderer.renderImageWithDPI(i, 72);
				ImageIcon icon = new ImageIcon(image);
				JLabel label = new JLabel(icon);
				panelPaginas.add(label);
				panelPaginas.add(Box.createRigidArea(new Dimension(0, 10)));
			}

			documento.close();
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(this, "No se pudo cargar el PDF: " + ex.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}

		JButton imprimirBtn = new JButton("Imprimir");
		imprimirBtn.addActionListener(e -> imprimirPDF());

		JButton cerrarBtn = new JButton("Cerrar");
		cerrarBtn.addActionListener(this::dispose);

		JPanel panelBotones = new JPanel();
		panelBotones.add(imprimirBtn);
		panelBotones.add(cerrarBtn);

		add(scrollPane, BorderLayout.CENTER);
		add(panelBotones, BorderLayout.SOUTH);
	}

	private void imprimirPDF() {
		try {
			PDDocument documento = PDDocument.load(archivoPDF);
			PrinterJob job = PrinterJob.getPrinterJob();
			job.setPageable(new PDFPageable(documento));

			if (job.printDialog()) {
				job.print(); // Imprime
				JOptionPane.showMessageDialog(this,
						"Ticket generado: " + archivoPDF.getName() + " en la carpeta PedidosPDF", "Impresión Exitosa",
						JOptionPane.INFORMATION_MESSAGE);
			}

			documento.close();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Error al imprimir el ticket: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		} catch (PrinterException e) {
			JOptionPane.showMessageDialog(this, "Error al imprimir el ticket: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void dispose(ActionEvent actionevent1) {
	}
}
