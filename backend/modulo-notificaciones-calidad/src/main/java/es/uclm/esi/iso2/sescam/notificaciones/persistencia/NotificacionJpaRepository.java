package es.uclm.esi.iso2.sescam.notificaciones.persistencia;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificacionJpaRepository extends JpaRepository<NotificacionEntity, Long> {

    java.util.Optional<NotificacionEntity> findByIdentificadorPublico(String identificadorPublico);

    List<NotificacionEntity> findByDestinatarioIdOrderByFechaEmisionAsc(String destinatarioId);

    @Query("""
            SELECT n FROM NotificacionEntity n
            WHERE n.destinatarioId = :usuarioId
              AND (:leida IS NULL OR n.leida = :leida)
              AND (:fechaDesde IS NULL OR n.fechaEmision >= :fechaDesde)
              AND (:fechaHasta IS NULL OR n.fechaEmision <= :fechaHasta)
            ORDER BY n.fechaEmision ASC
            """)
    List<NotificacionEntity> buscarConFiltros(
            @Param("usuarioId") String usuarioId,
            @Param("leida") Boolean leida,
            @Param("fechaDesde") LocalDateTime fechaDesde,
            @Param("fechaHasta") LocalDateTime fechaHasta);
}
