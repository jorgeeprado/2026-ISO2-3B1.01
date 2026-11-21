package es.uclm.esi.iso2.sescam.usuarios.persistencia;

import es.uclm.esi.iso2.sescam.common.dto.AuditoriaAccesoDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de repositorio desacoplado para el almacenamiento síncrono e inmutable
 * del log de auditoría médico-legal (CU-19 / RN-07 / RNF-06 / RNF-09).
 * <p>
 * Diseñado estrictamente en modo <b>append-only</b>: solo admite inserción y lectura.
 * Cualquier operación de modificación (UPDATE) o borrado (DELETE/TRUNCATE) está
 * terminantemente prohibida a nivel de especificación y arquitectura.
 * </p>
 */
public interface IAuditoriaRepository {

    /**
     * Inserta un nuevo registro de auditoría de forma síncrona y atómica (modo append-only).
     *
     * @param registro DTO con los metadatos del evento clínico a persistir.
     * @return El registro de auditoría persistido con su identificador inmutable.
     */
    AuditoriaAccesoDTO guardar(AuditoriaAccesoDTO registro);

    /**
     * Busca un registro de auditoría por su identificador unívoco.
     *
     * @param id Identificador inmutable del registro (ej. AUD-...).
     * @return {@link Optional} con el registro si existe, o vacío en caso contrario.
     */
    Optional<AuditoriaAccesoDTO> buscarPorId(String id);

    /**
     * Consulta registros de auditoría aplicando filtros temporales y/o de paciente afectado.
     *
     * @param desde      Fecha y hora de inicio de la ventana de inspección (opcional).
     * @param hasta      Fecha y hora de fin de la ventana de inspección (opcional).
     * @param pacienteId Identificador del paciente cuyos accesos se inspeccionan (opcional).
     * @return Lista inmutable de registros coincidentes.
     */
    List<AuditoriaAccesoDTO> buscar(LocalDateTime desde, LocalDateTime hasta, String pacienteId);

    /**
     * Consulta todos los eventos auditados ejecutados por un usuario específico.
     *
     * @param usuarioId Identificador del usuario operador.
     * @return Lista inmutable de registros asociados al usuario.
     */
    List<AuditoriaAccesoDTO> buscarPorUsuario(String usuarioId);

    /**
     * Consulta todos los eventos auditados asociados a un tipo de acción clínica.
     *
     * @param accion Acción ejecutada (ej. LECTURA, CREACION, MODIFICACION, FIRMA, EXPORTACION).
     * @return Lista inmutable de registros asociados a la acción.
     */
    List<AuditoriaAccesoDTO> buscarPorAccion(String accion);

    /**
     * Devuelve la totalidad de los registros de auditoría almacenados.
     *
     * @return Lista inmutable con todos los eventos médico-legales.
     */
    List<AuditoriaAccesoDTO> listarTodos();

    /**
     * Devuelve el recuento total acumulado de eventos de auditoría registrados.
     *
     * @return Número total de registros inmutables.
     */
    long contar();

    /**
     * Intento de modificar un registro de auditoría existente.
     * <p>
     * <b>PROHIBIDO por RN-07 / RNF-06 / D-007</b>: El log médico-legal es inmutable.
     * </p>
     *
     * @param registro Registro que se pretende alterar.
     * @throws UnsupportedOperationException Siempre, bloqueando cualquier mutación.
     */
    void actualizar(AuditoriaAccesoDTO registro);

    /**
     * Intento de eliminar un registro de auditoría existente.
     * <p>
     * <b>PROHIBIDO por RN-07 / RNF-06 / D-007</b>: Quedan expresamente prohibidas las sentencias DELETE.
     * </p>
     *
     * @param id Identificador del registro a eliminar.
     * @throws UnsupportedOperationException Siempre, bloqueando cualquier borrado.
     */
    void eliminar(String id);

    /**
     * Intento de vaciar o truncar el registro histórico de auditoría.
     * <p>
     * <b>PROHIBIDO por RN-07 / RNF-06 / D-007</b>: Destrucción de evidencia médico-legal prohibida.
     * </p>
     *
     * @throws UnsupportedOperationException Siempre, bloqueando el vaciado del log.
     */
    void vaciar();
}
