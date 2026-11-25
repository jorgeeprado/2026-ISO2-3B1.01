package es.uclm.esi.iso2.sescam.prescripcion.dominio;

import es.uclm.esi.iso2.sescam.common.dto.PacienteDTO;
import es.uclm.esi.iso2.sescam.common.dto.SolicitudPruebaDTO;
import es.uclm.esi.iso2.sescam.common.enums.EstadoSolicitud;
import es.uclm.esi.iso2.sescam.common.enums.Prioridad;
import es.uclm.esi.iso2.sescam.common.interfaces.IUsuariosSeguridadService;
import es.uclm.esi.iso2.sescam.prescripcion.persistencia.PrescripcionRepositoryInMemory;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrescripcionServiceTest {

    @Mock
    private IUsuariosSeguridadService usuariosService;

    private PrescripcionRepositoryInMemory repositorio;
    private PrescripcionService service;

    @BeforeEach
    void setUp() {
        repositorio = new PrescripcionRepositoryInMemory();
        service = new PrescripcionService(repositorio, usuariosService);
    }

    @Test
    void prescribirPruebaGeneraIdentificadorFechaYEstadoInicial() {
        SolicitudPruebaDTO solicitud = solicitud(null, Prioridad.PREFERENTE, EstadoSolicitud.COMPLETADA);
        solicitud.setEsSeguimientoPeriodico(true);
        solicitud.setPeriodicidadMeses(3);
        when(usuariosService.obtenerPaciente("PAC-1")).thenReturn(new PacienteDTO());

        SolicitudPruebaDTO resultado = service.prescribirPrueba(solicitud);

        assertNotNull(resultado.getId());
        assertTrue(resultado.getId().startsWith("PRES-"));
        assertNotNull(resultado.getFechaSolicitud());
        assertEquals(EstadoSolicitud.PENDIENTE_CITACION, resultado.getEstado());
        assertFalse(resultado.isEsSeguimientoPeriodico());
        assertNull(resultado.getPeriodicidadMeses());
        assertEquals("PAC-1", resultado.getPacienteId());
        assertNotSame(solicitud, resultado);
        assertEquals(resultado.getId(), service.consultarSolicitud(resultado.getId()).getId());
        verify(usuariosService).obtenerPaciente("PAC-1");
    }

    @Test
    void prescribirPruebaConservaIdentificadorYFechaProporcionados() {
        LocalDateTime fecha = LocalDateTime.of(2026, 11, 16, 10, 30);
        SolicitudPruebaDTO solicitud = solicitud("PRES-CUSTOM", Prioridad.ORDINARIO, EstadoSolicitud.CANCELADA);
        solicitud.setFechaSolicitud(fecha);
        when(usuariosService.obtenerPaciente("PAC-1")).thenReturn(new PacienteDTO());

        SolicitudPruebaDTO resultado = service.prescribirPrueba(solicitud);

        assertEquals("PRES-CUSTOM", resultado.getId());
        assertEquals(fecha, resultado.getFechaSolicitud());
        assertEquals(EstadoSolicitud.PENDIENTE_CITACION, resultado.getEstado());
    }

    @Test
    void prescribirPruebaRechazaSolicitudNulaYCadaCampoObligatorio() {
        assertThrows(IllegalArgumentException.class, () -> service.prescribirPrueba(null));

        SolicitudPruebaDTO solicitud = solicitud(null, Prioridad.ORDINARIO, null);
        solicitud.setPacienteId(" ");
        assertThrows(IllegalArgumentException.class, () -> service.prescribirPrueba(solicitud));
        solicitud.setPacienteId("PAC-1");
        solicitud.setMedicoPrescriptorId(null);
        assertThrows(IllegalArgumentException.class, () -> service.prescribirPrueba(solicitud));
        solicitud.setMedicoPrescriptorId("MED-1");
        solicitud.setTipoPruebaId("");
        assertThrows(IllegalArgumentException.class, () -> service.prescribirPrueba(solicitud));
        solicitud.setTipoPruebaId("TAC");
        solicitud.setIndicacionClinica(" ");
        assertThrows(IllegalArgumentException.class, () -> service.prescribirPrueba(solicitud));
        solicitud.setIndicacionClinica("Control");
        solicitud.setJustificacionDiagnostica(null);
        assertThrows(IllegalArgumentException.class, () -> service.prescribirPrueba(solicitud));
        solicitud.setJustificacionDiagnostica("Seguimiento clínico");
        solicitud.setPrioridad(null);
        assertThrows(IllegalArgumentException.class, () -> service.prescribirPrueba(solicitud));

        verifyNoInteractions(usuariosService);
    }

    @Test
    void prescribirPruebaRechazaPacienteInexistente() {
        when(usuariosService.obtenerPaciente("PAC-1")).thenReturn(null);

        assertThrows(PacienteNoEncontradoException.class,
                () -> service.prescribirPrueba(solicitud(null, Prioridad.URGENTE, null)));
        assertTrue(repositorio.listarTodos().isEmpty());
    }

    @Test
    void prescribirSeguimientoPeriodicoPersistePeriodicidad() {
        when(usuariosService.obtenerPaciente("PAC-1")).thenReturn(new PacienteDTO());

        SolicitudPruebaDTO resultado = service.prescribirSeguimientoPeriodico(
                solicitud(null, Prioridad.ORDINARIO, null), 6);

        assertTrue(resultado.isEsSeguimientoPeriodico());
        assertEquals(6, resultado.getPeriodicidadMeses());
        assertEquals(EstadoSolicitud.PENDIENTE_CITACION, resultado.getEstado());
    }

    @Test
    void prescribirSeguimientoPeriodicoRechazaPeriodicidadNoPositiva() {
        SolicitudPruebaDTO solicitud = solicitud(null, Prioridad.ORDINARIO, null);

        assertThrows(IllegalArgumentException.class, () -> service.prescribirSeguimientoPeriodico(solicitud, 0));
        assertThrows(IllegalArgumentException.class, () -> service.prescribirSeguimientoPeriodico(solicitud, -1));
        verifyNoInteractions(usuariosService);
    }

    @Test
    void consultarSolicitudValidaElIdentificadorYLaExistencia() {
        assertThrows(IllegalArgumentException.class, () -> service.consultarSolicitud(" "));
        assertThrows(PrescripcionNoEncontradaException.class, () -> service.consultarSolicitud("NO-EXISTE"));

        when(usuariosService.obtenerPaciente("PAC-1")).thenReturn(new PacienteDTO());
        SolicitudPruebaDTO guardada = service.prescribirPrueba(
                solicitud("PRES-1", Prioridad.URGENTE, null));
        SolicitudPruebaDTO resultado = service.consultarSolicitud("PRES-1");

        assertEquals(guardada.getPacienteId(), resultado.getPacienteId());
        assertNotSame(guardada, resultado);
    }

    @Test
    void listarSolicitudesPorPacienteFiltraInactivasYOrdenaPorPrioridadYFecha() {
        guardar("ORD", "PAC-1", Prioridad.ORDINARIO, EstadoSolicitud.PENDIENTE_CITACION,
                LocalDateTime.of(2026, 1, 1, 0, 0));
        guardar("PREF-TARDE", "PAC-1", Prioridad.PREFERENTE, EstadoSolicitud.CITADA,
                LocalDateTime.of(2026, 1, 2, 0, 0));
        guardar("URG", "PAC-1", Prioridad.URGENTE, EstadoSolicitud.EN_CURSO,
                LocalDateTime.of(2026, 1, 3, 0, 0));
        guardar("PREF-PRONTO", "PAC-1", Prioridad.PREFERENTE, EstadoSolicitud.PENDIENTE_CITACION,
                LocalDateTime.of(2026, 1, 1, 0, 0));
        guardar("COMPLETADA", "PAC-1", Prioridad.URGENTE, EstadoSolicitud.COMPLETADA,
                LocalDateTime.of(2025, 1, 1, 0, 0));
        guardar("CANCELADA", "PAC-1", Prioridad.URGENTE, EstadoSolicitud.CANCELADA,
                LocalDateTime.of(2025, 1, 1, 0, 0));

        List<SolicitudPruebaDTO> resultado = service.listarSolicitudesPorPaciente("PAC-1");

        assertEquals(List.of("URG", "PREF-PRONTO", "PREF-TARDE", "ORD"),
                resultado.stream().map(SolicitudPruebaDTO::getId).toList());
        assertTrue(service.listarSolicitudesPorPaciente("PAC-SIN-SOLICITUDES").isEmpty());
        assertThrows(IllegalArgumentException.class, () -> service.listarSolicitudesPorPaciente(null));
    }

    @Test
    void listarSolicitudesPorMedicoDevuelveSusSolicitudesEnOrdenCronologicoInverso() {
        guardar("ANTIGUA", "PAC-1", "MED-1", Prioridad.ORDINARIO, EstadoSolicitud.COMPLETADA,
                LocalDateTime.of(2026, 1, 1, 0, 0));
        guardar("RECIENTE", "PAC-2", "MED-1", Prioridad.URGENTE, EstadoSolicitud.CANCELADA,
                LocalDateTime.of(2026, 2, 1, 0, 0));
        guardar("OTRO-MEDICO", "PAC-3", "MED-2", Prioridad.URGENTE, EstadoSolicitud.PENDIENTE_CITACION,
                LocalDateTime.of(2026, 3, 1, 0, 0));

        assertEquals(List.of("RECIENTE", "ANTIGUA"),
                service.listarSolicitudesPorMedico("MED-1").stream().map(SolicitudPruebaDTO::getId).toList());
        assertTrue(service.listarSolicitudesPorMedico("MED-SIN-SOLICITUDES").isEmpty());
        assertThrows(IllegalArgumentException.class, () -> service.listarSolicitudesPorMedico(""));
    }

    @Test
    void autogenerarSiguienteCicloCreaSolicitudRelacionada() {
        when(usuariosService.obtenerPaciente("PAC-1")).thenReturn(new PacienteDTO());
        SolicitudPruebaDTO original = solicitud("SEGUIMIENTO-1", Prioridad.PREFERENTE, EstadoSolicitud.COMPLETADA);
        original.setEsSeguimientoPeriodico(true);
        original.setPeriodicidadMeses(12);
        repositorio.guardar(Prescripcion.desdeDTO(original));

        SolicitudPruebaDTO siguiente = service.autogenerarSiguienteCicloSeguimiento("SEGUIMIENTO-1");

        assertNotEquals(original.getId(), siguiente.getId());
        assertEquals(original.getPacienteId(), siguiente.getPacienteId());
        assertEquals(original.getMedicoPrescriptorId(), siguiente.getMedicoPrescriptorId());
        assertEquals(original.getTipoPruebaId(), siguiente.getTipoPruebaId());
        assertEquals(original.getPrioridad(), siguiente.getPrioridad());
        assertEquals(original.getIndicacionClinica(), siguiente.getIndicacionClinica());
        assertEquals(original.getJustificacionDiagnostica(), siguiente.getJustificacionDiagnostica());
        assertTrue(siguiente.isEsSeguimientoPeriodico());
        assertEquals(12, siguiente.getPeriodicidadMeses());
        assertEquals(EstadoSolicitud.PENDIENTE_CITACION, siguiente.getEstado());
        assertEquals(original.getFechaSolicitud().plusMonths(12), siguiente.getFechaSolicitud());
        assertEquals(2, repositorio.listarTodos().size());
        verify(usuariosService).obtenerPaciente("PAC-1");
    }

    @Test
    void autogenerarSiguienteCicloRechazaIdentificadoresYEstadosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> service.autogenerarSiguienteCicloSeguimiento(null));
        assertThrows(PrescripcionNoEncontradaException.class,
                () -> service.autogenerarSiguienteCicloSeguimiento("NO-EXISTE"));

        SolicitudPruebaDTO noPeriodica = solicitud("NO-PERIODICA", Prioridad.ORDINARIO,
                EstadoSolicitud.COMPLETADA);
        repositorio.guardar(Prescripcion.desdeDTO(noPeriodica));
        assertThrows(SeguimientoNoValidoException.class,
                () -> service.autogenerarSiguienteCicloSeguimiento("NO-PERIODICA"));

        SolicitudPruebaDTO pendiente = solicitud("PENDIENTE", Prioridad.ORDINARIO,
                EstadoSolicitud.PENDIENTE_CITACION);
        pendiente.setEsSeguimientoPeriodico(true);
        pendiente.setPeriodicidadMeses(3);
        repositorio.guardar(Prescripcion.desdeDTO(pendiente));
        assertThrows(SeguimientoNoValidoException.class,
                () -> service.autogenerarSiguienteCicloSeguimiento("PENDIENTE"));

        SolicitudPruebaDTO sinPeriodicidad = solicitud("SIN-PERIODICIDAD", Prioridad.ORDINARIO,
                EstadoSolicitud.COMPLETADA);
        sinPeriodicidad.setEsSeguimientoPeriodico(true);
        repositorio.guardar(Prescripcion.desdeDTO(sinPeriodicidad));
        assertThrows(SeguimientoNoValidoException.class,
                () -> service.autogenerarSiguienteCicloSeguimiento("SIN-PERIODICIDAD"));
        verifyNoInteractions(usuariosService);
    }

    @Test
    void constructorRechazaDependenciasNulas() {
        assertThrows(NullPointerException.class, () -> new PrescripcionService(null, usuariosService));
        assertThrows(NullPointerException.class, () -> new PrescripcionService(repositorio, null));
    }

    private SolicitudPruebaDTO solicitud(String id, Prioridad prioridad, EstadoSolicitud estado) {
        return new SolicitudPruebaDTO(id, "PAC-1", "MED-1", "TAC", prioridad,
                "Control evolutivo", "Indicación clínica", LocalDateTime.of(2026, 1, 1, 0, 0),
                estado, false, null);
    }

    private void guardar(String id, String pacienteId, Prioridad prioridad, EstadoSolicitud estado,
                         LocalDateTime fecha) {
        guardar(id, pacienteId, "MED-1", prioridad, estado, fecha);
    }

    private void guardar(String id, String pacienteId, String medicoId, Prioridad prioridad,
                         EstadoSolicitud estado, LocalDateTime fecha) {
        SolicitudPruebaDTO solicitud = new SolicitudPruebaDTO(id, pacienteId, medicoId, "TAC", prioridad,
                "Control", "Seguimiento", fecha, estado, false, null);
        repositorio.guardar(Prescripcion.desdeDTO(solicitud));
    }
}
