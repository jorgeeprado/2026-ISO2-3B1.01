package es.uclm.esi.iso2.sescam.usuarios.dominio;

/**
 * Excepción lanzada cuando se intenta dar de alta o modificar un paciente cuyo CIP o DNI
 * ya se encuentra registrado en el censo del sistema.
 */
public class PacienteYaExisteException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public PacienteYaExisteException(String mensaje) {
        super(mensaje);
    }
}

