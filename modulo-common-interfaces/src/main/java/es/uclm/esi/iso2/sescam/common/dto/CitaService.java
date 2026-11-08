package es.uclm.esi.iso2.sescam.common.interfaces;

import es.uclm.esi.iso2.sescam.common.dto.CitaDTO;
import java.util.List;

/**
 * Contrato de servicios para la gestión de Citas (CU-11 y relacionados),
 * adaptado al CitaDTO oficial del equipo.
 */
public interface CitaService {
    CitaDTO programarCita(CitaDTO cita);
    CitaDTO modificarCita(CitaDTO cita);
    boolean cancelarCita(String idCita, String motivoCancelacion);
    CitaDTO obtenerCitaPorId(String idCita);
    List<CitaDTO> listarCitasPorPaciente(String pacienteId);
}