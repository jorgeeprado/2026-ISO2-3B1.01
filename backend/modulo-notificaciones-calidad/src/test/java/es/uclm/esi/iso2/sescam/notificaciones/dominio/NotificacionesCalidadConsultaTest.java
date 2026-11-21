package es.uclm.esi.iso2.sescam.notificaciones.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esi.iso2.sescam.common.dto.FiltroNotificacionDTO;
import es.uclm.esi.iso2.sescam.common.dto.NotificacionDTO;
import es.uclm.esi.iso2.sescam.common.enums.TipoNotificacion;
import es.uclm.esi.iso2.sescam.notificaciones.persistencia.NotificacionRepositoryInMemory;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NotificacionesCalidadConsultaTest {

    private NotificacionRepositoryInMemory repository;
    private NotificacionesCalidadService service;

    @BeforeEach
    void setUp() {
        repository = new NotificacionRepositoryInMemory();
        service = new NotificacionesCalidadService(
                repository, List.of(notificacion -> {}),
                Clock.fixed(LocalDateTime.parse("2026-11-07T12:00:00").toInstant(ZoneOffset.UTC), ZoneOffset.UTC));
        guardar("n-1", "paciente-1", LocalDateTime.parse("2026-11-05T10:00:00"), false);
        guardar("n-2", "paciente-1", LocalDateTime.parse("2026-11-06T10:00:00"), true);
        guardar("n-3", "paciente-2", LocalDateTime.parse("2026-11-06T10:00:00"), false);
    }

    @Test
    void aplicaUsuarioEstadoYLimitesInclusivosDeFecha() {
        FiltroNotificacionDTO filtro = new FiltroNotificacionDTO(
                "paciente-1",
                false,
                LocalDateTime.parse("2026-11-05T10:00:00"),
                LocalDateTime.parse("2026-11-06T10:00:00"));

        List<NotificacionDTO> resultado = service.consultarNotificacionesPorUsuario(filtro);

        assertEquals(1, resultado.size());
        assertEquals("n-1", resultado.get(0).getId());
        assertEquals(1L, resultado.get(0).getNotificacionId());
    }

    @Test
    void aplicaSoloElUsuarioCuandoLosFiltrosOpcionalesSonNulos() {
        List<NotificacionDTO> resultado = service.consultarNotificacionesPorUsuario(
                new FiltroNotificacionDTO("paciente-1", null, null, null));

        assertEquals(2, resultado.size());
        assertEquals("n-1", resultado.get(0).getId());
        assertEquals("n-2", resultado.get(1).getId());
    }

    @Test
    void marcarComoLeidaPersisteElCambioYConservaLaIdentidad() {
        NotificacionDTO antes = repository.buscarPorDestinatario("paciente-1").get(0).getDto();

        NotificacionDTO marcada = service.marcarComoLeida(antes.getNotificacionId());

        assertTrue(marcada.isLeida());
        assertEquals(antes.getId(), marcada.getId());
        assertFalse(service.consultarNotificacionesPorUsuario(
                new FiltroNotificacionDTO("paciente-1", false, null, null))
                .stream().anyMatch(n -> n.getId().equals(antes.getId())));
    }

    @Test
    void rechazaFiltrosInvalidosYNotificacionesInexistentes() {
        assertThrows(IllegalArgumentException.class, () -> service.consultarNotificacionesPorUsuario(
                new FiltroNotificacionDTO(" ", null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> service.consultarNotificacionesPorUsuario(
                new FiltroNotificacionDTO("paciente-1", null,
                        LocalDateTime.parse("2026-11-07T00:00:00"),
                        LocalDateTime.parse("2026-11-06T00:00:00"))));
        assertThrows(IllegalArgumentException.class, () -> service.marcarComoLeida(0L));
        assertThrows(NotificacionNoEncontradaException.class, () -> service.marcarComoLeida(99L));
    }

    private void guardar(String id, String destinatario, LocalDateTime fecha, boolean leida) {
        NotificacionDTO dto = new NotificacionDTO(
                id, destinatario, TipoNotificacion.RECORDATORIO_CITA,
                "Título", "Mensaje", fecha, leida, "cita-1");
        repository.guardar(new Notificacion(dto, EstadoEnvio.ENVIADA, 1, 0));
    }
}
