package es.uclm.esi.iso2.sescam.prescripcion.dominio;

/**
 * Indica que no existe una prescripción con el identificador solicitado.
 */
public class PrescripcionNoEncontradaException extends IllegalArgumentException {

    public PrescripcionNoEncontradaException(String prescripcionId) {
        super("No existe la prescripcion con identificador: " + prescripcionId);
    }
}
