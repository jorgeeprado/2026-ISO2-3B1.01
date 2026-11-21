package es.uclm.esi.iso2.sescam.citas.presentacion;

import es.uclm.esi.iso2.sescam.common.dto.CitaDTO;
import es.uclm.esi.iso2.sescam.common.interfaces.CitacionAdministrativaService;
import es.uclm.esi.iso2.sescam.common.interfaces.ConsultaCitasService;

import java.util.List;
import java.util.Objects;

/**
 * Controlador de presentación del CU-13 para el Personal Administrativo de Admisión:
 * asignación, reprogramación y cancelación definitiva de citas (RN-02).
 */
public class CitacionController {

    private final CitacionAdministrativaService citacionAdministrativaService;
    private final ConsultaCitasService consultaCitasService;

    public CitacionController(CitacionAdministrativaService citacionAdministrativaService,
                              ConsultaCitasService consultaCitasService) {
        this.citacionAdministrativaService = Objects.requireNonNull(citacionAdministrativaService,
                "El servicio de citación administrativa no puede ser nulo");
        this.consultaCitasService = Objects.requireNonNull(consultaCitasService,
                "El servicio de consulta de citas no puede ser nulo");
    }

    public CitaDTO asignarCita(String idSolicitud, String idTurno, String motivo) {
        return citacionAdministrativaService.asignarCita(idSolicitud, idTurno, motivo);
    }

    public CitaDTO reprogramarCita(String idCita, String idTurnoNuevo, String motivo) {
        return citacionAdministrativaService.reprogramarCita(idCita, idTurnoNuevo, motivo);
    }

    public CitaDTO cancelarCita(String idCita, String motivo) {
        return citacionAdministrativaService.cancelarCita(idCita, motivo);
    }

    public CitaDTO obtenerCita(String idCita) {
        return consultaCitasService.obtenerCita(idCita);
    }

    public List<CitaDTO> listarCitasPorSolicitud(String idSolicitud) {
        return consultaCitasService.listarCitasPorSolicitud(idSolicitud);
    }
}
