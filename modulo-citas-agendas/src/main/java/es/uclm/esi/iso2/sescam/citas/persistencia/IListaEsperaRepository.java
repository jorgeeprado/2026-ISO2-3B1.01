package es.uclm.esi.iso2.sescam.citas.persistencia;

import es.uclm.esi.iso2.sescam.common.dto.EntradaListaEsperaDTO;
import es.uclm.esi.iso2.sescam.common.enums.EstadoEntradaEspera;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistencia de las entradas de lista de espera (CU-15).
 * Las entradas nunca se borran: la retirada es un cambio de estado (D-021).
 * El orden de RN-01 lo aplica el dominio, no el repositorio.
 */
public interface IListaEsperaRepository {

    /** Guarda o reemplaza la entrada de la misma solicitud. */
    EntradaListaEsperaDTO guardar(EntradaListaEsperaDTO entrada);

    Optional<EntradaListaEsperaDTO> buscarPorSolicitud(String idSolicitud);

    List<EntradaListaEsperaDTO> buscarPorTipoPrueba(String tipoPruebaId);

    List<EntradaListaEsperaDTO> buscarPorEstado(EstadoEntradaEspera estado);
}
