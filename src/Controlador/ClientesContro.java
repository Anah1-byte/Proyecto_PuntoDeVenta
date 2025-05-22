package Controlador;

import Modelo.Cliente;
import Modelo.Clientee;
import Modelo.ClienteImpl;
import Vista.clientes;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class ClientesContro {
    private final Clientee modelo;
    private final clientes vista;
    private final Cliente cliente;
	private String telefono,  id;
	 private final DefaultTableModel modeloTabla;

	 public ClientesContro(Clientee modelo, clientes vista, Cliente cliente, DefaultTableModel modeloTabla) {
		    Objects.requireNonNull(modelo, "El modelo ClienteDAO no puede ser nulo");
		    Objects.requireNonNull(vista, "La vista no puede ser nula");
		    Objects.requireNonNull(modeloTabla, "El modelo de tabla no puede ser nulo");
		 this.modelo = modelo;
		    this.vista = vista;
		    this.cliente = cliente;
		    this.modeloTabla = modeloTabla;
		    
		    // Verificar y preparar la tabla al iniciar
		    if (modelo instanceof ClienteImpl) {
		        ((ClienteImpl)modelo).verificarYEstructurarTabla();
		    }
		    
		    inicializarControlador();
		}
	 
	private void inicializarControlador() {
        SwingUtilities.invokeLater(() -> {
            configurarListeners();
            cargarClientesIniciales();
        });
    }
	
    private void configurarListeners() {
        vista.getBtnAgregar().addActionListener(e -> agregarCliente());
        vista.getBtnEditar().addActionListener(e -> editarCliente());
        vista.getBtnEliminar().addActionListener(e -> eliminarCliente());
    }
    
    private void cargarClientesIniciales() {
        try {
            List<Cliente> clientes = modelo.obtenerTodos();
            // Actualizar la vista con los clientes...
        } catch (Exception e) {
            mostrarError("Error al cargar clientes: " + e.getMessage());
        }
    }
    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(vista, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
    private void cargarClientes() {
        modeloTabla.setRowCount(0); // Limpiar tabla ANTES de cargar
        List<Cliente> clientes = modelo.obtenerTodos();
        for (Cliente cliente : clientes) {
            modeloTabla.addRow(new Object[]{
                cliente.getId(),
                cliente.getTelefono(),
                cliente.getNombre(),
                cliente.getUltimaCompra(),
                cliente.getPuntos()
            });
        }
    }
   
    public void agregarCliente() {
        Cliente nuevoCliente = new Cliente();
        
        if (!vista.mostrarFormularioCliente(nuevoCliente)) {
            return; // Si se canceló
        }

        try {
            // Generar ID único para el nuevo cliente
            nuevoCliente.setId(generarNuevoId());
            
            // Establecer fecha de registro
            nuevoCliente.setFechaRegistro(new Date());
            
            // Validar datos antes de insertar
            validarDatosCliente(nuevoCliente);
            
            // Insertar en la base de datos
            modelo.agregarCliente(nuevoCliente);
            
            // Actualizar vista
            vista.actualizarTablaClientes();
            actualizarVistaDespuesDeAgregar(nuevoCliente);
            
        } catch (Exception e) {
            vista.mostrarError("Error al guardar: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Métodos auxiliares
    private void validarDatosCliente(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("Cliente no puede ser nulo");
        }
        
        if (cliente.getTelefono() == null || cliente.getTelefono().trim().isEmpty()) {
            throw new IllegalArgumentException("Teléfono es requerido");
        }
        
        if (!cliente.getTelefono().matches("\\d{7,15}")) {
            throw new IllegalArgumentException("Teléfono debe contener solo números (7-15 dígitos)");
        }
        
        if (cliente.getNombre() == null || cliente.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("Nombre es requerido");
        }
        
        // Verificar si ya existe un cliente con ese teléfono
        Cliente existente = modelo.buscarPorTelefono(cliente.getTelefono());
        if (existente != null) {
            throw new IllegalStateException("Ya existe un cliente con este teléfono");
        }
    }

    private void actualizarVistaDespuesDeAgregar(Cliente cliente) {
        cargarClientes(); // Recargar datos
        
        // Seleccionar el nuevo cliente en la tabla
        int fila = encontrarFilaCliente(cliente.getId());
        if (fila >= 0) {
            vista.getTablaClientes().setRowSelectionInterval(fila, fila);
            vista.getTablaClientes().scrollRectToVisible(
                vista.getTablaClientes().getCellRect(fila, 0, true));
        }
    }

    private int encontrarFilaCliente(String idCliente) {
        DefaultTableModel model = (DefaultTableModel) vista.getTablaClientes().getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            if (idCliente.equals(model.getValueAt(i, 0))) {
                return i;
            }
        }
        return -1;
    }

    private String generarNuevoId() {
        return "CLI-" + System.currentTimeMillis();
    }

    public void editarCliente() {
        try {
            // Obtener el ID del cliente seleccionado
            String id = vista.getSelectedClienteId();
            if (id == null) return; // Salida silenciosa

            Cliente clienteOriginal = modelo.buscarPorId(id);
            if (clienteOriginal == null) return;

            // Crear copia para edición
            Cliente clienteEditable = new Cliente();
            clienteEditable.copiarDe(clienteOriginal);

            // Mostrar formulario sin validaciones previas
            if (vista.mostrarFormularioEdicion(clienteEditable)) {
                // Aplicar cambios directamente
                if (!clienteOriginal.getTelefono().equals(clienteEditable.getTelefono())) {
                    if (modelo.buscarPorTelefono(clienteEditable.getTelefono()) != null) {
                        vista.mostrarError("El teléfono ya está registrado");
                        return;
                    }
                }

                // Actualizar campos
                clienteOriginal.setTelefono(clienteEditable.getTelefono());
                clienteOriginal.setNombre(clienteEditable.getNombre());
                clienteOriginal.setUltimaCompra(clienteEditable.getUltimaCompra());

                modelo.actualizarCliente(clienteOriginal);
                vista.actualizarTablaClientes();
            }
        } catch (Exception e) {
            vista.mostrarError("Error durante la edición: " + e.getMessage());
        }
    }
    
    private void seleccionarClienteEnTabla(String id) {
        JTable tabla = vista.getTablaClientes();
        DefaultTableModel model = (DefaultTableModel) tabla.getModel();
        
        for (int i = 0; i < model.getRowCount(); i++) {
            if (id.equals(model.getValueAt(i, 0))) {
                tabla.setRowSelectionInterval(i, i);
                tabla.scrollRectToVisible(tabla.getCellRect(i, 0, true));
                break;
            }
        }
    }
    public void eliminarCliente() {
        JTable tabla = vista.getTablaClientes();
        int fila = tabla.getSelectedRow();
        
        // Si no hay fila seleccionada, simplemente retornar sin mensaje
        if (fila < 0) return;
        
        String id = tabla.getValueAt(fila, 0).toString();
        Cliente cliente = modelo.buscarPorId(id);
        
        // Si el cliente no existe, mostrar mensaje de error y retornar
        if (cliente != null && vista.mostrarConfirmacion("¿Eliminar a " + cliente.getNombre() + "?")) {
            modelo.eliminarCliente(id);
            ((DefaultTableModel)tabla.getModel()).removeRow(fila);
        }
    }
    
    public void actualizarCliente(Cliente clienteActualizado) {
        modelo.actualizarCliente(clienteActualizado);
        cargarClientes(); // Recargar datos
    }

    public Cliente buscarClientePorTelefono(String telefono) {
        if (telefono == null || telefono.trim().isEmpty()) {
            throw new IllegalArgumentException("El teléfono no puede estar vacío");
        }
        
        try {
            return modelo.buscarPorTelefono(telefono);
        } catch (Exception e) {
            // Loggear el error
            System.err.println("Error al buscar cliente por teléfono: " + e.getMessage());
            throw new RuntimeException("Error al buscar cliente", e);
        }
    }

    public List<Cliente> obtenerTodosClientes() {
        try {
            List<Cliente> todosClientes = modelo.obtenerTodos();
            
            // Filtrar clientes eliminados
            // return todosClientes.stream()
            //     .filter(c -> c.getFechaEliminacion() == null)
            //     .collect(Collectors.toList());
            
            return todosClientes;
        } catch (Exception e) {
            // Loggear el error
            System.err.println("Error al obtener todos los clientes: " + e.getMessage());
            throw new RuntimeException("Error al obtener clientes", e);
        }
    }
    public Cliente buscarClientePorId(String id) {
        try {
            // Verificar que el ID no sea nulo o vacío
            if (id == null || id.trim().isEmpty()) {
                vista.mostrarError("El ID no puede estar vacío");
                return null;
            }
            
            // Buscar en el modelo usando el ID
            Cliente cliente = modelo.buscarPorId(id);
            
            if (cliente == null) {
                vista.mostrarError("No se encontró cliente con ID: " + id);
            }
            
            return cliente;
        } catch (Exception e) {
            vista.mostrarError("Error al buscar cliente por ID: " + e.getMessage());
            return null;
        }
    }
}