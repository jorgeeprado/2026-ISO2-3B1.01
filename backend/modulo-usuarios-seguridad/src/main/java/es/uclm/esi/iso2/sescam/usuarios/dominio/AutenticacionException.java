package es.uclm.esi.iso2.sescam.usuarios.dominio;

/**
 * Excepción lanzada cuando falla la autenticación de un usuario con el proveedor federado SSO (CU-18 / CU-20).
 */
public class AutenticacionException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public AutenticacionException(String mensaje) {
        super(mensaje);
    }
}
