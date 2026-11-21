package es.uclm.esi.iso2.sescam.informes.persistencia;

import es.uclm.esi.iso2.sescam.common.dto.ConsentimientoDTO;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implementación en memoria del repositorio de consentimientos informados (simulación DAO).
 * Diseñado bajo principios de hilo seguro (ConcurrentHashMap) para tests y desarrollo desacoplado.
 */
public class ConsentimientoRepositoryInMemory implements IConsentimientoRepository {

    private final Map<String, ConsentimientoDTO> storage = new ConcurrentHashMap<>();

    @Override
    public ConsentimientoDTO guardar(ConsentimientoDTO consentimiento) {
        if (consentimiento == null) {
            throw new IllegalArgumentException("El consentimiento no puede ser nulo");
        }
        storage.put(consentimiento.getId(), consentimiento);
        return consentimiento;
    }

    @Override
    public Optional<ConsentimientoDTO> buscarPorId(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<ConsentimientoDTO> buscarPorPacienteYPrueba(String pacienteId, String tipoPruebaId) {
        if (pacienteId == null || tipoPruebaId == null) {
            return Optional.empty();
        }
        return storage.values().stream()
                .filter(c -> pacienteId.equals(c.getPacienteId()) && tipoPruebaId.equals(c.getTipoPruebaId()))
                .reduce((first, second) -> second); // Obtiene el más recientemente añadido
    }

    @Override
    public List<ConsentimientoDTO> listarPorPaciente(String pacienteId) {
        if (pacienteId == null) {
            return List.of();
        }
        return storage.values().stream()
                .filter(c -> pacienteId.equals(c.getPacienteId()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean existeConsentimientoValido(String pacienteId, String tipoPruebaId) {
        return buscarPorPacienteYPrueba(pacienteId, tipoPruebaId)
                .map(c -> c.isFirmado() && c.isChecklistSeguridadCompletado())
                .orElse(false);
    }

    @Override
    public void limpiar() {
        storage.clear();
    }
}

