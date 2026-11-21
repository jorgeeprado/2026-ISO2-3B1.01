package es.uclm.esi.iso2.sescam.usuarios.dominio;

import es.uclm.esi.iso2.sescam.common.dto.PacienteDTO;
import es.uclm.esi.iso2.sescam.common.dto.UsuarioDTO;
import es.uclm.esi.iso2.sescam.common.enums.RolUsuario;
import es.uclm.esi.iso2.sescam.common.interfaces.IAuditoriaService;
import es.uclm.esi.iso2.sescam.common.interfaces.IUsuariosSeguridadService;
import es.uclm.esi.iso2.sescam.common.ports.ISisAdmisionClient;
import es.uclm.esi.iso2.sescam.common.ports.ISsoClient;
import es.uclm.esi.iso2.sescam.usuarios.persistencia.IPacienteRepository;
import es.uclm.esi.iso2.sescam.usuarios.persistencia.IUsuarioRepository;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Implementación principal del contrato transversal {@link IUsuariosSeguridadService} para el Módulo 5.
 * Diseñado bajo la metodología Spec-Driven Development (SDD) y el Proceso Unificado de Desarrollo (PUD):
 * - Iteración 2 (v0.2.0): CU-18 (Gestionar roles y permisos de usuarios bajo modelo RBAC).
 * - Iteración 3: CU-12 (Gestionar alta, modificación y baja lógica de pacientes, RN-08).
 * - Iteración 21: CU-20 (Autenticación federada SSO corporativo, ISsoClient / RNF-04).
 * - Cumplimiento estricto de RN-05: Herencia funcional del rol Médico respecto a Paciente.
 * - Arquitectura desacoplada basada 100% en interfaces (RNF-09).
 */
public class UsuariosSeguridadService implements IUsuariosSeguridadService {

    // =========================================================================
    // Catálogo de permisos de la matriz RBAC
    // =========================================================================

    public static final Set<String> PERMISOS_PACIENTE = Set.of(
            "CONSULTAR_CITA",
            "SOLICITAR_CAMBIO_CITA",
            "SOLICITAR_CANCELACION_CITA",
            "FIRMAR_CONSENTIMIENTO",
            "CHECKLIST_SEGURIDAD",
            "CONSULTAR_HISTORIAL",
            "CONSULTAR_INFORME",
            "COMUNICAR_INCIDENCIA",
            "REALIZAR_ENCUESTA"
    );

    public static final Set<String> PERMISOS_MEDICO_ESPECIFICOS = Set.of(
            "PRESCRIBIR_PRUEBA",
            "PRESCRIBIR_SEGUIMIENTO",
            "REGISTRAR_REALIZACION_PRUEBA",
            "REGISTRAR_INCIDENCIA",
            "CREAR_INFORME",
            "FIRMAR_INFORME",
            "EMITIR_ALERTA_CRITICA",
            "CONSULTAR_ESTADISTICAS_MEDICAS",
            "CONSULTAR_HISTORIAL_CLINICO"
    );

    public static final Set<String> PERMISOS_ADMINISTRATIVO_ADMISION = Set.of(
            "ASIGNAR_CITA",
            "REPROGRAMAR_CITA",
            "CANCELAR_CITA",
            "ADMINISTRAR_AGENDAS",
            "GESTION_LISTA_ESPERA",
            "REASIGNAR_HUECOS",
            "ALTA_PACIENTE",
            "MODIFICAR_PACIENTE",
            "BAJA_PACIENTE",
            "CONSULTAR_PACIENTE",
            "REPORTES_DEMORA"
    );

    public static final Set<String> PERMISOS_ADMIN_SISTEMA = Set.of(
            "CONFIG_SISTEMA",
            "GESTION_ROLES",
            "ADMIN_USUARIOS",
            "CONSULTAR_AUDITORIA",
            "SUPERVISAR_CONECTORES",
            "EXPORTAR_ESTADISTICAS"
    );

    private final IUsuarioRepository usuarioRepository;
    private final ISsoClient ssoClient;
    private final IAuditoriaService auditoriaService;
    private final PacienteService pacienteService;

    // =========================================================================
    // Constructores sobrecargados (CU-12 / CU-18)
    // =========================================================================

    public UsuariosSeguridadService(IPacienteRepository pacienteRepository) {
        this(pacienteRepository, null, null);
    }

    public UsuariosSeguridadService(IPacienteRepository pacienteRepository, ISisAdmisionClient sisAdmisionClient) {
        this(pacienteRepository, sisAdmisionClient, null);
    }

    public UsuariosSeguridadService(IPacienteRepository pacienteRepository,
                                    ISisAdmisionClient sisAdmisionClient,
                                    IAuditoriaService auditoriaService) {
        Objects.requireNonNull(pacienteRepository, "El repositorio de pacientes no puede ser nulo");
        this.pacienteService = new PacienteService(pacienteRepository, sisAdmisionClient, auditoriaService);
        this.usuarioRepository = null;
        this.ssoClient = null;
        this.auditoriaService = auditoriaService;
    }

    public UsuariosSeguridadService(IUsuarioRepository usuarioRepository) {
        this(usuarioRepository, null, null);
    }

    public UsuariosSeguridadService(IUsuarioRepository usuarioRepository, ISsoClient ssoClient) {
        this(usuarioRepository, ssoClient, null);
    }

    public UsuariosSeguridadService(IUsuarioRepository usuarioRepository,
                                    ISsoClient ssoClient,
                                    IAuditoriaService auditoriaService) {
        this.usuarioRepository = Objects.requireNonNull(usuarioRepository, "El repositorio de usuarios no puede ser nulo");
        this.ssoClient = ssoClient;
        this.auditoriaService = auditoriaService;
        this.pacienteService = null;
    }

    public UsuariosSeguridadService(IUsuarioRepository usuarioRepository,
                                    IPacienteRepository pacienteRepository,
                                    ISsoClient ssoClient,
                                    ISisAdmisionClient sisAdmisionClient,
                                    IAuditoriaService auditoriaService) {
        this.usuarioRepository = usuarioRepository;
        this.ssoClient = ssoClient;
        this.auditoriaService = auditoriaService;
        this.pacienteService = (pacienteRepository != null)
                ? new PacienteService(pacienteRepository, sisAdmisionClient, auditoriaService)
                : null;
    }

    // =========================================================================
    // CU-12: Gestionar alta, modificación y baja lógica de pacientes (RN-08)
    // =========================================================================

    @Override
    public PacienteDTO darAltaPaciente(PacienteDTO paciente) {
        if (pacienteService != null) {
            return pacienteService.darAltaPaciente(paciente);
        }
        throw new UnsupportedOperationException(
                "El caso de uso CU-12 (Gestión de Pacientes) no está configurado sin un repositorio de pacientes.");
    }

    @Override
    public PacienteDTO modificarPaciente(PacienteDTO paciente) {
        if (pacienteService != null) {
            return pacienteService.modificarPaciente(paciente);
        }
        throw new UnsupportedOperationException(
                "El caso de uso CU-12 (Gestión de Pacientes) no está configurado sin un repositorio de pacientes.");
    }

    @Override
    public boolean darBajaLogicaPaciente(String pacienteId) {
        if (pacienteService != null) {
            return pacienteService.darBajaLogicaPaciente(pacienteId);
        }
        throw new UnsupportedOperationException(
                "El caso de uso CU-12 (Gestión de Pacientes) no está configurado sin un repositorio de pacientes.");
    }

    @Override
    public PacienteDTO obtenerPaciente(String pacienteId) {
        if (pacienteService != null) {
            return pacienteService.obtenerPaciente(pacienteId);
        }
        throw new UnsupportedOperationException(
                "El caso de uso CU-12 (Gestión de Pacientes) no está configurado sin un repositorio de pacientes.");
    }

    public List<PacienteDTO> listarPacientes(boolean soloActivos) {
        if (pacienteService != null) {
            return pacienteService.listarPacientes(soloActivos);
        }
        return Collections.emptyList();
    }

    // =========================================================================
    // CU-18: Gestión de cuentas de usuario y asignación de roles
    // =========================================================================

    public UsuarioDTO registrarUsuario(UsuarioDTO usuario) {
        validarDatosUsuario(usuario);
        if (usuarioRepository == null) {
            throw new UnsupportedOperationException("El caso de uso CU-18 requiere la configuración de IUsuarioRepository.");
        }

        String usernameLimpio = usuario.getUsername().trim().toLowerCase();
        if (usuarioRepository.existePorUsername(usernameLimpio)) {
            throw new UsuarioYaExisteException("Ya existe una cuenta con el username: " + usernameLimpio);
        }

        if (usuario.getId() == null || usuario.getId().trim().isEmpty()) {
            usuario.setId("USU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        usuario.setUsername(usernameLimpio);
        usuario.setNombre(usuario.getNombre().trim());
        usuario.setApellidos(usuario.getApellidos().trim());
        if (usuario.getEmail() != null) {
            usuario.setEmail(usuario.getEmail().trim().toLowerCase());
        }
        usuario.setActivo(true);

        UsuarioDTO guardado = usuarioRepository.guardar(usuario);
        registrarAuditoriaSegura("CREACION_USUARIO", guardado.getId(), "CONTROL_ACCESO_RBAC");
        return guardado;
    }

    public UsuarioDTO modificarUsuario(UsuarioDTO usuario) {
        if (usuario == null) {
            throw new UsuarioInvalidoException("El usuario a modificar no puede ser nulo");
        }
        if (usuario.getId() == null || usuario.getId().trim().isEmpty()) {
            throw new UsuarioInvalidoException("El ID de usuario es obligatorio para su modificación");
        }
        if (usuarioRepository == null) {
            throw new UnsupportedOperationException("El caso de uso CU-18 requiere la configuración de IUsuarioRepository.");
        }

        UsuarioDTO existente = usuarioRepository.buscarPorId(usuario.getId().trim())
                .orElseThrow(() -> new UsuarioNoEncontradoException("No se encontró el usuario con ID: " + usuario.getId()));

        if (usuario.getUsername() != null && !usuario.getUsername().trim().isEmpty()) {
            String nuevoUsername = usuario.getUsername().trim().toLowerCase();
            Optional<UsuarioDTO> conflicto = usuarioRepository.buscarPorUsername(nuevoUsername);
            if (conflicto.isPresent() && !conflicto.get().getId().equals(existente.getId())) {
                throw new UsuarioYaExisteException("El username " + nuevoUsername + " ya está asignado a otro usuario");
            }
            existente.setUsername(nuevoUsername);
        }

        if (usuario.getNombre() != null) {
            if (usuario.getNombre().trim().isEmpty()) {
                throw new UsuarioInvalidoException("El nombre no puede estar vacío");
            }
            existente.setNombre(usuario.getNombre().trim());
        }

        if (usuario.getApellidos() != null) {
            if (usuario.getApellidos().trim().isEmpty()) {
                throw new UsuarioInvalidoException("Los apellidos no pueden estar vacíos");
            }
            existente.setApellidos(usuario.getApellidos().trim());
        }

        if (usuario.getEmail() != null) {
            existente.setEmail(usuario.getEmail().trim().toLowerCase());
        }

        if (usuario.getRol() != null) {
            existente.setRol(usuario.getRol());
        }

        UsuarioDTO actualizado = usuarioRepository.guardar(existente);
        registrarAuditoriaSegura("MODIFICACION_USUARIO", actualizado.getId(), "CONTROL_ACCESO_RBAC");
        return actualizado;
    }

    public UsuarioDTO asignarRol(String usuarioId, RolUsuario nuevoRol) {
        if (usuarioId == null || usuarioId.trim().isEmpty()) {
            throw new IllegalArgumentException("El identificador del usuario no puede ser nulo o vacío");
        }
        if (nuevoRol == null) {
            throw new IllegalArgumentException("El rol a asignar no puede ser nulo");
        }
        if (usuarioRepository == null) {
            throw new UnsupportedOperationException("El caso de uso CU-18 requiere la configuración de IUsuarioRepository.");
        }

        UsuarioDTO usuario = buscarUsuarioPorIdOUsername(usuarioId.trim())
                .orElseThrow(() -> new UsuarioNoEncontradoException("No se localizó al usuario: " + usuarioId));

        usuario.setRol(nuevoRol);
        UsuarioDTO guardado = usuarioRepository.guardar(usuario);
        registrarAuditoriaSegura("CAMBIO_ROL_" + nuevoRol.name(), guardado.getId(), "CONTROL_ACCESO_RBAC");
        return guardado;
    }

    public boolean cambiarEstadoUsuario(String usuarioId, boolean activo) {
        if (usuarioId == null || usuarioId.trim().isEmpty()) {
            throw new IllegalArgumentException("El identificador del usuario no puede ser nulo o vacío");
        }
        if (usuarioRepository == null) {
            throw new UnsupportedOperationException("El caso de uso CU-18 requiere la configuración de IUsuarioRepository.");
        }

        UsuarioDTO usuario = buscarUsuarioPorIdOUsername(usuarioId.trim())
                .orElseThrow(() -> new UsuarioNoEncontradoException("No se localizó al usuario: " + usuarioId));

        usuario.setActivo(activo);
        usuarioRepository.guardar(usuario);
        registrarAuditoriaSegura(activo ? "ACTIVAR_USUARIO" : "DESACTIVAR_USUARIO", usuario.getId(), "CONTROL_ACCESO_RBAC");
        return true;
    }

    public Optional<UsuarioDTO> obtenerUsuarioPorId(String id) {
        if (id == null || id.trim().isEmpty() || usuarioRepository == null) {
            return Optional.empty();
        }
        return usuarioRepository.buscarPorId(id.trim());
    }

    public Optional<UsuarioDTO> obtenerUsuarioPorUsername(String username) {
        if (username == null || username.trim().isEmpty() || usuarioRepository == null) {
            return Optional.empty();
        }
        return usuarioRepository.buscarPorUsername(username.trim());
    }

    public List<UsuarioDTO> listarUsuarios() {
        if (usuarioRepository == null) {
            return Collections.emptyList();
        }
        return usuarioRepository.listarTodos();
    }

    public List<UsuarioDTO> listarUsuariosPorRol(RolUsuario rol) {
        if (usuarioRepository == null) {
            return Collections.emptyList();
        }
        return usuarioRepository.listarPorRol(rol);
    }

    public Set<String> obtenerPermisosDeUsuario(String usuarioId) {
        if (usuarioId == null || usuarioId.trim().isEmpty() || usuarioRepository == null) {
            return Collections.emptySet();
        }

        Optional<UsuarioDTO> opt = buscarUsuarioPorIdOUsername(usuarioId.trim());
        if (opt.isEmpty() || !opt.get().isActivo() || opt.get().getRol() == null) {
            return Collections.emptySet();
        }

        UsuarioDTO user = opt.get();
        RolUsuario rol = user.getRol();

        switch (rol) {
            case ADMIN_SISTEMA:
                Set<String> all = new HashSet<>();
                all.addAll(PERMISOS_ADMIN_SISTEMA);
                all.addAll(PERMISOS_ADMINISTRATIVO_ADMISION);
                all.addAll(PERMISOS_MEDICO_ESPECIFICOS);
                all.addAll(PERMISOS_PACIENTE);
                return Collections.unmodifiableSet(all);

            case ADMINISTRATIVO_ADMISION:
                return PERMISOS_ADMINISTRATIVO_ADMISION;

            case PACIENTE:
                return PERMISOS_PACIENTE;

            case MEDICO:
                Set<String> permisosMedico = new HashSet<>(PERMISOS_MEDICO_ESPECIFICOS);
                permisosMedico.addAll(PERMISOS_PACIENTE);
                return Collections.unmodifiableSet(permisosMedico);

            default:
                return Collections.emptySet();
        }
    }

    // =========================================================================
    // CU-18: Validación de matriz de permisos RBAC con herencia funcional (RN-05)
    // =========================================================================

    @Override
    public boolean verificarPermiso(String usuarioId, String accionRequerida) {
        if (usuarioRepository == null) {
            throw new UnsupportedOperationException(
                    "El caso de uso CU-18 (Control de acceso RBAC y herencia médica RN-05) requiere la configuración de IUsuarioRepository.");
        }
        if (usuarioId == null || usuarioId.trim().isEmpty() || accionRequerida == null || accionRequerida.trim().isEmpty()) {
            return false;
        }

        Optional<UsuarioDTO> optUser = buscarUsuarioPorIdOUsername(usuarioId.trim());
        if (optUser.isEmpty() || !optUser.get().isActivo()) {
            return false;
        }

        UsuarioDTO user = optUser.get();
        RolUsuario rol = user.getRol();
        if (rol == null) {
            return false;
        }

        String accionLimpia = accionRequerida.trim().toUpperCase();
        boolean autorizado = false;

        switch (rol) {
            case ADMIN_SISTEMA:
                autorizado = true;
                break;

            case ADMINISTRATIVO_ADMISION:
                autorizado = PERMISOS_ADMINISTRATIVO_ADMISION.contains(accionLimpia);
                break;

            case PACIENTE:
                autorizado = PERMISOS_PACIENTE.contains(accionLimpia);
                break;

            case MEDICO:
                autorizado = PERMISOS_MEDICO_ESPECIFICOS.contains(accionLimpia)
                        || PERMISOS_PACIENTE.contains(accionLimpia);
                break;

            default:
                autorizado = false;
                break;
        }

        registrarAuditoriaSegura("VERIFICACION_RBAC_" + (autorizado ? "OK" : "DENIED"), user.getId(), "CONTROL_ACCESO_RBAC");
        return autorizado;
    }

    // =========================================================================
    // CU-18 / CU-20: Autenticación federada contra SSO Corporativo (ISsoClient)
    // =========================================================================

    @Override
    public UsuarioDTO autenticarUsuario(String username, String tokenSso) {
        if (usuarioRepository == null) {
            throw new UnsupportedOperationException(
                    "El caso de uso CU-18 / CU-20 (Autenticación federada SSO) requiere la configuración de IUsuarioRepository.");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio para la autenticación");
        }
        if (tokenSso == null || tokenSso.trim().isEmpty()) {
            throw new IllegalArgumentException("El token SSO es obligatorio para la autenticación");
        }
        if (ssoClient == null) {
            throw new IllegalStateException("El puerto de SSO corporativo (ISsoClient) no ha sido inyectado");
        }

        UsuarioDTO federado = ssoClient.validarTokenFederado(tokenSso.trim());
        if (federado == null) {
            throw new AutenticacionException("El token federado SSO proporcionado no es válido o ha expirado");
        }

        if (federado.getUsername() == null || !federado.getUsername().equalsIgnoreCase(username.trim())) {
            throw new AutenticacionException("El usuario asociado al token SSO no coincide con las credenciales solicitadas");
        }

        if (!federado.isActivo()) {
            throw new AutenticacionException("La cuenta de usuario se encuentra inactiva o bloqueada en el censo corporativo");
        }

        if (federado.getId() == null || federado.getId().trim().isEmpty()) {
            federado.setId("USU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        if (federado.getRol() == null) {
            federado.setRol(RolUsuario.PACIENTE);
        }

        usuarioRepository.guardar(federado);
        registrarAuditoriaSegura("LOGIN_SSO_EXITOSO", federado.getId(), "SSO_CORPORATIVO");
        return federado;
    }

    // =========================================================================
    // Métodos auxiliares privados
    // =========================================================================

    private Optional<UsuarioDTO> buscarUsuarioPorIdOUsername(String idOUsername) {
        if (usuarioRepository == null) {
            return Optional.empty();
        }
        Optional<UsuarioDTO> porId = usuarioRepository.buscarPorId(idOUsername);
        if (porId.isPresent()) {
            return porId;
        }
        return usuarioRepository.buscarPorUsername(idOUsername);
    }

    private void validarDatosUsuario(UsuarioDTO usuario) {
        if (usuario == null) {
            throw new UsuarioInvalidoException("El usuario a registrar no puede ser nulo");
        }
        if (usuario.getUsername() == null || usuario.getUsername().trim().isEmpty()) {
            throw new UsuarioInvalidoException("El username es obligatorio");
        }
        if (usuario.getNombre() == null || usuario.getNombre().trim().isEmpty()) {
            throw new UsuarioInvalidoException("El nombre es obligatorio");
        }
        if (usuario.getApellidos() == null || usuario.getApellidos().trim().isEmpty()) {
            throw new UsuarioInvalidoException("Los apellidos son obligatorios");
        }
        if (usuario.getRol() == null) {
            throw new UsuarioInvalidoException("El rol del usuario es obligatorio para el control RBAC");
        }
    }

    private void registrarAuditoriaSegura(String accion, String entidadId, String recurso) {
        if (auditoriaService != null) {
            try {
                auditoriaService.registrarAccesoClinico("SISTEMA_SEGURIDAD", entidadId, accion, recurso, "127.0.0.1");
            } catch (Exception e) {
                // Trazabilidad resiliente: no bloquea la lógica principal
            }
        }
    }
}
