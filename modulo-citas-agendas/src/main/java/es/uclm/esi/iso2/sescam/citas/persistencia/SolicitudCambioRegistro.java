package es.uclm.esi.iso2.sescam.citas.persistencia;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Registro persistido de una solicitud de cambio o cancelación realizada
 * por el paciente en CU-01. Queda en estado {@code PENDIENTE} hasta que
 * el Personal Administrativo la consolide (CU-13 / RN-02).
 *
 * @param idSolicitudCambio identificador único de la solicitud de cambio
 * @param idCita            cita sobre la que se solicita el cambio
 * @param pacienteId        paciente que realiza la solicitud
 * @param tipo              tipo de solicitud (CAMBIO o CANCELACION)
 * @param motivo            motivo indicado por el paciente
 * @param estado            estado de la solicitud (PENDIENTE, ACEPTADA, RECHAZADA)
 * @param fechaSolicitud    momento en que se registró la solicitud
 */
public record SolicitudCambioRegistro(
        String idSolicitudCambio,
        String idCita,
        String pacienteId,
        TipoSolicitudCambio tipo,
        String motivo,
        EstadoSolicitudCambio estado,
        LocalDateTime fechaSolicitud
) {
    public SolicitudCambioRegistro {
        Objects.requireNonNull(idSolicitudCambio, "El identificador de la solicitud de cambio es obligatorio");
        if (idSolicitudCambio.isBlank()) {
            throw new IllegalArgumentException("El identificador de la solicitud de cambio es obligatorio");
        }
        Objects.requireNonNull(idCita, "El identificador de la cita es obligatorio");
        if (idCita.isBlank()) {
            throw new IllegalArgumentException("El identificador de la cita es obligatorio");
        }
        Objects.requireNonNull(pacienteId, "El identificador del paciente es obligatorio");
        if (pacienteId.isBlank()) {
            throw new IllegalArgumentException("El identificador del paciente es obligatorio");
        }
        Objects.requireNonNull(tipo, "El tipo de solicitud de cambio es obligatorio");
        Objects.requireNonNull(motivo, "El motivo de la solicitud es obligatorio");
        if (motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo de la solicitud es obligatorio");
        }
        Objects.requireNonNull(estado, "El estado de la solicitud de cambio es obligatorio");
        Objects.requireNonNull(fechaSolicitud, "La fecha de solicitud es obligatoria");
    }

    /** Tipos de solicitud de cambio del paciente sobre una cita. */
    public enum TipoSolicitudCambio {
        CAMBIO,
        CANCELACION
    }

    /** Estados del ciclo de vida de la solicitud de cambio. */
    public enum EstadoSolicitudCambio {
        PENDIENTE,
        ACEPTADA,
        RECHAZADA
    }
}
