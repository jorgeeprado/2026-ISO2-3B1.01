package es.uclm.esi.iso2.sescam.citas.persistencia;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementación en memoria (DAO simulado) del repositorio de solicitudes
 * de cambio y cancelación del paciente (CU-01).
 */
public class SolicitudCambioRepositoryInMemory implements ISolicitudCambioRepository {

    private final Map<String, SolicitudCambioRegistro> almacenamiento = new ConcurrentHashMap<>();

    @Override
    public SolicitudCambioRegistro guardar(SolicitudCambioRegistro registro) {
        if (registro == null) {
            throw new IllegalArgumentException("El registro de solicitud de cambio no puede ser nulo");
        }
        almacenamiento.put(registro.idSolicitudCambio(), registro);
        return registro;
    }

    @Override
    public Optional<SolicitudCambioRegistro> buscarPorId(String idSolicitudCambio) {
        return idSolicitudCambio == null
                ? Optional.empty()
                : Optional.ofNullable(almacenamiento.get(idSolicitudCambio));
    }

    @Override
    public List<SolicitudCambioRegistro> buscarPorPaciente(String pacienteId) {
        if (pacienteId == null || pacienteId.isBlank()) {
            throw new IllegalArgumentException("El identificador del paciente es obligatorio");
        }
        return almacenamiento.values().stream()
                .filter(r -> r.pacienteId().equals(pacienteId))
                .toList();
    }

    @Override
    public List<SolicitudCambioRegistro> buscarPorCita(String idCita) {
        if (idCita == null || idCita.isBlank()) {
            throw new IllegalArgumentException("El identificador de la cita es obligatorio");
        }
        return almacenamiento.values().stream()
                .filter(r -> r.idCita().equals(idCita))
                .toList();
    }
}
