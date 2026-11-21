package es.uclm.esi.iso2.sescam.citas.dominio;

import es.uclm.esi.iso2.sescam.citas.persistencia.IListaEsperaRepository;
import es.uclm.esi.iso2.sescam.common.dto.AgendaMedicaDTO;
import es.uclm.esi.iso2.sescam.common.dto.EntradaListaEsperaDTO;
import es.uclm.esi.iso2.sescam.common.dto.SolicitudPruebaDTO;
import es.uclm.esi.iso2.sescam.common.dto.TurnoAgendaDTO;
import es.uclm.esi.iso2.sescam.common.enums.EstadoEntradaEspera;
import es.uclm.esi.iso2.sescam.common.enums.EstadoSolicitud;
import es.uclm.esi.iso2.sescam.common.enums.EstadoTurno;
import es.uclm.esi.iso2.sescam.common.enums.Prioridad;
import es.uclm.esi.iso2.sescam.common.enums.TipoRecurso;
import es.uclm.esi.iso2.sescam.common.interfaces.AgendaService;
import es.uclm.esi.iso2.sescam.common.interfaces.DisponibilidadAgendaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias del dominio del CU-15 con el repositorio y los servicios de agenda simulados.
 */
@ExtendWith(MockitoExtension.class)
class ListaEsperaServiceImplMockTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 10, 9, 30);
    private static final LocalDateTime PRESCRIPCION = LocalDateTime.of(2026, 10, 1, 12, 0);
    private static final LocalDate FECHA_TURNO = LocalDate.of(2026, 11, 16);
    private static final String TURNO = "F1#09:00";

    @Mock
    private IListaEsperaRepository repositorio;
    @Mock
    private DisponibilidadAgendaService disponibilidad;
    @Mock
    private AgendaService agendas;
    @Captor
    private ArgumentCaptor<EntradaListaEsperaDTO> captor;

    private ListaEsperaServiceImpl servicio;

    @BeforeEach
    void setUp() {
        Clock reloj = Clock.fixed(AHORA.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        servicio = new ListaEsperaServiceImpl(repositorio, disponibilidad, agendas, reloj);
    }

    // ------------------------------------------------------------------ Construcción y DTO

    @Test
    void constructoresRechazanDependenciasNulas() {
        Clock reloj = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);
        assertThrows(NullPointerException.class, () -> new ListaEsperaServiceImpl(null, disponibilidad, agendas, reloj));
        assertThrows(NullPointerException.class, () -> new ListaEsperaServiceImpl(repositorio, null, agendas, reloj));
        assertThrows(NullPointerException.class, () -> new ListaEsperaServiceImpl(repositorio, disponibilidad, null, reloj));
        assertThrows(NullPointerException.class, () -> new ListaEsperaServiceImpl(repositorio, disponibilidad, agendas, null));
        assertDoesNotThrow(() -> new ListaEsperaServiceImpl(disponibilidad, agendas));
    }

    @Test
    void elDtoDeEntradaValidaSusInvariantes() {
        assertThrows(IllegalArgumentException.class, () -> dto(" ", "p", "TAC", Prioridad.URGENTE, PRESCRIPCION, AHORA, EstadoEntradaEspera.EN_ESPERA, null));
        assertThrows(IllegalArgumentException.class, () -> dto("S1", null, "TAC", Prioridad.URGENTE, PRESCRIPCION, AHORA, EstadoEntradaEspera.EN_ESPERA, null));
        assertThrows(IllegalArgumentException.class, () -> dto("S1", "p", "", Prioridad.URGENTE, PRESCRIPCION, AHORA, EstadoEntradaEspera.EN_ESPERA, null));
        assertThrows(IllegalArgumentException.class, () -> dto("S1", "p", "TAC", null, PRESCRIPCION, AHORA, EstadoEntradaEspera.EN_ESPERA, null));
        assertThrows(IllegalArgumentException.class, () -> dto("S1", "p", "TAC", Prioridad.URGENTE, null, AHORA, EstadoEntradaEspera.EN_ESPERA, null));
        assertThrows(IllegalArgumentException.class, () -> dto("S1", "p", "TAC", Prioridad.URGENTE, PRESCRIPCION, null, EstadoEntradaEspera.EN_ESPERA, null));
        assertThrows(IllegalArgumentException.class, () -> dto("S1", "p", "TAC", Prioridad.URGENTE, PRESCRIPCION, AHORA, null, null));
        assertThrows(IllegalArgumentException.class, () -> dto("S1", "p", "TAC", Prioridad.URGENTE, PRESCRIPCION, AHORA, EstadoEntradaEspera.CITADA, " "));
        assertThrows(IllegalArgumentException.class, () -> dto("S1", "p", "TAC", Prioridad.URGENTE, PRESCRIPCION, AHORA, EstadoEntradaEspera.EN_ESPERA, "C1"));
        assertDoesNotThrow(() -> dto("S1", "p", "TAC", Prioridad.URGENTE, PRESCRIPCION, AHORA, EstadoEntradaEspera.CITADA, "C1"));
    }

    // ------------------------------------------------------------------ inscribir

    @Test
    void inscribirCopiaPrioridadYFechaDePrescripcionYQuedaEnEspera() {
        when(repositorio.buscarPorSolicitud("S1")).thenReturn(Optional.empty());
        when(repositorio.guardar(any())).then(returnsFirstArg());

        EntradaListaEsperaDTO entrada = servicio.inscribir(solicitud("S1", Prioridad.PREFERENTE,
                EstadoSolicitud.PENDIENTE_CITACION));

        verify(repositorio).guardar(captor.capture());
        EntradaListaEsperaDTO guardada = captor.getValue();
        assertSame(guardada, entrada);
        assertEquals("pac-S1", guardada.pacienteId());
        assertEquals("TAC", guardada.tipoPruebaId());
        assertEquals(Prioridad.PREFERENTE, guardada.prioridad());
        assertEquals(PRESCRIPCION, guardada.fechaPrescripcion());
        assertEquals(AHORA, guardada.fechaInscripcion());
        assertEquals(EstadoEntradaEspera.EN_ESPERA, guardada.estado());
        assertNull(guardada.idCita());
        assertNull(guardada.motivo());
    }

    @Test
    void inscribirRechazaSolicitudesInvalidasSinTocarElRepositorio() {
        assertThrows(IllegalArgumentException.class, () -> servicio.inscribir(null));
        SolicitudPruebaDTO sinId = solicitud(" ", Prioridad.URGENTE, EstadoSolicitud.PENDIENTE_CITACION);
        assertThrows(IllegalArgumentException.class, () -> servicio.inscribir(sinId));
        SolicitudPruebaDTO sinPaciente = solicitud("S1", Prioridad.URGENTE, EstadoSolicitud.PENDIENTE_CITACION);
        sinPaciente.setPacienteId(null);
        assertThrows(IllegalArgumentException.class, () -> servicio.inscribir(sinPaciente));
        SolicitudPruebaDTO sinTipo = solicitud("S1", Prioridad.URGENTE, EstadoSolicitud.PENDIENTE_CITACION);
        sinTipo.setTipoPruebaId("");
        assertThrows(IllegalArgumentException.class, () -> servicio.inscribir(sinTipo));
        SolicitudPruebaDTO sinPrioridad = solicitud("S1", null, EstadoSolicitud.PENDIENTE_CITACION);
        assertThrows(IllegalArgumentException.class, () -> servicio.inscribir(sinPrioridad));
        SolicitudPruebaDTO sinFecha = solicitud("S1", Prioridad.URGENTE, EstadoSolicitud.PENDIENTE_CITACION);
        sinFecha.setFechaSolicitud(null);
        assertThrows(IllegalArgumentException.class, () -> servicio.inscribir(sinFecha));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.inscribir(solicitud("S1", Prioridad.URGENTE, EstadoSolicitud.CITADA)));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.inscribir(solicitud("S1", Prioridad.URGENTE, null)));

        verifyNoInteractions(repositorio);
    }

    @Test
    void inscribirRechazaUnaSolicitudYaInscrita() {
        when(repositorio.buscarPorSolicitud("S1")).thenReturn(Optional.of(enEspera("S1", Prioridad.ORDINARIO)));

        assertThrows(IllegalArgumentException.class,
                () -> servicio.inscribir(solicitud("S1", Prioridad.URGENTE, EstadoSolicitud.PENDIENTE_CITACION)));
        verify(repositorio, never()).guardar(any());
    }

    // ------------------------------------------------------------------ cambiarPrioridad y retirar

    @Test
    void cambiarPrioridadConservaFechasYGuardaElMotivo() {
        when(repositorio.buscarPorSolicitud("S1")).thenReturn(Optional.of(enEspera("S1", Prioridad.ORDINARIO)));
        when(repositorio.guardar(any())).then(returnsFirstArg());

        EntradaListaEsperaDTO cambiada = servicio.cambiarPrioridad("S1", Prioridad.URGENTE, "Empeoramiento clínico");

        assertEquals(Prioridad.URGENTE, cambiada.prioridad());
        assertEquals("Empeoramiento clínico", cambiada.motivo());
        assertEquals(PRESCRIPCION, cambiada.fechaPrescripcion());
        assertEquals(AHORA.minusDays(1), cambiada.fechaInscripcion());
        assertEquals(EstadoEntradaEspera.EN_ESPERA, cambiada.estado());
    }

    @Test
    void cambiarPrioridadValidaDatosExistenciaYEstado() {
        assertThrows(IllegalArgumentException.class, () -> servicio.cambiarPrioridad("S1", null, "m"));
        assertThrows(IllegalArgumentException.class, () -> servicio.cambiarPrioridad("S1", Prioridad.URGENTE, " "));
        assertThrows(IllegalArgumentException.class, () -> servicio.cambiarPrioridad(null, Prioridad.URGENTE, "m"));
        when(repositorio.buscarPorSolicitud("NO")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> servicio.cambiarPrioridad("NO", Prioridad.URGENTE, "m"));
        when(repositorio.buscarPorSolicitud("S1")).thenReturn(Optional.of(retirada("S1")));
        assertThrows(IllegalStateException.class, () -> servicio.cambiarPrioridad("S1", Prioridad.URGENTE, "m"));

        verify(repositorio, never()).guardar(any());
    }

    @Test
    void retirarDejaLaEntradaRetiradaConMotivoSinBorrarla() {
        when(repositorio.buscarPorSolicitud("S1")).thenReturn(Optional.of(enEspera("S1", Prioridad.PREFERENTE)));
        when(repositorio.guardar(any())).then(returnsFirstArg());

        EntradaListaEsperaDTO retirada = servicio.retirar("S1", "Realizada en centro concertado");

        assertEquals(EstadoEntradaEspera.RETIRADA, retirada.estado());
        assertEquals("Realizada en centro concertado", retirada.motivo());
        assertEquals(Prioridad.PREFERENTE, retirada.prioridad());
    }

    @Test
    void retirarExigeMotivoYEntradaEnEspera() {
        assertThrows(IllegalArgumentException.class, () -> servicio.retirar("S1", null));
        when(repositorio.buscarPorSolicitud("S1")).thenReturn(Optional.of(citada("S1")));
        assertThrows(IllegalStateException.class, () -> servicio.retirar("S1", "m"));

        verify(repositorio, never()).guardar(any());
    }

    // ------------------------------------------------------------------ consultas

    @Test
    void consultarListaDevuelveSoloEnEsperaEnOrdenRn01() {
        when(repositorio.buscarPorTipoPrueba("TAC")).thenReturn(List.of(
                enEspera("ORD", Prioridad.ORDINARIO),
                retirada("RET"),
                enEspera("URG", Prioridad.URGENTE),
                citada("CIT"),
                enEspera("PRE", Prioridad.PREFERENTE)));

        assertEquals(List.of("URG", "PRE", "ORD"), ids(servicio.consultarLista("TAC")));
    }

    @Test
    void consultarListaVaciaYTipoObligatorio() {
        when(repositorio.buscarPorTipoPrueba("RM")).thenReturn(List.of());

        assertTrue(servicio.consultarLista("RM").isEmpty());
        assertThrows(IllegalArgumentException.class, () -> servicio.consultarLista(" "));
    }

    @Test
    void consultarPosicionDevuelveLaPosicionEmpezandoEnUno() {
        EntradaListaEsperaDTO pre = enEspera("PRE", Prioridad.PREFERENTE);
        when(repositorio.buscarPorSolicitud("PRE")).thenReturn(Optional.of(pre));
        when(repositorio.buscarPorTipoPrueba("TAC")).thenReturn(List.of(
                enEspera("ORD", Prioridad.ORDINARIO), pre, enEspera("URG", Prioridad.URGENTE)));

        assertEquals(2, servicio.consultarPosicion("PRE"));
    }

    @Test
    void consultarPosicionExigeEntradaEnEsperaPresenteEnSuLista() {
        when(repositorio.buscarPorSolicitud("CIT")).thenReturn(Optional.of(citada("CIT")));
        assertThrows(IllegalStateException.class, () -> servicio.consultarPosicion("CIT"));

        when(repositorio.buscarPorSolicitud("S1")).thenReturn(Optional.of(enEspera("S1", Prioridad.URGENTE)));
        when(repositorio.buscarPorTipoPrueba("TAC")).thenReturn(List.of());
        assertThrows(IllegalStateException.class, () -> servicio.consultarPosicion("S1"));
    }

    @Test
    void obtenerEntradaDevuelveNullSiNoExiste() {
        EntradaListaEsperaDTO s1 = enEspera("S1", Prioridad.URGENTE);
        when(repositorio.buscarPorSolicitud("S1")).thenReturn(Optional.of(s1));
        when(repositorio.buscarPorSolicitud("NO")).thenReturn(Optional.empty());

        assertSame(s1, servicio.obtenerEntrada("S1"));
        assertNull(servicio.obtenerEntrada("NO"));
        assertThrows(IllegalArgumentException.class, () -> servicio.obtenerEntrada(""));
    }

    // ------------------------------------------------------------------ proponerCandidatos

    @Test
    void proponerCandidatosFiltraPorLaPruebaDeLaFranjaOrdenaYLimitaSinOcuparElTurno() {
        when(disponibilidad.obtenerTurno(TURNO)).thenReturn(turno(EstadoTurno.LIBRE));
        when(agendas.obtenerAgendaPorId("F1")).thenReturn(franja("TAC"));
        when(repositorio.buscarPorTipoPrueba("TAC")).thenReturn(List.of(
                enEspera("ORD", Prioridad.ORDINARIO),
                enEspera("URG", Prioridad.URGENTE),
                retirada("RET"),
                enEspera("PRE", Prioridad.PREFERENTE)));

        assertEquals(List.of("URG", "PRE"), ids(servicio.proponerCandidatos(TURNO, 2)));
        verify(repositorio, never()).buscarPorEstado(any());
        verify(disponibilidad, never()).ocuparTurno(any(), any());
        verify(repositorio, never()).guardar(any());
    }

    @Test
    void proponerCandidatosConFranjaSinPruebaAdmiteCualquierTipo() {
        when(disponibilidad.obtenerTurno(TURNO)).thenReturn(turno(EstadoTurno.LIBRE));
        when(agendas.obtenerAgendaPorId("F1")).thenReturn(franja(null), franja("  "));
        when(repositorio.buscarPorEstado(EstadoEntradaEspera.EN_ESPERA)).thenReturn(List.of(
                enEspera("ORD", Prioridad.ORDINARIO), enEsperaDeTipo("RM-URG", "RM", Prioridad.URGENTE)));

        assertEquals(List.of("RM-URG", "ORD"), ids(servicio.proponerCandidatos(TURNO, 10)));
        assertEquals(List.of("RM-URG"), ids(servicio.proponerCandidatos(TURNO, 1)));
        verify(repositorio, never()).buscarPorTipoPrueba(any());
    }

    @Test
    void proponerCandidatosSinEntradasCompatiblesDevuelveListaVacia() {
        when(disponibilidad.obtenerTurno(TURNO)).thenReturn(turno(EstadoTurno.LIBRE));
        when(agendas.obtenerAgendaPorId("F1")).thenReturn(franja("RM"));
        when(repositorio.buscarPorTipoPrueba("RM")).thenReturn(List.of());

        assertTrue(servicio.proponerCandidatos(TURNO, 3).isEmpty());
    }

    @Test
    void proponerCandidatosValidaTurnoMaximoYFranja() {
        assertThrows(IllegalArgumentException.class, () -> servicio.proponerCandidatos(" ", 1));
        assertThrows(IllegalArgumentException.class, () -> servicio.proponerCandidatos(TURNO, 0));
        when(disponibilidad.obtenerTurno("NO")).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> servicio.proponerCandidatos("NO", 1));
        when(disponibilidad.obtenerTurno("OCU")).thenReturn(turno(EstadoTurno.OCUPADO));
        assertThrows(IllegalStateException.class, () -> servicio.proponerCandidatos("OCU", 1));
        when(disponibilidad.obtenerTurno(TURNO)).thenReturn(turno(EstadoTurno.LIBRE));
        when(agendas.obtenerAgendaPorId("F1")).thenReturn(null);
        assertThrows(IllegalStateException.class, () -> servicio.proponerCandidatos(TURNO, 1));

        verifyNoInteractions(repositorio);
    }

    // ------------------------------------------------------------------ marcarCitada y reincorporar

    @Test
    void marcarCitadaEnlazaLaCitaYSacaLaEntradaDeLaEspera() {
        when(repositorio.buscarPorSolicitud("S1")).thenReturn(Optional.of(enEspera("S1", Prioridad.URGENTE)));
        when(repositorio.guardar(any())).then(returnsFirstArg());

        EntradaListaEsperaDTO citada = servicio.marcarCitada("S1", "C-1");

        assertEquals(EstadoEntradaEspera.CITADA, citada.estado());
        assertEquals("C-1", citada.idCita());
        assertEquals(PRESCRIPCION, citada.fechaPrescripcion());
    }

    @Test
    void marcarCitadaExigeCitaYEntradaEnEspera() {
        assertThrows(IllegalArgumentException.class, () -> servicio.marcarCitada("S1", " "));
        when(repositorio.buscarPorSolicitud("S1")).thenReturn(Optional.of(retirada("S1")));
        assertThrows(IllegalStateException.class, () -> servicio.marcarCitada("S1", "C-1"));

        verify(repositorio, never()).guardar(any());
    }

    @Test
    void reincorporarVuelveAEsperaConservandoLaAntiguedad() {
        when(repositorio.buscarPorSolicitud("S1")).thenReturn(Optional.of(citada("S1")));
        when(repositorio.guardar(any())).then(returnsFirstArg());

        EntradaListaEsperaDTO reincorporada = servicio.reincorporar("S1", "Cita cancelada por avería");

        assertEquals(EstadoEntradaEspera.EN_ESPERA, reincorporada.estado());
        assertNull(reincorporada.idCita());
        assertEquals("Cita cancelada por avería", reincorporada.motivo());
        assertEquals(PRESCRIPCION, reincorporada.fechaPrescripcion());
        assertEquals(AHORA.minusDays(1), reincorporada.fechaInscripcion());
    }

    @Test
    void reincorporarExigeMotivoYEntradaCitada() {
        assertThrows(IllegalArgumentException.class, () -> servicio.reincorporar("S1", ""));
        when(repositorio.buscarPorSolicitud("S1")).thenReturn(Optional.of(enEspera("S1", Prioridad.URGENTE)));
        assertThrows(IllegalStateException.class, () -> servicio.reincorporar("S1", "m"));

        verify(repositorio, never()).guardar(any());
    }

    // ------------------------------------------------------------------ Datos de prueba

    private static SolicitudPruebaDTO solicitud(String id, Prioridad prioridad, EstadoSolicitud estado) {
        return new SolicitudPruebaDTO(id, "pac-" + id, "med-1", "TAC", prioridad, "Indicación", "Justificación",
                PRESCRIPCION, estado, false, null);
    }

    private static EntradaListaEsperaDTO enEspera(String id, Prioridad prioridad) {
        return enEsperaDeTipo(id, "TAC", prioridad);
    }

    private static EntradaListaEsperaDTO enEsperaDeTipo(String id, String tipo, Prioridad prioridad) {
        return new EntradaListaEsperaDTO(id, "pac-" + id, tipo, prioridad, PRESCRIPCION, AHORA.minusDays(1),
                EstadoEntradaEspera.EN_ESPERA, null, null);
    }

    private static EntradaListaEsperaDTO retirada(String id) {
        return new EntradaListaEsperaDTO(id, "pac-" + id, "TAC", Prioridad.URGENTE, PRESCRIPCION, AHORA.minusDays(1),
                EstadoEntradaEspera.RETIRADA, null, "Retirada");
    }

    private static EntradaListaEsperaDTO citada(String id) {
        return new EntradaListaEsperaDTO(id, "pac-" + id, "TAC", Prioridad.URGENTE, PRESCRIPCION, AHORA.minusDays(1),
                EstadoEntradaEspera.CITADA, "C-" + id, null);
    }

    private static EntradaListaEsperaDTO dto(String id, String paciente, String tipo, Prioridad prioridad,
                                             LocalDateTime prescripcion, LocalDateTime inscripcion,
                                             EstadoEntradaEspera estado, String idCita) {
        return new EntradaListaEsperaDTO(id, paciente, tipo, prioridad, prescripcion, inscripcion, estado, idCita, null);
    }

    private static TurnoAgendaDTO turno(EstadoTurno estado) {
        return new TurnoAgendaDTO(TURNO, "F1", TipoRecurso.SALA, "sala-tac", FECHA_TURNO, LocalTime.of(9, 0),
                LocalTime.of(9, 30), estado, null, estado == EstadoTurno.OCUPADO ? "C-9" : null);
    }

    private static AgendaMedicaDTO franja(String pruebaAsociada) {
        return new AgendaMedicaDTO("F1", "sala-tac", pruebaAsociada, FECHA_TURNO, LocalTime.of(9, 0),
                LocalTime.of(11, 0), 30);
    }

    private static List<String> ids(List<EntradaListaEsperaDTO> entradas) {
        return entradas.stream().map(EntradaListaEsperaDTO::idSolicitud).toList();
    }
}
