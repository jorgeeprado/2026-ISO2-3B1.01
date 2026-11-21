package es.uclm.esi.iso2.sescam.citas.presentacion;

import es.uclm.esi.iso2.sescam.common.dto.EntradaListaEsperaDTO;
import es.uclm.esi.iso2.sescam.common.dto.SolicitudPruebaDTO;
import es.uclm.esi.iso2.sescam.common.dto.TurnoAgendaDTO;
import es.uclm.esi.iso2.sescam.common.enums.EstadoEntradaEspera;
import es.uclm.esi.iso2.sescam.common.enums.EstadoTurno;
import es.uclm.esi.iso2.sescam.common.enums.Prioridad;
import es.uclm.esi.iso2.sescam.common.enums.TipoRecurso;
import es.uclm.esi.iso2.sescam.common.interfaces.CandidatosListaEsperaService;
import es.uclm.esi.iso2.sescam.common.interfaces.DisponibilidadAgendaService;
import es.uclm.esi.iso2.sescam.common.interfaces.GestionListaEsperaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Pruebas del controlador de presentación del CU-15 con los servicios de dominio simulados.
 */
@ExtendWith(MockitoExtension.class)
class ListaEsperaControllerTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 11, 16);

    @Mock
    private GestionListaEsperaService gestion;
    @Mock
    private CandidatosListaEsperaService candidatos;
    @Mock
    private DisponibilidadAgendaService disponibilidad;

    private ListaEsperaController controller;
    private EntradaListaEsperaDTO entrada;

    @BeforeEach
    void setUp() {
        controller = new ListaEsperaController(gestion, candidatos, disponibilidad);
        entrada = new EntradaListaEsperaDTO("S1", "pac-1", "TAC", Prioridad.URGENTE,
                LocalDateTime.of(2026, 10, 1, 9, 0), LocalDateTime.of(2026, 11, 10, 9, 0),
                EstadoEntradaEspera.EN_ESPERA, null, null);
    }

    @Test
    void rechazaServiciosNulos() {
        assertThrows(NullPointerException.class, () -> new ListaEsperaController(null, candidatos, disponibilidad));
        assertThrows(NullPointerException.class, () -> new ListaEsperaController(gestion, null, disponibilidad));
        assertThrows(NullPointerException.class, () -> new ListaEsperaController(gestion, candidatos, null));
    }

    @Test
    void delegaLaGestionDeLaLista() {
        SolicitudPruebaDTO solicitud = new SolicitudPruebaDTO();
        when(gestion.inscribir(solicitud)).thenReturn(entrada);
        when(gestion.cambiarPrioridad("S1", Prioridad.PREFERENTE, "m")).thenReturn(entrada);
        when(gestion.retirar("S1", "m")).thenReturn(entrada);
        when(gestion.consultarLista("TAC")).thenReturn(List.of(entrada));
        when(gestion.consultarPosicion("S1")).thenReturn(3);
        when(gestion.obtenerEntrada("S1")).thenReturn(entrada);

        assertSame(entrada, controller.inscribir(solicitud));
        assertSame(entrada, controller.cambiarPrioridad("S1", Prioridad.PREFERENTE, "m"));
        assertSame(entrada, controller.retirar("S1", "m"));
        assertEquals(List.of(entrada), controller.consultarLista("TAC"));
        assertEquals(3, controller.consultarPosicion("S1"));
        assertSame(entrada, controller.obtenerEntrada("S1"));
    }

    @Test
    void delegaHuecosYCandidatos() {
        TurnoAgendaDTO turno = new TurnoAgendaDTO("F1#09:00", "F1", TipoRecurso.SALA, "sala-1", FECHA,
                LocalTime.of(9, 0), LocalTime.of(9, 30), EstadoTurno.LIBRE, null, null);
        when(disponibilidad.consultarHuecosLibres(TipoRecurso.SALA, "sala-1", FECHA)).thenReturn(List.of(turno));
        when(candidatos.proponerCandidatos("F1#09:00", 5)).thenReturn(List.of(entrada));

        assertEquals(List.of(turno), controller.consultarHuecosLibres(TipoRecurso.SALA, "sala-1", FECHA));
        assertEquals(List.of(entrada), controller.proponerCandidatos("F1#09:00", 5));
    }
}
