package es.uclm.esi.iso2.sescam.citas.dominio;

import es.uclm.esi.iso2.sescam.citas.persistencia.IConsultaAgendasRepository;
import es.uclm.esi.iso2.sescam.citas.presentacion.ConsultaSolicitudesAgendasController;
import es.uclm.esi.iso2.sescam.common.dto.SolicitudPruebaDTO;
import es.uclm.esi.iso2.sescam.common.dto.TurnoAgendaDTO;
import es.uclm.esi.iso2.sescam.common.enums.EstadoSolicitud;
import es.uclm.esi.iso2.sescam.common.enums.EstadoTurno;
import es.uclm.esi.iso2.sescam.common.enums.Prioridad;
import es.uclm.esi.iso2.sescam.common.enums.TipoRecurso;
import es.uclm.esi.iso2.sescam.common.interfaces.ConsultaSolicitudesAgendasService;
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

@ExtendWith(MockitoExtension.class)
class CU08ConsultaSolicitudesTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 11, 20);

    @Mock
    private IPrescripcionService prescripcionService;
    @Mock
    private IConsultaAgendasRepository consultaAgendasRepository;
    @Mock
    private ConsultaSolicitudesAgendasService consultaService;

    private ConsultaSolicitudesAgendasServiceImpl servicio;

    @BeforeEach
    void setUp() {
        servicio = new ConsultaSolicitudesAgendasServiceImpl(prescripcionService, consultaAgendasRepository);
    }

    @Test
    void constructorExigeServicios() {
        assertThrows(NullPointerException.class,
            () -> new ConsultaSolicitudesAgendasServiceImpl(null, consultaAgendasRepository));
        assertThrows(NullPointerException.class,
            () -> new ConsultaSolicitudesAgendasServiceImpl(prescripcionService, null));
        assertThrows(NullPointerException.class, () -> new ConsultaSolicitudesAgendasController(null));
    }

    @Test
    void consultaSolicitudesDelMedicoConservaEstadoYSinCompartirDto() {
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
        verifyNoInteractions(consultaAgendasRepository);
    }

    @Test
    void consultaSolicitudesExigeMedico() {
        assertThrows(IllegalArgumentException.class, () -> servicio.consultarSolicitudesPorMedico(null));
        assertThrows(IllegalArgumentException.class, () -> servicio.consultarSolicitudesPorMedico(" "));
        verifyNoInteractions(prescripcionService, consultaAgendasRepository);
    }

    @Test
    void consultaHuecosDisponiblesDelegaYDevuelveListaInmutable() {
        TurnoAgendaDTO turno = new TurnoAgendaDTO("A1#09:00", "A1", TipoRecurso.MEDICO, "MED-1", FECHA,
                LocalTime.of(9, 0), LocalTime.of(9, 30), EstadoTurno.LIBRE, null, null);
        when(consultaAgendasRepository.consultarAgendasDisponibles(TipoRecurso.MEDICO, "MED-1", FECHA))
                .thenReturn(List.of(turno));

        List<TurnoAgendaDTO> resultado = servicio.consultarAgendasDisponibles(TipoRecurso.MEDICO, "MED-1", FECHA);

        assertEquals(List.of(turno), resultado);
        assertThrows(UnsupportedOperationException.class, resultado::clear);
        verify(consultaAgendasRepository).consultarAgendasDisponibles(TipoRecurso.MEDICO, "MED-1", FECHA);
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
        verifyNoInteractions(consultaAgendasRepository, prescripcionService);
    }

    @Test
    void controladorDelegaConsultasAlServicio() {
        ConsultaSolicitudesAgendasController controlador = new ConsultaSolicitudesAgendasController(consultaService);
        List<SolicitudPruebaDTO> solicitudes = List.of();
        List<TurnoAgendaDTO> turnos = List.of();
        when(consultaService.consultarSolicitudesPorMedico("MED-1")).thenReturn(solicitudes);
        when(consultaService.consultarAgendasDisponibles(TipoRecurso.SALA, "SALA-1", FECHA)).thenReturn(turnos);

        assertEquals(solicitudes, controlador.consultarSolicitudesPorMedico("MED-1"));
        assertEquals(turnos, controlador.consultarAgendasDisponibles(TipoRecurso.SALA, "SALA-1", FECHA));

        verify(consultaService).consultarSolicitudesPorMedico("MED-1");
        verify(consultaService).consultarAgendasDisponibles(TipoRecurso.SALA, "SALA-1", FECHA);
        verifyNoInteractions(prescripcionService, consultaAgendasRepository);
    }
}
