package es.uclm.esi.iso2.sescam.usuarios.dominio;

/**
 * Excepción lanzada cuando los datos del usuario son nulos o no cumplen las validaciones mínimas.
 */
public class UsuarioInvalidoException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;

    public UsuarioInvalidoException(String mensaje) {
        super(mensaje);
    }
}
