package es.uclm.esi.iso2.sescam.notificaciones.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.uclm.esi.iso2.sescam.common.dto.NotificacionDTO;
import es.uclm.esi.iso2.sescam.common.enums.TipoNotificacion;
import es.uclm.esi.iso2.sescam.notificaciones.dominio.EstadoEnvio;
import es.uclm.esi.iso2.sescam.notificaciones.dominio.Notificacion;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NotificacionRepositoryInMemoryTest {

    private NotificacionRepositoryInMemory repository;

    @BeforeEach
    void setUp() {
        repository = new NotificacionRepositoryInMemory();
    }

    @Test
    void guardaYBuscaNotificacionesPorDestinatarioEnOrdenTemporal() {
        repository.guardar(crear("n-2", "paciente-1", LocalDateTime.parse("2026-11-07T11:00:00")));
        Notificacion primera = crear("n-1", "paciente-1", LocalDateTime.parse("2026-11-07T10:00:00"));
        repository.guardar(primera);

        assertEquals(primera.getDto().getId(),
                repository.buscarPorDestinatario("paciente-1").get(0).getDto().getId());
        assertEquals(2L,
                repository.buscarPorDestinatario("paciente-1").get(0).getDto().getNotificacionId());
        assertEquals(2, repository.buscarPorDestinatario("paciente-1").size());
        assertEquals(0, repository.buscarPorDestinatario("otro-paciente").size());
    }

    @Test
    void reemplazaElEstadoDeUnaNotificacionConElMismoId() {
        Notificacion pendiente = crear("n-1", "paciente-1", LocalDateTime.now());
        Notificacion enviada = new Notificacion(pendiente.getDto(), EstadoEnvio.ENVIADA, 2, 0);
        repository.guardar(pendiente);
        repository.guardar(enviada);

        assertEquals(EstadoEnvio.ENVIADA,
                repository.buscarPorDestinatario("paciente-1").get(0).getEstadoEnvio());
    }

    @Test
    void limpiarEliminaLasNotificacionesAlmacenadas() {
        repository.guardar(crear("n-1", "paciente-1", LocalDateTime.now()));

        repository.limpiar();

        assertEquals(0, repository.buscarPorDestinatario("paciente-1").size());
    }

    @Test
    void rechazaEntradasInvalidas() {
        assertThrows(NullPointerException.class, () -> repository.guardar(null));
        assertThrows(IllegalArgumentException.class, () -> repository.buscarPorDestinatario(" "));
    }

    private static Notificacion crear(String id, String destinatario, LocalDateTime fecha) {
        return new Notificacion(
                new NotificacionDTO(id, destinatario, TipoNotificacion.RECORDATORIO_CITA,
                        "Título", "Mensaje", fecha, false, "cita-1"),
                EstadoEnvio.PENDIENTE,
                0,
                0);
    }
}
