package es.uclm.esi.iso2.sescam.notificaciones.persistencia;

import es.uclm.esi.iso2.sescam.notificaciones.dominio.Notificacion;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class NotificacionRepositoryInMemory implements INotificacionRepository {

    private final ConcurrentMap<String, Notificacion> notificaciones = new ConcurrentHashMap<>();

    @Override
    public Notificacion guardar(Notificacion notificacion) {
        Objects.requireNonNull(notificacion, "La notificación no puede ser nula");
        String id = Objects.requireNonNull(notificacion.getDto().getId(), "El id de notificación no puede ser nulo");
        notificaciones.put(id, notificacion);
        return notificacion;
    }

    @Override
    public List<Notificacion> buscarPorDestinatario(String destinatarioId) {
        if (destinatarioId == null || destinatarioId.isBlank()) {
            throw new IllegalArgumentException("El id del destinatario es obligatorio");
        }
        return notificaciones.values().stream()
                .filter(notificacion -> destinatarioId.equals(notificacion.getDto().getDestinatarioId()))
                .sorted(Comparator.comparing(notificacion -> notificacion.getDto().getFechaEmision()))
                .toList();
    }

    @Override
    public void limpiar() {
        notificaciones.clear();
    }
}
