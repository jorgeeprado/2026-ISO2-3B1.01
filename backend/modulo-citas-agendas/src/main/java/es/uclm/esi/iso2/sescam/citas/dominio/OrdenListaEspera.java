package es.uclm.esi.iso2.sescam.citas.dominio;

import es.uclm.esi.iso2.sescam.common.dto.EntradaListaEsperaDTO;
import es.uclm.esi.iso2.sescam.common.enums.Prioridad;

import java.util.Comparator;

/**
 * Orden de la lista de espera según RN-01 (D-018):
 * <ol>
 *     <li>prioridad clínica: URGENTE, PREFERENTE, ORDINARIO;</li>
 *     <li>FIFO por fecha de prescripción, de más antigua a más reciente;</li>
 *     <li>desempate por fecha de inscripción;</li>
 *     <li>desempate final por identificador de solicitud, para que el orden sea siempre el mismo.</li>
 * </ol>
 * El rango de cada prioridad es explícito y no depende del {@code ordinal()} del enum.
 */
public class OrdenListaEspera implements Comparator<EntradaListaEsperaDTO> {

    private static final Comparator<EntradaListaEsperaDTO> ORDEN = Comparator
            .comparingInt((EntradaListaEsperaDTO e) -> rango(e.prioridad()))
            .thenComparing(EntradaListaEsperaDTO::fechaPrescripcion)
            .thenComparing(EntradaListaEsperaDTO::fechaInscripcion)
            .thenComparing(EntradaListaEsperaDTO::idSolicitud);

    @Override
    public int compare(EntradaListaEsperaDTO a, EntradaListaEsperaDTO b) {
        return ORDEN.compare(a, b);
    }

    /** Rango de la prioridad: cuanto menor, antes se atiende. */
    static int rango(Prioridad prioridad) {
        return switch (prioridad) {
            case URGENTE -> 0;
            case PREFERENTE -> 1;
            case ORDINARIO -> 2;
        };
    }
}
