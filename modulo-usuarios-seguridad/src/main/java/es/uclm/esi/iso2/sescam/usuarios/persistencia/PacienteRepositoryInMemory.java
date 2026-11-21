package es.uclm.esi.iso2.sescam.usuarios.persistencia;

import es.uclm.esi.iso2.sescam.common.dto.PacienteDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implementación en memoria del repositorio de pacientes (simulación DAO).
 * Diseñado con concurrencia segura mediante {@link ConcurrentHashMap} para pruebas y desacoplamiento (RNF-10).
 */
public class PacienteRepositoryInMemory implements IPacienteRepository {

    private final Map<String, PacienteDTO> storage = new ConcurrentHashMap<>();

    @Override
    public PacienteDTO guardar(PacienteDTO paciente) {
        if (paciente == null) {
            throw new IllegalArgumentException("El paciente a persistir no puede ser nulo");
        }
        if (paciente.getId() == null || paciente.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("El paciente debe disponer de un identificador no vacío para persistirse");
        }
        storage.put(paciente.getId(), paciente);
        return paciente;
    }

    @Override
    public Optional<PacienteDTO> buscarPorId(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<PacienteDTO> buscarPorCip(String cip) {
        if (cip == null || cip.trim().isEmpty()) {
            return Optional.empty();
        }
        return storage.values().stream()
                .filter(p -> p.getCip() != null && p.getCip().equalsIgnoreCase(cip.trim()))
                .findFirst();
    }

    @Override
    public Optional<PacienteDTO> buscarPorDni(String dni) {
        if (dni == null || dni.trim().isEmpty()) {
            return Optional.empty();
        }
        return storage.values().stream()
                .filter(p -> p.getDni() != null && p.getDni().equalsIgnoreCase(dni.trim()))
                .findFirst();
    }

    @Override
    public Optional<PacienteDTO> buscarPorIdOCip(String idOCip) {
        if (idOCip == null || idOCip.trim().isEmpty()) {
            return Optional.empty();
        }
        Optional<PacienteDTO> porId = buscarPorId(idOCip);
        if (porId.isPresent()) {
            return porId;
        }
        return buscarPorCip(idOCip);
    }

    @Override
    public List<PacienteDTO> listarTodos() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<PacienteDTO> listarActivos() {
        return storage.values().stream()
                .filter(PacienteDTO::isActivo)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existePorCip(String cip) {
        if (cip == null || cip.trim().isEmpty()) {
            return false;
        }
        return storage.values().stream()
                .anyMatch(p -> p.getCip() != null && p.getCip().equalsIgnoreCase(cip.trim()));
    }

    @Override
    public boolean existePorDni(String dni) {
        if (dni == null || dni.trim().isEmpty()) {
            return false;
        }
        return storage.values().stream()
                .anyMatch(p -> p.getDni() != null && p.getDni().equalsIgnoreCase(dni.trim()));
    }

    @Override
    public void limpiar() {
        storage.clear();
    }
}

