package es.uclm.esi.iso2.sescam.citas.dominio;

import es.uclm.esi.iso2.sescam.citas.persistencia.FranjaRegistro;
import es.uclm.esi.iso2.sescam.citas.persistencia.IFranjaRepository;
import es.uclm.esi.iso2.sescam.citas.persistencia.ITurnoRepository;
import es.uclm.esi.iso2.sescam.common.dto.AgendaMedicaDTO;
import es.uclm.esi.iso2.sescam.common.dto.TurnoAgendaDTO;
import es.uclm.esi.iso2.sescam.common.enums.EstadoTurno;
import es.uclm.esi.iso2.sescam.common.enums.TipoRecurso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias del dominio del CU-14 aislando la persistencia con mocks (RNF-10).
 */
@ExtendWith(MockitoExtension.class)
class AgendaServiceImplMockTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 11, 16);

    @Mock
    private IFranjaRepository franjaRepository;
    @Mock
    private ITurnoRepository turnoRepository;
    @Captor
    private ArgumentCaptor<List<TurnoAgendaDTO>> turnosCaptor;
    @Captor
    private ArgumentCaptor<TurnoAgendaDTO> turnoCaptor;

    private AgendaServiceImpl servicio;

    @BeforeEach
    void setUp() {
        servicio = new AgendaServiceImpl(franjaRepository, turnoRepository);
    }

    @Test
    void constructorRechazaRepositoriosNulos() {
        assertThrows(NullPointerException.class, () -> new AgendaServiceImpl(null, turnoRepository));
        assertThrows(NullPointerException.class, () -> new AgendaServiceImpl(franjaRepository, null));
    }

    @Test
    void crearFranjaGuardaLaFranjaYGeneraLosTurnos() {
        AgendaMedicaDTO franja = franja("F1", "sala-1", 9, 0, 10, 0, 20);
        when(franjaRepository.buscarPorId("F1")).thenReturn(Optional.empty());
        when(franjaRepository.buscarPorRecursoYFecha(TipoRecurso.SALA, "sala-1", FECHA)).thenReturn(List.of());

        assertSame(franja, servicio.crearFranja(franja, TipoRecurso.SALA));

        verify(franjaRepository).guardar(new FranjaRegistro(franja, TipoRecurso.SALA));
        verify(turnoRepository).guardarTodos(turnosCaptor.capture());
        List<TurnoAgendaDTO> turnos = turnosCaptor.getValue();
        assertEquals(3, turnos.size());
        assertEquals("F1#09:00", turnos.get(0).idTurno());
        assertEquals(LocalTime.of(10, 0), turnos.get(2).horaFin());
        assertTrue(turnos.stream().allMatch(t -> t.estado() == EstadoTurno.LIBRE
                && t.tipoRecurso() == TipoRecurso.SALA && "sala-1".equals(t.idRecurso())));
    }

    @Test
    void crearFranjaRechazaSolapeConOtraFranjaDelMismoRecursoYFecha() {
        AgendaMedicaDTO nueva = franja("F2", "medico-1", 9, 30, 11, 0, 15);
        AgendaMedicaDTO existente = franja("F1", "medico-1", 9, 0, 10, 0, 15);
        when(franjaRepository.buscarPorId("F2")).thenReturn(Optional.empty());
        when(franjaRepository.buscarPorRecursoYFecha(TipoRecurso.MEDICO, "medico-1", FECHA))
                .thenReturn(List.of(new FranjaRegistro(existente, TipoRecurso.MEDICO)));

        assertThrows(IllegalStateException.class, () -> servicio.crearFranja(nueva, TipoRecurso.MEDICO));

        verify(franjaRepository, never()).guardar(any());
        verify(turnoRepository, never()).guardarTodos(anyList());
    }

    @Test
    void crearFranjaRechazaIdentificadorDuplicadoYTipoNulo() {
        AgendaMedicaDTO franja = franja("F1", "medico-1", 9, 0, 10, 0, 15);
        when(franjaRepository.buscarPorId("F1"))
                .thenReturn(Optional.of(new FranjaRegistro(franja, TipoRecurso.MEDICO)));

        assertThrows(IllegalArgumentException.class, () -> servicio.crearFranja(franja, TipoRecurso.MEDICO));
        assertThrows(IllegalArgumentException.class, () -> servicio.crearFranja(franja, null));
        verify(franjaRepository, never()).guardar(any());
    }

    @Test
    void crearFranjaRechazaFranjaSinTurnoCompleto() {
        AgendaMedicaDTO franja = franja("F1", "medico-1", 9, 0, 9, 10, 15);

        assertThrows(IllegalArgumentException.class, () -> servicio.crearFranja(franja, TipoRecurso.MEDICO));
        verifyNoInteractions(franjaRepository, turnoRepository);
    }

    @Test
    void modificarFranjaConTurnosOcupadosFalla() {
        AgendaMedicaDTO franja = franja("F1", "medico-1", 9, 0, 10, 0, 15);
        when(franjaRepository.buscarPorId("F1"))
                .thenReturn(Optional.of(new FranjaRegistro(franja, TipoRecurso.MEDICO)));
        when(turnoRepository.buscarPorAgenda("F1")).thenReturn(List.of(turno("F1", 9, 0, EstadoTurno.OCUPADO)));

        assertThrows(IllegalStateException.class, () -> servicio.modificarFranja(franja));
        verify(turnoRepository, never()).eliminarPorAgenda(any());
    }

    @Test
    void modificarFranjaInexistenteFalla() {
        when(franjaRepository.buscarPorId("F9")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> servicio.modificarFranja(franja("F9", "medico-1", 9, 0, 10, 0, 15)));
    }

    @Test
    void modificarFranjaRegeneraTurnosConservandoBloqueosCoincidentes() {
        AgendaMedicaDTO original = franja("F1", "medico-1", 9, 0, 10, 0, 30);
        AgendaMedicaDTO ampliada = franja("F1", "medico-1", 9, 0, 11, 0, 30);
        TurnoAgendaDTO bloqueado = new TurnoAgendaDTO("F1#09:30", "F1", TipoRecurso.MEDICO, "medico-1", FECHA,
                LocalTime.of(9, 30), LocalTime.of(10, 0), EstadoTurno.BLOQUEADO, "Sesión clínica", null);
        when(franjaRepository.buscarPorId("F1"))
                .thenReturn(Optional.of(new FranjaRegistro(original, TipoRecurso.MEDICO)));
        when(turnoRepository.buscarPorAgenda("F1"))
                .thenReturn(List.of(turno("F1", 9, 0, EstadoTurno.LIBRE), bloqueado));
        when(franjaRepository.buscarPorRecursoYFecha(TipoRecurso.MEDICO, "medico-1", FECHA))
                .thenReturn(List.of(new FranjaRegistro(original, TipoRecurso.MEDICO)));

        servicio.modificarFranja(ampliada);

        verify(franjaRepository).guardar(new FranjaRegistro(ampliada, TipoRecurso.MEDICO));
        verify(turnoRepository).eliminarPorAgenda("F1");
        verify(turnoRepository).guardarTodos(turnosCaptor.capture());
        List<TurnoAgendaDTO> turnos = turnosCaptor.getValue();
        assertEquals(4, turnos.size());
        assertEquals(EstadoTurno.BLOQUEADO, turnos.get(1).estado());
        assertEquals("Sesión clínica", turnos.get(1).motivo());
        assertEquals(1, turnos.stream().filter(t -> t.estado() == EstadoTurno.BLOQUEADO).count());
    }

    @Test
    void eliminarAgendaConTurnosOcupadosFallaYSinFranjaDevuelveFalse() {
        AgendaMedicaDTO franja = franja("F1", "medico-1", 9, 0, 10, 0, 15);
        when(franjaRepository.buscarPorId("F1"))
                .thenReturn(Optional.of(new FranjaRegistro(franja, TipoRecurso.MEDICO)));
        when(turnoRepository.buscarPorAgenda("F1")).thenReturn(List.of(turno("F1", 9, 0, EstadoTurno.OCUPADO)));
        when(franjaRepository.buscarPorId("F2")).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> servicio.eliminarAgenda("F1"));
        assertFalse(servicio.eliminarAgenda("F2"));
        verify(franjaRepository, never()).eliminar(any());
    }

    @Test
    void eliminarAgendaBorraTurnosYFranja() {
        AgendaMedicaDTO franja = franja("F1", "medico-1", 9, 0, 10, 0, 15);
        when(franjaRepository.buscarPorId("F1"))
                .thenReturn(Optional.of(new FranjaRegistro(franja, TipoRecurso.MEDICO)));
        when(turnoRepository.buscarPorAgenda("F1")).thenReturn(List.of(turno("F1", 9, 0, EstadoTurno.BLOQUEADO)));
        when(franjaRepository.eliminar("F1")).thenReturn(true);

        assertTrue(servicio.eliminarAgenda("F1"));
        verify(turnoRepository).eliminarPorAgenda("F1");
    }

    @Test
    void bloquearTurnoLibreGuardaEstadoYMotivo() {
        when(turnoRepository.buscarPorId("F1#09:00")).thenReturn(Optional.of(turno("F1", 9, 0, EstadoTurno.LIBRE)));
        when(turnoRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        TurnoAgendaDTO resultado = servicio.bloquearTurno("F1#09:00", "Mantenimiento del equipo");

        verify(turnoRepository).guardar(turnoCaptor.capture());
        assertEquals(EstadoTurno.BLOQUEADO, turnoCaptor.getValue().estado());
        assertEquals("Mantenimiento del equipo", resultado.motivo());
    }

    @Test
    void bloquearTurnoNoLibreFalla() {
        when(turnoRepository.buscarPorId("F1#09:00"))
                .thenReturn(Optional.of(turno("F1", 9, 0, EstadoTurno.OCUPADO)));

        assertThrows(IllegalStateException.class, () -> servicio.bloquearTurno("F1#09:00", "Motivo"));
        verify(turnoRepository, never()).guardar(any());
    }

    @Test
    void desbloquearTurnoNoBloqueadoFalla() {
        when(turnoRepository.buscarPorId("F1#09:00")).thenReturn(Optional.of(turno("F1", 9, 0, EstadoTurno.LIBRE)));

        assertThrows(IllegalStateException.class, () -> servicio.desbloquearTurno("F1#09:00", "Motivo"));
    }

    @Test
    void operacionesSobreTurnoInexistenteFallan() {
        when(turnoRepository.buscarPorId("X")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> servicio.bloquearTurno("X", "Motivo"));
        assertThrows(IllegalArgumentException.class, () -> servicio.ocuparTurno("X", "cita-1"));
        assertThrows(IllegalArgumentException.class, () -> servicio.liberarTurno("X"));
        assertNull(servicio.obtenerTurno("X"));
    }

    @Test
    void ocuparTurnoLibreAsociaLaCita() {
        when(turnoRepository.buscarPorId("F1#09:00")).thenReturn(Optional.of(turno("F1", 9, 0, EstadoTurno.LIBRE)));
        when(turnoRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        TurnoAgendaDTO ocupado = servicio.ocuparTurno("F1#09:00", "cita-7");

        assertEquals(EstadoTurno.OCUPADO, ocupado.estado());
        assertEquals("cita-7", ocupado.idCita());
    }

    @Test
    void ocuparTurnoNoLibreYLiberarTurnoNoOcupadoFallan() {
        when(turnoRepository.buscarPorId("F1#09:00"))
                .thenReturn(Optional.of(turno("F1", 9, 0, EstadoTurno.BLOQUEADO)));

        assertThrows(IllegalStateException.class, () -> servicio.ocuparTurno("F1#09:00", "cita-1"));
        assertThrows(IllegalStateException.class, () -> servicio.liberarTurno("F1#09:00"));
        verify(turnoRepository, never()).guardar(any());
    }

    @Test
    void liberarTurnoOcupadoLoDejaLibreSinCita() {
        when(turnoRepository.buscarPorId("F1#09:00"))
                .thenReturn(Optional.of(turno("F1", 9, 0, EstadoTurno.OCUPADO)));
        when(turnoRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        TurnoAgendaDTO libre = servicio.liberarTurno("F1#09:00");

        assertEquals(EstadoTurno.LIBRE, libre.estado());
        assertNull(libre.idCita());
    }

    @Test
    void bloquearFranjaSoloBloqueaLosTurnosLibres() {
        AgendaMedicaDTO franja = franja("F1", "sala-1", 9, 0, 10, 0, 20);
        when(franjaRepository.buscarPorId("F1")).thenReturn(Optional.of(new FranjaRegistro(franja, TipoRecurso.SALA)));
        when(turnoRepository.buscarPorAgenda("F1")).thenReturn(List.of(
                turno("F1", 9, 40, EstadoTurno.LIBRE),
                turno("F1", 9, 0, EstadoTurno.LIBRE),
                turno("F1", 9, 20, EstadoTurno.OCUPADO)));
        when(turnoRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        List<TurnoAgendaDTO> bloqueados = servicio.bloquearFranja("F1", "Avería del TAC");

        assertEquals(2, bloqueados.size());
        assertEquals(LocalTime.of(9, 0), bloqueados.get(0).horaInicio());
        assertTrue(bloqueados.stream().allMatch(t -> t.estado() == EstadoTurno.BLOQUEADO
                && "Avería del TAC".equals(t.motivo())));
    }

    @Test
    void bloquearFranjaInexistenteFalla() {
        when(franjaRepository.buscarPorId("F9")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> servicio.bloquearFranja("F9", "Motivo"));
    }

    @Test
    void consultarHuecosLibresFiltraYOrdenaPorHora() {
        when(turnoRepository.buscarPorRecursoYFecha(TipoRecurso.SALA, "sala-1", FECHA)).thenReturn(List.of(
                turno("F1", 10, 0, EstadoTurno.LIBRE),
                turno("F1", 9, 0, EstadoTurno.BLOQUEADO),
                turno("F1", 9, 30, EstadoTurno.LIBRE)));

        List<TurnoAgendaDTO> huecos = servicio.consultarHuecosLibres(TipoRecurso.SALA, "sala-1", FECHA);

        assertEquals(List.of(LocalTime.of(9, 30), LocalTime.of(10, 0)),
                huecos.stream().map(TurnoAgendaDTO::horaInicio).toList());
        assertEquals(3, servicio.consultarAgenda(TipoRecurso.SALA, "sala-1", FECHA).size());
    }

    @Test
    void validaParametrosObligatorios() {
        assertThrows(IllegalArgumentException.class, () -> servicio.consultarAgenda(null, "r", FECHA));
        assertThrows(IllegalArgumentException.class, () -> servicio.consultarAgenda(TipoRecurso.SALA, " ", FECHA));
        assertThrows(IllegalArgumentException.class, () -> servicio.consultarAgenda(TipoRecurso.SALA, "r", null));
        assertThrows(IllegalArgumentException.class, () -> servicio.listarFranjas(null, "r", FECHA));
        assertThrows(IllegalArgumentException.class, () -> servicio.consultarHuecosLibres(TipoRecurso.SALA, null, FECHA));
        assertThrows(IllegalArgumentException.class, () -> servicio.bloquearTurno("F1#09:00", " "));
        assertThrows(IllegalArgumentException.class, () -> servicio.desbloquearTurno("F1#09:00", null));
        assertThrows(IllegalArgumentException.class, () -> servicio.bloquearTurno(null, "Motivo"));
        assertThrows(IllegalArgumentException.class, () -> servicio.bloquearFranja(" ", "Motivo"));
        assertThrows(IllegalArgumentException.class, () -> servicio.bloquearFranja("F1", " "));
        assertThrows(IllegalArgumentException.class, () -> servicio.ocuparTurno("F1#09:00", " "));
        assertThrows(IllegalArgumentException.class, () -> servicio.obtenerTurno(" "));
        assertThrows(IllegalArgumentException.class, () -> servicio.modificarFranja(null));
        verifyNoInteractions(franjaRepository, turnoRepository);
    }

    private static AgendaMedicaDTO franja(String id, String recurso, int hIni, int mIni, int hFin, int mFin,
                                          int intervalo) {
        return new AgendaMedicaDTO(id, recurso, null, FECHA, LocalTime.of(hIni, mIni), LocalTime.of(hFin, mFin),
                intervalo);
    }

    private static TurnoAgendaDTO turno(String idAgenda, int hora, int minuto, EstadoTurno estado) {
        LocalTime inicio = LocalTime.of(hora, minuto);
        return new TurnoAgendaDTO(AgendaServiceImpl.idTurno(idAgenda, inicio), idAgenda, TipoRecurso.MEDICO,
                "medico-1", FECHA, inicio, inicio.plusMinutes(15), estado,
                estado == EstadoTurno.BLOQUEADO ? "Bloqueo previo" : null,
                estado == EstadoTurno.OCUPADO ? "cita-1" : null);
    }
}
