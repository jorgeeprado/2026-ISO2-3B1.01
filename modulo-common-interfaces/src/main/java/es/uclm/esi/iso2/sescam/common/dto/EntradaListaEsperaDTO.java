package es.uclm.esi.iso2.sescam.common.dto;

import es.uclm.esi.iso2.sescam.common.enums.EstadoEntradaEspera;
import es.uclm.esi.iso2.sescam.common.enums.Prioridad;

import java.time.LocalDateTime;

/**
 * Entrada de la lista de espera de un tipo de prueba (CU-15). Es una copia de los datos de la
 * prescripción ({@link SolicitudPruebaDTO}) tomada al inscribirla (D-017).
 *
 * @param idSolicitud       identificador de la solicitud; identifica la entrada
 * @param pacienteId        paciente de la solicitud
 * @param tipoPruebaId      tipo de prueba; cada tipo tiene su propia lista
 * @param prioridad         prioridad clínica vigente (RN-01)
 * @param fechaPrescripcion fecha de prescripción ({@code SolicitudPruebaDTO.fechaSolicitud}), criterio FIFO (RN-01)
 * @param fechaInscripcion  momento de la inscripción en la lista; desempate tras la fecha de prescripción
 * @param estado            estado de la entrada
 * @param idCita            cita consolidada por CU-13; solo informado si el estado es CITADA
 * @param motivo            motivo del último cambio de prioridad, retirada o reincorporación; puede ser nulo
 */
public record EntradaListaEsperaDTO(
        String idSolicitud,
        String pacienteId,
        String tipoPruebaId,
        Prioridad prioridad,
        LocalDateTime fechaPrescripcion,
        LocalDateTime fechaInscripcion,
        EstadoEntradaEspera estado,
        String idCita,
        String motivo
) {
    public EntradaListaEsperaDTO {
        if (idSolicitud == null || idSolicitud.isBlank()) {
            throw new IllegalArgumentException("El identificador de la solicitud es obligatorio");
        }
        if (pacienteId == null || pacienteId.isBlank()) {
            throw new IllegalArgumentException("El identificador del paciente es obligatorio");
        }
        if (tipoPruebaId == null || tipoPruebaId.isBlank()) {
            throw new IllegalArgumentException("El identificador del tipo de prueba es obligatorio");
        }
        if (prioridad == null) {
            throw new IllegalArgumentException("La prioridad clínica es obligatoria");
        }
        if (fechaPrescripcion == null) {
            throw new IllegalArgumentException("La fecha de prescripción es obligatoria");
        }
        if (fechaInscripcion == null) {
            throw new IllegalArgumentException("La fecha de inscripción es obligatoria");
        }
        if (estado == null) {
            throw new IllegalArgumentException("El estado de la entrada es obligatorio");
        }
        boolean tieneCita = idCita != null && !idCita.isBlank();
        if (estado == EstadoEntradaEspera.CITADA && !tieneCita) {
            throw new IllegalArgumentException("Una entrada citada debe referenciar una cita");
        }
        if (estado != EstadoEntradaEspera.CITADA && idCita != null) {
            throw new IllegalArgumentException("Solo una entrada citada puede referenciar una cita");
        }
    }
}
