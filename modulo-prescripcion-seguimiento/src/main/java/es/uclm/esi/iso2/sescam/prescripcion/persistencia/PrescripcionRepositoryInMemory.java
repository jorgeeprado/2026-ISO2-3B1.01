package es.uclm.esi.iso2.sescam.prescripcion.persistencia;

import es.uclm.esi.iso2.sescam.prescripcion.dominio.Prescripcion;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementación thread-safe en memoria del repositorio de prescripciones.
 */
public class PrescripcionRepositoryInMemory implements IPrescripcionRepository {

    private final Map<String, Prescripcion> storage = new ConcurrentHashMap<>();

    @Override
    public Prescripcion guardar(Prescripcion prescripcion) {
        if (prescripcion == null) {
            throw new IllegalArgumentException("La prescripcion no puede ser nula");
        }
        if (prescripcion.getId() == null || prescripcion.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("El identificador de la prescripcion es obligatorio");
        }
        storage.put(prescripcion.getId(), prescripcion);
        return prescripcion;
    }

    @Override
    public Optional<Prescripcion> buscarPorId(String id) {
        return id == null ? Optional.empty() : Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Prescripcion> listarPorPaciente(String pacienteId) {
        if (pacienteId == null) {
            return List.of();
        }
        return storage.values().stream()
                .filter(prescripcion -> pacienteId.equals(prescripcion.getPacienteId()))
                .toList();
    }

    @Override
    public List<Prescripcion> listarPorMedico(String medicoId) {
        if (medicoId == null) {
            return List.of();
        }
        return storage.values().stream()
                .filter(prescripcion -> medicoId.equals(prescripcion.getMedicoPrescriptorId()))
                .toList();
    }

    @Override
    public List<Prescripcion> listarTodos() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public void limpiar() {
        storage.clear();
    }
}
