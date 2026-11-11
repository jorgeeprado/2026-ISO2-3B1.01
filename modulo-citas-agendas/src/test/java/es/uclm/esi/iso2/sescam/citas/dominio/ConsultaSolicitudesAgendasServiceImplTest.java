package es.uclm.esi.iso2.sescam.citas.dominio;

import es.uclm.esi.iso2.sescam.citas.persistencia.FranjaRepositoryInMemory;
import es.uclm.esi.iso2.sescam.citas.persistencia.TurnoRepositoryInMemory;
import es.uclm.esi.iso2.sescam.common.dto.AgendaMedicaDTO;
import es.uclm.esi.iso2.sescam.common.dto.SolicitudPruebaDTO;
import es.uclm.esi.iso2.sescam.common.dto.TurnoAgendaDTO;
import es.uclm.esi.iso2.sescam.common.enums.EstadoSolicitud;
import es.uclm.esi.iso2.sescam.common.enums.EstadoTurno;
import es.uclm.esi.iso2.sescam.common.enums.Prioridad;
import es.uclm.esi.iso2.sescam.common.enums.TipoRecurso;
import es.uclm.esi.iso2.sescam.common.interfaces.DisponibilidadAgendaService;
import es.uclm.esi.iso2.sescam.common.interfaces.IPrescripcionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** Pruebas unitarias del servicio de consulta CU-08. */
@ExtendWith(MockitoExtension.class)
class ConsultaSolicitudesAgendasServiceImplTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 11, 20);

    @Mock
    private IPrescripcionService prescripcionService;
    @Mock
    private DisponibilidadAgendaService disponibilidadAgendaService;

    private ConsultaSolicitudesAgendasServiceImpl servicio;

    @BeforeEach
    void setUp() {
        servicio = new ConsultaSolicitudesAgendasServiceImpl(prescripcionService, disponibilidadAgendaService);
    }

    @Test
    void constructorExigeServicios() {
        assertThrows(NullPointerException.class,
                () -> new ConsultaSolicitudesAgendasServiceImpl(null, disponibilidadAgendaService));
        assertThrows(NullPointerException.class,
                () -> new ConsultaSolicitudesAgendasServiceImpl(prescripcionService, null));
    }

    @Test
    void consultaSolicitudesConservaEstadoYSinCompartirDto() {
        SolicitudPruebaDTO original = new SolicitudPruebaDTO("SOL-1", "PAC-1", "MED-1", "TAC",
                Prioridad.URGENTE, "Dolor", "Descartar patología", LocalDateTime.of(2026, 11, 1, 10, 0),
                EstadoSolicitud.PENDIENTE_CITACION, true, 6);
        List<SolicitudPruebaDTO> origenMutable = new ArrayList<>(List.of(original));
        when(prescripcionService.listarSolicitudesPorMedico("MED-1")).thenReturn(origenMutable);

        List<SolicitudPruebaDTO> resultado = servicio.consultarSolicitudesPorMedico("MED-1");
        original.setEstado(EstadoSolicitud.CANCELADA);
        origenMutable.clear();

        assertEquals(1, resultado.size());
        assertNotSame(original, resultado.get(0));
        assertEquals(EstadoSolicitud.PENDIENTE_CITACION, resultado.get(0).getEstado());
        assertEquals("TAC", resultado.get(0).getTipoPruebaId());
        assertThrows(UnsupportedOperationException.class, resultado::clear);
        verify(prescripcionService).listarSolicitudesPorMedico("MED-1");
        verifyNoInteractions(disponibilidadAgendaService);
    }

    @Test
    void consultaSolicitudesRechazaIdentificadorInvalidoYRespuestaNula() {
        assertThrows(IllegalArgumentException.class, () -> servicio.consultarSolicitudesPorMedico(null));
        assertThrows(IllegalArgumentException.class, () -> servicio.consultarSolicitudesPorMedico(" "));
        when(prescripcionService.listarSolicitudesPorMedico("MED-1")).thenReturn(null);
        assertThrows(IllegalStateException.class, () -> servicio.consultarSolicitudesPorMedico("MED-1"));
        verifyNoInteractions(disponibilidadAgendaService);
    }

    @Test
    void consultaHuecosDelegaEnDisponibilidadYDevuelveListaInmutable() {
        TurnoAgendaDTO turno = new TurnoAgendaDTO("A1#09:00", "A1", TipoRecurso.MEDICO, "MED-1", FECHA,
                LocalTime.of(9, 0), LocalTime.of(9, 30), EstadoTurno.LIBRE, null, null);
        when(disponibilidadAgendaService.consultarHuecosLibres(TipoRecurso.MEDICO, "MED-1", FECHA))
                .thenReturn(List.of(turno));

        List<TurnoAgendaDTO> resultado = servicio.consultarAgendasDisponibles(TipoRecurso.MEDICO, "MED-1", FECHA);

        assertEquals(List.of(turno), resultado);
        assertThrows(UnsupportedOperationException.class, resultado::clear);
        verify(disponibilidadAgendaService).consultarHuecosLibres(TipoRecurso.MEDICO, "MED-1", FECHA);
        verifyNoInteractions(prescripcionService);
    }

    @Test
    void consultaHuecosValidaParametrosAntesDeDelegar() {
        assertThrows(IllegalArgumentException.class,
                () -> servicio.consultarAgendasDisponibles(null, "MED-1", FECHA));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.consultarAgendasDisponibles(TipoRecurso.MEDICO, " ", FECHA));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.consultarAgendasDisponibles(TipoRecurso.MEDICO, "MED-1", null));
        verifyNoInteractions(disponibilidadAgendaService, prescripcionService);
    }

    @Test
    void consultaHuecosUsaAgendaServiceRealYRespetaTurnosOcupadosYBloqueados() {
        AgendaServiceImpl agendaService = new AgendaServiceImpl(
                new FranjaRepositoryInMemory(), new TurnoRepositoryInMemory());
        AgendaMedicaDTO franja = new AgendaMedicaDTO("A1", "MED-1", "TAC", FECHA,
                LocalTime.of(9, 0), LocalTime.of(11, 0), 30);
        agendaService.crearFranja(franja, TipoRecurso.MEDICO);
        agendaService.ocuparTurno("A1#09:00", "CITA-1");
        agendaService.bloquearTurno("A1#09:30", "Mantenimiento");
        ConsultaSolicitudesAgendasServiceImpl servicioConAgendaReal =
                new ConsultaSolicitudesAgendasServiceImpl(prescripcionService, agendaService);

        List<TurnoAgendaDTO> resultado = servicioConAgendaReal.consultarAgendasDisponibles(
                TipoRecurso.MEDICO, "MED-1", FECHA);

        assertEquals(List.of("A1#10:00", "A1#10:30"), resultado.stream()
                .map(TurnoAgendaDTO::idTurno)
                .toList());
        verifyNoInteractions(prescripcionService);
    }
}