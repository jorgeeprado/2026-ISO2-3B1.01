package es.uclm.esi.iso2.sescam.citas.presentacion;

import es.uclm.esi.iso2.sescam.citas.dominio.ReasignacionHuecosServiceImpl;
import es.uclm.esi.iso2.sescam.common.dto.CitaDTO;

import java.util.Objects;
import java.util.Optional;

/** Controlador de presentación del CU-23 para la reasignación urgente de huecos. */
public class ReasignacionHuecosController {

    private final ReasignacionHuecosServiceImpl reasignacionService;

    public ReasignacionHuecosController(ReasignacionHuecosServiceImpl reasignacionService) {
        this.reasignacionService = Objects.requireNonNull(reasignacionService,
                "El servicio de reasignación no puede ser nulo");
    }

    public Optional<CitaDTO> reasignarHuecoLiberado(String idTurno, String idSolicitudCancelada) {
        return reasignacionService.reasignarHuecoLiberado(idTurno, idSolicitudCancelada);
    }
}