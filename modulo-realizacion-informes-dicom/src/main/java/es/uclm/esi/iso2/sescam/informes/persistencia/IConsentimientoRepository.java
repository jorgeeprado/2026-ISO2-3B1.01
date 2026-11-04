package es.uclm.esi.iso2.sescam.informes.persistencia;

import es.uclm.esi.iso2.sescam.common.dto.ConsentimientoDTO;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistencia para los consentimientos informados y cuestionarios de seguridad.
 * Satisface la capa de persistencia del Módulo 3 (modulo-realizacion-informes-dicom).
 */
public interface IConsentimientoRepository {

    /**
     * Guarda o actualiza un consentimiento informado en el repositorio.
     *
     * @param consentimiento DTO con los datos del consentimiento y checklist.
     * @return El consentimiento persistido.
     */
    ConsentimientoDTO guardar(ConsentimientoDTO consentimiento);

    /**
     * Busca un consentimiento por su identificador único.
     *
     * @param id Identificador del consentimiento.
     * @return Optional con el consentimiento si existe.
     */
    Optional<ConsentimientoDTO> buscarPorId(String id);

    /**
     * Busca el consentimiento más reciente de un paciente para un tipo de prueba específico.
     *
     * @param pacienteId   Identificador del paciente.
     * @param tipoPruebaId Identificador del tipo de prueba radiológica.
     * @return Optional con el consentimiento si existe.
     */
    Optional<ConsentimientoDTO> buscarPorPacienteYPrueba(String pacienteId, String tipoPruebaId);

    /**
     * Lista todos los consentimientos registrados de un paciente.
     *
     * @param pacienteId Identificador del paciente.
     * @return Lista de consentimientos del paciente.
     */
    List<ConsentimientoDTO> listarPorPaciente(String pacienteId);

    /**
     * Verifica si existe un consentimiento válido registrado para el paciente y prueba.
     *
     * @param pacienteId   Identificador del paciente.
     * @param tipoPruebaId Identificador del tipo de prueba.
     * @return true si existe y está validado; false en caso contrario.
     */
    boolean existeConsentimientoValido(String pacienteId, String tipoPruebaId);

    /**
     * Limpia los datos almacenados (útil para pruebas unitarias).
     */
    void limpiar();
}

