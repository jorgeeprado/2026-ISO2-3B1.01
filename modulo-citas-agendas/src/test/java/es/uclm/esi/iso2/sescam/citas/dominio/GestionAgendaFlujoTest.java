package es.uclm.esi.iso2.sescam.citas.dominio;

import es.uclm.esi.iso2.sescam.common.dto.AgendaMedicaDTO;
import es.uclm.esi.iso2.sescam.common.dto.TurnoAgendaDTO;
import es.uclm.esi.iso2.sescam.common.enums.EstadoTurno;
import es.uclm.esi.iso2.sescam.common.enums.TipoRecurso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de flujo del CU-14 sobre el DAO simulado en memoria (escenarios completos del caso de uso).
 */
class GestionAgendaFlujoTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 11, 16);

    private AgendaServiceImpl servicio;

    @BeforeEach
    void setUp() {
        servicio = new AgendaServiceImpl();
    }

    @Test
    void flujoCompletoDeFranjaBloqueoYOcupacion() {
        servicio.crearFranja(franja("SALA-TAC-M", "sala-tac", 8, 0, 10, 0, 30), TipoRecurso.SALA);
        assertEquals(4, servicio.consultarHuecosLibres(TipoRecurso.SALA, "sala-tac", FECHA).size());

        servicio.bloquearTurno("SALA-TAC-M#08:30", "Calibración");
        servicio.ocuparTurno("SALA-TAC-M#09:00", "cita-1");

        List<TurnoAgendaDTO> agenda = servicio.consultarAgenda(TipoRecurso.SALA, "sala-tac", FECHA);
        assertEquals(List.of(EstadoTurno.LIBRE, EstadoTurno.BLOQUEADO, EstadoTurno.OCUPADO, EstadoTurno.LIBRE),
                agenda.stream().map(TurnoAgendaDTO::estado).toList());
        assertEquals(2, servicio.consultarHuecosLibres(TipoRecurso.SALA, "sala-tac", FECHA).size());

        assertThrows(IllegalStateException.class, () -> servicio.eliminarAgenda("SALA-TAC-M"));
        assertThrows(IllegalStateException.class,
                () -> servicio.modificarFranja(franja("SALA-TAC-M", "sala-tac", 8, 0, 11, 0, 30)));

        servicio.liberarTurno("SALA-TAC-M#09:00");
        TurnoAgendaDTO desbloqueado = servicio.desbloquearTurno("SALA-TAC-M#08:30", "Calibración finalizada");
        assertEquals("Calibración finalizada", desbloqueado.motivo());
        assertEquals(4, servicio.consultarHuecosLibres(TipoRecurso.SALA, "sala-tac", FECHA).size());

        assertTrue(servicio.eliminarAgenda("SALA-TAC-M"));
        assertTrue(servicio.consultarAgenda(TipoRecurso.SALA, "sala-tac", FECHA).isEmpty());
        assertNull(servicio.obtenerTurno("SALA-TAC-M#08:00"));
    }

    @Test
    void franjasContiguasNoSeSolapanYLosRecursosSonIndependientes() {
        servicio.crearFranja(franja("M1", "rec-1", 8, 0, 10, 0, 15), TipoRecurso.MEDICO);

        assertDoesNotThrow(() -> servicio.crearFranja(franja("M2", "rec-1", 10, 0, 12, 0, 15), TipoRecurso.MEDICO));
        assertDoesNotThrow(() -> servicio.crearFranja(franja("S1", "rec-1", 8, 0, 10, 0, 15), TipoRecurso.SALA));
        assertDoesNotThrow(() -> servicio.crearFranja(
                new AgendaMedicaDTO("M3", "rec-1", null, FECHA.plusDays(1), LocalTime.of(8, 0),
                        LocalTime.of(10, 0), 15), TipoRecurso.MEDICO));
        assertThrows(IllegalStateException.class,
                () -> servicio.crearFranja(franja("M4", "rec-1", 11, 45, 12, 30, 15), TipoRecurso.MEDICO));

        assertEquals(List.of("M1", "M2"), servicio.listarFranjas(TipoRecurso.MEDICO, "rec-1", FECHA).stream()
                .map(AgendaMedicaDTO::idAgenda).toList());
    }

    @Test
    void modificarFranjaNoPuedeProvocarSolape() {
        servicio.crearFranja(franja("M1", "medico-1", 8, 0, 10, 0, 15), TipoRecurso.MEDICO);
        servicio.crearFranja(franja("M2", "medico-1", 10, 0, 12, 0, 15), TipoRecurso.MEDICO);

        assertThrows(IllegalStateException.class,
                () -> servicio.modificarFranja(franja("M2", "medico-1", 9, 30, 12, 0, 15)));
        assertEquals(LocalTime.of(10, 0), servicio.obtenerAgendaPorId("M2").horaInicio());
        assertEquals(8, servicio.consultarAgenda(TipoRecurso.MEDICO, "medico-1", FECHA).stream()
                .filter(t -> t.idAgenda().equals("M2")).count());
    }

    @Test
    void generaTurnosIgnorandoElRestoQueNoCompletaUnTurno() {
        servicio.crearFranja(franja("M1", "medico-1", 9, 0, 10, 10, 20), TipoRecurso.MEDICO);

        List<TurnoAgendaDTO> turnos = servicio.consultarAgenda(TipoRecurso.MEDICO, "medico-1", FECHA);

        assertEquals(3, turnos.size());
        assertEquals(LocalTime.of(10, 0), turnos.get(2).horaFin());
    }

    @Test
    void crearAgendaHeredadaUsaTipoMedicoYGeneraTurnos() {
        servicio.crearAgenda(franja("A1", "medico-1", 9, 0, 10, 0, 15));

        assertEquals(4, servicio.consultarHuecosLibres(TipoRecurso.MEDICO, "medico-1", FECHA).size());
        assertTrue(servicio.consultarHuecosLibres(TipoRecurso.SALA, "medico-1", FECHA).isEmpty());
        assertFalse(servicio.listarAgendasPorMedico("medico-1").isEmpty());
    }

    private static AgendaMedicaDTO franja(String id, String recurso, int hIni, int mIni, int hFin, int mFin,
                                          int intervalo) {
        return new AgendaMedicaDTO(id, recurso, null, FECHA, LocalTime.of(hIni, mIni), LocalTime.of(hFin, mFin),
                intervalo);
    }
}
