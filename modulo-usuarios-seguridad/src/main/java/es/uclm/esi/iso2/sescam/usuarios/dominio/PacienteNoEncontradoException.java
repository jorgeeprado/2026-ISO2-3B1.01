package es.uclm.esi.iso2.sescam.usuarios.dominio;

/**
 * Excepción lanzada cuando no se localiza un paciente en el sistema por su ID o CIP.
 */
public class PacienteNoEncontradoException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public PacienteNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}

