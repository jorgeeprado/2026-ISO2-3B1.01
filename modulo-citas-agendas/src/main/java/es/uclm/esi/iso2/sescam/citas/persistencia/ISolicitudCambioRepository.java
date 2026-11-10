package es.uclm.esi.iso2.sescam.citas.persistencia;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistencia de las solicitudes de cambio y cancelación
 * realizadas por el paciente en CU-01 (autogestión de citas).
 * Estas solicitudes quedan pendientes hasta que el Personal Administrativo
 * las consolide (CU-13, RN-02).
 */
public interface ISolicitudCambioRepository {

    /** Guarda o reemplaza una solicitud de cambio/cancelación. */
    SolicitudCambioRegistro guardar(SolicitudCambioRegistro registro);

    /** Busca una solicitud por su identificador. */
    Optional<SolicitudCambioRegistro> buscarPorId(String idSolicitudCambio);

    /** Lista todas las solicitudes pendientes de un paciente. */
    List<SolicitudCambioRegistro> buscarPorPaciente(String pacienteId);

    /** Lista todas las solicitudes asociadas a una cita concreta. */
    List<SolicitudCambioRegistro> buscarPorCita(String idCita);
}
