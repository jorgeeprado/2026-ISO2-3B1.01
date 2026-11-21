package es.uclm.esi.iso2.sescam.usuarios.dominio;

/**
 * Excepción lanzada ante cualquier intento de vulnerar la inmutabilidad de los registros de auditoría (RN-07 / RNF-06 / D-007).
 */
public class OperacionAuditoriaNoPermitidaException extends UnsupportedOperationException {

    private static final long serialVersionUID = 1L;

    public OperacionAuditoriaNoPermitidaException(String mensaje) {
        super(mensaje);
    }

    public OperacionAuditoriaNoPermitidaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
