package es.uclm.esi.iso2.sescam.notificaciones.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esi.iso2.sescam.common.dto.FiltroNotificacionDTO;
import es.uclm.esi.iso2.sescam.common.dto.NotificacionDTO;
import es.uclm.esi.iso2.sescam.common.enums.TipoNotificacion;
import es.uclm.esi.iso2.sescam.notificaciones.dominio.EstadoEnvio;
import es.uclm.esi.iso2.sescam.notificaciones.dominio.ICanalNotificacion;
import es.uclm.esi.iso2.sescam.notificaciones.dominio.Notificacion;
import es.uclm.esi.iso2.sescam.notificaciones.dominio.NotificacionesCalidadService;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;

@DataJpaTest
@ContextConfiguration(classes = NotificacionRepositoryJpaIntegrationTest.JpaTestConfiguration.class)
class NotificacionRepositoryJpaIntegrationTest {

    @Autowired
    private NotificacionRepositoryJpa repository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void limpiar() {
        repository.limpiar();
    }

    @Test
    void persisteFiltraYMarcaComoLeidaEnH2() {
        repository.guardar(crear("n-1", "paciente-1", "2026-11-05T10:00:00", false));
        repository.guardar(crear("n-2", "paciente-1", "2026-11-06T10:00:00", true));
        repository.guardar(crear("n-3", "paciente-2", "2026-11-06T10:00:00", false));
        entityManager.flush();
        entityManager.clear();

        List<Notificacion> noLeidas = repository.buscar(
                new FiltroNotificacionDTO("paciente-1", false,
                        LocalDateTime.parse("2026-11-05T10:00:00"),
                        LocalDateTime.parse("2026-11-06T10:00:00")));

        assertEquals(1, noLeidas.size());
        assertEquals("n-1", noLeidas.get(0).getDto().getId());
        assertEquals(2, repository.buscar(
                new FiltroNotificacionDTO("paciente-1", null, null, null)).size());
        assertEquals(1, repository.buscar(
                new FiltroNotificacionDTO("paciente-1", true, null, null)).size());
        assertTrue(repository.marcarComoLeida(noLeidas.get(0).getNotificacionId()).getDto().isLeida());
        assertEquals(0, repository.buscar(
                new FiltroNotificacionDTO("paciente-1", false, null, null)).size());
    }

    @Test
    void alertaDeHallazgoCriticoSePersisteConElDetalleMaximoYEsConsultable() {
        NotificacionesCalidadService service = new NotificacionesCalidadService(
                repository,
                List.<ICanalNotificacion>of(notificacion -> {}),
                Clock.fixed(Instant.parse("2026-11-27T10:00:00Z"), ZoneOffset.UTC));
        String detalle = "x".repeat(2000);

        NotificacionDTO emitida = service.emitirAlertaHallazgoCritico("medico-7", "informe-3", detalle);
        entityManager.flush();
        entityManager.clear();

        List<NotificacionDTO> recibidas = service.consultarNotificaciones("medico-7");
        assertEquals(1, recibidas.size());
        assertEquals(emitida.getId(), recibidas.get(0).getId());
        assertEquals(TipoNotificacion.ALERTA_HALLAZGO_CRITICO, recibidas.get(0).getTipo());
        assertEquals("informe-3", recibidas.get(0).getReferenciaEntidadId());
        assertTrue(recibidas.get(0).getMensaje().contains(detalle));
    }

    private static Notificacion crear(
            String id, String destinatario, String fecha, boolean leida) {
        NotificacionDTO dto = new NotificacionDTO(
                id,
                destinatario,
                TipoNotificacion.RECORDATORIO_CITA,
                "Título",
                "Mensaje",
                LocalDateTime.parse(fecha),
                leida,
                "cita-1");
        return new Notificacion(dto, EstadoEnvio.ENVIADA, 1, 0);
    }

    @Configuration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = NotificacionEntity.class)
    @EnableJpaRepositories(basePackageClasses = NotificacionJpaRepository.class)
    @Import(NotificacionRepositoryJpa.class)
    static class JpaTestConfiguration {}
}
