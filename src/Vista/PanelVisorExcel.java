package Vista; 

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class PanelVisorExcel extends JPanel {

    private JTable tablaExcel;
    private DefaultTableModel modeloTabla;
    private String rutaArchivoActual;

    public PanelVisorExcel() {
        super(new BorderLayout());
        inicializarUI();
    }

    private void inicializarUI() {
        modeloTabla = new DefaultTableModel();
        tablaExcel = new JTable(modeloTabla);
        tablaExcel.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JScrollPane panelDesplazamiento = new JScrollPane(tablaExcel);
        this.add(panelDesplazamiento, BorderLayout.CENTER);
    }

    public void cargarDocumento(String rutaArchivo) {
        if (rutaArchivo == null || rutaArchivo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "La ruta del archivo Excel no puede estar vacía.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File archivoExcel = new File(rutaArchivo);
        if (!archivoExcel.exists() || !archivoExcel.isFile() ||
            (!archivoExcel.getName().toLowerCase().endsWith(".xlsx") && !archivoExcel.getName().toLowerCase().endsWith(".xls"))) {
            JOptionPane.showMessageDialog(this, "El archivo Excel no existe o no es válido: " + rutaArchivo, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Limpiar el modelo actual antes de cargar nuevos datos
        modeloTabla.setRowCount(0);
        modeloTabla.setColumnCount(0);

        try (FileInputStream fis = new FileInputStream(archivoExcel);
             Workbook libroTrabajo = new XSSFWorkbook(fis)) { // Asume .xlsx, puedes añadir lógica para .xls con HSSFWorkbook

            Sheet hoja = libroTrabajo.getSheetAt(0); // Obtener la primera hoja

            // Leer encabezados (primera fila)
            Row filaEncabezado = hoja.getRow(0);
            if (filaEncabezado != null) {
                for (Cell celda : filaEncabezado) {
                    modeloTabla.addColumn(obtenerValorCeldaComoCadena(celda));
                }
            } else {
                 // Si no hay encabezados en la primera fila, creamos columnas por defecto
                int maximoColumnas = 0;
                for (Row fila : hoja) {
                    maximoColumnas = Math.max(maximoColumnas, fila.getLastCellNum());
                }
                for (int i = 0; i < maximoColumnas; i++) {
                    modeloTabla.addColumn("Columna " + (i + 1));
                }
            }

            // Leer datos
            for (int r = (filaEncabezado != null ? 1 : 0); r <= hoja.getLastRowNum(); r++) {
                Row fila = hoja.getRow(r);
                if (fila != null) {
                    Object[] datosFila = new Object[modeloTabla.getColumnCount()];
                    for (int c = 0; c < modeloTabla.getColumnCount(); c++) {
                        Cell celda = fila.getCell(c);
                        datosFila[c] = obtenerValorCeldaComoCadena(celda);
                    }
                    modeloTabla.addRow(datosFila);
                }
            }
            this.rutaArchivoActual = rutaArchivo;

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar el documento Excel: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String obtenerValorCeldaComoCadena(Cell celda) {
        if (celda == null) {
            return "";
        }
        return switch (celda.getCellType()) {
            case STRING -> celda.getStringCellValue();
            case NUMERIC -> String.valueOf(celda.getNumericCellValue());
            case BOOLEAN -> String.valueOf(celda.getBooleanCellValue());
            case FORMULA -> {
                // Intenta evaluar la fórmula, si no, devuelve la fórmula como string
                try {
                    DataFormatter formateadorDatos = new DataFormatter();
                    yield formateadorDatos.formatCellValue(celda);
                } catch (Exception e) {
                    yield celda.getCellFormula();
                }
            }
            case BLANK -> "";
            default -> celda.toString();
        };
    }

    public String obtenerRutaArchivoActual() {
        return rutaArchivoActual;
    }

    public void guardarComo() {
        if (rutaArchivoActual == null || rutaArchivoActual.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay ningún documento Excel cargado para guardar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser selectorArchivos = new JFileChooser();
        selectorArchivos.setDialogTitle("Guardar como...");
        selectorArchivos.setFileSelectionMode(JFileChooser.FILES_ONLY);
        selectorArchivos.setSelectedFile(new File(new File(rutaArchivoActual).getName())); // Sugerir el nombre original

        int seleccionUsuario = selectorArchivos.showSaveDialog(this);

        if (seleccionUsuario == JFileChooser.APPROVE_OPTION) {
            File archivoAGuardar = selectorArchivos.getSelectedFile();
            // Asegúrate de que tenga la extensión correcta
            String extension = "";
            String nombre = archivoAGuardar.getName();
            int i = nombre.lastIndexOf('.');
            if (i > 0) {
                extension = nombre.substring(i + 1);
            }
            if (!extension.equalsIgnoreCase("xlsx") && !extension.equalsIgnoreCase("xls")) {
                archivoAGuardar = new File(archivoAGuardar.getAbsolutePath() + ".xlsx"); // Asumiendo que generas .xlsx
            }

            try {
                java.nio.file.Files.copy(new File(rutaArchivoActual).toPath(), archivoAGuardar.toPath(),
                                          java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                JOptionPane.showMessageDialog(this, "Archivo guardado exitosamente en: " + archivoAGuardar.getAbsolutePath(), "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al guardar el archivo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}