package es.uclm.esi.iso2.sescam.notificaciones.persistencia;

import es.uclm.esi.iso2.sescam.common.dto.FiltroNotificacionDTO;
import es.uclm.esi.iso2.sescam.notificaciones.dominio.Notificacion;
import es.uclm.esi.iso2.sescam.notificaciones.dominio.NotificacionNoEncontradaException;
import java.util.List;
import java.util.Objects;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Primary
public class NotificacionRepositoryJpa implements INotificacionRepository {

    private final NotificacionJpaRepository repository;

    public NotificacionRepositoryJpa(NotificacionJpaRepository repository) {
        this.repository = Objects.requireNonNull(repository, "El repositorio JPA no puede ser nulo");
    }

    @Override
    @Transactional
    public Notificacion guardar(Notificacion notificacion) {
        Objects.requireNonNull(notificacion, "La notificación no puede ser nula");
        NotificacionEntity entity = notificacion.getNotificacionId() == null
                ? repository.findByIdentificadorPublico(notificacion.getDto().getId())
                        .orElseGet(NotificacionEntity::new)
                : repository.findById(notificacion.getNotificacionId())
                        .orElseThrow(() -> new NotificacionNoEncontradaException(notificacion.getNotificacionId()));
        entity.actualizarDesde(notificacion);
        return repository.save(entity).aDominio();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notificacion> buscarPorDestinatario(String destinatarioId) {
        if (destinatarioId == null || destinatarioId.isBlank()) {
            throw new IllegalArgumentException("El id del destinatario es obligatorio");
        }
        return repository.findByDestinatarioIdOrderByFechaEmisionAsc(destinatarioId).stream()
                .map(NotificacionEntity::aDominio)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notificacion> buscar(FiltroNotificacionDTO filtro) {
        validarFiltro(filtro);
        return repository.buscarConFiltros(
                        filtro.getUsuarioId(), filtro.getLeida(), filtro.getFechaDesde(), filtro.getFechaHasta())
                .stream()
                .map(NotificacionEntity::aDominio)
                .toList();
    }

    @Override
    @Transactional
    public Notificacion marcarComoLeida(Long notificacionId) {
        if (notificacionId == null || notificacionId <= 0) {
            throw new IllegalArgumentException("El id numérico de notificación debe ser positivo");
        }
        NotificacionEntity entity = repository.findById(notificacionId)
                .orElseThrow(() -> new NotificacionNoEncontradaException(notificacionId));
        Notificacion actualizada = entity.aDominio();
        var dto = actualizada.getDto();
        dto.setLeida(true);
        entity.actualizarDesde(new Notificacion(
                notificacionId, dto, actualizada.getEstadoEnvio(),
                actualizada.getCanalesEnviados(), actualizada.getCanalesFallidos()));
        return repository.save(entity).aDominio();
    }

    @Override
    @Transactional
    public void limpiar() {
        repository.deleteAll();
    }

    private static void validarFiltro(FiltroNotificacionDTO filtro) {
        Objects.requireNonNull(filtro, "El filtro no puede ser nulo");
        if (filtro.getUsuarioId() == null || filtro.getUsuarioId().isBlank()) {
            throw new IllegalArgumentException("El id del usuario es obligatorio");
        }
        if (filtro.getFechaDesde() != null && filtro.getFechaHasta() != null
                && filtro.getFechaDesde().isAfter(filtro.getFechaHasta())) {
            throw new IllegalArgumentException("La fecha desde no puede ser posterior a la fecha hasta");
        }
    }
}
