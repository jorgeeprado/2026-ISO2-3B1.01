package es.uclm.esi.iso2.sescam.common.interfaces;

import es.uclm.esi.iso2.sescam.common.dto.CitaDTO;

import java.util.Optional;

/** Contrato de reasignación explícita de huecos liberados (CU-23). */
public interface ReasignacionHuecosService {

    /**
     * Reasigna un hueco libre próximo a otra solicitud compatible en espera.
     *
     * @return la cita asignada, o vacío si no hay un candidato elegible
     */
    Optional<CitaDTO> reasignarHuecoLiberado(String idTurno, String idSolicitudCancelada);
}
