package es.uclm.esi.iso2.sescam.citas.persistencia;

import es.uclm.esi.iso2.sescam.common.dto.EntradaListaEsperaDTO;
import es.uclm.esi.iso2.sescam.common.enums.EstadoEntradaEspera;
import es.uclm.esi.iso2.sescam.common.enums.Prioridad;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del DAO simulado en memoria de la lista de espera (CU-15).
 */
class ListaEsperaRepositoryInMemoryTest {

    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 10, 1, 9, 0);

    @Test
    void guardaBuscaYFiltraPorTipoYEstado() {
        ListaEsperaRepositoryInMemory repo = new ListaEsperaRepositoryInMemory();
        EntradaListaEsperaDTO tac = entrada("S1", "TAC", EstadoEntradaEspera.EN_ESPERA);
        EntradaListaEsperaDTO rm = entrada("S2", "RM", EstadoEntradaEspera.EN_ESPERA);
        EntradaListaEsperaDTO retirada = entrada("S3", "TAC", EstadoEntradaEspera.RETIRADA);
        repo.guardar(tac);
        repo.guardar(rm);
        repo.guardar(retirada);

        assertEquals(tac, repo.buscarPorSolicitud("S1").orElseThrow());
        assertTrue(repo.buscarPorSolicitud("NO").isEmpty());
        assertTrue(repo.buscarPorSolicitud(null).isEmpty());
        assertEquals(2, repo.buscarPorTipoPrueba("TAC").size());
        assertTrue(repo.buscarPorTipoPrueba("US").isEmpty());
        assertEquals(List.of(retirada), repo.buscarPorEstado(EstadoEntradaEspera.RETIRADA));
        assertEquals(2, repo.buscarPorEstado(EstadoEntradaEspera.EN_ESPERA).size());
    }

    @Test
    void guardarReemplazaLaEntradaDeLaMismaSolicitudYRechazaNulos() {
        ListaEsperaRepositoryInMemory repo = new ListaEsperaRepositoryInMemory();
        repo.guardar(entrada("S1", "TAC", EstadoEntradaEspera.EN_ESPERA));
        EntradaListaEsperaDTO retirada = entrada("S1", "TAC", EstadoEntradaEspera.RETIRADA);

        assertEquals(retirada, repo.guardar(retirada));
        assertEquals(retirada, repo.buscarPorSolicitud("S1").orElseThrow());
        assertTrue(repo.buscarPorEstado(EstadoEntradaEspera.EN_ESPERA).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> repo.guardar(null));
    }

    private static EntradaListaEsperaDTO entrada(String id, String tipo, EstadoEntradaEspera estado) {
        return new EntradaListaEsperaDTO(id, "pac-" + id, tipo, Prioridad.ORDINARIO, FECHA, FECHA.plusDays(1),
                estado, null, null);
    }
}
