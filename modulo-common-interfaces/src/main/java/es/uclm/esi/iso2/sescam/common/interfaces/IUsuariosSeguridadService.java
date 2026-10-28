package es.uclm.esi.iso2.sescam.common.interfaces;

import es.uclm.esi.iso2.sescam.common.dto.PacienteDTO;
import es.uclm.esi.iso2.sescam.common.dto.UsuarioDTO;
import java.util.List;

/**
 * Contrato de servicios de usuarios, seguridad RBAC y filiación del Módulo 5 (modulo-usuarios-seguridad).
 * Responsable: Carlos de la Dueña Marques.
 * Casos de uso: CU-12, CU-18, CU-20.
 */
public interface IUsuariosSeguridadService {

    /**
     * Autentica a un usuario federado mediante SSO corporativo (CU-18 / CU-20).
     */
    UsuarioDTO autenticarUsuario(String username, String tokenSso);

    /**
     * Da de alta a un paciente en el sistema (CU-12 / RF-12).
     */
    PacienteDTO darAltaPaciente(PacienteDTO paciente);

    /**
     * Modifica los datos demográficos o de contacto de un paciente (CU-12 / RF-12).
     */
    PacienteDTO modificarPaciente(PacienteDTO paciente);

    /**
     * Ejecuta la baja lógica de un paciente para preservar integridad referencial (CU-12 / RN-08).
     */
    boolean darBajaLogicaPaciente(String pacienteId);

    /**
     * Obtiene el expediente demográfico de un paciente por su ID o CIP.
     */
    PacienteDTO obtenerPaciente(String pacienteId);

    /**
     * Verifica los permisos de acceso según la matriz RBAC considerando la herencia funcional del médico (RN-05).
     */
    boolean verificarPermiso(String usuarioId, String accionRequerida);
}
