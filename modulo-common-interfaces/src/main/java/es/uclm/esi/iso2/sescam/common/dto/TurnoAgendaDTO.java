package es.uclm.esi.iso2.sescam.common.dto;

import es.uclm.esi.iso2.sescam.common.enums.EstadoTurno;
import es.uclm.esi.iso2.sescam.common.enums.TipoRecurso;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Turno (hueco citable) generado a partir de una franja {@link AgendaMedicaDTO} de un recurso (CU-14).
 *
 * @param idTurno     identificador determinista: {@code idAgenda + "#" + HH:mm}
 * @param idAgenda    franja de la que procede el turno
 * @param tipoRecurso tipo de recurso (médico o sala)
 * @param idRecurso   identificador del médico o de la sala
 * @param fecha       fecha del turno
 * @param horaInicio  hora de inicio (incluida)
 * @param horaFin     hora de fin (excluida)
 * @param estado      estado del turno
 * @param motivo      motivo del último bloqueo o desbloqueo; puede ser nulo
 * @param idCita      cita que ocupa el turno; solo informado si el estado es OCUPADO
 */
public record TurnoAgendaDTO(
        String idTurno,
        String idAgenda,
        TipoRecurso tipoRecurso,
        String idRecurso,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        EstadoTurno estado,
        String motivo,
        String idCita
) {
    public TurnoAgendaDTO {
        if (idTurno == null || idTurno.isBlank()) {
            throw new IllegalArgumentException("El identificador del turno es obligatorio");
        }
        if (idAgenda == null || idAgenda.isBlank()) {
            throw new IllegalArgumentException("El identificador de la agenda es obligatorio");
        }
        if (tipoRecurso == null) {
            throw new IllegalArgumentException("El tipo de recurso es obligatorio");
        }
        if (idRecurso == null || idRecurso.isBlank()) {
            throw new IllegalArgumentException("El identificador del recurso es obligatorio");
        }
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha del turno no puede ser nula");
        }
        if (horaInicio == null || horaFin == null || !horaInicio.isBefore(horaFin)) {
            throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de fin");
        }
        if (estado == null) {
            throw new IllegalArgumentException("El estado del turno es obligatorio");
        }
        if (estado == EstadoTurno.OCUPADO && (idCita == null || idCita.isBlank())) {
            throw new IllegalArgumentException("Un turno ocupado debe referenciar una cita");
        }
    }
}
