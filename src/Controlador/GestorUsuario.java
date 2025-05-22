package Controlador;

import Modelo.Usuario;
import java.util.HashMap;
import Modelo.Usuarioo;

import java.util.List;

public class GestorUsuario {
    private Usuarioo usuarioo;

    public GestorUsuario() {
        this.usuarioo = new Usuarioo();
    }

    public boolean registrarUsuario(String nombre, String contraseña, String rol) {
        return usuarioo.registrarUsuario(nombre, contraseña, rol);
    }
    
    public boolean autenticar(String username, String password) {
        return usuarioo.autenticarUsuario(username, password);
    }

    public List<Usuario> listarUsuarios() {
        return usuarioo.obtenerTodosUsuarios();
    }

    public boolean cambiarRolUsuario(String username, String nuevoRol) {
        return usuarioo.actualizarRol(username, nuevoRol);
    }

    public boolean eliminarUsuario(String username) {
        return usuarioo.eliminarUsuario(username);
    }
}