package es.uclm.esi.iso2.sescam.citas.presentacion;

import es.uclm.esi.iso2.sescam.common.dto.AgendaMedicaDTO;
import es.uclm.esi.iso2.sescam.common.dto.TurnoAgendaDTO;
import es.uclm.esi.iso2.sescam.common.enums.EstadoTurno;
import es.uclm.esi.iso2.sescam.common.enums.TipoRecurso;
import es.uclm.esi.iso2.sescam.common.interfaces.DisponibilidadAgendaService;
import es.uclm.esi.iso2.sescam.common.interfaces.GestionAgendaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Pruebas del controlador de presentación del CU-14 con los servicios de dominio simulados.
 */
@ExtendWith(MockitoExtension.class)
class AgendaControllerTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 11, 16);

    @Mock
    private GestionAgendaService gestion;
    @Mock
    private DisponibilidadAgendaService disponibilidad;

    private AgendaController controller;
    private AgendaMedicaDTO franja;
    private TurnoAgendaDTO turno;

    @BeforeEach
    void setUp() {
        controller = new AgendaController(gestion, disponibilidad);
        franja = new AgendaMedicaDTO("F1", "sala-1", null, FECHA, LocalTime.of(9, 0), LocalTime.of(10, 0), 30);
        turno = new TurnoAgendaDTO("F1#09:00", "F1", TipoRecurso.SALA, "sala-1", FECHA,
                LocalTime.of(9, 0), LocalTime.of(9, 30), EstadoTurno.LIBRE, null, null);
    }

    @Test
    void rechazaServiciosNulos() {
        assertThrows(NullPointerException.class, () -> new AgendaController(null, disponibilidad));
        assertThrows(NullPointerException.class, () -> new AgendaController(gestion, null));
    }

    @Test
    void delegaLasOperacionesDeFranja() {
        when(gestion.crearFranja(franja, TipoRecurso.SALA)).thenReturn(franja);
        when(gestion.modificarFranja(franja)).thenReturn(franja);
        when(gestion.eliminarAgenda("F1")).thenReturn(true);
        when(gestion.listarFranjas(TipoRecurso.SALA, "sala-1", FECHA)).thenReturn(List.of(franja));

        assertSame(franja, controller.altaFranja(franja, TipoRecurso.SALA));
        assertSame(franja, controller.modificarFranja(franja));
        assertTrue(controller.eliminarFranja("F1"));
        assertEquals(List.of(franja), controller.listarFranjas(TipoRecurso.SALA, "sala-1", FECHA));
    }

    @Test
    void delegaConsultasYBloqueos() {
        when(gestion.consultarAgenda(TipoRecurso.SALA, "sala-1", FECHA)).thenReturn(List.of(turno));
        when(disponibilidad.consultarHuecosLibres(TipoRecurso.SALA, "sala-1", FECHA)).thenReturn(List.of(turno));
        when(gestion.bloquearTurno("F1#09:00", "Avería")).thenReturn(turno);
        when(gestion.desbloquearTurno("F1#09:00", "Reparado")).thenReturn(turno);
        when(gestion.bloquearFranja("F1", "Avería")).thenReturn(List.of(turno));

        assertEquals(List.of(turno), controller.consultarAgenda(TipoRecurso.SALA, "sala-1", FECHA));
        assertEquals(List.of(turno), controller.consultarHuecosLibres(TipoRecurso.SALA, "sala-1", FECHA));
        assertSame(turno, controller.bloquearTurno("F1#09:00", "Avería"));
        assertSame(turno, controller.desbloquearTurno("F1#09:00", "Reparado"));
        assertEquals(List.of(turno), controller.bloquearFranja("F1", "Avería"));
    }
}
