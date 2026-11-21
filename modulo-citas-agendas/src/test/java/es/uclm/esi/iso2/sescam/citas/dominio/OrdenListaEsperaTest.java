package es.uclm.esi.iso2.sescam.citas.dominio;

import es.uclm.esi.iso2.sescam.common.dto.EntradaListaEsperaDTO;
import es.uclm.esi.iso2.sescam.common.enums.EstadoEntradaEspera;
import es.uclm.esi.iso2.sescam.common.enums.Prioridad;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del orden de la lista de espera según RN-01 (D-018).
 */
class OrdenListaEsperaTest {

    private static final LocalDateTime DIA_1 = LocalDateTime.of(2026, 10, 1, 9, 0);
    private static final LocalDateTime DIA_2 = DIA_1.plusDays(1);
    private static final LocalDateTime INSCRIPCION = LocalDateTime.of(2026, 11, 10, 8, 0);

    private final OrdenListaEspera orden = new OrdenListaEspera();

    @Test
    void laPrioridadPrevaleceSobreLaFechaDePrescripcion() {
        EntradaListaEsperaDTO ordinariaAntigua = entrada("S1", Prioridad.ORDINARIO, DIA_1, INSCRIPCION);
        EntradaListaEsperaDTO preferente = entrada("S2", Prioridad.PREFERENTE, DIA_2, INSCRIPCION);
        EntradaListaEsperaDTO urgenteReciente = entrada("S3", Prioridad.URGENTE, DIA_2.plusDays(5), INSCRIPCION);

        assertEquals(List.of("S3", "S2", "S1"), ordenar(ordinariaAntigua, preferente, urgenteReciente));
    }

    @Test
    void aIgualPrioridadRigeFifoPorFechaDePrescripcion() {
        EntradaListaEsperaDTO reciente = entrada("S1", Prioridad.PREFERENTE, DIA_2, INSCRIPCION.minusDays(3));
        EntradaListaEsperaDTO antigua = entrada("S2", Prioridad.PREFERENTE, DIA_1, INSCRIPCION);

        assertEquals(List.of("S2", "S1"), ordenar(reciente, antigua));
    }

    @Test
    void aIgualFechaDePrescripcionDesempataLaInscripcionYDespuesElIdentificador() {
        EntradaListaEsperaDTO inscritaTarde = entrada("S1", Prioridad.URGENTE, DIA_1, INSCRIPCION.plusHours(1));
        EntradaListaEsperaDTO inscritaPronto = entrada("S2", Prioridad.URGENTE, DIA_1, INSCRIPCION);
        EntradaListaEsperaDTO mismoInstanteB = entrada("S4", Prioridad.URGENTE, DIA_2, INSCRIPCION);
        EntradaListaEsperaDTO mismoInstanteA = entrada("S3", Prioridad.URGENTE, DIA_2, INSCRIPCION);

        assertEquals(List.of("S2", "S1", "S3", "S4"),
                ordenar(inscritaTarde, mismoInstanteB, inscritaPronto, mismoInstanteA));
    }

    @Test
    void unCambioDePrioridadRecolocaLaEntradaPorAntiguedadEnElNuevoNivel() {
        EntradaListaEsperaDTO urgenteAntigua = entrada("S1", Prioridad.URGENTE, DIA_1, INSCRIPCION);
        EntradaListaEsperaDTO urgenteReciente = entrada("S2", Prioridad.URGENTE, DIA_2.plusDays(1), INSCRIPCION);
        EntradaListaEsperaDTO ordinaria = entrada("S3", Prioridad.ORDINARIO, DIA_2, INSCRIPCION);
        assertEquals(List.of("S1", "S2", "S3"), ordenar(urgenteAntigua, urgenteReciente, ordinaria));

        EntradaListaEsperaDTO ascendida = entrada("S3", Prioridad.URGENTE, DIA_2, INSCRIPCION);

        assertEquals(List.of("S1", "S3", "S2"), ordenar(urgenteAntigua, urgenteReciente, ascendida));
    }

    @Test
    void entradasEquivalentesSonIgualesYLasListasVaciasOUnitariasNoCambian() {
        EntradaListaEsperaDTO a = entrada("S1", Prioridad.ORDINARIO, DIA_1, INSCRIPCION);
        assertEquals(0, orden.compare(a, entrada("S1", Prioridad.ORDINARIO, DIA_1, INSCRIPCION)));
        assertTrue(ordenar().isEmpty());
        assertEquals(List.of("S1"), ordenar(a));
    }

    @Test
    void elRangoDeLaPrioridadEsExplicito() {
        assertEquals(0, OrdenListaEspera.rango(Prioridad.URGENTE));
        assertEquals(1, OrdenListaEspera.rango(Prioridad.PREFERENTE));
        assertEquals(2, OrdenListaEspera.rango(Prioridad.ORDINARIO));
    }

    private List<String> ordenar(EntradaListaEsperaDTO... entradas) {
        List<EntradaListaEsperaDTO> lista = new ArrayList<>(List.of(entradas));
        lista.sort(orden);
        return lista.stream().map(EntradaListaEsperaDTO::idSolicitud).toList();
    }

    private static EntradaListaEsperaDTO entrada(String id, Prioridad prioridad, LocalDateTime prescripcion,
                                                 LocalDateTime inscripcion) {
        return new EntradaListaEsperaDTO(id, "pac-" + id, "TAC", prioridad, prescripcion, inscripcion,
                EstadoEntradaEspera.EN_ESPERA, null, null);
    }
}
