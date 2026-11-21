package es.uclm.esi.iso2.sescam.usuarios.persistencia;

import es.uclm.esi.iso2.sescam.common.dto.PacienteDTO;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistencia para la gestión de pacientes y censo demográfico.
 * Satisface la capa de persistencia del Módulo 5 (modulo-usuarios-seguridad).
 */
public interface IPacienteRepository {

    /**
     * Guarda o actualiza los datos de un paciente en el repositorio.
     *
     * @param paciente DTO con la información demográfica y clínica del paciente.
     * @return El paciente persistido.
     */
    PacienteDTO guardar(PacienteDTO paciente);

    /**
     * Busca un paciente por su identificador interno único.
     *
     * @param id Identificador interno del paciente (ej: PAC-001).
     * @return Optional con el paciente si existe.
     */
    Optional<PacienteDTO> buscarPorId(String id);

    /**
     * Busca un paciente por su Código de Identificación del Paciente (CIP) del SESCAM.
     *
     * @param cip Código CIP oficial.
     * @return Optional con el paciente si existe.
     */
    Optional<PacienteDTO> buscarPorCip(String cip);

    /**
     * Busca un paciente por su Documento Nacional de Identidad (DNI/NIE).
     *
     * @param dni DNI/NIE del paciente.
     * @return Optional con el paciente si existe.
     */
    Optional<PacienteDTO> buscarPorDni(String dni);

    /**
     * Busca un paciente verificando primero por identificador interno y, si no coincide, por CIP.
     *
     * @param idOCip Identificador o CIP del paciente.
     * @return Optional con el paciente si existe.
     */
    Optional<PacienteDTO> buscarPorIdOCip(String idOCip);

    /**
     * Devuelve la lista completa de pacientes censados, independientemente de su estado lógico.
     *
     * @return Lista total de pacientes.
     */
    List<PacienteDTO> listarTodos();

    /**
     * Devuelve la lista de pacientes con estado activo (activo == true).
     *
     * @return Lista de pacientes activos.
     */
    List<PacienteDTO> listarActivos();

    /**
     * Comprueba si existe algún paciente registrado con el CIP indicado.
     *
     * @param cip Código CIP a comprobar.
     * @return true si existe; false en caso contrario.
     */
    boolean existePorCip(String cip);

    /**
     * Comprueba si existe algún paciente registrado con el DNI indicado.
     *
     * @param dni DNI a comprobar.
     * @return true si existe; false en caso contrario.
     */
    boolean existePorDni(String dni);

    /**
     * Limpia los datos almacenados en el repositorio (diseñado para aislamiento de pruebas unitarias).
     */
    void limpiar();
}

