package es.uclm.esi.iso2.sescam.citas.persistencia;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementación en memoria (DAO simulado) del repositorio de citas de CU-13.
 * {@link CitaRegistro} ya guarda y devuelve copias de la cita.
 */
public class CitaRepositoryInMemory implements ICitaRepository {

    private final Map<String, CitaRegistro> almacenamiento = new ConcurrentHashMap<>();

    @Override
    public CitaRegistro guardar(CitaRegistro registro) {
        if (registro == null) {
            throw new IllegalArgumentException("El registro de la cita no puede ser nulo");
        }
        almacenamiento.put(registro.idCita(), registro);
        return registro;
    }

    @Override
    public Optional<CitaRegistro> buscarPorId(String idCita) {
        return idCita == null ? Optional.empty() : Optional.ofNullable(almacenamiento.get(idCita));
    }

    @Override
    public List<CitaRegistro> buscarPorSolicitud(String idSolicitud) {
        return almacenamiento.values().stream()
                .filter(r -> r.idSolicitud().equals(idSolicitud))
                .toList();
    }
}
