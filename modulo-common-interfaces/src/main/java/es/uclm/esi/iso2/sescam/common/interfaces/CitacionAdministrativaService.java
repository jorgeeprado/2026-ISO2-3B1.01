package es.uclm.esi.iso2.sescam.common.interfaces;

import es.uclm.esi.iso2.sescam.common.dto.CitaDTO;

/**
 * Contrato de citación administrativa definitiva (CU-13) para el Personal Administrativo de Admisión.
 * Es la única vía que ocupa o libera turnos de agenda (CU-14) por una cita y que marca como citadas
 * las entradas de la lista de espera (CU-15), conforme a RN-02.
 *
 * <p>Cada cita ocupa un único turno: si es de sala se informa {@code salaId}; si es de médico,
 * {@code medicoRadiologoId}; el otro campo queda nulo (D-023).</p>
 *
 * <p>Errores: {@link IllegalArgumentException} ante datos inválidos, motivos obligatorios ausentes
 * o identificadores inexistentes; {@link IllegalStateException} si el turno, la entrada o la cita no están
 * en el estado requerido, si el turno no es compatible o si ya ha pasado. Si falla un paso intermedio,
 * se deshacen los pasos previos y se relanza la excepción original (D-024).</p>
 */
public interface CitacionAdministrativaService {

    /**
     * Asigna un turno libre y futuro a una solicitud en espera compatible con él y crea la cita
     * {@code PROGRAMADA}. Si la solicitud no es la primera candidata según RN-01, el motivo es obligatorio
     * y queda registrado; si es la primera, el motivo es opcional y no se registra.
     */
    CitaDTO asignarCita(String idSolicitud, String idTurno, String motivo);

    /**
     * Traslada una cita activa a otro turno libre, futuro y compatible con su tipo de prueba, con motivo
     * obligatorio. La cita conserva su identificador y queda {@code PROGRAMADA} con la nueva fecha y hora.
     * La lista de espera no cambia.
     */
    CitaDTO reprogramarCita(String idCita, String idTurnoNuevo, String motivo);

    /**
     * Cancela una cita activa con motivo obligatorio: libera su turno, reincorpora la solicitud a la lista
     * de espera conservando su antigüedad (RN-01) y deja la cita {@code CANCELADA}.
     */
    CitaDTO cancelarCita(String idCita, String motivo);
}
