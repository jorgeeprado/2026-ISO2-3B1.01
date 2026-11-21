package es.uclm.esi.iso2.sescam.informes.dominio;

/**
 * Excepción de negocio lanzada cuando se intenta registrar la ejecución de una prueba diagnóstica
 * que utiliza contraste o modalidad de Resonancia Magnética sin que conste un consentimiento informado
 * y cuestionario de seguridad previo validado y firmado por el paciente (Regla de Negocio RN-04).
 */
public class ConsentimientoRequeridoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ConsentimientoRequeridoException(String mensaje) {
        super(mensaje);
    }
}

