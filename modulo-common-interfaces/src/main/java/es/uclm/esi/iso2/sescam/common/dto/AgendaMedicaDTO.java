package es.uclm.esi.iso2.sescam.common.interfaces.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO que representa una agenda médica o de recursos diagnósticos en el sistema SESCAM.
 * Utilizado para la planificación de horarios, turnos y disponibilidad del personal sanitario.
 */
public record AgendaMedicaDTO(
        String idAgenda,
        String idMedico,
        String idPruebaAsociada,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        int intervaloMinutos
) {
    public AgendaMedicaDTO {
        if (idAgenda == null || idAgenda.isBlank()) {
            throw new IllegalArgumentException("El identificador de la agenda es obligatorio");
        }
        if (idMedico == null || idMedico.isBlank()) {
            throw new IllegalArgumentException("El identificador del médico o recurso es obligatorio");
        }
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha de la agenda no puede ser nula");
        }
        if (horaInicio == null || horaFin == null) {
            throw new IllegalArgumentException("Las horas de inicio y fin son obligatorias");
        }
        if (horaInicio.isAfter(horaFin) || horaInicio.equals(horaFin)) {
            throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de fin");
        }
    }
}