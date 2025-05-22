package Controlador;

import Modelo.Proveedor;
import Modelo.Proveedorr;
import Vista.proveedores;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.table.DefaultTableModel;

public class ProveedoresContro {
    private final Proveedorr proveedorr;
    private final proveedores vista;
    private final DefaultTableModel modeloTabla;

    public ProveedoresContro(Proveedorr proveedorr, proveedores vista, DefaultTableModel modeloTabla) {
        this.proveedorr = proveedorr;
        this.vista = vista;
        this.modeloTabla = modeloTabla;
    }

    public boolean agregarProveedor(Proveedor nuevoProveedor) {
        if (proveedorr.agregarProveedor(nuevoProveedor)) {
            actualizarTablaProveedores();
            vista.mostrarMensaje("Proveedor registrado con ID: " + nuevoProveedor.getId(), 1);
            return true;
        }
        vista.mostrarMensaje("Error al registrar proveedor", 3);
        return false;
    }

    public boolean editarProveedor(Proveedor proveedor) {
        if (proveedorr.actualizarProveedor(proveedor)) {
            actualizarTablaProveedores();
            vista.mostrarMensaje("Proveedor actualizado exitosamente", 1);
            return true;
        }
        vista.mostrarMensaje("Error al actualizar proveedor", 3);
        return false;
    }

    public boolean eliminarProveedor(String id) {
        if (proveedorr.eliminarProveedor(id)) {
            actualizarTablaProveedores();
            vista.mostrarMensaje("Proveedor eliminado exitosamente", 1);
            return true;
        }
        vista.mostrarMensaje("Error al eliminar proveedor", 3);
        return false;
    }

    public Proveedor buscarProveedorPorId(String id) {
        return proveedorr.buscarProveedorPorId(id);
    }

    public void actualizarTablaProveedores() {
        try {
            DefaultTableModel model = (DefaultTableModel) vista.getTablaProveedores().getModel();
            model.setRowCount(0); // Limpiar tabla
            
            List<Proveedor> proveedores = proveedorr.obtenerTodosProveedores();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            
            for (Proveedor p : proveedores) {
                model.addRow(new Object[]{
                    p.getId(),
                    p.getNombre(),
                    p.getTelefono(),
                    p.getDireccion(),
                    p.getProductoSuministrado(),
                    p.getUltimaVisita() != null ? sdf.format(p.getUltimaVisita()) : ""
                });
            }
            
            // Notificar cambios y actualizar visualización
            model.fireTableDataChanged();
            vista.getTablaProveedores().repaint();
            
        } catch (Exception e) {
            vista.mostrarMensaje("Error al actualizar tabla: " + e.getMessage(), 3);
            e.printStackTrace();
        }
    }

    public void registrarVisitaProveedor(String idProveedor) {
        Proveedor proveedor = proveedorr.buscarProveedorPorId(idProveedor);
        if (proveedor != null) {
            proveedor.setUltimaVisita(new Timestamp(System.currentTimeMillis()));
            if (proveedorr.actualizarProveedor(proveedor)) {
                actualizarTablaProveedores();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                vista.mostrarMensaje("Visita registrada para " + proveedor.getNombre() + " - " + 
                    sdf.format(proveedor.getUltimaVisita()), 1);
            } else {
                vista.mostrarMensaje("Error al registrar visita", 3);
            }
        }
    }

}