package es.uclm.esi.iso2.sescam.common.interfaces;

import es.uclm.esi.iso2.sescam.common.dto.TurnoAgendaDTO;
import es.uclm.esi.iso2.sescam.common.enums.TipoRecurso;

import java.time.LocalDate;
import java.util.List;

/**
 * Contrato reducido de disponibilidad de agenda (CU-14), pensado para ser consumido
 * por la citación administrativa (CU-13) sin depender de la administración de agendas.
 *
 * <p>Errores: {@link IllegalArgumentException} ante datos inválidos o turnos inexistentes;
 * {@link IllegalStateException} si el turno no está en el estado requerido.</p>
 */
public interface DisponibilidadAgendaService {

    /**
     * Devuelve los turnos libres de un recurso en una fecha, ordenados por hora.
     */
    List<TurnoAgendaDTO> consultarHuecosLibres(TipoRecurso tipoRecurso, String idRecurso, LocalDate fecha);

    /**
     * Obtiene un turno por su identificador, o {@code null} si no existe.
     */
    TurnoAgendaDTO obtenerTurno(String idTurno);

    /**
     * Ocupa un turno libre con una cita. Falla si el turno no está LIBRE.
     */
    TurnoAgendaDTO ocuparTurno(String idTurno, String idCita);

    /**
     * Libera un turno ocupado, que vuelve a estado LIBRE. Falla si el turno no está OCUPADO.
     */
    TurnoAgendaDTO liberarTurno(String idTurno);
}
