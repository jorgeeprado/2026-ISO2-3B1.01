package es.uclm.esi.iso2.sescam.notificaciones.presentacion;

import es.uclm.esi.iso2.sescam.common.dto.NotificacionDTO;
import es.uclm.esi.iso2.sescam.common.interfaces.INotificacionesCalidadService;
import java.util.List;
import java.util.Objects;

public class NotificacionController {

    private final INotificacionesCalidadService service;

    public NotificacionController(INotificacionesCalidadService service) {
        this.service = Objects.requireNonNull(service, "El servicio de notificaciones no puede ser nulo");
    }

    public NotificacionDTO enviarRecordatorioCita(String citaId, String pacienteId) {
        return service.enviarRecordatorioCita(citaId, pacienteId);
    }

    public NotificacionDTO notificarInformeDisponible(String pacienteId, String informeId) {
        return service.notificarInformeDisponible(pacienteId, informeId);
    }

    public List<NotificacionDTO> consultarNotificaciones(String usuarioId) {
        return service.consultarNotificaciones(usuarioId);
    }
}
