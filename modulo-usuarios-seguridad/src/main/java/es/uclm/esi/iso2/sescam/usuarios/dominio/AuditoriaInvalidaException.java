package es.uclm.esi.iso2.sescam.usuarios.dominio;

/**
 * Excepción lanzada cuando los datos de un evento de auditoría son incompletos, nulos o no válidos (CU-19 / RNF-06).
 */
public class AuditoriaInvalidaException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    public AuditoriaInvalidaException(String mensaje) {
        super(mensaje);
    }

    public AuditoriaInvalidaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
