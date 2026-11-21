package es.uclm.esi.iso2.sescam.usuarios.persistencia;

import es.uclm.esi.iso2.sescam.common.dto.UsuarioDTO;
import es.uclm.esi.iso2.sescam.common.enums.RolUsuario;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistencia para la gestión de cuentas de usuario y control de acceso (RBAC).
 * Satisface la capa de persistencia del Módulo 5 (modulo-usuarios-seguridad) para el CU-18.
 */
public interface IUsuarioRepository {

    /**
     * Guarda o actualiza un usuario en el repositorio.
     *
     * @param usuario DTO con la información de la cuenta y rol asignado.
     * @return El usuario persistido.
     */
    UsuarioDTO guardar(UsuarioDTO usuario);

    /**
     * Busca un usuario por su identificador interno único.
     *
     * @param id Identificador interno del usuario.
     * @return Optional con el usuario si existe.
     */
    Optional<UsuarioDTO> buscarPorId(String id);

    /**
     * Busca un usuario por su nombre de usuario (username / login).
     *
     * @param username Nombre de usuario único en el sistema.
     * @return Optional con el usuario si existe.
     */
    Optional<UsuarioDTO> buscarPorUsername(String username);

    /**
     * Devuelve la totalidad de usuarios censados en el sistema.
     *
     * @return Lista de todos los usuarios.
     */
    List<UsuarioDTO> listarTodos();

    /**
     * Devuelve la lista de usuarios asignados a un rol específico.
     *
     * @param rol Rol del usuario a filtrar.
     * @return Lista de usuarios con dicho rol.
     */
    List<UsuarioDTO> listarPorRol(RolUsuario rol);

    /**
     * Comprueba si ya existe un usuario registrado con el username indicado.
     *
     * @param username Nombre de usuario a comprobar.
     * @return true si existe; false en caso contrario.
     */
    boolean existePorUsername(String username);

    /**
     * Comprueba si ya existe un usuario registrado con el ID indicado.
     *
     * @param id Identificador a comprobar.
     * @return true si existe; false en caso contrario.
     */
    boolean existePorId(String id);

    /**
     * Limpia los datos almacenados (diseñado exclusivamente para aislamiento de pruebas unitarias).
     */
    void limpiar();
}
