package es.uclm.esi.iso2.sescam.frontend.client;

import es.uclm.esi.iso2.sescam.common.dto.CitaDTO;
import es.uclm.esi.iso2.sescam.common.dto.ConsentimientoDTO;
import es.uclm.esi.iso2.sescam.common.dto.EstadisticaOperativaDTO;
import es.uclm.esi.iso2.sescam.common.dto.IncidenciaDTO;
import es.uclm.esi.iso2.sescam.common.dto.InformeRadiologicoDTO;
import es.uclm.esi.iso2.sescam.common.dto.NotificacionDTO;
import es.uclm.esi.iso2.sescam.common.dto.PacienteDTO;
import es.uclm.esi.iso2.sescam.common.dto.PruebaCatalogoDTO;
import es.uclm.esi.iso2.sescam.common.dto.SolicitudPruebaDTO;
import es.uclm.esi.iso2.sescam.common.dto.UsuarioDTO;
import es.uclm.esi.iso2.sescam.common.enums.RolUsuario;
import es.uclm.esi.iso2.sescam.common.interfaces.IAnaliticaEstadisticasService;
import es.uclm.esi.iso2.sescam.common.interfaces.ICatalogoConfiguracionService;
import es.uclm.esi.iso2.sescam.common.interfaces.ICitasAgendasService;
import es.uclm.esi.iso2.sescam.common.interfaces.INotificacionesCalidadService;
import es.uclm.esi.iso2.sescam.common.interfaces.IPrescripcionService;
import es.uclm.esi.iso2.sescam.common.interfaces.IRealizacionInformesService;
import es.uclm.esi.iso2.sescam.common.interfaces.IUsuariosSeguridadService;
import es.uclm.esi.iso2.sescam.frontend.ui.SescamClientView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias :: Capa Cliente (Frontend) - SescamBackendGateway y SescamClientView (RNF-11)")
class SescamBackendGatewayTest {

    @Mock
    private IUsuariosSeguridadService usuariosService;
    @Mock
    private IPrescripcionService prescripcionService;
    @Mock
    private ICitasAgendasService citasService;
    @Mock
    private IRealizacionInformesService informesService;
    @Mock
    private INotificacionesCalidadService notificacionesService;
    @Mock
    private ICatalogoConfiguracionService catalogoService;
    @Mock
    private IAnaliticaEstadisticasService analiticaService;

    private SescamBackendGateway gateway;
    private SescamClientView view;

    @BeforeEach
    void setUp() {
        gateway = new SescamBackendGateway(
                usuariosService, prescripcionService, citasService,
                informesService, notificacionesService, catalogoService,
                analiticaService
        );
        view = new SescamClientView();
    }

    @Test
    @DisplayName("Debe fallar al instanciar gateway con dependencias nulas")
    void testConstructorValidaciones() {
        assertThrows(NullPointerException.class, () -> new SescamBackendGateway(
                null, prescripcionService, citasService, informesService, notificacionesService, catalogoService, analiticaService
        ));
    }

    @Test
    @DisplayName("Debe delegar autenticación y verificación de permisos al backend")
    void testUsuariosSeguridadDelegacion() {
        UsuarioDTO user = new UsuarioDTO("U1", "jorge", "Jorge", "Prado", "jorge@sescam.es", RolUsuario.MEDICO, true);
        when(usuariosService.autenticarUsuario("jorge", "token-valido")).thenReturn(user);
        when(usuariosService.verificarPermiso("U1", "PRESCRIBIR_PRUEBA")).thenReturn(true);

        UsuarioDTO auth = gateway.autenticar("jorge", "token-valido");
        assertNotNull(auth);
        assertEquals("jorge", auth.getUsername());

        assertTrue(gateway.verificarAcceso("U1", "PRESCRIBIR_PRUEBA"));
        verify(usuariosService, times(1)).autenticarUsuario("jorge", "token-valido");
        verify(usuariosService, times(1)).verificarPermiso("U1", "PRESCRIBIR_PRUEBA");
    }

    @Test
    @DisplayName("Debe delegar alta y consulta de pacientes")
    void testPacientesDelegacion() {
        PacienteDTO pac = new PacienteDTO("P1", "CIP1", "123Z", "Ana", "G", LocalDate.now(), "600", "a@a.es", true);
        when(usuariosService.darAltaPaciente(any(PacienteDTO.class))).thenReturn(pac);
        when(usuariosService.obtenerPaciente("P1")).thenReturn(pac);

        PacienteDTO creado = gateway.registrarPaciente(pac);
        assertEquals("P1", creado.getId());

        PacienteDTO consultado = gateway.consultarPaciente("P1");
        assertEquals("P1", consultado.getId());
    }

    @Test
    @DisplayName("Debe delegar prescripción de pruebas y consulta de historial")
    void testPrescripcionDelegacion() {
        SolicitudPruebaDTO sol = new SolicitudPruebaDTO();
        sol.setId("SOL-1");
        when(prescripcionService.prescribirPrueba(sol)).thenReturn(sol);
        when(prescripcionService.listarSolicitudesPorPaciente("P1")).thenReturn(List.of(sol));

        assertEquals("SOL-1", gateway.prescribirPrueba(sol).getId());
        assertEquals(1, gateway.listarPrescripcionesPaciente("P1").size());
    }

    @Test
    @DisplayName("Debe delegar gestión de citas")
    void testCitasDelegacion() {
        CitaDTO cita = new CitaDTO();
        cita.setId("C1");
        when(citasService.listarCitasPorPaciente("P1")).thenReturn(List.of(cita));
        when(citasService.confirmarCita("C1")).thenReturn(true);
        when(citasService.solicitarCambioCita("C1", "viaje")).thenReturn(cita);

        assertEquals(1, gateway.listarCitasPaciente("P1").size());
        assertTrue(gateway.confirmarCita("C1"));
        assertNotNull(gateway.solicitarCambioCita("C1", "viaje"));
    }

    @Test
    @DisplayName("Debe delegar informes y consentimientos")
    void testInformesYConsentimientoDelegacion() {
        InformeRadiologicoDTO inf = new InformeRadiologicoDTO();
        inf.setId("INF-1");
        when(informesService.consultarHistorialPruebas("P1")).thenReturn(List.of(inf));

        ConsentimientoDTO cons = new ConsentimientoDTO();
        cons.setId("CONS-1");
        when(informesService.firmarConsentimientoInformado(cons)).thenReturn(cons);

        assertEquals(1, gateway.consultarHistorialInformes("P1").size());
        assertEquals("CONS-1", gateway.firmarConsentimiento(cons).getId());
    }

    @Test
    @DisplayName("Debe delegar notificaciones y calidad")
    void testNotificacionesYCalidadDelegacion() {
        NotificacionDTO notif = new NotificacionDTO();
        when(notificacionesService.consultarNotificaciones("U1")).thenReturn(List.of(notif));

        IncidenciaDTO inc = new IncidenciaDTO();
        inc.setId("INC-1");
        when(notificacionesService.registrarIncidenciaOEncuesta(inc)).thenReturn(inc);

        assertEquals(1, gateway.consultarNotificaciones("U1").size());
        assertEquals("INC-1", gateway.reportarIncidenciaOEncuesta(inc).getId());

        NotificacionDTO alerta = new NotificacionDTO();
        when(notificacionesService.emitirAlertaHallazgoCritico("M1", "INF-1", "Hemorragia")).thenReturn(alerta);

        assertSame(alerta, gateway.emitirAlertaHallazgoCritico("M1", "INF-1", "Hemorragia"));
    }

    @Test
    @DisplayName("Debe delegar catálogo y analítica")
    void testCatalogoYAnaliticaDelegacion() {
        PruebaCatalogoDTO prueba = new PruebaCatalogoDTO();
        when(catalogoService.listarCatalogoActivo()).thenReturn(List.of(prueba));

        EstadisticaOperativaDTO est = new EstadisticaOperativaDTO();
        when(analiticaService.generarInformeOperativoAgendas(any(), any())).thenReturn(est);
        when(analiticaService.generarCuadroMandoGlobal()).thenReturn(Map.of("total", 100));

        assertEquals(1, gateway.obtenerCatalogoActivo().size());
        assertNotNull(gateway.obtenerInformeDemoras(LocalDate.now(), LocalDate.now()));
        assertEquals(100, gateway.obtenerCuadroMando().get("total"));
    }

    @Test
    @DisplayName("Debe renderizar encabezados de vista cliente de forma segura")
    void testClientViewEncabezado() {
        assertTrue(view.renderEncabezado(null).contains("Sesión no iniciada"));

        UsuarioDTO user = new UsuarioDTO("pedro", "pedro.fuente", "Pedro", "Fuente", "p@sescam.es", RolUsuario.ADMINISTRATIVO_ADMISION, true);
        String header = view.renderEncabezado(user);
        assertTrue(header.contains("pedro.fuente"));
        assertTrue(header.contains("Pedro Fuente"));
        assertTrue(header.contains("ADMINISTRATIVO_ADMISION"));
    }

    @Test
    @DisplayName("Debe proveer opciones de menú acordes a cada rol de usuario")
    void testClientViewMenusPorRol() {
        assertTrue(view.obtenerOpcionesMenuPorRol(null).isEmpty());

        List<String> menuPaciente = view.obtenerOpcionesMenuPorRol(RolUsuario.PACIENTE);
        assertFalse(menuPaciente.isEmpty());
        assertTrue(view.esAccesoPermitidoEnMenu(RolUsuario.PACIENTE, "Citas"));
        assertFalse(view.esAccesoPermitidoEnMenu(RolUsuario.PACIENTE, "Prescribir"));

        List<String> menuMedico = view.obtenerOpcionesMenuPorRol(RolUsuario.MEDICO);
        assertTrue(view.esAccesoPermitidoEnMenu(RolUsuario.MEDICO, "Prescribir"));
        assertTrue(view.esAccesoPermitidoEnMenu(RolUsuario.MEDICO, "DICOM"));

        List<String> menuAdmision = view.obtenerOpcionesMenuPorRol(RolUsuario.ADMINISTRATIVO_ADMISION);
        assertTrue(view.esAccesoPermitidoEnMenu(RolUsuario.ADMINISTRATIVO_ADMISION, "Agendas"));

        List<String> menuAdmin = view.obtenerOpcionesMenuPorRol(RolUsuario.ADMIN_SISTEMA);
        assertTrue(view.esAccesoPermitidoEnMenu(RolUsuario.ADMIN_SISTEMA, "Auditoría"));

        assertFalse(view.esAccesoPermitidoEnMenu(null, "Citas"));
        assertFalse(view.esAccesoPermitidoEnMenu(RolUsuario.PACIENTE, null));
    }

    @Test
    @DisplayName("Debe retornar los servicios subyacentes mediante getters")
    void testGetters() {
        assertSame(usuariosService, gateway.getUsuariosSeguridadService());
        assertSame(prescripcionService, gateway.getPrescripcionService());
        assertSame(citasService, gateway.getCitasAgendasService());
        assertSame(informesService, gateway.getRealizacionInformesService());
        assertSame(notificacionesService, gateway.getNotificacionesCalidadService());
        assertSame(catalogoService, gateway.getCatalogoConfiguracionService());
        assertSame(analiticaService, gateway.getAnaliticaEstadisticasService());
    }
}
