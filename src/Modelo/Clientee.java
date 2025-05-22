package Modelo;

import java.sql.Connection;
import java.util.List;

import ConexionBD.ConexionAccess;

public interface Clientee {
    List<Cliente> obtenerTodos();
    Cliente buscarPorId(String id);	
    Cliente buscarPorTelefono(String telefono);
    void agregarCliente(Cliente cliente);
    void actualizarCliente(Cliente cliente);
    void eliminarCliente(String id);
    Connection conn = ConexionAccess.conectar();

}