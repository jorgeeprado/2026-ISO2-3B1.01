package es.uclm.esi.iso2.sescam.common.interfaces;

import es.uclm.esi.iso2.sescam.common.dto.CitaDTO;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Contrato de servicios del Módulo 1 (modulo-citas-agendas).
 * Responsables: Pedro de la Fuente y Pablo Sánchez.
 * Casos de uso: CU-01, CU-08, CU-13, CU-14, CU-15, CU-23.
 */
public interface ICitasAgendasService {

    /**
     * Consulta una cita por su identificador único.
     */
    CitaDTO consultarCita(String citaId);

    /**
     * Lista las citas activas o históricas de un paciente (CU-01).
     */
    List<CitaDTO> listarCitasPorPaciente(String pacienteId);

    /**
     * Solicita confirmación de asistencia del paciente a una cita (CU-01).
     */
    boolean confirmarCita(String citaId);

    /**
     * Solicita cambio de cita por el paciente, sujeto a consolidación (RN-02).
     */
    CitaDTO solicitarCambioCita(String citaId, String motivoCambio);

    /**
     * Solicita cancelación de cita por el paciente (CU-01 / RN-02).
     */
    boolean solicitarCancelacionCita(String citaId, String motivoCancelacion);

    /**
     * Asignación definitiva de hueco de agenda por Personal Administrativo (CU-13 / RN-02).
     */
    CitaDTO asignarCitaDefinitiva(String solicitudId, LocalDateTime fechaHora, String salaId, String medicoRadiologoId);

    /**
     * Reprogramación administrativa definitiva de una cita existente (CU-13).
     */
    CitaDTO reprogramarCita(String citaId, LocalDateTime nuevaFechaHora, String nuevaSalaId);

    /**
     * Cancelación definitiva ejecutada por Personal Administrativo (CU-13).
     */
    boolean cancelarCitaDefinitiva(String citaId, String motivo);

    /**
     * Reasignación inteligente de huecos liberados por cancelación con antelación menor a 48h (CU-23).
     */
    List<CitaDTO> reasignarHuecosLiberados(String salaId, LocalDateTime fecha);
}

