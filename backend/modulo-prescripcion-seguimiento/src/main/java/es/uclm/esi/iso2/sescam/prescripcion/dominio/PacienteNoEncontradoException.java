package es.uclm.esi.iso2.sescam.prescripcion.dominio;

/**
 * Indica que no existe el paciente al que se intenta prescribir una prueba.
 */
public class PacienteNoEncontradoException extends IllegalArgumentException {

    public PacienteNoEncontradoException(String pacienteId) {
        super("No existe el paciente con identificador: " + pacienteId);
    }
}
