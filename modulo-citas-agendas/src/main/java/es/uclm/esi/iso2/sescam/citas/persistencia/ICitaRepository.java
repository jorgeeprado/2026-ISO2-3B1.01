package es.uclm.esi.iso2.sescam.citas.persistencia;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistencia de las citas de la citación administrativa (CU-13).
 * Las citas no se borran: la cancelación es un cambio de estado.
 */
public interface ICitaRepository {

    /** Guarda o reemplaza el registro de la misma cita. */
    CitaRegistro guardar(CitaRegistro registro);

    Optional<CitaRegistro> buscarPorId(String idCita);

    List<CitaRegistro> buscarPorSolicitud(String idSolicitud);
}
