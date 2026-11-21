package es.uclm.esi.iso2.sescam.citas.dominio;

import es.uclm.esi.iso2.sescam.citas.persistencia.CitaRegistro;
import es.uclm.esi.iso2.sescam.citas.persistencia.ICitaRepository;
import es.uclm.esi.iso2.sescam.common.dto.AgendaMedicaDTO;
import es.uclm.esi.iso2.sescam.common.dto.CitaDTO;
import es.uclm.esi.iso2.sescam.common.dto.EntradaListaEsperaDTO;
import es.uclm.esi.iso2.sescam.common.dto.TurnoAgendaDTO;
import es.uclm.esi.iso2.sescam.common.enums.EstadoCita;
import es.uclm.esi.iso2.sescam.common.enums.EstadoEntradaEspera;
import es.uclm.esi.iso2.sescam.common.enums.EstadoTurno;
import es.uclm.esi.iso2.sescam.common.enums.Prioridad;
import es.uclm.esi.iso2.sescam.common.enums.TipoRecurso;
import es.uclm.esi.iso2.sescam.common.interfaces.AgendaService;
import es.uclm.esi.iso2.sescam.common.interfaces.CandidatosListaEsperaService;
import es.uclm.esi.iso2.sescam.common.interfaces.DisponibilidadAgendaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias del dominio del CU-13 con el repositorio, las agendas (CU-14) y la lista de espera
 * (CU-15) simulados, incluidos los fallos a mitad de cada operación y sus compensaciones (D-024).
 */
@ExtendWith(MockitoExtension.class)
class CitacionServiceImplMockTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 10, 9, 0);
    private static final LocalDate FECHA = LocalDate.of(2026, 11, 16);
    private static final String T1 = "F1#09:00";
    private static final String T2 = "F2#10:00";
    private static final String ID = "CITA-000001";
    private static final int SIN_LIMITE = CitacionServiceImpl.SIN_LIMITE;

    @Mock
    private ICitaRepository repositorio;
    @Mock
    private DisponibilidadAgendaService disponibilidad;
    @Mock
    private AgendaService agendas;
    @Mock
    private CandidatosListaEsperaService candidatos;
    @Captor
    private ArgumentCaptor<CitaRegistro> captor;

    private Clock reloj;
    private CitacionServiceImpl servicio;

    @BeforeEach
    void setUp() {
        reloj = Clock.fixed(AHORA.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        servicio = conGenerador(() -> ID);
    }

    // ------------------------------------------------------------------ Construcción

    @Test
    void constructoresRechazanDependenciasNulas() {
        Supplier<String> gen = () -> ID;
        assertThrows(NullPointerException.class, () -> new CitacionServiceImpl(null, disponibilidad, agendas, candidatos, reloj, gen));
        assertThrows(NullPointerException.class, () -> new CitacionServiceImpl(repositorio, null, agendas, candidatos, reloj, gen));
        assertThrows(NullPointerException.class, () -> new CitacionServiceImpl(repositorio, disponibilidad, null, candidatos, reloj, gen));
        assertThrows(NullPointerException.class, () -> new CitacionServiceImpl(repositorio, disponibilidad, agendas, null, reloj, gen));
        assertThrows(NullPointerException.class, () -> new CitacionServiceImpl(repositorio, disponibilidad, agendas, candidatos, null, gen));
        assertThrows(NullPointerException.class, () -> new CitacionServiceImpl(repositorio, disponibilidad, agendas, candidatos, reloj, null));
        assertDoesNotThrow(() -> new CitacionServiceImpl(disponibilidad, agendas, candidatos));
    }

    @Test
    void elGeneradorSecuencialProduceIdentificadoresLegibles() {
        Supplier<String> generador = CitacionServiceImpl.generadorSecuencial();
        assertEquals("CITA-000001", generador.get());
        assertEquals("CITA-000002", generador.get());
    }

    // ------------------------------------------------------------------ asignarCita

    @Test
    void asignarAlPrimerCandidatoOcupaMarcaYGuardaEnEseOrden() {
        prepararAsignacion(List.of(entrada("S1"), entrada("S2")), turno(T1, "F1", TipoRecurso.SALA, "sala-tac", FECHA, 9, EstadoTurno.LIBRE));

        CitaDTO cita = servicio.asignarCita("S1", T1, "Motivo innecesario");

        InOrder orden = inOrder(disponibilidad, candidatos, repositorio);
        orden.verify(candidatos).proponerCandidatos(T1, SIN_LIMITE);
        orden.verify(disponibilidad).ocuparTurno(T1, ID);
        orden.verify(candidatos).marcarCitada("S1", ID);
        orden.verify(repositorio).guardar(captor.capture());
        CitaRegistro registro = captor.getValue();
        assertEquals(T1, registro.idTurno());
        assertNull(registro.motivoAsignacion(), "Sin salto de orden no se registra motivo");
        assertNull(registro.motivoReprogramacion());

        assertEquals(ID, cita.getId());
        assertEquals("S1", cita.getSolicitudId());
        assertEquals("pac-S1", cita.getPacienteId());
        assertEquals("TAC", cita.getTipoPruebaId());
        assertEquals(LocalDateTime.of(FECHA, LocalTime.of(9, 0)), cita.getFechaHora());
        assertEquals(30, cita.getDuracionMinutos());
        assertEquals("sala-tac", cita.getSalaId());
        assertNull(cita.getMedicoRadiologoId());
        assertEquals(EstadoCita.PROGRAMADA, cita.getEstado());
        assertNull(cita.getMotivoCancelacion());
        verify(disponibilidad, never()).liberarTurno(anyString());
    }

    @Test
    void asignarEnTurnoDeMedicoInformaElRadiologoYDejaLaSalaANull() {
        prepararAsignacion(List.of(entrada("S1")), turno(T1, "F1", TipoRecurso.MEDICO, "dr-1", FECHA, 9, EstadoTurno.LIBRE));

        CitaDTO cita = servicio.asignarCita("S1", T1, null);

        assertEquals("dr-1", cita.getMedicoRadiologoId());
        assertNull(cita.getSalaId());
    }

    @Test
    void asignarAUnCandidatoQueNoEsElPrimeroExigeMotivoYLoRegistra() {
        prepararAsignacion(List.of(entrada("S1"), entrada("S2"), entrada("S3")),
                turno(T1, "F1", TipoRecurso.SALA, "sala-tac", FECHA, 9, EstadoTurno.LIBRE));

        assertThrows(IllegalArgumentException.class, () -> servicio.asignarCita("S3", T1, null));
        assertThrows(IllegalArgumentException.class, () -> servicio.asignarCita("S3", T1, "  "));
        verify(disponibilidad, never()).ocuparTurno(anyString(), anyString());

        servicio.asignarCita("S3", T1, "Paciente solo disponible ese día");

        verify(repositorio).guardar(captor.capture());
        assertEquals("Paciente solo disponible ese día", captor.getValue().motivoAsignacion());
        assertEquals("S3", captor.getValue().idSolicitud());
    }

    @Test
    void asignarConsultaLosCandidatosSinLimiteParaNoDejarFueraAlFinalDeLaLista() {
        List<EntradaListaEsperaDTO> muchos = new ArrayList<>();
        for (int i = 1; i <= 60; i++) {
            muchos.add(entrada("S" + i));
        }
        prepararAsignacion(muchos, turno(T1, "F1", TipoRecurso.SALA, "sala-tac", FECHA, 9, EstadoTurno.LIBRE));

        servicio.asignarCita("S60", T1, "Último de la lista");

        verify(candidatos).proponerCandidatos(T1, Integer.MAX_VALUE);
        verify(candidatos).marcarCitada("S60", ID);
    }

    @Test
    void asignarValidaDatosCandidaturaYTurno() {
        assertThrows(IllegalArgumentException.class, () -> servicio.asignarCita(" ", T1, null));
        assertThrows(IllegalArgumentException.class, () -> servicio.asignarCita("S1", null, null));
        verifyNoInteractions(candidatos, disponibilidad, repositorio);

        when(candidatos.proponerCandidatos("T-AJENO", SIN_LIMITE)).thenReturn(List.of(entrada("S2")));
        assertThrows(IllegalStateException.class, () -> servicio.asignarCita("S1", "T-AJENO", null));

        when(candidatos.proponerCandidatos("T-BORRADO", SIN_LIMITE)).thenReturn(List.of(entrada("S1")));
        assertThrows(IllegalStateException.class, () -> servicio.asignarCita("S1", "T-BORRADO", null));

        when(candidatos.proponerCandidatos("T-PASADO", SIN_LIMITE)).thenReturn(List.of(entrada("S1")));
        when(disponibilidad.obtenerTurno("T-PASADO")).thenReturn(
                turno("T-PASADO", "F0", TipoRecurso.SALA, "sala-tac", FECHA.minusDays(7), 9, EstadoTurno.LIBRE));
        assertThrows(IllegalStateException.class, () -> servicio.asignarCita("S1", "T-PASADO", null));

        verify(disponibilidad, never()).ocuparTurno(anyString(), anyString());
        verify(repositorio, never()).guardar(any());
    }

    @Test
    void asignarPropagaLosErroresDeTurnoDeLaListaDeEspera() {
        when(candidatos.proponerCandidatos(T1, SIN_LIMITE)).thenThrow(new IllegalStateException("Turno ocupado"));

        assertThrows(IllegalStateException.class, () -> servicio.asignarCita("S1", T1, null));
        verifyNoInteractions(disponibilidad, repositorio);
    }

    @Test
    void asignarRechazaIdentificadoresGeneradosVaciosODuplicados() {
        TurnoAgendaDTO libre = turno(T1, "F1", TipoRecurso.SALA, "sala-tac", FECHA, 9, EstadoTurno.LIBRE);
        prepararAsignacion(List.of(entrada("S1")), libre);
        when(repositorio.buscarPorId(ID)).thenReturn(Optional.of(registro(ID, T1, EstadoCita.PROGRAMADA, null)));

        assertThrows(IllegalStateException.class, () -> servicio.asignarCita("S1", T1, null));
        assertThrows(IllegalStateException.class, () -> conGenerador(() -> " ").asignarCita("S1", T1, null));
        assertThrows(IllegalStateException.class, () -> conGenerador(() -> null).asignarCita("S1", T1, null));
        verify(disponibilidad, never()).ocuparTurno(anyString(), anyString());
    }

    @Test
    void asignarSiFallaOcuparElTurnoNoHayNadaQueCompensar() {
        prepararAsignacion(List.of(entrada("S1")), turno(T1, "F1", TipoRecurso.SALA, "sala-tac", FECHA, 9, EstadoTurno.LIBRE));
        IllegalStateException fallo = new IllegalStateException("Ocupado por otra cita");
        when(disponibilidad.ocuparTurno(T1, ID)).thenThrow(fallo);

        assertSame(fallo, assertThrows(IllegalStateException.class, () -> servicio.asignarCita("S1", T1, null)));
        verify(candidatos, never()).marcarCitada(anyString(), anyString());
        verify(disponibilidad, never()).liberarTurno(anyString());
        verify(repositorio, never()).guardar(any());
    }

    @Test
    void asignarSiFallaMarcarCitadaLiberaElTurno() {
        prepararAsignacion(List.of(entrada("S1")), turno(T1, "F1", TipoRecurso.SALA, "sala-tac", FECHA, 9, EstadoTurno.LIBRE));
        IllegalStateException fallo = new IllegalStateException("La entrada ya no está en espera");
        when(candidatos.marcarCitada("S1", ID)).thenThrow(fallo);

        assertSame(fallo, assertThrows(IllegalStateException.class, () -> servicio.asignarCita("S1", T1, null)));
        InOrder orden = inOrder(disponibilidad, candidatos);
        orden.verify(disponibilidad).ocuparTurno(T1, ID);
        orden.verify(candidatos).marcarCitada("S1", ID);
        orden.verify(disponibilidad).liberarTurno(T1);
        verify(repositorio, never()).guardar(any());
        assertEquals(0, fallo.getSuppressed().length);
    }

    @Test
    void asignarSiFallaGuardarReincorporaYLiberaEnOrdenInverso() {
        prepararAsignacion(List.of(entrada("S1")), turno(T1, "F1", TipoRecurso.SALA, "sala-tac", FECHA, 9, EstadoTurno.LIBRE));
        RuntimeException fallo = new RuntimeException("Fallo de persistencia");
        when(repositorio.guardar(any())).thenThrow(fallo);

        assertSame(fallo, assertThrows(RuntimeException.class, () -> servicio.asignarCita("S1", T1, null)));
        InOrder orden = inOrder(disponibilidad, candidatos, repositorio);
        orden.verify(disponibilidad).ocuparTurno(T1, ID);
        orden.verify(candidatos).marcarCitada("S1", ID);
        orden.verify(repositorio).guardar(any());
        orden.verify(candidatos).reincorporar("S1", "Compensación: no se pudo registrar la cita " + ID);
        orden.verify(disponibilidad).liberarTurno(T1);
    }

    @Test
    void siFallaUnaCompensacionSeAnadeComoSuprimidaYSeSigueCompensando() {
        prepararAsignacion(List.of(entrada("S1")), turno(T1, "F1", TipoRecurso.SALA, "sala-tac", FECHA, 9, EstadoTurno.LIBRE));
        RuntimeException fallo = new RuntimeException("Fallo de persistencia");
        IllegalStateException falloCompensacion = new IllegalStateException("No se pudo reincorporar");
        when(repositorio.guardar(any())).thenThrow(fallo);
        when(candidatos.reincorporar(anyString(), anyString())).thenThrow(falloCompensacion);

        RuntimeException lanzada = assertThrows(RuntimeException.class, () -> servicio.asignarCita("S1", T1, null));

        assertSame(fallo, lanzada);
        assertArrayEquals(new Throwable[]{falloCompensacion}, lanzada.getSuppressed());
        verify(disponibilidad).liberarTurno(T1);
    }

    // ------------------------------------------------------------------ reprogramarCita

    @Test
    void reprogramarOcupaElNuevoLiberaElAntiguoYConservaLaCitaProgramada() {
        prepararReprogramacion(EstadoCita.PROGRAMADA, "TAC");

        CitaDTO cita = servicio.reprogramarCita(ID, T2, "Avería del equipo");

        InOrder orden = inOrder(disponibilidad, repositorio);
        orden.verify(disponibilidad).ocuparTurno(T2, ID);
        orden.verify(disponibilidad).liberarTurno(T1);
        orden.verify(repositorio).guardar(captor.capture());
        CitaRegistro registro = captor.getValue();
        assertEquals(T2, registro.idTurno());
        assertEquals("Avería del equipo", registro.motivoReprogramacion());
        assertEquals("Motivo de asignación", registro.motivoAsignacion());
        assertEquals(ID, cita.getId());
        assertEquals(EstadoCita.PROGRAMADA, cita.getEstado());
        assertEquals(LocalDateTime.of(FECHA, LocalTime.of(10, 0)), cita.getFechaHora());
        assertEquals("sala-2", cita.getSalaId());
        verifyNoInteractions(candidatos);
    }

    @Test
    void reprogramarUnaCitaConfirmadaEnFranjaSinPruebaLaDejaProgramada() {
        prepararReprogramacion(EstadoCita.CONFIRMADA, null);

        assertEquals(EstadoCita.PROGRAMADA, servicio.reprogramarCita(ID, T2, "Cambio de sala").getEstado());
    }

    @Test
    void reprogramarValidaCitaTurnoFechaYCompatibilidad() {
        assertThrows(IllegalArgumentException.class, () -> servicio.reprogramarCita(ID, " ", "m"));
        assertThrows(IllegalArgumentException.class, () -> servicio.reprogramarCita(ID, T2, null));
        assertThrows(IllegalArgumentException.class, () -> servicio.reprogramarCita(" ", T2, "m"));
        assertThrows(IllegalArgumentException.class, () -> servicio.reprogramarCita("NO", T2, "m"));
        when(repositorio.buscarPorId("CAN")).thenReturn(Optional.of(registro("CAN", T1, EstadoCita.CANCELADA, null)));
        assertThrows(IllegalStateException.class, () -> servicio.reprogramarCita("CAN", T2, "m"));

        when(repositorio.buscarPorId(ID)).thenReturn(Optional.of(registro(ID, T1, EstadoCita.PROGRAMADA, null)));
        assertThrows(IllegalArgumentException.class, () -> servicio.reprogramarCita(ID, T1, "m"));
        assertThrows(IllegalArgumentException.class, () -> servicio.reprogramarCita(ID, "NO-EXISTE", "m"));
        when(disponibilidad.obtenerTurno("OCU")).thenReturn(turno("OCU", "F2", TipoRecurso.SALA, "sala-2", FECHA, 10, EstadoTurno.OCUPADO));
        assertThrows(IllegalStateException.class, () -> servicio.reprogramarCita(ID, "OCU", "m"));
        when(disponibilidad.obtenerTurno("PAS")).thenReturn(turno("PAS", "F2", TipoRecurso.SALA, "sala-2", LocalDate.of(2026, 11, 10), 8, EstadoTurno.LIBRE));
        assertThrows(IllegalStateException.class, () -> servicio.reprogramarCita(ID, "PAS", "m"));
        when(disponibilidad.obtenerTurno("SIN-FRANJA")).thenReturn(turno("SIN-FRANJA", "F-BORRADA", TipoRecurso.SALA, "sala-2", FECHA, 10, EstadoTurno.LIBRE));
        assertThrows(IllegalStateException.class, () -> servicio.reprogramarCita(ID, "SIN-FRANJA", "m"));
        when(disponibilidad.obtenerTurno("RM")).thenReturn(turno("RM", "F-RM", TipoRecurso.SALA, "sala-rm", FECHA, 10, EstadoTurno.LIBRE));
        when(agendas.obtenerAgendaPorId("F-RM")).thenReturn(franja("F-RM", "RM"));
        assertThrows(IllegalStateException.class, () -> servicio.reprogramarCita(ID, "RM", "m"));

        verify(disponibilidad, never()).ocuparTurno(anyString(), anyString());
        verify(repositorio, never()).guardar(any());
    }

    @Test
    void reprogramarAUnTurnoQueEmpiezaJustoAhoraEstaPermitido() {
        when(repositorio.buscarPorId(ID)).thenReturn(Optional.of(registro(ID, T1, EstadoCita.PROGRAMADA, null)));
        when(disponibilidad.obtenerTurno("AHORA")).thenReturn(
                turno("AHORA", "F2", TipoRecurso.SALA, "sala-2", AHORA.toLocalDate(), 9, EstadoTurno.LIBRE));
        when(agendas.obtenerAgendaPorId("F2")).thenReturn(franja("F2", "TAC"));

        assertEquals(AHORA, servicio.reprogramarCita(ID, "AHORA", "Hueco inmediato").getFechaHora());
    }

    @Test
    void reprogramarSiFallaLiberarElAntiguoLiberaElNuevo() {
        prepararReprogramacion(EstadoCita.PROGRAMADA, "TAC");
        IllegalStateException fallo = new IllegalStateException("El turno antiguo no estaba ocupado");
        when(disponibilidad.liberarTurno(T1)).thenThrow(fallo);

        assertSame(fallo, assertThrows(IllegalStateException.class, () -> servicio.reprogramarCita(ID, T2, "m")));
        InOrder orden = inOrder(disponibilidad);
        orden.verify(disponibilidad).ocuparTurno(T2, ID);
        orden.verify(disponibilidad).liberarTurno(T1);
        orden.verify(disponibilidad).liberarTurno(T2);
        verify(repositorio, never()).guardar(any());
    }

    @Test
    void reprogramarSiFallaGuardarReocupaElAntiguoYLiberaElNuevo() {
        prepararReprogramacion(EstadoCita.PROGRAMADA, "TAC");
        RuntimeException fallo = new RuntimeException("Fallo de persistencia");
        when(repositorio.guardar(any())).thenThrow(fallo);

        assertSame(fallo, assertThrows(RuntimeException.class, () -> servicio.reprogramarCita(ID, T2, "m")));
        InOrder orden = inOrder(disponibilidad, repositorio);
        orden.verify(disponibilidad).ocuparTurno(T2, ID);
        orden.verify(disponibilidad).liberarTurno(T1);
        orden.verify(repositorio).guardar(any());
        orden.verify(disponibilidad).ocuparTurno(T1, ID);
        orden.verify(disponibilidad).liberarTurno(T2);
    }

    // ------------------------------------------------------------------ cancelarCita

    @Test
    void cancelarLiberaReincorporaYGuardaLaCitaCancelada() {
        when(repositorio.buscarPorId(ID)).thenReturn(Optional.of(registro(ID, T1, EstadoCita.PROGRAMADA, "Motivo de asignación")));

        CitaDTO cita = servicio.cancelarCita(ID, "Paciente ingresado");

        InOrder orden = inOrder(disponibilidad, candidatos, repositorio);
        orden.verify(disponibilidad).liberarTurno(T1);
        orden.verify(candidatos).reincorporar("S1", "Paciente ingresado");
        orden.verify(repositorio).guardar(captor.capture());
        assertEquals(EstadoCita.CANCELADA, cita.getEstado());
        assertEquals("Paciente ingresado", cita.getMotivoCancelacion());
        assertEquals(T1, captor.getValue().idTurno());
        assertEquals("Motivo de asignación", captor.getValue().motivoAsignacion());
    }

    @Test
    void cancelarSePermiteEnUnaCitaConfirmadaAunqueSuFechaHayaPasado() {
        CitaDTO pasada = cita(ID, EstadoCita.CONFIRMADA);
        pasada.setFechaHora(AHORA.minusDays(3));
        when(repositorio.buscarPorId(ID)).thenReturn(Optional.of(new CitaRegistro(pasada, T1, null, null)));

        assertEquals(EstadoCita.CANCELADA, servicio.cancelarCita(ID, "No acudió").getEstado());
    }

    @Test
    void cancelarValidaMotivoExistenciaYEstado() {
        assertThrows(IllegalArgumentException.class, () -> servicio.cancelarCita(ID, " "));
        assertThrows(IllegalArgumentException.class, () -> servicio.cancelarCita(null, "m"));
        assertThrows(IllegalArgumentException.class, () -> servicio.cancelarCita("NO", "m"));
        when(repositorio.buscarPorId("CAN")).thenReturn(Optional.of(registro("CAN", T1, EstadoCita.CANCELADA, null)));
        assertThrows(IllegalStateException.class, () -> servicio.cancelarCita("CAN", "m"));

        verifyNoInteractions(disponibilidad, candidatos);
    }

    @Test
    void cancelarSiFallaLiberarNoHayNadaQueCompensar() {
        when(repositorio.buscarPorId(ID)).thenReturn(Optional.of(registro(ID, T1, EstadoCita.PROGRAMADA, null)));
        IllegalStateException fallo = new IllegalStateException("El turno no estaba ocupado");
        when(disponibilidad.liberarTurno(T1)).thenThrow(fallo);

        assertSame(fallo, assertThrows(IllegalStateException.class, () -> servicio.cancelarCita(ID, "m")));
        verifyNoInteractions(candidatos);
        verify(repositorio, never()).guardar(any());
    }

    @Test
    void cancelarSiFallaReincorporarVuelveAOcuparElTurno() {
        when(repositorio.buscarPorId(ID)).thenReturn(Optional.of(registro(ID, T1, EstadoCita.PROGRAMADA, null)));
        IllegalStateException fallo = new IllegalStateException("La entrada no estaba citada");
        when(candidatos.reincorporar("S1", "m")).thenThrow(fallo);

        assertSame(fallo, assertThrows(IllegalStateException.class, () -> servicio.cancelarCita(ID, "m")));
        InOrder orden = inOrder(disponibilidad, candidatos);
        orden.verify(disponibilidad).liberarTurno(T1);
        orden.verify(candidatos).reincorporar("S1", "m");
        orden.verify(disponibilidad).ocuparTurno(T1, ID);
        verify(repositorio, never()).guardar(any());
    }

    @Test
    void cancelarSiFallaGuardarVuelveAMarcarCitadaYReocupaElTurno() {
        when(repositorio.buscarPorId(ID)).thenReturn(Optional.of(registro(ID, T1, EstadoCita.PROGRAMADA, null)));
        RuntimeException fallo = new RuntimeException("Fallo de persistencia");
        when(repositorio.guardar(any())).thenThrow(fallo);
        IllegalStateException falloCompensacion = new IllegalStateException("No se pudo marcar");
        when(candidatos.marcarCitada("S1", ID)).thenThrow(falloCompensacion);

        RuntimeException lanzada = assertThrows(RuntimeException.class, () -> servicio.cancelarCita(ID, "m"));

        assertSame(fallo, lanzada);
        assertArrayEquals(new Throwable[]{falloCompensacion}, lanzada.getSuppressed());
        InOrder orden = inOrder(disponibilidad, candidatos, repositorio);
        orden.verify(disponibilidad).liberarTurno(T1);
        orden.verify(candidatos).reincorporar("S1", "m");
        orden.verify(repositorio).guardar(any());
        orden.verify(candidatos).marcarCitada("S1", ID);
        orden.verify(disponibilidad).ocuparTurno(T1, ID);
    }

    // ------------------------------------------------------------------ ConsultaCitasService

    @Test
    void obtenerCitaDevuelveCopiasONull() {
        when(repositorio.buscarPorId(ID)).thenReturn(Optional.of(registro(ID, T1, EstadoCita.PROGRAMADA, null)));

        CitaDTO primera = servicio.obtenerCita(ID);
        primera.setEstado(EstadoCita.CANCELADA);

        assertEquals(EstadoCita.PROGRAMADA, servicio.obtenerCita(ID).getEstado());
        assertNotSame(primera, servicio.obtenerCita(ID));
        assertNull(servicio.obtenerCita("NO"));
        assertThrows(IllegalArgumentException.class, () -> servicio.obtenerCita(" "));
    }

    @Test
    void listarCitasPorSolicitudLasOrdenaPorCreacion() {
        when(repositorio.buscarPorSolicitud("S1")).thenReturn(List.of(
                registro("CITA-000007", T2, EstadoCita.PROGRAMADA, null),
                registro("CITA-000002", T1, EstadoCita.CANCELADA, null)));
        when(repositorio.buscarPorSolicitud("S9")).thenReturn(List.of());

        assertEquals(List.of("CITA-000002", "CITA-000007"),
                servicio.listarCitasPorSolicitud("S1").stream().map(CitaDTO::getId).toList());
        assertTrue(servicio.listarCitasPorSolicitud("S9").isEmpty());
        assertThrows(IllegalArgumentException.class, () -> servicio.listarCitasPorSolicitud(null));
    }

    // ------------------------------------------------------------------ Datos de prueba

    private CitacionServiceImpl conGenerador(Supplier<String> generador) {
        return new CitacionServiceImpl(repositorio, disponibilidad, agendas, candidatos, reloj, generador);
    }

    private void prepararAsignacion(List<EntradaListaEsperaDTO> lista, TurnoAgendaDTO turno) {
        when(candidatos.proponerCandidatos(turno.idTurno(), SIN_LIMITE)).thenReturn(lista);
        when(disponibilidad.obtenerTurno(turno.idTurno())).thenReturn(turno);
    }

    private void prepararReprogramacion(EstadoCita estado, String pruebaFranjaNueva) {
        when(repositorio.buscarPorId(ID)).thenReturn(Optional.of(registro(ID, T1, estado, "Motivo de asignación")));
        when(disponibilidad.obtenerTurno(T2)).thenReturn(turno(T2, "F2", TipoRecurso.SALA, "sala-2", FECHA, 10, EstadoTurno.LIBRE));
        when(agendas.obtenerAgendaPorId("F2")).thenReturn(franja("F2", pruebaFranjaNueva));
    }

    private static EntradaListaEsperaDTO entrada(String idSolicitud) {
        return new EntradaListaEsperaDTO(idSolicitud, "pac-" + idSolicitud, "TAC", Prioridad.ORDINARIO,
                LocalDateTime.of(2026, 10, 1, 9, 0), AHORA.minusDays(1), EstadoEntradaEspera.EN_ESPERA, null, null);
    }

    private static TurnoAgendaDTO turno(String idTurno, String idAgenda, TipoRecurso tipo, String recurso, LocalDate fecha,
                                        int hora, EstadoTurno estado) {
        return new TurnoAgendaDTO(idTurno, idAgenda, tipo, recurso, fecha, LocalTime.of(hora, 0), LocalTime.of(hora, 30),
                estado, null, estado == EstadoTurno.OCUPADO ? "CITA-OTRA" : null);
    }

    private static AgendaMedicaDTO franja(String idAgenda, String prueba) {
        return new AgendaMedicaDTO(idAgenda, "sala-2", prueba, FECHA, LocalTime.of(8, 0), LocalTime.of(14, 0), 30);
    }

    private static CitaDTO cita(String idCita, EstadoCita estado) {
        return new CitaDTO(idCita, "S1", "pac-S1", "TAC", LocalDateTime.of(FECHA, LocalTime.of(9, 0)), 30, "sala-tac",
                null, estado, estado == EstadoCita.CANCELADA ? "Cancelada" : null);
    }

    private static CitaRegistro registro(String idCita, String idTurno, EstadoCita estado, String motivoAsignacion) {
        return new CitaRegistro(cita(idCita, estado), idTurno, motivoAsignacion, null);
    }
}
