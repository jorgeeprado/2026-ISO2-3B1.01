package es.uclm.esi.iso2.sescam.citas.dominio;

import es.uclm.esi.iso2.sescam.citas.persistencia.CitaRepositoryInMemory;
import es.uclm.esi.iso2.sescam.citas.persistencia.ListaEsperaRepositoryInMemory;
import es.uclm.esi.iso2.sescam.common.dto.AgendaMedicaDTO;
import es.uclm.esi.iso2.sescam.common.dto.CitaDTO;
import es.uclm.esi.iso2.sescam.common.dto.EntradaListaEsperaDTO;
import es.uclm.esi.iso2.sescam.common.dto.SolicitudPruebaDTO;
import es.uclm.esi.iso2.sescam.common.enums.EstadoCita;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de flujo del CU-13 sobre los DAOs en memoria, con las agendas (CU-14)
 * y la lista de espera (CU-15) reales.
 */
class CitacionFlujoTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 10, 9, 0);
    private static final LocalDate FECHA = LocalDate.of(2026, 11, 16);

    private AgendaServiceImpl agendas;
    private ListaEsperaServiceImpl listaEspera;
    private CitaRepositoryInMemory citas;
    private CitacionServiceImpl citacion;

    @BeforeEach
    void setUp() {
        Clock reloj = Clock.fixed(AHORA.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        agendas = new AgendaServiceImpl();
        listaEspera = new ListaEsperaServiceImpl(new ListaEsperaRepositoryInMemory(), agendas, agendas, reloj);
        citas = new CitaRepositoryInMemory();
        citacion = new CitacionServiceImpl(citas, agendas, agendas, listaEspera, reloj,
                CitacionServiceImpl.generadorSecuencial());

        agendas.crearFranja(franja("TAC-M", "sala-tac", "TAC", FECHA, 8), TipoRecurso.SALA);
        agendas.crearFranja(franja("GEN-M", "sala-gen", null, FECHA, 8), TipoRecurso.SALA);
        agendas.crearFranja(franja("RM-M", "sala-rm", "RM", FECHA, 8), TipoRecurso.SALA);
        agendas.crearFranja(franja("DR-M", "dr-1", "TAC", FECHA, 10), TipoRecurso.MEDICO);
        agendas.crearFranja(franja("TAC-AYER", "sala-tac", "TAC", AHORA.toLocalDate().minusDays(1), 8),
                TipoRecurso.SALA);

        listaEspera.inscribir(solicitud("S1", Prioridad.ORDINARIO, 1));
        listaEspera.inscribir(solicitud("S2", Prioridad.URGENTE, 5));
        listaEspera.inscribir(solicitud("S3", Prioridad.PREFERENTE, 2));
    }

    @Test
    void flujoCompletoDeAsignacionReprogramacionYCancelacion() {
        // Asignar al primer candidato (RN-01): S2 es URGENTE.
        CitaDTO c1 = citacion.asignarCita("S2", "TAC-M#08:00", null);
        assertEquals("CITA-000001", c1.getId());
        assertEquals(LocalDateTime.of(FECHA, LocalTime.of(8, 0)), c1.getFechaHora());
        assertEquals("sala-tac", c1.getSalaId());
        assertNull(c1.getMedicoRadiologoId());
        assertEquals(EstadoTurno.OCUPADO, agendas.obtenerTurno("TAC-M#08:00").estado());
        assertEquals("CITA-000001", agendas.obtenerTurno("TAC-M#08:00").idCita());
        assertEquals(EstadoEntradaEspera.CITADA, listaEspera.obtenerEntrada("S2").estado());
        assertEquals(List.of("S3", "S1"), ids(listaEspera.consultarLista("TAC")));
        assertNull(citas.buscarPorId("CITA-000001").orElseThrow().motivoAsignacion());

        // Saltar el orden exige motivo, que queda registrado.
        assertThrows(IllegalArgumentException.class, () -> citacion.asignarCita("S1", "TAC-M#08:30", null));
        assertEquals(EstadoTurno.LIBRE, agendas.obtenerTurno("TAC-M#08:30").estado());
        citacion.asignarCita("S1", "TAC-M#08:30", "Paciente solo disponible ese día");
        assertEquals("Paciente solo disponible ese día",
                citas.buscarPorId("CITA-000002").orElseThrow().motivoAsignacion());

        // Turno de médico: se informa el radiólogo y la sala queda a null.
        CitaDTO c3 = citacion.asignarCita("S3", "DR-M#10:00", null);
        assertEquals("dr-1", c3.getMedicoRadiologoId());
        assertNull(c3.getSalaId());

        // Reprogramar a una franja sin prueba asociada: la lista de espera no cambia.
        CitaDTO reprogramada = citacion.reprogramarCita("CITA-000001", "GEN-M#08:00", "Avería del TAC");
        assertEquals(EstadoCita.PROGRAMADA, reprogramada.getEstado());
        assertEquals("sala-gen", reprogramada.getSalaId());
        assertEquals(EstadoTurno.LIBRE, agendas.obtenerTurno("TAC-M#08:00").estado());
        assertEquals(EstadoTurno.OCUPADO, agendas.obtenerTurno("GEN-M#08:00").estado());
        assertEquals("CITA-000001", listaEspera.obtenerEntrada("S2").idCita());
        assertEquals("Avería del TAC", citas.buscarPorId("CITA-000001").orElseThrow().motivoReprogramacion());

        // Turno incompatible o pasado: se rechaza sin cambiar nada.
        assertThrows(IllegalStateException.class,
                () -> citacion.reprogramarCita("CITA-000001", "RM-M#08:00", "m"));
        assertThrows(IllegalStateException.class,
                () -> citacion.reprogramarCita("CITA-000001", "TAC-AYER#08:00", "m"));
        assertEquals(EstadoTurno.OCUPADO, agendas.obtenerTurno("GEN-M#08:00").estado());
        assertEquals(EstadoTurno.LIBRE, agendas.obtenerTurno("RM-M#08:00").estado());

        // Cancelar: se libera el turno y S2 vuelve a la lista con su antigüedad (primera por ser URGENTE).
        CitaDTO cancelada = citacion.cancelarCita("CITA-000001", "Paciente ingresado");
        assertEquals(EstadoCita.CANCELADA, cancelada.getEstado());
        assertEquals("Paciente ingresado", cancelada.getMotivoCancelacion());
        assertEquals(EstadoTurno.LIBRE, agendas.obtenerTurno("GEN-M#08:00").estado());
        assertEquals(1, listaEspera.consultarPosicion("S2"));
        assertThrows(IllegalStateException.class, () -> citacion.cancelarCita("CITA-000001", "otra vez"));

        // Nueva asignación: la solicitud conserva el historial de sus citas.
        citacion.asignarCita("S2", "TAC-M#08:00", null);
        List<CitaDTO> historial = citacion.listarCitasPorSolicitud("S2");
        assertEquals(List.of("CITA-000001", "CITA-000004"), historial.stream().map(CitaDTO::getId).toList());
        assertEquals(List.of(EstadoCita.CANCELADA, EstadoCita.PROGRAMADA),
                historial.stream().map(CitaDTO::getEstado).toList());
        assertEquals(EstadoCita.PROGRAMADA, citacion.obtenerCita("CITA-000004").getEstado());
    }

    @Test
    void noSeAsignaEnTurnosPasadosYLaSolicitudSigueEnEspera() {
        assertThrows(IllegalStateException.class, () -> citacion.asignarCita("S2", "TAC-AYER#08:00", null));

        assertEquals(EstadoTurno.LIBRE, agendas.obtenerTurno("TAC-AYER#08:00").estado());
        assertEquals(EstadoEntradaEspera.EN_ESPERA, listaEspera.obtenerEntrada("S2").estado());
        assertEquals(List.of(), citacion.listarCitasPorSolicitud("S2"));
    }

    @Test
    void noSeAsignaUnaSolicitudIncompatibleOYaCitada() {
        assertThrows(IllegalStateException.class, () -> citacion.asignarCita("S2", "RM-M#08:00", null));

        citacion.asignarCita("S2", "TAC-M#08:00", null);
        assertThrows(IllegalStateException.class, () -> citacion.asignarCita("S2", "TAC-M#08:30", "m"));
        assertThrows(IllegalStateException.class, () -> citacion.asignarCita("S3", "TAC-M#08:00", null));
        assertEquals(EstadoTurno.LIBRE, agendas.obtenerTurno("TAC-M#08:30").estado());
    }

    @Test
    void cancelarYReasignarAsignaElHuecoAlSiguienteCandidatoYExcluyeLaSolicitudCancelada() {
        Clock reloj = Clock.fixed(AHORA.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        String idTurno = "TAC-CU23#10:30";
        agendas.crearFranja(new AgendaMedicaDTO("TAC-CU23", "sala-tac", "TAC", AHORA.toLocalDate(),
                LocalTime.of(10, 30), LocalTime.of(11, 30), 30), TipoRecurso.SALA);

        CitaDTO original = citacion.asignarCita("S2", idTurno, null);
        citacion.cancelarCita(original.getId(), "Paciente cancela");

        ReasignacionHuecosServiceImpl reasignacion =
                new ReasignacionHuecosServiceImpl(agendas, listaEspera, citacion, reloj);
        CitaDTO reasignada = reasignacion.reasignarHuecoLiberado(idTurno, "S2").orElseThrow();

        assertEquals("S3", citas.buscarPorId(reasignada.getId()).orElseThrow().idSolicitud());
        assertEquals(EstadoEntradaEspera.EN_ESPERA, listaEspera.obtenerEntrada("S2").estado());
        assertEquals(reasignada.getId(), listaEspera.obtenerEntrada("S3").idCita());
        assertEquals(EstadoTurno.OCUPADO, agendas.obtenerTurno(idTurno).estado());
    }

    @Test
    void reasignacionDevuelveVacioCuandoSoloQuedaLaSolicitudCancelada() {
        Clock reloj = Clock.fixed(AHORA.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        AgendaServiceImpl agendaLocal = new AgendaServiceImpl();
        ListaEsperaServiceImpl listaLocal = new ListaEsperaServiceImpl(new ListaEsperaRepositoryInMemory(),
                agendaLocal, agendaLocal, reloj);
        CitaRepositoryInMemory citasLocales = new CitaRepositoryInMemory();
        CitacionServiceImpl citacionLocal = new CitacionServiceImpl(citasLocales, agendaLocal, agendaLocal,
                listaLocal, reloj, CitacionServiceImpl.generadorSecuencial());
        String idTurno = "SOLO-CU23#10:30";
        agendaLocal.crearFranja(new AgendaMedicaDTO("SOLO-CU23", "sala-solo", "SOLO", AHORA.toLocalDate(),
                LocalTime.of(10, 30), LocalTime.of(11, 0), 30), TipoRecurso.SALA);
        listaLocal.inscribir(new SolicitudPruebaDTO("SOL-SOLO", "pac-SOL-SOLO", "med-1", "SOLO",
                Prioridad.ORDINARIO, "Indicación", "Justificación", LocalDateTime.of(2026, 10, 1, 10, 0),
                EstadoSolicitud.PENDIENTE_CITACION, false, null));

        CitaDTO original = citacionLocal.asignarCita("SOL-SOLO", idTurno, null);
        citacionLocal.cancelarCita(original.getId(), "Paciente cancela");

        ReasignacionHuecosServiceImpl reasignacion =
                new ReasignacionHuecosServiceImpl(agendaLocal, listaLocal, citacionLocal, reloj);

        assertTrue(reasignacion.reasignarHuecoLiberado(idTurno, "SOL-SOLO").isEmpty());
        assertEquals(EstadoEntradaEspera.EN_ESPERA, listaLocal.obtenerEntrada("SOL-SOLO").estado());
        assertEquals(EstadoTurno.LIBRE, agendaLocal.obtenerTurno(idTurno).estado());
    }

    @Test
    void proponerCandidatosAceptaIntegerMaxValueComoLimite() {
        assertEquals(3, listaEspera.proponerCandidatos("TAC-M#08:00", Integer.MAX_VALUE).size());
    }

    private static AgendaMedicaDTO franja(String id, String recurso, String prueba, LocalDate fecha, int hora) {
        return new AgendaMedicaDTO(id, recurso, prueba, fecha, LocalTime.of(hora, 0), LocalTime.of(hora + 1, 0), 30);
    }

    private static SolicitudPruebaDTO solicitud(String id, Prioridad prioridad, int diaOctubre) {
        return new SolicitudPruebaDTO(id, "pac-" + id, "med-1", "TAC", prioridad, "Indicación", "Justificación",
                LocalDateTime.of(2026, 10, diaOctubre, 10, 0), EstadoSolicitud.PENDIENTE_CITACION, false, null);
    }

    private static List<String> ids(List<EntradaListaEsperaDTO> entradas) {
        return entradas.stream().map(EntradaListaEsperaDTO::idSolicitud).toList();
    }
}
