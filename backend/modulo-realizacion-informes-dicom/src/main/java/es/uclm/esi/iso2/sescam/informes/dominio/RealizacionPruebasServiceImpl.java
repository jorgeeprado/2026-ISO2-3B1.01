package es.uclm.esi.iso2.sescam.informes.dominio;

import es.uclm.esi.iso2.sescam.common.dto.RegistroRealizacionDTO;
import es.uclm.esi.iso2.sescam.common.ports.IPacsClient;
import es.uclm.esi.iso2.sescam.informes.persistencia.IRegistroRealizacionRepository;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio de dominio que implementa la lógica de negocio para documentar la realización técnica
 * y clínica de pruebas radiológicas (CU-10 / RF-10).
 * Integra la verificación de consentimiento obligatorio (RN-04) y la validación DICOM en RIS/PACS.
 */
public class RealizacionPruebasServiceImpl {

    private final IRegistroRealizacionRepository registroRepository;
    private final ConsentimientoService consentimientoService;
    private final IPacsClient pacsClient;

    public RealizacionPruebasServiceImpl(IRegistroRealizacionRepository registroRepository,
                                         ConsentimientoService consentimientoService,
                                         IPacsClient pacsClient) {
        this.registroRepository = Objects.requireNonNull(registroRepository, "El repositorio de registros no puede ser nulo");
        this.consentimientoService = Objects.requireNonNull(consentimientoService, "El servicio de consentimiento no puede ser nulo");
        this.pacsClient = Objects.requireNonNull(pacsClient, "El cliente PACS no puede ser nulo");
    }

    /**
     * Registra la ejecución material de una prueba radiológica con validación de seguridad contextual (CU-10).
     *
     * @param registro               DTO con los parámetros técnicos de ejecución, dosis, incidencias y StudyInstanceUID.
     * @param pacienteId             Identificador del paciente a quien se le realiza la prueba.
     * @param tipoPruebaId           Identificador del tipo de prueba del catálogo.
     * @param esResonanciaMagnetica  true si la prueba es una Resonancia Magnética (RM).
     * @return El registro de realización persistido.
     * @throws IllegalArgumentException         si los parámetros requeridos son nulos o vacíos.
     * @throws EstudioDicomNoValidoException    si el StudyInstanceUID no supera la verificación en el PACS.
     * @throws ConsentimientoRequeridoException si se usa contraste o RM y no consta consentimiento previo validado (RN-04).
     */
    public RegistroRealizacionDTO registrarRealizacion(RegistroRealizacionDTO registro,
                                                       String pacienteId,
                                                       String tipoPruebaId,
                                                       boolean esResonanciaMagnetica) {
        validarParametrosObligatorios(registro);

        // 1. Integración con RIS/PACS: Verificación del StudyInstanceUID
        boolean dicomValido = pacsClient.validarEstudioDicom(registro.getStudyInstanceUID());
        if (!dicomValido) {
            throw new EstudioDicomNoValidoException("El StudyInstanceUID [" + registro.getStudyInstanceUID()
                    + "] no existe o no es válido en el servidor central PACS");
        }

        // 2. Regla de Negocio RN-04: Bloqueo por consentimiento y cuestionario de seguridad previo
        boolean requiereConsentimiento = registro.isContrasteAdministrado() || esResonanciaMagnetica;
        if (requiereConsentimiento) {
            boolean consentimientoValido = false;
            if (pacienteId != null && tipoPruebaId != null) {
                consentimientoValido = consentimientoService.validarConsentimientoPrevio(pacienteId, tipoPruebaId);
            } else {
                consentimientoValido = registro.isConsentimientoPrevioValidado();
            }

            if (!consentimientoValido) {
                throw new ConsentimientoRequeridoException("Bloqueo de seguridad asistencial (RN-04): "
                        + "No consta consentimiento informado firmado y checklist de seguridad apto para el paciente ["
                        + pacienteId + "] y prueba [" + tipoPruebaId + "]. Imposible documentar prueba con contraste o RM.");
            }
            registro.setConsentimientoPrevioValidado(true);
        }

        // 3. Autogeneración de identificador y fecha si no fueron suministrados
        if (registro.getId() == null || registro.getId().trim().isEmpty()) {
            registro.setId("REG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        if (registro.getFechaHoraEjecucion() == null) {
            registro.setFechaHoraEjecucion(LocalDateTime.now());
        }

        // 4. Mapeo a entidad de dominio y persistencia
        RegistroRealizacion entidad = RegistroRealizacion.desdeDTO(registro);
        entidad.setPacienteId(pacienteId);
        entidad.setTipoPruebaId(tipoPruebaId);

        RegistroRealizacion guardada = registroRepository.guardar(entidad);
        return guardada.aDTO();
    }

    /**
     * Sobrecarga para registrar una realización delegada directamente desde el contrato general IRealizacionInformesService.
     *
     * @param registro DTO con los datos de realización.
     * @return El registro persistido.
     */
    public RegistroRealizacionDTO registrarRealizacionPrueba(RegistroRealizacionDTO registro) {
        return registrarRealizacion(registro, null, null, false);
    }

    /**
     * Consulta el registro de realización asociado a una cita.
     *
     * @param citaId Identificador de la cita médica.
     * @return Optional con el DTO si existe.
     */
    public Optional<RegistroRealizacionDTO> consultarPorCitaId(String citaId) {
        return registroRepository.buscarPorCitaId(citaId).map(RegistroRealizacion::aDTO);
    }

    /**
     * Consulta un registro de realización por su StudyInstanceUID de PACS.
     *
     * @param studyInstanceUID Identificador del estudio DICOM.
     * @return Optional con el DTO si existe.
     */
    public Optional<RegistroRealizacionDTO> consultarPorStudyInstanceUID(String studyInstanceUID) {
        return registroRepository.buscarPorStudyInstanceUID(studyInstanceUID).map(RegistroRealizacion::aDTO);
    }

    private void validarParametrosObligatorios(RegistroRealizacionDTO r) {
        if (r == null) {
            throw new IllegalArgumentException("El registro de realizacion no puede ser nulo");
        }
        if (r.getCitaId() == null || r.getCitaId().trim().isEmpty()) {
            throw new IllegalArgumentException("El identificador de cita es obligatorio");
        }
        if (r.getMedicoRadiologoId() == null || r.getMedicoRadiologoId().trim().isEmpty()) {
            throw new IllegalArgumentException("El identificador del medico o tecnico responsable es obligatorio");
        }
        if (r.getStudyInstanceUID() == null || r.getStudyInstanceUID().trim().isEmpty()) {
            throw new IllegalArgumentException("El StudyInstanceUID es obligatorio para vincular las imagenes DICOM");
        }
        if (r.getDosisRadiacionMgy() < 0.0) {
            throw new IllegalArgumentException("La dosis de radiacion administrada no puede ser negativa");
        }
    }
}

