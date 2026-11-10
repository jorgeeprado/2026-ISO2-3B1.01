package es.uclm.esi.iso2.sescam.usuarios.dominio;

import es.uclm.esi.iso2.sescam.common.dto.PacienteDTO;
import es.uclm.esi.iso2.sescam.common.dto.UsuarioDTO;
import es.uclm.esi.iso2.sescam.common.interfaces.IAuditoriaService;
import es.uclm.esi.iso2.sescam.common.interfaces.IUsuariosSeguridadService;
import es.uclm.esi.iso2.sescam.common.ports.ISisAdmisionClient;
import es.uclm.esi.iso2.sescam.usuarios.persistencia.IPacienteRepository;

import java.util.List;
import java.util.Objects;

/**
 * Implementación principal del contrato transversal {@link IUsuariosSeguridadService} para el Módulo 5.
 * Diseñado bajo la metodología Spec-Driven Development (SDD) y el Proceso Unificado de Desarrollo (PUD):
 * - Iteración 3: CU-12 (Gestionar alta, modificación y baja lógica de pacientes, RN-08).
 * - Iteraciones posteriores: CU-18 (RBAC / RN-05 en Iteración 2), CU-20 (SSO en Iteración 21).
 * Depende exclusivamente de interfaces (RNF-09).
 */
public class UsuariosSeguridadService implements IUsuariosSeguridadService {

    private final PacienteService pacienteService;

    public UsuariosSeguridadService(IPacienteRepository pacienteRepository) {
        this(pacienteRepository, null, null);
    }

    public UsuariosSeguridadService(IPacienteRepository pacienteRepository, ISisAdmisionClient sisAdmisionClient) {
        this(pacienteRepository, sisAdmisionClient, null);
    }

    public UsuariosSeguridadService(IPacienteRepository pacienteRepository,
                                    ISisAdmisionClient sisAdmisionClient,
                                    IAuditoriaService auditoriaService) {
        Objects.requireNonNull(pacienteRepository, "El repositorio de pacientes no puede ser nulo");
        this.pacienteService = new PacienteService(pacienteRepository, sisAdmisionClient, auditoriaService);
    }

    // =========================================================================
    // CU-12: Gestionar alta, modificación y baja lógica de pacientes (RN-08)
    // =========================================================================

    @Override
    public PacienteDTO darAltaPaciente(PacienteDTO paciente) {
        return pacienteService.darAltaPaciente(paciente);
    }

    @Override
    public PacienteDTO modificarPaciente(PacienteDTO paciente) {
        return pacienteService.modificarPaciente(paciente);
    }

    @Override
    public boolean darBajaLogicaPaciente(String pacienteId) {
        return pacienteService.darBajaLogicaPaciente(pacienteId);
    }

    @Override
    public PacienteDTO obtenerPaciente(String pacienteId) {
        return pacienteService.obtenerPaciente(pacienteId);
    }

    public List<PacienteDTO> listarPacientes(boolean soloActivos) {
        return pacienteService.listarPacientes(soloActivos);
    }

    // =========================================================================
    // Métodos reservados para iteraciones posteriores (CU-18 / CU-20)
    // =========================================================================

    @Override
    public UsuarioDTO autenticarUsuario(String username, String tokenSso) {
        throw new UnsupportedOperationException(
                "El caso de uso CU-18 / CU-20 (Autenticación federada SSO) está planificado para su desarrollo en la Iteración 2 / Iteración 21.");
    }

    @Override
    public boolean verificarPermiso(String usuarioId, String accionRequerida) {
        throw new UnsupportedOperationException(
                "El caso de uso CU-18 (Control de acceso RBAC y herencia médica RN-05) está planificado para su desarrollo en la Iteración 2.");
    }
}
