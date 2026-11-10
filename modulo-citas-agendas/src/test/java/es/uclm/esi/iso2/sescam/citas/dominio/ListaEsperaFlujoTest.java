package es.uclm.esi.iso2.sescam.citas.dominio;

import es.uclm.esi.iso2.sescam.citas.persistencia.ListaEsperaRepositoryInMemory;
import es.uclm.esi.iso2.sescam.common.dto.AgendaMedicaDTO;
import es.uclm.esi.iso2.sescam.common.dto.EntradaListaEsperaDTO;
import es.uclm.esi.iso2.sescam.common.dto.SolicitudPruebaDTO;
import es.uclm.esi.iso2.sescam.common.enums.EstadoEntradaEspera;
import es.uclm.esi.iso2.sescam.common.enums.EstadoSolicitud;
import es.uclm.esi.iso2.sescam.common.enums.EstadoTurno;
import es.uclm.esi.iso2.sescam.common.enums.Prioridad;
import es.uclm.esi.iso2.sescam.common.enums.TipoRecurso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de flujo del CU-15 sobre los DAOs en memoria y las agendas reales de CU-14.
 */
class ListaEsperaFlujoTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 11, 16);
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 10, 9, 0);

    private AgendaServiceImpl agendas;
    private ListaEsperaServiceImpl listaEspera;

    @BeforeEach
    void setUp() {
        agendas = new AgendaServiceImpl();
        listaEspera = new ListaEsperaServiceImpl(new ListaEsperaRepositoryInMemory(), agendas, agendas,
                Clock.fixed(AHORA.toInstant(ZoneOffset.UTC), ZoneOffset.UTC));
        agendas.crearFranja(new AgendaMedicaDTO("TAC-M", "sala-tac", "TAC", FECHA, LocalTime.of(8, 0),
                LocalTime.of(9, 0), 30), TipoRecurso.SALA);
        agendas.crearFranja(new AgendaMedicaDTO("GEN-M", "sala-gen", null, FECHA, LocalTime.of(8, 0),
                LocalTime.of(9, 0), 30), TipoRecurso.SALA);
    }

    @Test
    void flujoCompletoDeListaEsperaConHuecosDeAgenda() {
        listaEspera.inscribir(solicitud("S1", "TAC", Prioridad.ORDINARIO, dia(10, 1)));
        listaEspera.inscribir(solicitud("S2", "TAC", Prioridad.URGENTE, dia(10, 5)));
        listaEspera.inscribir(solicitud("S3", "TAC", Prioridad.PREFERENTE, dia(10, 2)));
        listaEspera.inscribir(solicitud("S4", "TAC", Prioridad.ORDINARIO, dia(9, 20)));
        listaEspera.inscribir(solicitud("S5", "RM", Prioridad.URGENTE, dia(10, 3)));

        assertEquals(List.of("S2", "S3", "S4", "S1"), ids(listaEspera.consultarLista("TAC")));
        assertEquals(4, listaEspera.consultarPosicion("S1"));

        // Cambio de prioridad: S1 sube a PREFERENTE y, por ser más antigua que S3, la adelanta.
        listaEspera.cambiarPrioridad("S1", Prioridad.PREFERENTE, "Empeoramiento clínico");
        assertEquals(List.of("S2", "S1", "S3", "S4"), ids(listaEspera.consultarLista("TAC")));

        // Huecos de CU-14: la franja de TAC solo admite TAC; la genérica admite cualquier prueba.
        assertEquals(2, agendas.consultarHuecosLibres(TipoRecurso.SALA, "sala-tac", FECHA).size());
        assertEquals(List.of("S2", "S1"), ids(listaEspera.proponerCandidatos("TAC-M#08:00", 2)));
        assertEquals(List.of("S5", "S2", "S1", "S3", "S4"),
                ids(listaEspera.proponerCandidatos("GEN-M#08:00", 10)));
        assertEquals(EstadoTurno.LIBRE, agendas.obtenerTurno("TAC-M#08:00").estado());

        // CU-13 (simulado) consolida la cita: el turno se ocupa y S2 sale de la espera.
        agendas.ocuparTurno("TAC-M#08:00", "C-1");
        listaEspera.marcarCitada("S2", "C-1");
        assertEquals(List.of("S1", "S3", "S4"), ids(listaEspera.consultarLista("TAC")));
        assertThrows(IllegalStateException.class, () -> listaEspera.proponerCandidatos("TAC-M#08:00", 1));

        // CU-13 (simulado) cancela la cita: el turno se libera y S2 recupera su posición.
        agendas.liberarTurno("TAC-M#08:00");
        listaEspera.reincorporar("S2", "Cita cancelada");
        assertEquals(1, listaEspera.consultarPosicion("S2"));

        // Retirada: la entrada sale de la lista pero se conserva con su motivo.
        listaEspera.retirar("S4", "Prueba realizada en otro centro");
        assertEquals(List.of("S2", "S1", "S3"), ids(listaEspera.consultarLista("TAC")));
        assertEquals(EstadoEntradaEspera.RETIRADA, listaEspera.obtenerEntrada("S4").estado());
        assertThrows(IllegalStateException.class, () -> listaEspera.retirar("S4", "otra vez"));
        assertThrows(IllegalArgumentException.class,
                () -> listaEspera.inscribir(solicitud("S4", "TAC", Prioridad.URGENTE, dia(11, 1))));
    }

    @Test
    void listaVaciaNoProponeCandidatos() {
        assertTrue(listaEspera.consultarLista("TAC").isEmpty());
        assertTrue(listaEspera.proponerCandidatos("TAC-M#08:30", 5).isEmpty());
        assertTrue(listaEspera.proponerCandidatos("GEN-M#08:30", 5).isEmpty());
    }

    @Test
    void mismaPrioridadYMismaFechaDesempataDeFormaEstable() {
        listaEspera.inscribir(solicitud("S-B", "TAC", Prioridad.URGENTE, dia(10, 1)));
        listaEspera.inscribir(solicitud("S-A", "TAC", Prioridad.URGENTE, dia(10, 1)));

        assertEquals(List.of("S-A", "S-B"), ids(listaEspera.consultarLista("TAC")));
    }

    private static SolicitudPruebaDTO solicitud(String id, String tipo, Prioridad prioridad, LocalDateTime fecha) {
        return new SolicitudPruebaDTO(id, "pac-" + id, "med-1", tipo, prioridad, "Indicación", "Justificación",
                fecha, EstadoSolicitud.PENDIENTE_CITACION, false, null);
    }

    private static LocalDateTime dia(int mes, int dia) {
        return LocalDateTime.of(2026, mes, dia, 10, 0);
    }

    private static List<String> ids(List<EntradaListaEsperaDTO> entradas) {
        return entradas.stream().map(EntradaListaEsperaDTO::idSolicitud).toList();
    }
}
