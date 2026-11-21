package es.uclm.esi.iso2.sescam.citas.persistencia;

import es.uclm.esi.iso2.sescam.common.dto.CitaDTO;

import java.util.Objects;

/**
 * Registro persistido de una cita de CU-13 junto al turno que ocupa, porque {@link CitaDTO}
 * no tiene identificador de turno (D-023). Guarda una copia de la cita y devuelve copias,
 * ya que {@code CitaDTO} es mutable.
 *
 * @param cita                 cita (copia defensiva)
 * @param idTurno              turno de agenda (CU-14) que ocupa o ocupó la cita
 * @param motivoAsignacion     motivo por el que se asignó a una solicitud que no era la primera
 *                             candidata según RN-01; nulo si se respetó el orden
 * @param motivoReprogramacion motivo de la última reprogramación; nulo si no se ha reprogramado
 */
public record CitaRegistro(CitaDTO cita, String idTurno, String motivoAsignacion, String motivoReprogramacion) {

    public CitaRegistro {
        Objects.requireNonNull(cita, "La cita no puede ser nula");
        if (cita.getId() == null || cita.getId().isBlank()) {
            throw new IllegalArgumentException("El identificador de la cita es obligatorio");
        }
        if (cita.getSolicitudId() == null || cita.getSolicitudId().isBlank()) {
            throw new IllegalArgumentException("El identificador de la solicitud es obligatorio");
        }
        if (idTurno == null || idTurno.isBlank()) {
            throw new IllegalArgumentException("El identificador del turno es obligatorio");
        }
        cita = copiar(cita);
    }

    /** Devuelve una copia de la cita almacenada. */
    @Override
    public CitaDTO cita() {
        return copiar(cita);
    }

    public String idCita() {
        return cita.getId();
    }

    public String idSolicitud() {
        return cita.getSolicitudId();
    }

    /** Copia campo a campo de un {@link CitaDTO}. */
    public static CitaDTO copiar(CitaDTO c) {
        return new CitaDTO(c.getId(), c.getSolicitudId(), c.getPacienteId(), c.getTipoPruebaId(), c.getFechaHora(),
                c.getDuracionMinutos(), c.getSalaId(), c.getMedicoRadiologoId(), c.getEstado(),
                c.getMotivoCancelacion());
    }
}
