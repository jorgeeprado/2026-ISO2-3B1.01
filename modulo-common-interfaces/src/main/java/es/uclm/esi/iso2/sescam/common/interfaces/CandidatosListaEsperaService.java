package es.uclm.esi.iso2.sescam.common.interfaces;

import es.uclm.esi.iso2.sescam.common.dto.EntradaListaEsperaDTO;

import java.util.List;

/**
 * Contrato reducido de la lista de espera (CU-15) para la citación administrativa (CU-13)
 * y la reasignación de huecos liberados (CU-23). Este contrato no ocupa turnos: la asignación
 * formal del hueco es potestad de la citación administrativa (RN-02).
 *
 * <p>Errores: {@link IllegalArgumentException} ante datos inválidos, turnos o solicitudes
 * inexistentes; {@link IllegalStateException} si el turno o la entrada no están en el estado requerido.</p>
 */
public interface CandidatosListaEsperaService {

    /**
     * Propone, como máximo, {@code maximo} entradas {@code EN_ESPERA} compatibles con un turno
     * {@code LIBRE}, en el orden de RN-01. Son compatibles las del tipo de prueba de la franja del turno;
     * si la franja no tiene prueba asociada, lo son todas. No reserva ni ocupa el turno.
     */
    List<EntradaListaEsperaDTO> proponerCandidatos(String idTurno, int maximo);

    /**
     * Marca como {@code CITADA} una entrada {@code EN_ESPERA} cuando CU-13 consolida la cita (RN-02).
     */
    EntradaListaEsperaDTO marcarCitada(String idSolicitud, String idCita);

    /**
     * Devuelve a {@code EN_ESPERA} una entrada {@code CITADA} cuya cita se ha cancelado, con motivo
     * obligatorio. Conserva su fecha de prescripción y su fecha de inscripción (RN-01).
     */
    EntradaListaEsperaDTO reincorporar(String idSolicitud, String motivo);
}
