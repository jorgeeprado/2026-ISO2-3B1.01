package es.uclm.esi.iso2.sescam.usuarios.presentacion;

import es.uclm.esi.iso2.sescam.common.dto.PacienteDTO;
import es.uclm.esi.iso2.sescam.common.interfaces.IUsuariosSeguridadService;

import java.util.Objects;

/**
 * Controlador de la capa de presentación para la gestión y filiación de pacientes (CU-12).
 * Conecta las interfaces de usuario (mostrador de Admisión, portal web) con la capa de negocio y seguridad.
 * Depende EXCLUSIVAMENTE de la interfaz transversal {@link IUsuariosSeguridadService} (RNF-09).
 */
public class PacienteController {

    private final IUsuariosSeguridadService usuariosSeguridadService;

    public PacienteController(IUsuariosSeguridadService usuariosSeguridadService) {
        this.usuariosSeguridadService = Objects.requireNonNull(usuariosSeguridadService,
                "El servicio de usuarios y seguridad no puede ser nulo");
    }

    /**
     * Procesa la solicitud de alta de un nuevo paciente en el censo.
     *
     * @param paciente DTO del paciente recibido desde la vista.
     * @return PacienteDTO registrado y asignado.
     */
    public PacienteDTO darAltaPaciente(PacienteDTO paciente) {
        return usuariosSeguridadService.darAltaPaciente(paciente);
    }

    /**
     * Procesa la actualización de datos demográficos o de localización del paciente.
     *
     * @param paciente DTO con las modificaciones aplicadas.
     * @return PacienteDTO actualizado.
     */
    public PacienteDTO modificarPaciente(PacienteDTO paciente) {
        return usuariosSeguridadService.modificarPaciente(paciente);
    }

    /**
     * Solicita la baja lógica de un paciente para preservar el registro médico-legal (RN-08).
     *
     * @param pacienteId Identificador o CIP del paciente a dar de baja.
     * @return true si la baja lógica fue consolidada; false si no se encontró al paciente.
     */
    public boolean darBajaLogicaPaciente(String pacienteId) {
        return usuariosSeguridadService.darBajaLogicaPaciente(pacienteId);
    }

    /**
     * Consulta el expediente del paciente por su identificador único o CIP.
     *
     * @param pacienteId Identificador o CIP del paciente.
     * @return PacienteDTO con los datos del paciente o null si no existe.
     */
    public PacienteDTO obtenerPaciente(String pacienteId) {
        return usuariosSeguridadService.obtenerPaciente(pacienteId);
    }

    /**
     * Comprueba de manera rápida si un paciente existe y su estado es activo (activo == true).
     *
     * @param pacienteId Identificador o CIP del paciente.
     * @return true si el paciente existe y está activo; false en caso contrario.
     */
    public boolean esPacienteActivo(String pacienteId) {
        if (pacienteId == null || pacienteId.trim().isEmpty()) {
            return false;
        }
        PacienteDTO paciente = usuariosSeguridadService.obtenerPaciente(pacienteId.trim());
        return paciente != null && paciente.isActivo();
    }
}
