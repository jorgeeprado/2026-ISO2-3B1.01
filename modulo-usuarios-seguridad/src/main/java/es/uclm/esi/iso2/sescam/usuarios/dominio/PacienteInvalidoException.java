package es.uclm.esi.iso2.sescam.usuarios.dominio;

/**
 * Excepción lanzada cuando los datos demográficos o identificativos de un paciente no cumplen
 * con los criterios mínimos de validación clínica o legal.
 */
public class PacienteInvalidoException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;

    public PacienteInvalidoException(String mensaje) {
        super(mensaje);
    }
}

