package es.uclm.esi.iso2.sescam.usuarios.presentacion;

import es.uclm.esi.iso2.sescam.common.dto.AuditoriaAccesoDTO;
import es.uclm.esi.iso2.sescam.common.interfaces.IAuditoriaService;
import es.uclm.esi.iso2.sescam.usuarios.dominio.AuditoriaService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Controlador de la capa de presentación para la inspección, consulta y exportación
 * de trazas de auditoría clínico-legal (CU-19 / RN-07 / RNF-06).
 * <p>
 * Depende exclusivamente del contrato transversal {@link IAuditoriaService} (RNF-09).
 * Proporciona a los auditores y administradores médicos del SESCAM los puntos de acceso
 * seguros para verificar accesos a historias clínicas y estudios DICOM.
 * </p>
 */
public class AuditoriaController {

    private final IAuditoriaService auditoriaService;

    public AuditoriaController(IAuditoriaService auditoriaService) {
        this.auditoriaService = Objects.requireNonNull(auditoriaService,
                "El servicio de auditoría no puede ser nulo");
    }

    /**
     * Registra de manera síncrona e inmutable un evento de acceso a recursos clínicos.
     *
     * @param usuarioId          Identificador del usuario que ejecuta la acción.
     * @param pacienteIdAfectado Identificador del paciente cuyos datos son consultados o modificados.
     * @param accion             Tipo de acción clínica (ej. LECTURA, CREACION, MODIFICACION, FIRMA).
     * @param recursoAccedido    Recurso clínico involucrado (ej. HISTORIA_CLINICA, INFORME_DICOM).
     * @param ipOrigen           Dirección IP del cliente u origen de la petición.
     */
    public void registrarAccesoClinico(String usuarioId, String pacienteIdAfectado, String accion,
                                      String recursoAccedido, String ipOrigen) {
        auditoriaService.registrarAccesoClinico(usuarioId, pacienteIdAfectado, accion, recursoAccedido, ipOrigen);
    }

    /**
     * Consulta trazas de auditoría según filtros opcionales de ventana temporal y paciente.
     *
     * @param desde      Fecha y hora de inicio (opcional).
     * @param hasta      Fecha y hora de fin (opcional).
     * @param pacienteId Identificador del paciente (opcional).
     * @return Lista inmutable de registros coincidentes.
     */
    public List<AuditoriaAccesoDTO> consultarAuditorias(LocalDateTime desde, LocalDateTime hasta, String pacienteId) {
        return auditoriaService.consultarAuditorias(desde, hasta, pacienteId);
    }

    /**
     * Exporta el conjunto de trazas solicitadas en formato CSV para inspección regulatoria.
     *
     * @param desde      Fecha y hora de inicio (opcional).
     * @param hasta      Fecha y hora de fin (opcional).
     * @param pacienteId Identificador del paciente (opcional).
     * @return Cadena formateada en CSV con encabezados estándar.
     */
    public String exportarAuditoriasCsv(LocalDateTime desde, LocalDateTime hasta, String pacienteId) {
        if (auditoriaService instanceof AuditoriaService) {
            AuditoriaService serviceConcreto = (AuditoriaService) auditoriaService;
            return serviceConcreto.exportarAuditoriasCsv(desde, hasta, pacienteId);
        }

        // Respaldo conforme al contrato IAuditoriaService
        List<AuditoriaAccesoDTO> trazas = auditoriaService.consultarAuditorias(desde, hasta, pacienteId);
        StringBuilder sb = new StringBuilder();
        sb.append("ID,TIMESTAMP,USUARIO_ID,ROL_USUARIO,PACIENTE_ID,ACCION,RECURSO,IP_ORIGEN\n");
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        for (AuditoriaAccesoDTO t : trazas) {
            sb.append(t.getId() != null ? t.getId() : "").append(",");
            sb.append(t.getTimestamp() != null ? t.getTimestamp().format(fmt) : "").append(",");
            sb.append(t.getUsuarioId() != null ? t.getUsuarioId() : "").append(",");
            sb.append(t.getRolUsuario() != null ? t.getRolUsuario() : "").append(",");
            sb.append(t.getPacienteIdAfectado() != null ? t.getPacienteIdAfectado() : "").append(",");
            sb.append(t.getAccion() != null ? t.getAccion() : "").append(",");
            sb.append(t.getRecursoAccedido() != null ? t.getRecursoAccedido() : "").append(",");
            sb.append(t.getIpOrigen() != null ? t.getIpOrigen() : "").append("\n");
        }
        return sb.toString();
    }

    /**
     * Consulta trazas ejecutadas por un usuario específico.
     *
     * @param usuarioId Identificador del operador.
     * @return Lista de trazas del usuario.
     */
    public List<AuditoriaAccesoDTO> consultarAuditoriasPorUsuario(String usuarioId) {
        if (auditoriaService instanceof AuditoriaService) {
            AuditoriaService serviceConcreto = (AuditoriaService) auditoriaService;
            return serviceConcreto.consultarAuditoriasPorUsuario(usuarioId);
        }
        if (usuarioId == null) {
            return Collections.emptyList();
        }
        return auditoriaService.consultarAuditorias(null, null, null).stream()
                .filter(a -> usuarioId.equalsIgnoreCase(a.getUsuarioId()))
                .collect(Collectors.toList());
    }

    /**
     * Consulta trazas por tipo de acción realizada.
     *
     * @param accion Tipo de acción.
     * @return Lista de trazas filtradas.
     */
    public List<AuditoriaAccesoDTO> consultarAuditoriasPorAccion(String accion) {
        if (auditoriaService instanceof AuditoriaService) {
            AuditoriaService serviceConcreto = (AuditoriaService) auditoriaService;
            return serviceConcreto.consultarAuditoriasPorAccion(accion);
        }
        if (accion == null) {
            return Collections.emptyList();
        }
        return auditoriaService.consultarAuditorias(null, null, null).stream()
                .filter(a -> accion.equalsIgnoreCase(a.getAccion()))
                .collect(Collectors.toList());
    }

    /**
     * Intenta actualizar un registro de auditoría, verificando el bloqueo estricto de inmutabilidad (RN-07 / RNF-06).
     *
     * @param registro Registro que se pretende mutar.
     * @return false si la modificación fue bloqueada; true si de manera anómala se permitiera.
     */
    public boolean intentarModificarRegistro(AuditoriaAccesoDTO registro) {
        if (auditoriaService instanceof AuditoriaService) {
            AuditoriaService serviceConcreto = (AuditoriaService) auditoriaService;
            try {
                serviceConcreto.intentarModificarRegistro(registro);
                return true;
            } catch (UnsupportedOperationException ex) {
                return false;
            }
        }
        return false;
    }

    /**
     * Intenta eliminar un registro de auditoría, verificando el bloqueo estricto de inmutabilidad (RN-07 / RNF-06).
     *
     * @param id Identificador del registro a eliminar.
     * @return false si la eliminación fue bloqueada; true si de manera anómala se permitiera.
     */
    public boolean intentarEliminarRegistro(String id) {
        if (auditoriaService instanceof AuditoriaService) {
            AuditoriaService serviceConcreto = (AuditoriaService) auditoriaService;
            try {
                serviceConcreto.intentarEliminarRegistro(id);
                return true;
            } catch (UnsupportedOperationException ex) {
                return false;
            }
        }
        return false;
    }
}
