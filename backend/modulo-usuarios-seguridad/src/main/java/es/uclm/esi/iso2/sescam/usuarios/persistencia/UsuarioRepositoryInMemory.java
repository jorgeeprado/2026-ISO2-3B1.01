package es.uclm.esi.iso2.sescam.usuarios.persistencia;

import es.uclm.esi.iso2.sescam.common.dto.UsuarioDTO;
import es.uclm.esi.iso2.sescam.common.enums.RolUsuario;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implementación en memoria del repositorio de usuarios y roles RBAC (simulación DAO).
 * Diseñado bajo principios de hilo seguro mediante {@link ConcurrentHashMap} para pruebas y desacoplamiento (RNF-10).
 */
public class UsuarioRepositoryInMemory implements IUsuarioRepository {

    private final Map<String, UsuarioDTO> storage = new ConcurrentHashMap<>();

    @Override
    public UsuarioDTO guardar(UsuarioDTO usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario a persistir no puede ser nulo");
        }
        if (usuario.getId() == null || usuario.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("El usuario debe disponer de un identificador no vacío para persistirse");
        }
        storage.put(usuario.getId(), usuario);
        return usuario;
    }

    @Override
    public Optional<UsuarioDTO> buscarPorId(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<UsuarioDTO> buscarPorUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return Optional.empty();
        }
        return storage.values().stream()
                .filter(u -> u.getUsername() != null && u.getUsername().equalsIgnoreCase(username.trim()))
                .findFirst();
    }

    @Override
    public List<UsuarioDTO> listarTodos() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<UsuarioDTO> listarPorRol(RolUsuario rol) {
        if (rol == null) {
            return List.of();
        }
        return storage.values().stream()
                .filter(u -> rol.equals(u.getRol()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean existePorUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return storage.values().stream()
                .anyMatch(u -> u.getUsername() != null && u.getUsername().equalsIgnoreCase(username.trim()));
    }

    @Override
    public boolean existePorId(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        return storage.containsKey(id);
    }

    @Override
    public void limpiar() {
        storage.clear();
    }
}
