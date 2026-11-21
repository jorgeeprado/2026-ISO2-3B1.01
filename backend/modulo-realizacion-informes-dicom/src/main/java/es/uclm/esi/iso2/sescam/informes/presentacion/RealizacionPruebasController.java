package es.uclm.esi.iso2.sescam.informes.presentacion;

import es.uclm.esi.iso2.sescam.common.dto.RegistroRealizacionDTO;
import es.uclm.esi.iso2.sescam.informes.dominio.RealizacionPruebasServiceImpl;

import java.util.Objects;
import java.util.Optional;

/**
 * Controlador de la capa de presentación para documentar la ejecución técnica y clínica de pruebas (CU-10).
 * Expone endpoints o métodos invocados desde el terminal de sala diagnóstica o visor de estación de trabajo.
 */
public class RealizacionPruebasController {

    private final RealizacionPruebasServiceImpl realizacionService;

    public RealizacionPruebasController(RealizacionPruebasServiceImpl realizacionService) {
        this.realizacionService = Objects.requireNonNull(realizacionService, "El servicio de realizacion no puede ser nulo");
    }

    /**
     * Documenta la realización de la prueba radiológica con comprobación de seguridad.
     *
     * @param registro DTO con parámetros técnicos, incidencias, dosis y StudyInstanceUID.
     * @param pacienteId Identificador del paciente citado.
     * @param tipoPruebaId Tipo de prueba realizada.
     * @param esRM true si se trata de Resonancia Magnética.
     * @return El registro de realización validado y persistido.
     */
    public RegistroRealizacionDTO registrarEjecucionPrueba(RegistroRealizacionDTO registro,
                                                           String pacienteId,
                                                           String tipoPruebaId,
                                                           boolean esRM) {
        return realizacionService.registrarRealizacion(registro, pacienteId, tipoPruebaId, esRM);
    }

    /**
     * Consulta el registro de realización asociado a una cita.
     *
     * @param citaId Identificador de la cita.
     * @return Optional con el DTO si existe.
     */
    public Optional<RegistroRealizacionDTO> consultarRegistroPorCita(String citaId) {
        return realizacionService.consultarPorCitaId(citaId);
    }

    /**
     * Consulta el registro de realización a partir del StudyInstanceUID de PACS.
     *
     * @param studyInstanceUID Identificador del estudio DICOM.
     * @return Optional con el DTO si existe.
     */
    public Optional<RegistroRealizacionDTO> consultarRegistroPorStudyInstanceUID(String studyInstanceUID) {
        return realizacionService.consultarPorStudyInstanceUID(studyInstanceUID);
    }
}

