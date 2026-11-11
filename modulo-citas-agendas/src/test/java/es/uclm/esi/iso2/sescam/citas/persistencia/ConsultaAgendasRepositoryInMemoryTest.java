package es.uclm.esi.iso2.sescam.citas.persistencia;

import es.uclm.esi.iso2.sescam.common.dto.TurnoAgendaDTO;
import es.uclm.esi.iso2.sescam.common.enums.EstadoTurno;
import es.uclm.esi.iso2.sescam.common.enums.TipoRecurso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConsultaAgendasRepositoryInMemoryTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 11, 20);

    private TurnoRepositoryInMemory turnos;
    private ConsultaAgendasRepositoryInMemory repositorio;

    @BeforeEach
    void setUp() {
	turnos = new TurnoRepositoryInMemory();
	repositorio = new ConsultaAgendasRepositoryInMemory(turnos);
    }

    @Test
    void consultaSoloTurnosLibresDelRecursoYFechaOrdenadosPorHora() {
	TurnoAgendaDTO libreTarde = turno("A1#10:00", TipoRecurso.MEDICO, "MED-1",
		LocalTime.of(10, 0), EstadoTurno.LIBRE);
	TurnoAgendaDTO ocupado = turno("A1#09:30", TipoRecurso.MEDICO, "MED-1",
		LocalTime.of(9, 30), EstadoTurno.OCUPADO);
	TurnoAgendaDTO libreManana = turno("A1#09:00", TipoRecurso.MEDICO, "MED-1",
		LocalTime.of(9, 0), EstadoTurno.LIBRE);
	TurnoAgendaDTO bloqueado = turno("A1#09:15", TipoRecurso.MEDICO, "MED-1",
		LocalTime.of(9, 15), EstadoTurno.BLOQUEADO);
	TurnoAgendaDTO otroMedico = turno("A2#09:00", TipoRecurso.MEDICO, "MED-2",
		LocalTime.of(9, 0), EstadoTurno.LIBRE);
	TurnoAgendaDTO otraFecha = turno("A3#09:00", TipoRecurso.MEDICO, "MED-1",
		LocalTime.of(9, 0), EstadoTurno.LIBRE, FECHA.plusDays(1));
	turnos.guardarTodos(List.of(libreTarde, ocupado, libreManana, bloqueado, otroMedico, otraFecha));

	assertEquals(List.of(libreManana, libreTarde),
		repositorio.consultarAgendasDisponibles(TipoRecurso.MEDICO, "MED-1", FECHA));
    }

    @Test
    void consultaSinResultadosDevuelveListaVacia() {
	assertEquals(List.of(), repositorio.consultarAgendasDisponibles(TipoRecurso.SALA, "SALA-1", FECHA));
    }

    @Test
    void consultaValidaTipoRecursoIdentificadorYFecha() {
	assertThrows(IllegalArgumentException.class,
		() -> repositorio.consultarAgendasDisponibles(null, "MED-1", FECHA));
	assertThrows(IllegalArgumentException.class,
		() -> repositorio.consultarAgendasDisponibles(TipoRecurso.MEDICO, " ", FECHA));
	assertThrows(IllegalArgumentException.class,
		() -> repositorio.consultarAgendasDisponibles(TipoRecurso.MEDICO, "MED-1", null));
    }

    @Test
    void constructoresExigenRepositorioYOfrecenAlmacenamientoEnMemoria() {
	assertThrows(NullPointerException.class, () -> new ConsultaAgendasRepositoryInMemory(null));
	assertEquals(List.of(), new ConsultaAgendasRepositoryInMemory()
		.consultarAgendasDisponibles(TipoRecurso.MEDICO, "MED-1", FECHA));
    }

    private static TurnoAgendaDTO turno(String id, TipoRecurso tipo, String recurso,
					LocalTime inicio, EstadoTurno estado) {
	return turno(id, tipo, recurso, inicio, estado, FECHA);
    }

    private static TurnoAgendaDTO turno(String id, TipoRecurso tipo, String recurso,
					LocalTime inicio, EstadoTurno estado, LocalDate fecha) {
	String idCita = estado == EstadoTurno.OCUPADO ? "CITA-1" : null;
	return new TurnoAgendaDTO(id, id.split("#")[0], tipo, recurso, fecha, inicio,
		inicio.plusMinutes(30), estado, null, idCita);
    }
}
