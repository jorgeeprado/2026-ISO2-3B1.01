package es.uclm.esi.iso2.sescam.common.interfaces;

import es.uclm.esi.iso2.sescam.common.dto.AuditoriaAccesoDTO;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Contrato de auditoría síncrona e inmutable del Módulo 5 (modulo-usuarios-seguridad).
 * Exigido por RN-07 y RNF-06 (No repudio y trazabilidad clínica).
 * Responsable: Carlos de la Dueña Marques.
 * Casos de uso: CU-19.
 */
public interface IAuditoriaService {

    /**
     * Registra de forma síncrona e inmutable un acceso o mutación sobre datos clínicos o estudios DICOM.
     */
    void registrarAccesoClinico(String usuarioId, String pacienteIdAfectado, String accion, String recursoAccedido, String ipOrigen);

    /**
     * Consulta el registro de auditoría filtrado por rango temporal o paciente (CU-19 / RF-19).
     */
    List<AuditoriaAccesoDTO> consultarAuditorias(LocalDateTime desde, LocalDateTime hasta, String pacienteId);
}
