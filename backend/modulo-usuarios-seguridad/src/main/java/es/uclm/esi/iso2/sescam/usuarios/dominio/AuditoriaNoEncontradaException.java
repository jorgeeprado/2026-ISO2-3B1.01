package es.uclm.esi.iso2.sescam.usuarios.dominio;

/**
 * Excepción lanzada cuando una traza de auditoría requerida por su identificador no existe en el sistema.
 */
public class AuditoriaNoEncontradaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AuditoriaNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
