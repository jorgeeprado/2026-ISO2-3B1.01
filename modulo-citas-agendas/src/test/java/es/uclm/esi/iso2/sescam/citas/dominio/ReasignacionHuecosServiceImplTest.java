package es.uclm.esi.iso2.sescam.citas.dominio;

import es.uclm.esi.iso2.sescam.common.dto.CitaDTO;
import es.uclm.esi.iso2.sescam.common.dto.EntradaListaEsperaDTO;
import es.uclm.esi.iso2.sescam.common.dto.TurnoAgendaDTO;
import es.uclm.esi.iso2.sescam.common.enums.EstadoEntradaEspera;
import es.uclm.esi.iso2.sescam.common.enums.EstadoTurno;
import es.uclm.esi.iso2.sescam.common.enums.Prioridad;
import es.uclm.esi.iso2.sescam.common.enums.TipoRecurso;
import es.uclm.esi.iso2.sescam.common.interfaces.CandidatosListaEsperaService;
import es.uclm.esi.iso2.sescam.common.interfaces.CitacionAdministrativaService;
import es.uclm.esi.iso2.sescam.common.interfaces.DisponibilidadAgendaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReasignacionHuecosServiceImplTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 12, 10, 0);
    private static final LocalDate FECHA = AHORA.toLocalDate();
    private static final String ID_TURNO = "F1#11:00";

    @Mock
    private DisponibilidadAgendaService disponibilidad;
    @Mock
    private CandidatosListaEsperaService candidatos;
    @Mock
    private CitacionAdministrativaService citacion;

    private ReasignacionHuecosServiceImpl servicio;

    @BeforeEach
    void setUp() {
        Clock reloj = Clock.fixed(AHORA.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        servicio = new ReasignacionHuecosServiceImpl(disponibilidad, candidatos, citacion, reloj);
    }

    @Test
    void constructorExigeDependencias() {
        Clock reloj = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);
        assertThrows(NullPointerException.class,
                () -> new ReasignacionHuecosServiceImpl(null, candidatos, citacion, reloj));
        assertThrows(NullPointerException.class,
                () -> new ReasignacionHuecosServiceImpl(disponibilidad, null, citacion, reloj));
        assertThrows(NullPointerException.class,
                () -> new ReasignacionHuecosServiceImpl(disponibilidad, candidatos, null, reloj));
        assertThrows(NullPointerException.class,
                () -> new ReasignacionHuecosServiceImpl(disponibilidad, candidatos, citacion, null));
    }

    @Test
        void reasignaAlPrimerCandidatoElegibleYDejaLaPersistenciaEnCitacionAdministrativa() {
        TurnoAgendaDTO turno = turno(FECHA, LocalTime.of(11, 0), EstadoTurno.LIBRE);
        EntradaListaEsperaDTO primero = entrada("SOL-1", Prioridad.URGENTE);
        EntradaListaEsperaDTO segundo = entrada("SOL-2", Prioridad.ORDINARIO);
        CitaDTO citaCreada = new CitaDTO();
        citaCreada.setId("CITA-1");
        when(disponibilidad.obtenerTurno(ID_TURNO)).thenReturn(turno);
        when(candidatos.proponerCandidatos(ID_TURNO, Integer.MAX_VALUE)).thenReturn(List.of(primero, segundo));
                String motivo = "CU-23: reasignación urgente tras cancelar la solicitud SOL-CANCELADA";
                when(citacion.asignarCita("SOL-1", ID_TURNO, motivo)).thenReturn(citaCreada);

                Optional<CitaDTO> resultado = servicio.reasignarHuecoLiberado(ID_TURNO, "SOL-CANCELADA");

        assertEquals(Optional.of(citaCreada), resultado);
        InOrder orden = inOrder(disponibilidad, candidatos, citacion);
        orden.verify(disponibilidad).obtenerTurno(ID_TURNO);
        orden.verify(candidatos).proponerCandidatos(ID_TURNO, Integer.MAX_VALUE);
        orden.verify(citacion).asignarCita("SOL-1", ID_TURNO, motivo);
    }

    @Test
    void noReasignaSiNoHayCandidatos() {
        when(disponibilidad.obtenerTurno(ID_TURNO)).thenReturn(
                turno(FECHA, LocalTime.of(11, 0), EstadoTurno.LIBRE));
        when(candidatos.proponerCandidatos(ID_TURNO, Integer.MAX_VALUE)).thenReturn(List.of());

        assertEquals(Optional.empty(), servicio.reasignarHuecoLiberado(ID_TURNO, "SOL-CANCELADA"));
        verify(citacion, never()).asignarCita("SOL-1", ID_TURNO, null);
    }

    @Test
    void excluyeLaSolicitudCanceladaYSeleccionaElSiguienteCandidatoRn01() {
        when(disponibilidad.obtenerTurno(ID_TURNO)).thenReturn(
                turno(FECHA, LocalTime.of(11, 0), EstadoTurno.LIBRE));
        when(candidatos.proponerCandidatos(ID_TURNO, Integer.MAX_VALUE)).thenReturn(List.of(
                entrada("SOL-CANCELADA", Prioridad.URGENTE),
                entrada("SOL-2", Prioridad.ORDINARIO)));
        CitaDTO cita = new CitaDTO();
        when(citacion.asignarCita("SOL-2", ID_TURNO,
                "CU-23: reasignación urgente tras cancelar la solicitud SOL-CANCELADA")).thenReturn(cita);

        assertEquals(Optional.of(cita), servicio.reasignarHuecoLiberado(ID_TURNO, "SOL-CANCELADA"));
        verify(citacion).asignarCita("SOL-2", ID_TURNO,
                "CU-23: reasignación urgente tras cancelar la solicitud SOL-CANCELADA");
    }

    @Test
    void noReasignaCuandoSoloQuedaLaSolicitudCancelada() {
        when(disponibilidad.obtenerTurno(ID_TURNO)).thenReturn(
                turno(FECHA, LocalTime.of(11, 0), EstadoTurno.LIBRE));
        when(candidatos.proponerCandidatos(ID_TURNO, Integer.MAX_VALUE))
                .thenReturn(List.of(entrada("SOL-CANCELADA", Prioridad.URGENTE)));

        assertEquals(Optional.empty(), servicio.reasignarHuecoLiberado(ID_TURNO, "SOL-CANCELADA"));
        verifyNoInteractions(citacion);
    }

    @Test
    void noConsultaCandidatosSiElTurnoNoEstaDentroDeLasSiguientes48Horas() {
        when(disponibilidad.obtenerTurno(ID_TURNO)).thenReturn(
                turno(AHORA.plusHours(48).toLocalDate(), AHORA.plusHours(48).toLocalTime(), EstadoTurno.LIBRE));

        assertEquals(Optional.empty(), servicio.reasignarHuecoLiberado(ID_TURNO, "SOL-CANCELADA"));
        verifyNoInteractions(candidatos, citacion);
    }

    @Test
    void noReasignaTurnosPasadosNiQueYaHanEmpezado() {
        when(disponibilidad.obtenerTurno(ID_TURNO)).thenReturn(
                turno(FECHA, LocalTime.of(9, 0), EstadoTurno.LIBRE));
        assertEquals(Optional.empty(), servicio.reasignarHuecoLiberado(ID_TURNO, "SOL-CANCELADA"));
        verifyNoInteractions(candidatos, citacion);
    }

    @Test
    void turnoInexistenteOcupadoOIdentificadorInvalidoSeRechazan() {
        assertThrows(IllegalArgumentException.class,
                () -> servicio.reasignarHuecoLiberado(" ", "SOL-CANCELADA"));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.reasignarHuecoLiberado(ID_TURNO, " "));
        verifyNoInteractions(disponibilidad, candidatos, citacion);

        when(disponibilidad.obtenerTurno("NO-EXISTE")).thenReturn(null);
        assertThrows(IllegalArgumentException.class,
                () -> servicio.reasignarHuecoLiberado("NO-EXISTE", "SOL-CANCELADA"));

        when(disponibilidad.obtenerTurno(ID_TURNO)).thenReturn(
                turno(FECHA, LocalTime.of(11, 0), EstadoTurno.OCUPADO));
        assertThrows(IllegalStateException.class,
                () -> servicio.reasignarHuecoLiberado(ID_TURNO, "SOL-CANCELADA"));
        verifyNoInteractions(candidatos, citacion);
    }

    @Test
    void respuestaNulaDeCandidatosSeTrataComoErrorDeContrato() {
        when(disponibilidad.obtenerTurno(ID_TURNO)).thenReturn(
                turno(FECHA, LocalTime.of(11, 0), EstadoTurno.LIBRE));
        when(candidatos.proponerCandidatos(ID_TURNO, Integer.MAX_VALUE)).thenReturn(null);

        assertThrows(IllegalStateException.class,
                () -> servicio.reasignarHuecoLiberado(ID_TURNO, "SOL-CANCELADA"));
        verifyNoInteractions(citacion);
    }

    @Test
    void candidatoNuloSeTrataComoErrorDeContrato() {
        when(disponibilidad.obtenerTurno(ID_TURNO)).thenReturn(
                turno(FECHA, LocalTime.of(11, 0), EstadoTurno.LIBRE));
        when(candidatos.proponerCandidatos(ID_TURNO, Integer.MAX_VALUE)).thenReturn(
                java.util.Arrays.asList((EntradaListaEsperaDTO) null));

        assertThrows(IllegalStateException.class,
                () -> servicio.reasignarHuecoLiberado(ID_TURNO, "SOL-CANCELADA"));
        verifyNoInteractions(citacion);
    }

    @Test
    void propagaElFalloDeLaCitacionSinOcultarlo() {
        when(disponibilidad.obtenerTurno(ID_TURNO)).thenReturn(
                turno(FECHA, LocalTime.of(11, 0), EstadoTurno.LIBRE));
        when(candidatos.proponerCandidatos(ID_TURNO, Integer.MAX_VALUE))
                .thenReturn(List.of(entrada("SOL-1", Prioridad.URGENTE)));
        when(citacion.asignarCita("SOL-1", ID_TURNO,
                "CU-23: reasignación urgente tras cancelar la solicitud SOL-CANCELADA"))
                .thenThrow(new IllegalStateException("El turno ya no está libre"));

        assertThrows(IllegalStateException.class,
                () -> servicio.reasignarHuecoLiberado(ID_TURNO, "SOL-CANCELADA"));
    }

    @Test
    void rechazaUnaRespuestaNulaDelServicioDeCitacion() {
        when(disponibilidad.obtenerTurno(ID_TURNO)).thenReturn(
                turno(FECHA, LocalTime.of(11, 0), EstadoTurno.LIBRE));
        when(candidatos.proponerCandidatos(ID_TURNO, Integer.MAX_VALUE))
                .thenReturn(List.of(entrada("SOL-1", Prioridad.URGENTE)));
        when(citacion.asignarCita("SOL-1", ID_TURNO,
                "CU-23: reasignación urgente tras cancelar la solicitud SOL-CANCELADA"))
                .thenReturn(null);

        assertThrows(IllegalStateException.class,
                () -> servicio.reasignarHuecoLiberado(ID_TURNO, "SOL-CANCELADA"));
    }

    private static TurnoAgendaDTO turno(LocalDate fecha, LocalTime inicio, EstadoTurno estado) {
        String idCita = estado == EstadoTurno.OCUPADO ? "CITA-EXISTENTE" : null;
        return new TurnoAgendaDTO(ID_TURNO, "F1", TipoRecurso.SALA, "SALA-1", fecha,
                inicio, inicio.plusMinutes(30), estado, null, idCita);
    }

    private static EntradaListaEsperaDTO entrada(String id, Prioridad prioridad) {
        return new EntradaListaEsperaDTO(id, "PAC-1", "TAC", prioridad,
                AHORA.minusDays(1), AHORA.minusHours(1), EstadoEntradaEspera.EN_ESPERA, null, null);
    }
}