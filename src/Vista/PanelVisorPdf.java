
package Vista;

import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;
import javax.swing.*;
import java.awt.*;
import java.io.File;

public class PanelVisorPdf extends JPanel {
	private static final long serialVersionUID = 1L;

	private SwingController controladorPdf;
	private SwingViewBuilder constructorVista;
	private JPanel panelComponenteVisor;
	private JScrollPane panelDesplazamiento;
	private String rutaArchivoActual;

	public PanelVisorPdf() {
		super(new BorderLayout());
		inicializarVisor();
	}

	private void inicializarVisor() {
		controladorPdf = new SwingController();
		constructorVista = new SwingViewBuilder(controladorPdf);

		panelComponenteVisor = constructorVista.buildViewerPanel();
		panelDesplazamiento = new JScrollPane(panelComponenteVisor);
		this.add(panelDesplazamiento, BorderLayout.CENTER);

		JToolBar barraHerramientasDocumento = constructorVista.buildAnnotationToolBar();
		this.add(barraHerramientasDocumento, BorderLayout.NORTH);

		JToolBar barraNavegacion = constructorVista.buildUtilityToolBar(true);
		this.add(barraNavegacion, BorderLayout.SOUTH);

		this.addAncestorListener(new javax.swing.event.AncestorListener() {
			@Override
			public void ancestorAdded(javax.swing.event.AncestorEvent event) {
			}

			@Override
			public void ancestorRemoved(javax.swing.event.AncestorEvent event) {
				if (controladorPdf != null) {
					controladorPdf.closeDocument();
					controladorPdf.dispose();
				}
			}

			@Override
			public void ancestorMoved(javax.swing.event.AncestorEvent event) {
			}
		});
	}

	public void cargarDocumento(String rutaArchivo) {
		if (rutaArchivo == null || rutaArchivo.isEmpty()) {
			JOptionPane.showMessageDialog(this, "La ruta del archivo PDF no puede estar vacía.", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		File archivoPdf = new File(rutaArchivo);
		if (!archivoPdf.exists() || !archivoPdf.isFile() || !archivoPdf.getName().toLowerCase().endsWith(".pdf")) {
			JOptionPane.showMessageDialog(this, "El archivo PDF no existe o no es válido: " + rutaArchivo, "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		if (controladorPdf.getDocument() != null) {
			controladorPdf.closeDocument();
		}

		try {
			controladorPdf.openDocument(rutaArchivo);
			this.rutaArchivoActual = rutaArchivo;
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error al cargar el documento PDF: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public void cerrarDocumento() {
		if (controladorPdf != null && controladorPdf.getDocument() != null) {
			controladorPdf.closeDocument();
			this.rutaArchivoActual = null;
		}
	}

	public String obtenerRutaArchivoActual() {
		return rutaArchivoActual;
	}
}
