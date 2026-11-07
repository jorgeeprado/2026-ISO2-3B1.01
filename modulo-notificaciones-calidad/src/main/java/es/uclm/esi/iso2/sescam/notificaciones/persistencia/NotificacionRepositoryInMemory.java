package es.uclm.esi.iso2.sescam.notificaciones.persistencia;

import es.uclm.esi.iso2.sescam.common.dto.FiltroNotificacionDTO;
import es.uclm.esi.iso2.sescam.common.dto.NotificacionDTO;
import es.uclm.esi.iso2.sescam.notificaciones.dominio.Notificacion;
import es.uclm.esi.iso2.sescam.notificaciones.dominio.NotificacionNoEncontradaException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

public class NotificacionRepositoryInMemory implements INotificacionRepository {

    private final ConcurrentMap<Long, Notificacion> notificaciones = new ConcurrentHashMap<>();
    private final AtomicLong siguienteId = new AtomicLong();

    @Override
    public Notificacion guardar(Notificacion notificacion) {
        Objects.requireNonNull(notificacion, "La notificación no puede ser nula");
        NotificacionDTO dto = notificacion.getDto();
        Objects.requireNonNull(dto.getId(), "El id de notificación no puede ser nulo");
        Long id = notificacion.getNotificacionId();
        if (id == null) {
            id = notificaciones.values().stream()
                    .filter(existing -> dto.getId().equals(existing.getDto().getId()))
                    .map(Notificacion::getNotificacionId)
                    .findFirst()
                    .orElseGet(siguienteId::incrementAndGet);
        } else {
            siguienteId.accumulateAndGet(id, Math::max);
        }
        Notificacion guardada = new Notificacion(
                id, dto, notificacion.getEstadoEnvio(),
                notificacion.getCanalesEnviados(), notificacion.getCanalesFallidos());
        notificaciones.put(id, guardada);
        return guardada;
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
    public List<Notificacion> buscar(FiltroNotificacionDTO filtro) {
        Objects.requireNonNull(filtro, "El filtro no puede ser nulo");
        validarFiltro(filtro);
        return buscarPorDestinatario(filtro.getUsuarioId()).stream()
                .filter(n -> filtro.getLeida() == null || filtro.getLeida() == n.getDto().isLeida())
                .filter(n -> filtro.getFechaDesde() == null
                        || !n.getDto().getFechaEmision().isBefore(filtro.getFechaDesde()))
                .filter(n -> filtro.getFechaHasta() == null
                        || !n.getDto().getFechaEmision().isAfter(filtro.getFechaHasta()))
                .toList();
    }

    @Override
    public Notificacion marcarComoLeida(Long notificacionId) {
        if (notificacionId == null || notificacionId <= 0) {
            throw new IllegalArgumentException("El id numérico de notificación debe ser positivo");
        }
        Notificacion existente = notificaciones.get(notificacionId);
        if (existente == null) {
            throw new NotificacionNoEncontradaException(notificacionId);
        }
        NotificacionDTO dto = existente.getDto();
        dto.setLeida(true);
        Notificacion actualizada = new Notificacion(
                notificacionId, dto, existente.getEstadoEnvio(),
                existente.getCanalesEnviados(), existente.getCanalesFallidos());
        notificaciones.put(notificacionId, actualizada);
        return actualizada;
    }

    @Override
    public void limpiar() {
        notificaciones.clear();
        siguienteId.set(0);
    }

    private static void validarFiltro(FiltroNotificacionDTO filtro) {
        if (filtro.getUsuarioId() == null || filtro.getUsuarioId().isBlank()) {
            throw new IllegalArgumentException("El id del usuario es obligatorio");
        }
        if (filtro.getFechaDesde() != null && filtro.getFechaHasta() != null
                && filtro.getFechaDesde().isAfter(filtro.getFechaHasta())) {
            throw new IllegalArgumentException("La fecha desde no puede ser posterior a la fecha hasta");
        }
    }
}
