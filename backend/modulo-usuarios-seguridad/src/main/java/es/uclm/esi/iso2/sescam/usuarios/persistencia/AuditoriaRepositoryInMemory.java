package es.uclm.esi.iso2.sescam.usuarios.persistencia;

import es.uclm.esi.iso2.sescam.common.dto.AuditoriaAccesoDTO;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Implementación en memoria del repositorio de auditoría médica (CU-19 / RN-07 / RNF-06).
 * <p>
 * Diseñado con concurrencia segura (thread-safe) y estrictamente en modo <b>append-only</b>.
 * Emplea copias defensivas tanto al persistir como al retornar los datos para garantizar
 * que ningún consumidor pueda alterar los registros ya persistidos.
 * </p>
 */
public class AuditoriaRepositoryInMemory implements IAuditoriaRepository {

    private final List<AuditoriaAccesoDTO> registros = new CopyOnWriteArrayList<>();
    private final Map<String, AuditoriaAccesoDTO> indicePorId = new ConcurrentHashMap<>();

    @Override
    public AuditoriaAccesoDTO guardar(AuditoriaAccesoDTO registro) {
        Objects.requireNonNull(registro, "El registro de auditoría no puede ser nulo");
        Objects.requireNonNull(registro.getId(), "El ID del registro de auditoría no puede ser nulo");

        if (indicePorId.containsKey(registro.getId())) {
            throw new IllegalArgumentException("Ya existe un registro de auditoría con ID: " + registro.getId());
        }

        // Copia defensiva para preservar inmutabilidad frente a mutaciones externas
        AuditoriaAccesoDTO copiaInmutable = clonar(registro);

        registros.add(copiaInmutable);
        indicePorId.put(copiaInmutable.getId(), copiaInmutable);

        return clonar(copiaInmutable);
    }

    @Override
    public Optional<AuditoriaAccesoDTO> buscarPorId(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }
        AuditoriaAccesoDTO encontrado = indicePorId.get(id);
        return encontrado != null ? Optional.of(clonar(encontrado)) : Optional.empty();
    }

    @Override
    public List<AuditoriaAccesoDTO> buscar(LocalDateTime desde, LocalDateTime hasta, String pacienteId) {
        return Collections.unmodifiableList(
                registros.stream()
                        .filter(reg -> {
                            if (desde != null && reg.getTimestamp() != null && reg.getTimestamp().isBefore(desde)) {
                                return false;
                            }
                            if (hasta != null && reg.getTimestamp() != null && reg.getTimestamp().isAfter(hasta)) {
                                return false;
                            }
                            if (pacienteId != null && !pacienteId.trim().isEmpty()) {
                                return pacienteId.equalsIgnoreCase(reg.getPacienteIdAfectado());
                            }
                            return true;
                        })
                        .map(this::clonar)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public List<AuditoriaAccesoDTO> buscarPorUsuario(String usuarioId) {
        if (usuarioId == null || usuarioId.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(
                registros.stream()
                        .filter(reg -> usuarioId.equalsIgnoreCase(reg.getUsuarioId()))
                        .map(this::clonar)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public List<AuditoriaAccesoDTO> buscarPorAccion(String accion) {
        if (accion == null || accion.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(
                registros.stream()
                        .filter(reg -> accion.equalsIgnoreCase(reg.getAccion()))
                        .map(this::clonar)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public List<AuditoriaAccesoDTO> listarTodos() {
        return Collections.unmodifiableList(
                registros.stream()
                        .map(this::clonar)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public long contar() {
        return registros.size();
    }

    @Override
    public void actualizar(AuditoriaAccesoDTO registro) {
        throw new UnsupportedOperationException(
                "RN-07 / RNF-06: Operación UPDATE estrictamente prohibida en el log inmutable de auditoría médico-legal.");
    }

    @Override
    public void eliminar(String id) {
        throw new UnsupportedOperationException(
                "RN-07 / RNF-06: Operación DELETE estrictamente prohibida en el log inmutable de auditoría médico-legal.");
    }

    @Override
    public void vaciar() {
        throw new UnsupportedOperationException(
                "RN-07 / RNF-06: Operación TRUNCATE/DELETE estrictamente prohibida en el log inmutable de auditoría médico-legal.");
    }

    private AuditoriaAccesoDTO clonar(AuditoriaAccesoDTO original) {
        return new AuditoriaAccesoDTO(
                original.getId(),
                original.getTimestamp(),
                original.getUsuarioId(),
                original.getRolUsuario(),
                original.getPacienteIdAfectado(),
                original.getAccion(),
                original.getRecursoAccedido(),
                original.getIpOrigen()
        );
    }
}
