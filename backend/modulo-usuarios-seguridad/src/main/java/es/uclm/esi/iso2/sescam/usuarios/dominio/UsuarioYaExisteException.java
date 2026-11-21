package es.uclm.esi.iso2.sescam.usuarios.dominio;

/**
 * Excepción lanzada cuando se intenta registrar un usuario cuyo username o ID ya existe.
 */
public class UsuarioYaExisteException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public UsuarioYaExisteException(String mensaje) {
        super(mensaje);
    }
}
