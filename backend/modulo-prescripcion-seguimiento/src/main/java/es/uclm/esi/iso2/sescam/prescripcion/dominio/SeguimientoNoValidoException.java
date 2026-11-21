package es.uclm.esi.iso2.sescam.prescripcion.dominio;

/**
 * Indica que una prescripción no puede avanzar al siguiente ciclo periódico.
 */
public class SeguimientoNoValidoException extends IllegalStateException {

    public SeguimientoNoValidoException(String message) {
        super(message);
    }
}
