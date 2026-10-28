package es.uclm.esi.iso2.sescam.common.interfaces;

import es.uclm.esi.iso2.sescam.common.dto.SolicitudPruebaDTO;
import java.util.List;

/**
 * Contrato de servicios del Módulo 2 (modulo-prescripcion-seguimiento).
 * Responsable: Inés Ballesteros.
 * Casos de uso: CU-05, CU-06, CU-07.
 */
public interface IPrescripcionService {

    /**
     * Prescribe una prueba diagnóstica puntual (CU-06 / RF-06).
     */
    SolicitudPruebaDTO prescribirPrueba(SolicitudPruebaDTO solicitud);

    /**
     * Prescribe una prueba con seguimiento periódico automatizado (CU-07 / RN-03 / RF-07).
     */
    SolicitudPruebaDTO prescribirSeguimientoPeriodico(SolicitudPruebaDTO solicitud, int mesesPeriodicidad);

    /**
     * Consulta una prescripción/solicitud clínica por ID.
     */
    SolicitudPruebaDTO consultarSolicitud(String solicitudId);

    /**
     * Consulta las prescripciones activas de un paciente (CU-05).
     */
    List<SolicitudPruebaDTO> listarSolicitudesPorPaciente(String pacienteId);

    /**
     * Consulta las prescripciones emitidas por un facultativo prescriptor (CU-08).
     */
    List<SolicitudPruebaDTO> listarSolicitudesPorMedico(String medicoId);

    /**
     * Autogenera la siguiente solicitud de cita periódica tras completarse un ciclo (RN-03).
     */
    SolicitudPruebaDTO autogenerarSiguienteCicloSeguimiento(String solicitudOriginalId);
}

