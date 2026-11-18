package es.uclm.esi.iso2.sescam.prescripcion.presentacion;

import es.uclm.esi.iso2.sescam.common.dto.SolicitudPruebaDTO;
import es.uclm.esi.iso2.sescam.common.interfaces.IPrescripcionService;
import java.util.List;
import java.util.Objects;

/**
 * Controlador de presentación para prescripciones y seguimientos diagnósticos.
 */
public class PrescripcionController {

    private final IPrescripcionService prescripcionService;

    public PrescripcionController(IPrescripcionService prescripcionService) {
        this.prescripcionService = Objects.requireNonNull(prescripcionService,
                "El servicio de prescripcion no puede ser nulo");
    }

    public SolicitudPruebaDTO prescribirPrueba(SolicitudPruebaDTO solicitud) {
        return prescripcionService.prescribirPrueba(solicitud);
    }

    public SolicitudPruebaDTO prescribirSeguimientoPeriodico(SolicitudPruebaDTO solicitud, int mesesPeriodicidad) {
        return prescripcionService.prescribirSeguimientoPeriodico(solicitud, mesesPeriodicidad);
    }

    public SolicitudPruebaDTO consultarSolicitud(String solicitudId) {
        return prescripcionService.consultarSolicitud(solicitudId);
    }

    public List<SolicitudPruebaDTO> listarSolicitudesPorPaciente(String pacienteId) {
        return prescripcionService.listarSolicitudesPorPaciente(pacienteId);
    }

    public List<SolicitudPruebaDTO> listarSolicitudesPorMedico(String medicoId) {
        return prescripcionService.listarSolicitudesPorMedico(medicoId);
    }

    public SolicitudPruebaDTO autogenerarSiguienteCicloSeguimiento(String solicitudOriginalId) {
        return prescripcionService.autogenerarSiguienteCicloSeguimiento(solicitudOriginalId);
    }
}
