package es.uclm.esi.iso2.sescam.usuarios.presentacion;

import es.uclm.esi.iso2.sescam.common.dto.UsuarioDTO;
import es.uclm.esi.iso2.sescam.common.enums.RolUsuario;
import es.uclm.esi.iso2.sescam.common.interfaces.IUsuariosSeguridadService;
import es.uclm.esi.iso2.sescam.usuarios.dominio.UsuariosSeguridadService;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Controlador de la capa de presentación para la administración RBAC, autenticación SSO y cuentas (CU-18).
 * Expone las operaciones para la consola de administración del sistema y módulos cliente.
 * Depende de la interfaz transversal {@link IUsuariosSeguridadService} (RNF-09).
 */
public class UsuariosSeguridadController {

    private final IUsuariosSeguridadService usuariosSeguridadService;

    public UsuariosSeguridadController(IUsuariosSeguridadService usuariosSeguridadService) {
        this.usuariosSeguridadService = Objects.requireNonNull(usuariosSeguridadService,
                "El servicio de usuarios y seguridad no puede ser nulo");
    }

    /**
     * Da de alta a un nuevo usuario con rol asignado en el sistema.
     *
     * @param usuario DTO con los datos de cuenta y rol.
     * @return El usuario registrado.
     */
    public UsuarioDTO registrarUsuario(UsuarioDTO usuario) {
        if (usuariosSeguridadService instanceof UsuariosSeguridadService) {
            UsuariosSeguridadService serviceConcreta = (UsuariosSeguridadService) usuariosSeguridadService;
            return serviceConcreta.registrarUsuario(usuario);
        }
        throw new UnsupportedOperationException("La implementación subyacente no soporta registro directo");
    }

    /**
     * Modifica los datos de un usuario existente.
     *
     * @param usuario DTO con los datos a actualizar.
     * @return El usuario actualizado.
     */
    public UsuarioDTO modificarUsuario(UsuarioDTO usuario) {
        if (usuariosSeguridadService instanceof UsuariosSeguridadService) {
            UsuariosSeguridadService serviceConcreta = (UsuariosSeguridadService) usuariosSeguridadService;
            return serviceConcreta.modificarUsuario(usuario);
        }
        throw new UnsupportedOperationException("La implementación subyacente no soporta modificación directa");
    }

    /**
     * Asigna un nuevo rol RBAC a un usuario existente.
     *
     * @param usuarioId Identificador o username del usuario.
     * @param nuevoRol  Rol a asignar (PACIENTE, MEDICO, ADMINISTRATIVO_ADMISION, ADMIN_SISTEMA).
     * @return El usuario con el rol actualizado.
     */
    public UsuarioDTO asignarRol(String usuarioId, RolUsuario nuevoRol) {
        if (usuariosSeguridadService instanceof UsuariosSeguridadService) {
            UsuariosSeguridadService serviceConcreta = (UsuariosSeguridadService) usuariosSeguridadService;
            return serviceConcreta.asignarRol(usuarioId, nuevoRol);
        }
        throw new UnsupportedOperationException("La implementación subyacente no soporta asignación de roles");
    }

    /**
     * Habilita o deshabilita la cuenta de un usuario.
     *
     * @param usuarioId Identificador del usuario.
     * @param activo    true para activar, false para desactivar.
     * @return true si la operación se completó con éxito.
     */
    public boolean cambiarEstadoUsuario(String usuarioId, boolean activo) {
        if (usuariosSeguridadService instanceof UsuariosSeguridadService) {
            UsuariosSeguridadService serviceConcreta = (UsuariosSeguridadService) usuariosSeguridadService;
            return serviceConcreta.cambiarEstadoUsuario(usuarioId, activo);
        }
        throw new UnsupportedOperationException("La implementación subyacente no soporta cambio de estado");
    }

    /**
     * Verifica si un usuario dispone de autorización para ejecutar una acción requerida (RBAC / RN-05).
     *
     * @param usuarioId       Identificador del usuario.
     * @param accionRequerida Nombre de la acción o caso de uso.
     * @return true si está autorizado; false en caso contrario.
     */
    public boolean verificarAcceso(String usuarioId, String accionRequerida) {
        return usuariosSeguridadService.verificarPermiso(usuarioId, accionRequerida);
    }

    /**
     * Autentica a un usuario federado mediante SSO corporativo (ISsoClient / RNF-04).
     *
     * @param username Nombre de usuario.
     * @param tokenSso Token JWT / SAML emitido por el SSO.
     * @return UsuarioDTO autenticado y sincronizado.
     */
    public UsuarioDTO autenticarSso(String username, String tokenSso) {
        return usuariosSeguridadService.autenticarUsuario(username, tokenSso);
    }

    /**
     * Consulta los datos de un usuario por su identificador único.
     *
     * @param usuarioId Identificador del usuario.
     * @return Optional con el usuario si existe.
     */
    public Optional<UsuarioDTO> consultarUsuario(String usuarioId) {
        if (usuariosSeguridadService instanceof UsuariosSeguridadService) {
            UsuariosSeguridadService serviceConcreta = (UsuariosSeguridadService) usuariosSeguridadService;
            return serviceConcreta.obtenerUsuarioPorId(usuarioId);
        }
        return Optional.empty();
    }

    /**
     * Obtiene el catálogo de permisos efectivos concedidos a un usuario.
     *
     * @param usuarioId Identificador del usuario.
     * @return Set de permisos.
     */
    public Set<String> obtenerPermisosUsuario(String usuarioId) {
        if (usuariosSeguridadService instanceof UsuariosSeguridadService) {
            UsuariosSeguridadService serviceConcreta = (UsuariosSeguridadService) usuariosSeguridadService;
            return serviceConcreta.obtenerPermisosDeUsuario(usuarioId);
        }
        return Collections.emptySet();
    }

    /**
     * Lista usuarios filtrados por rol.
     *
     * @param rol Rol a filtrar.
     * @return Lista de usuarios.
     */
    public List<UsuarioDTO> listarUsuariosPorRol(RolUsuario rol) {
        if (usuariosSeguridadService instanceof UsuariosSeguridadService) {
            UsuariosSeguridadService serviceConcreta = (UsuariosSeguridadService) usuariosSeguridadService;
            return serviceConcreta.listarUsuariosPorRol(rol);
        }
        return Collections.emptyList();
    }
}
