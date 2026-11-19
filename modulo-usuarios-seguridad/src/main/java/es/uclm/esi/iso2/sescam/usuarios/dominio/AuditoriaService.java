package es.uclm.esi.iso2.sescam.usuarios.dominio;

import es.uclm.esi.iso2.sescam.common.dto.AuditoriaAccesoDTO;
import es.uclm.esi.iso2.sescam.common.interfaces.IAuditoriaService;
import es.uclm.esi.iso2.sescam.usuarios.persistencia.IAuditoriaRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio del dominio para el registro síncrono e inmutable de eventos de auditoría clínica (CU-19 / RN-07 / RNF-06).
 * <p>
 * Implementa el contrato transversal {@link IAuditoriaService} de {@code modulo-common-interfaces}.
 * Garantiza que cada acceso, consulta, mutación o firma sobre datos clínicos e imágenes DICOM
 * quede registrado de forma síncrona, trazable e inmutable en el repositorio médico-legal.
 * </p>
 */
public class AuditoriaService implements IAuditoriaService {

    private static final String DEFAULT_IP = "127.0.0.1";
    private static final String DEFAULT_PACIENTE = "N/A";
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final IAuditoriaRepository auditoriaRepository;

    public AuditoriaService(IAuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = Objects.requireNonNull(auditoriaRepository,
                "El repositorio de auditoría no puede ser nulo");
    }

    @Override
    public void registrarAccesoClinico(String usuarioId, String pacienteIdAfectado, String accion,
                                      String recursoAccedido, String ipOrigen) {
        validarParametrosObligatorios(usuarioId, accion, recursoAccedido);

        String idGenerado = "AUD-" + UUID.randomUUID().toString();
        LocalDateTime ahora = LocalDateTime.now();
        String rolResuelto = resolverRolUsuario(usuarioId);
        String pacienteEfectivo = (pacienteIdAfectado != null && !pacienteIdAfectado.trim().isEmpty())
                ? pacienteIdAfectado.trim()
                : DEFAULT_PACIENTE;
        String ipEfectiva = (ipOrigen != null && !ipOrigen.trim().isEmpty())
                ? ipOrigen.trim()
                : DEFAULT_IP;

        AuditoriaAccesoDTO dto = new AuditoriaAccesoDTO(
                idGenerado,
                ahora,
                usuarioId.trim(),
                rolResuelto,
                pacienteEfectivo,
                accion.trim().toUpperCase(),
                recursoAccedido.trim().toUpperCase(),
                ipEfectiva
        );

        auditoriaRepository.guardar(dto);
    }

    @Override
    public List<AuditoriaAccesoDTO> consultarAuditorias(LocalDateTime desde, LocalDateTime hasta, String pacienteId) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new AuditoriaInvalidaException(
                    "El parámetro temporal 'desde' (" + desde + ") no puede ser posterior a 'hasta' (" + hasta + ")");
        }
        return auditoriaRepository.buscar(desde, hasta, pacienteId);
    }

    /**
     * Sobrecarga de registro directo recibiendo un DTO ya ensamblado.
     *
     * @param registro DTO con los datos del evento a auditar.
     * @return El registro guardado con timestamp e identificador.
     */
    public AuditoriaAccesoDTO registrarAcceso(AuditoriaAccesoDTO registro) {
        if (registro == null) {
            throw new AuditoriaInvalidaException("El registro de auditoría no puede ser nulo");
        }
        validarParametrosObligatorios(registro.getUsuarioId(), registro.getAccion(), registro.getRecursoAccedido());

        if (registro.getId() == null || registro.getId().trim().isEmpty()) {
            registro.setId("AUD-" + UUID.randomUUID().toString());
        }
        if (registro.getTimestamp() == null) {
            registro.setTimestamp(LocalDateTime.now());
        }
        if (registro.getRolUsuario() == null || registro.getRolUsuario().trim().isEmpty()) {
            registro.setRolUsuario(resolverRolUsuario(registro.getUsuarioId()));
        }
        if (registro.getPacienteIdAfectado() == null || registro.getPacienteIdAfectado().trim().isEmpty()) {
            registro.setPacienteIdAfectado(DEFAULT_PACIENTE);
        }
        if (registro.getIpOrigen() == null || registro.getIpOrigen().trim().isEmpty()) {
            registro.setIpOrigen(DEFAULT_IP);
        }

        return auditoriaRepository.guardar(registro);
    }

    /**
     * Consulta el historial de accesos ejecutados por un facultativo, administrativo o paciente específico.
     *
     * @param usuarioId Identificador del usuario.
     * @return Lista inmutable de registros asociados.
     */
    public List<AuditoriaAccesoDTO> consultarAuditoriasPorUsuario(String usuarioId) {
        if (usuarioId == null || usuarioId.trim().isEmpty()) {
            throw new AuditoriaInvalidaException("El identificador de usuario a consultar no puede ser nulo o vacío");
        }
        return auditoriaRepository.buscarPorUsuario(usuarioId.trim());
    }

    /**
     * Consulta el historial de eventos por tipo de acción (ej. LECTURA, FIRMA, EXPORTACION).
     *
     * @param accion Acción clínica auditada.
     * @return Lista inmutable de registros asociados.
     */
    public List<AuditoriaAccesoDTO> consultarAuditoriasPorAccion(String accion) {
        if (accion == null || accion.trim().isEmpty()) {
            throw new AuditoriaInvalidaException("La acción clínica a consultar no puede ser nula o vacía");
        }
        return auditoriaRepository.buscarPorAccion(accion.trim().toUpperCase());
    }

    /**
     * Obtiene una traza concreta de auditoría por su identificador unívoco.
     *
     * @param id Identificador del registro.
     * @return {@link Optional} con el registro si existe.
     */
    public Optional<AuditoriaAccesoDTO> consultarAuditoriaPorId(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }
        return auditoriaRepository.buscarPorId(id.trim());
    }

    /**
     * Devuelve el total acumulado de eventos registrados en el log de auditoría.
     *
     * @return Número total de eventos.
     */
    public long contarAuditorias() {
        return auditoriaRepository.contar();
    }

    /**
     * Exporta las trazas de auditoría coincidentes en formato CSV estructurado para inspección médico-legal (AEPD / SESCAM).
     *
     * @param desde      Fecha y hora de inicio del filtro temporal (opcional).
     * @param hasta      Fecha y hora de fin del filtro temporal (opcional).
     * @param pacienteId Identificador del paciente a filtrar (opcional).
     * @return Cadena con el contenido CSV estandarizado.
     */
    public String exportarAuditoriasCsv(LocalDateTime desde, LocalDateTime hasta, String pacienteId) {
        List<AuditoriaAccesoDTO> trazas = consultarAuditorias(desde, hasta, pacienteId);
        StringBuilder sb = new StringBuilder();
        sb.append("ID,TIMESTAMP,USUARIO_ID,ROL_USUARIO,PACIENTE_ID,ACCION,RECURSO,IP_ORIGEN\n");

        for (AuditoriaAccesoDTO t : trazas) {
            sb.append(sanitizarCsv(t.getId())).append(",");
            sb.append(t.getTimestamp() != null ? t.getTimestamp().format(ISO_FORMATTER) : "").append(",");
            sb.append(sanitizarCsv(t.getUsuarioId())).append(",");
            sb.append(sanitizarCsv(t.getRolUsuario())).append(",");
            sb.append(sanitizarCsv(t.getPacienteIdAfectado())).append(",");
            sb.append(sanitizarCsv(t.getAccion())).append(",");
            sb.append(sanitizarCsv(t.getRecursoAccedido())).append(",");
            sb.append(sanitizarCsv(t.getIpOrigen())).append("\n");
        }
        return sb.toString();
    }

    /**
     * Verifica que cualquier intento de alterar un registro auditado sea estrictamente bloqueado (RN-07 / RNF-06).
     *
     * @param registro Registro que se pretende mutar.
     */
    public void intentarModificarRegistro(AuditoriaAccesoDTO registro) {
        try {
            auditoriaRepository.actualizar(registro);
        } catch (UnsupportedOperationException ex) {
            throw new OperacionAuditoriaNoPermitidaException(
                    "Bloqueo médico-legal: Se ha interceptado un intento ilegal de modificación en el log de auditoría",
                    ex);
        }
    }

    /**
     * Verifica que cualquier intento de eliminar un registro auditado sea estrictamente bloqueado (RN-07 / RNF-06).
     *
     * @param id Identificador del registro a eliminar.
     */
    public void intentarEliminarRegistro(String id) {
        try {
            auditoriaRepository.eliminar(id);
        } catch (UnsupportedOperationException ex) {
            throw new OperacionAuditoriaNoPermitidaException(
                    "Bloqueo médico-legal: Se ha interceptado un intento ilegal de borrado en el log de auditoría",
                    ex);
        }
    }

    private void validarParametrosObligatorios(String usuarioId, String accion, String recursoAccedido) {
        if (usuarioId == null || usuarioId.trim().isEmpty()) {
            throw new AuditoriaInvalidaException("El identificador del usuario operador es obligatorio para auditar");
        }
        if (accion == null || accion.trim().isEmpty()) {
            throw new AuditoriaInvalidaException("La acción clínica ejecutada es obligatoria para auditar");
        }
        if (recursoAccedido == null || recursoAccedido.trim().isEmpty()) {
            throw new AuditoriaInvalidaException("El recurso clínico accedido es obligatorio para auditar");
        }
    }

    private String resolverRolUsuario(String usuarioId) {
        if (usuarioId == null) {
            return "DESCONOCIDO";
        }
        String uLower = usuarioId.toLowerCase();
        if (uLower.startsWith("med") || uLower.contains("medico")) {
            return "MEDICO";
        }
        if (uLower.startsWith("adm") || uLower.contains("admin")) {
            return "ADMIN_SISTEMA";
        }
        if (uLower.startsWith("aux") || uLower.contains("admision")) {
            return "ADMINISTRATIVO_ADMISION";
        }
        if (uLower.startsWith("pac") || uLower.contains("paciente")) {
            return "PACIENTE";
        }
        return "USUARIO_AUTENTICADO";
    }

    private String sanitizarCsv(String valor) {
        if (valor == null) {
            return "";
        }
        if (valor.contains(",") || valor.contains("\"") || valor.contains("\n")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }
}
