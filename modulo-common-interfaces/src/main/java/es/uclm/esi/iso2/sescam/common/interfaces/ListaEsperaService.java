package es.uclm.esi.iso2.sescam.common.interfaces;

import java.util.List;

import es.uclm.esi.iso2.sescam.common.dto.ListaEsperaDTO;

/**
 * Contrato de servicios para la gestión de Listas de Espera (CU-12).
 */
public interface ListaEsperaService {
    ListaEsperaDTO registrarEnListaEspera(ListaEsperaDTO entrada);
    boolean eliminarDeListaEspera(String id);
    ListaEsperaDTO obtenerEntradaPorId(String id);
    List<ListaEsperaDTO> listarPorTipoPrueba(String tipoPruebaId);
    List<ListaEsperaDTO> listarPorPaciente(String pacienteId);
}