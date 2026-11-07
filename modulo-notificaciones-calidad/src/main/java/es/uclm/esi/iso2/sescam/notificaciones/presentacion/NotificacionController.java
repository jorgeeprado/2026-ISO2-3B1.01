package es.uclm.esi.iso2.sescam.notificaciones.presentacion;

import es.uclm.esi.iso2.sescam.common.dto.FiltroNotificacionDTO;
import es.uclm.esi.iso2.sescam.common.dto.NotificacionDTO;
import es.uclm.esi.iso2.sescam.common.interfaces.INotificacionesCalidadService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notificaciones")
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

    @GetMapping
    public List<NotificacionDTO> consultarNotificacionesPorUsuario(
            @RequestParam("usuarioId") String usuarioId,
            @RequestParam(name = "leida", required = false) Boolean leida,
            @RequestParam(name = "fechaDesde", required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(name = "fechaHasta", required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta) {
        return service.consultarNotificacionesPorUsuario(
                new FiltroNotificacionDTO(usuarioId, leida, fechaDesde, fechaHasta));
    }

    @PutMapping("/{notificacionId}/leida")
    public NotificacionDTO marcarComoLeida(@PathVariable("notificacionId") Long notificacionId) {
        return service.marcarComoLeida(notificacionId);
    }
}
