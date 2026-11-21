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
import es.uclm.esi.iso2.sescam.common.interfaces.IAnaliticaEstadisticasService;
import es.uclm.esi.iso2.sescam.common.interfaces.ICatalogoConfiguracionService;
import es.uclm.esi.iso2.sescam.common.interfaces.ICitasAgendasService;
import es.uclm.esi.iso2.sescam.common.interfaces.INotificacionesCalidadService;
import es.uclm.esi.iso2.sescam.common.interfaces.IPrescripcionService;
import es.uclm.esi.iso2.sescam.common.interfaces.IRealizacionInformesService;
import es.uclm.esi.iso2.sescam.common.interfaces.IUsuariosSeguridadService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Cliente / Gateway de acceso a los servicios del Backend del SESCAM (RNF-11).
 * Implementa el patrón Gateway / Facade para la capa Cliente (Frontend), comunicándose
 * exclusivamente a través de los contratos e interfaces de {@code modulo-common-interfaces} (RNF-09).
 */
public class SescamBackendGateway {

    private final IUsuariosSeguridadService usuariosSeguridadService;
    private final IPrescripcionService prescripcionService;
    private final ICitasAgendasService citasAgendasService;
    private final IRealizacionInformesService realizacionInformesService;
    private final INotificacionesCalidadService notificacionesCalidadService;
    private final ICatalogoConfiguracionService catalogoConfiguracionService;
    private final IAnaliticaEstadisticasService analiticaEstadisticasService;

    public SescamBackendGateway(IUsuariosSeguridadService usuariosSeguridadService,
                                IPrescripcionService prescripcionService,
                                ICitasAgendasService citasAgendasService,
                                IRealizacionInformesService realizacionInformesService,
                                INotificacionesCalidadService notificacionesCalidadService,
                                ICatalogoConfiguracionService catalogoConfiguracionService,
                                IAnaliticaEstadisticasService analiticaEstadisticasService) {
        this.usuariosSeguridadService = Objects.requireNonNull(usuariosSeguridadService, "El servicio de usuarios no puede ser nulo");
        this.prescripcionService = Objects.requireNonNull(prescripcionService, "El servicio de prescripcion no puede ser nulo");
        this.citasAgendasService = Objects.requireNonNull(citasAgendasService, "El servicio de citas no puede ser nulo");
        this.realizacionInformesService = Objects.requireNonNull(realizacionInformesService, "El servicio de realizacion e informes no puede ser nulo");
        this.notificacionesCalidadService = Objects.requireNonNull(notificacionesCalidadService, "El servicio de notificaciones no puede ser nulo");
        this.catalogoConfiguracionService = Objects.requireNonNull(catalogoConfiguracionService, "El servicio de catalogo no puede ser nulo");
        this.analiticaEstadisticasService = Objects.requireNonNull(analiticaEstadisticasService, "El servicio de analitica no puede ser nulo");
    }

    // --- Métodos de Usuarios y Seguridad (Módulo 5 / CU-12 / CU-18 / CU-20) ---
    public UsuarioDTO autenticar(String username, String tokenSso) {
        return usuariosSeguridadService.autenticarUsuario(username, tokenSso);
    }

    public boolean verificarAcceso(String usuarioId, String accionRequerida) {
        return usuariosSeguridadService.verificarPermiso(usuarioId, accionRequerida);
    }

    public PacienteDTO registrarPaciente(PacienteDTO paciente) {
        return usuariosSeguridadService.darAltaPaciente(paciente);
    }

    public PacienteDTO consultarPaciente(String pacienteId) {
        return usuariosSeguridadService.obtenerPaciente(pacienteId);
    }

    // --- Métodos de Prescripción (Módulo 2 / CU-06 / CU-07 / CU-05) ---
    public SolicitudPruebaDTO prescribirPrueba(SolicitudPruebaDTO solicitud) {
        return prescripcionService.prescribirPrueba(solicitud);
    }

    public List<SolicitudPruebaDTO> listarPrescripcionesPaciente(String pacienteId) {
        return prescripcionService.listarSolicitudesPorPaciente(pacienteId);
    }

    // --- Métodos de Citas y Agendas (Módulo 1 / CU-01 / CU-13) ---
    public List<CitaDTO> listarCitasPaciente(String pacienteId) {
        return citasAgendasService.listarCitasPorPaciente(pacienteId);
    }

    public boolean confirmarCita(String citaId) {
        return citasAgendasService.confirmarCita(citaId);
    }

    public CitaDTO solicitarCambioCita(String citaId, String motivo) {
        return citasAgendasService.solicitarCambioCita(citaId, motivo);
    }

    // --- Métodos de Realización e Informes (Módulo 3 / CU-03 / CU-09 / CU-22) ---
    public List<InformeRadiologicoDTO> consultarHistorialInformes(String pacienteId) {
        return realizacionInformesService.consultarHistorialPruebas(pacienteId);
    }

    public ConsentimientoDTO firmarConsentimiento(ConsentimientoDTO consentimiento) {
        return realizacionInformesService.firmarConsentimientoInformado(consentimiento);
    }

    // --- Métodos de Notificaciones y Calidad (Módulo 4 / CU-02 / CU-04) ---
    public List<NotificacionDTO> consultarNotificaciones(String usuarioId) {
        return notificacionesCalidadService.consultarNotificaciones(usuarioId);
    }

    public IncidenciaDTO reportarIncidenciaOEncuesta(IncidenciaDTO incidencia) {
        return notificacionesCalidadService.registrarIncidenciaOEncuesta(incidencia);
    }

    // --- Métodos de Catálogo (Módulo 6 / CU-17) ---
    public List<PruebaCatalogoDTO> obtenerCatalogoActivo() {
        return catalogoConfiguracionService.listarCatalogoActivo();
    }

    // --- Métodos de Analítica y Estadísticas (Módulo 7 / CU-16 / CU-21) ---
    public EstadisticaOperativaDTO obtenerInformeDemoras(LocalDate inicio, LocalDate fin) {
        return analiticaEstadisticasService.generarInformeOperativoAgendas(inicio, fin);
    }

    public Map<String, Object> obtenerCuadroMando() {
        return analiticaEstadisticasService.generarCuadroMandoGlobal();
    }

    // Getters de los contratos
    public IUsuariosSeguridadService getUsuariosSeguridadService() { return usuariosSeguridadService; }
    public IPrescripcionService getPrescripcionService() { return prescripcionService; }
    public ICitasAgendasService getCitasAgendasService() { return citasAgendasService; }
    public IRealizacionInformesService getRealizacionInformesService() { return realizacionInformesService; }
    public INotificacionesCalidadService getNotificacionesCalidadService() { return notificacionesCalidadService; }
    public ICatalogoConfiguracionService getCatalogoConfiguracionService() { return catalogoConfiguracionService; }
    public IAnaliticaEstadisticasService getAnaliticaEstadisticasService() { return analiticaEstadisticasService; }
}
