package es.uclm.esi.iso2.sescam.usuarios.dominio;

/**
 * Excepción lanzada cuando no se localiza un usuario en el sistema por su ID o username.
 */
public class UsuarioNoEncontradoException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public UsuarioNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
