package es.uclm.esi.iso2.sescam.citas.persistencia;

import es.uclm.esi.iso2.sescam.common.dto.AgendaMedicaDTO;
import es.uclm.esi.iso2.sescam.common.dto.TurnoAgendaDTO;
import es.uclm.esi.iso2.sescam.common.enums.EstadoTurno;
import es.uclm.esi.iso2.sescam.common.enums.TipoRecurso;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de los DAOs simulados en memoria del CU-14.
 */
class RepositoriosInMemoryTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 11, 16);

    @Test
    void franjaRepositoryFiltraPorRecursoTipoYFecha() {
        FranjaRepositoryInMemory repo = new FranjaRepositoryInMemory();
        FranjaRegistro medico = registro("F1", "r1", FECHA, TipoRecurso.MEDICO);
        FranjaRegistro sala = registro("F2", "r1", FECHA, TipoRecurso.SALA);
        FranjaRegistro otraFecha = registro("F3", "r1", FECHA.plusDays(1), TipoRecurso.MEDICO);
        repo.guardar(medico);
        repo.guardar(sala);
        repo.guardar(otraFecha);

        assertEquals(List.of(medico), repo.buscarPorRecursoYFecha(TipoRecurso.MEDICO, "r1", FECHA));
        assertEquals(2, repo.buscarPorRecurso(TipoRecurso.MEDICO, "r1").size());
        assertEquals(2, repo.buscarPorFecha(FECHA).size());
        assertEquals(sala, repo.buscarPorId("F2").orElseThrow());
        assertTrue(repo.buscarPorId(null).isEmpty());
        assertTrue(repo.eliminar("F2"));
        assertFalse(repo.eliminar("F2"));
        assertFalse(repo.eliminar(null));
        assertThrows(IllegalArgumentException.class, () -> repo.guardar(null));
    }

    @Test
    void franjaRegistroExigeFranjaYTipo() {
        AgendaMedicaDTO franja = registro("F1", "r1", FECHA, TipoRecurso.SALA).franja();
        assertThrows(NullPointerException.class, () -> new FranjaRegistro(null, TipoRecurso.SALA));
        assertThrows(NullPointerException.class, () -> new FranjaRegistro(franja, null));
    }

    @Test
    void turnoRepositoryGuardaBuscaYEliminaPorAgenda() {
        TurnoRepositoryInMemory repo = new TurnoRepositoryInMemory();
        TurnoAgendaDTO t1 = turno("F1", "r1", 9);
        TurnoAgendaDTO t2 = turno("F1", "r1", 10);
        TurnoAgendaDTO t3 = turno("F2", "r2", 9);
        repo.guardarTodos(List.of(t1, t2, t3));

        assertEquals(2, repo.buscarPorAgenda("F1").size());
        assertEquals(List.of(t3), repo.buscarPorRecursoYFecha(TipoRecurso.SALA, "r2", FECHA));
        assertTrue(repo.buscarPorRecursoYFecha(TipoRecurso.MEDICO, "r2", FECHA).isEmpty());
        assertEquals(t1, repo.buscarPorId(t1.idTurno()).orElseThrow());
        assertTrue(repo.buscarPorId(null).isEmpty());

        repo.eliminarPorAgenda("F1");

        assertTrue(repo.buscarPorAgenda("F1").isEmpty());
        assertEquals(1, repo.buscarPorAgenda("F2").size());
        assertThrows(IllegalArgumentException.class, () -> repo.guardar(null));
        assertThrows(IllegalArgumentException.class, () -> repo.guardarTodos(null));
    }

    private static FranjaRegistro registro(String id, String recurso, LocalDate fecha, TipoRecurso tipo) {
        return new FranjaRegistro(new AgendaMedicaDTO(id, recurso, null, fecha, LocalTime.of(9, 0),
                LocalTime.of(10, 0), 15), tipo);
    }

    private static TurnoAgendaDTO turno(String idAgenda, String recurso, int hora) {
        return new TurnoAgendaDTO(idAgenda + "#" + hora, idAgenda, TipoRecurso.SALA, recurso, FECHA,
                LocalTime.of(hora, 0), LocalTime.of(hora, 30), EstadoTurno.LIBRE, null, null);
    }
}
