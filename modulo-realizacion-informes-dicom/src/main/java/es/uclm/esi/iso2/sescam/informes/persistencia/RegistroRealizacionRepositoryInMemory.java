package es.uclm.esi.iso2.sescam.informes.persistencia;

import es.uclm.esi.iso2.sescam.informes.dominio.RegistroRealizacion;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementación en memoria thread-safe del repositorio de registros de realización de pruebas.
 */
public class RegistroRealizacionRepositoryInMemory implements IRegistroRealizacionRepository {

    private final Map<String, RegistroRealizacion> storage = new ConcurrentHashMap<>();

    @Override
    public RegistroRealizacion guardar(RegistroRealizacion registro) {
        if (registro == null) {
            throw new IllegalArgumentException("El registro de realizacion no puede ser nulo");
        }
        storage.put(registro.getId(), registro);
        return registro;
    }

    @Override
    public Optional<RegistroRealizacion> buscarPorId(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<RegistroRealizacion> buscarPorCitaId(String citaId) {
        if (citaId == null) {
            return Optional.empty();
        }
        return storage.values().stream()
                .filter(r -> citaId.equals(r.getCitaId()))
                .findFirst();
    }

    @Override
    public Optional<RegistroRealizacion> buscarPorStudyInstanceUID(String studyInstanceUID) {
        if (studyInstanceUID == null) {
            return Optional.empty();
        }
        return storage.values().stream()
                .filter(r -> studyInstanceUID.equals(r.getStudyInstanceUID()))
                .findFirst();
    }

    @Override
    public List<RegistroRealizacion> listarTodos() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public void limpiar() {
        storage.clear();
    }
}

