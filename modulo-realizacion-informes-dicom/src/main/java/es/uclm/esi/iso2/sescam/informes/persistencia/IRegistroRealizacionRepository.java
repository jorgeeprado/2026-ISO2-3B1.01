package es.uclm.esi.iso2.sescam.informes.persistencia;

import es.uclm.esi.iso2.sescam.informes.dominio.RegistroRealizacion;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistencia para los registros de realización efectiva de pruebas radiológicas.
 */
public interface IRegistroRealizacionRepository {

    /**
     * Guarda o actualiza un registro de realización de prueba.
     *
     * @param registro Entidad de dominio con los detalles técnicos y clínicos.
     * @return El registro guardado.
     */
    RegistroRealizacion guardar(RegistroRealizacion registro);

    /**
     * Busca un registro por su ID único.
     *
     * @param id Identificador del registro.
     * @return Optional con el registro si existe.
     */
    Optional<RegistroRealizacion> buscarPorId(String id);

    /**
     * Busca el registro de realización asociado a una cita específica.
     *
     * @param citaId Identificador de la cita médica.
     * @return Optional con el registro si existe.
     */
    Optional<RegistroRealizacion> buscarPorCitaId(String citaId);

    /**
     * Busca un registro por el identificador universal del estudio DICOM en PACS.
     *
     * @param studyInstanceUID Identificador del estudio DICOM.
     * @return Optional con el registro si existe.
     */
    Optional<RegistroRealizacion> buscarPorStudyInstanceUID(String studyInstanceUID);

    /**
     * Lista todos los registros de realización almacenados.
     *
     * @return Lista de registros.
     */
    List<RegistroRealizacion> listarTodos();

    /**
     * Limpia los datos almacenados (para soporte en pruebas unitarias).
     */
    void limpiar();
}

