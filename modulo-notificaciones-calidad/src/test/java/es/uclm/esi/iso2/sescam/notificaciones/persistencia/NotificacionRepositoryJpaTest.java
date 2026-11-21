package es.uclm.esi.iso2.sescam.notificaciones.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.uclm.esi.iso2.sescam.common.dto.FiltroNotificacionDTO;
import es.uclm.esi.iso2.sescam.common.dto.NotificacionDTO;
import es.uclm.esi.iso2.sescam.common.enums.TipoNotificacion;
import es.uclm.esi.iso2.sescam.notificaciones.dominio.EstadoEnvio;
import es.uclm.esi.iso2.sescam.notificaciones.dominio.Notificacion;
import es.uclm.esi.iso2.sescam.notificaciones.dominio.NotificacionNoEncontradaException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NotificacionRepositoryJpaTest {

    @Mock
    private NotificacionJpaRepository springRepository;

    private NotificacionRepositoryJpa repository;

    @BeforeEach
    void setUp() {
        repository = new NotificacionRepositoryJpa(springRepository);
    }

    @Test
    void guardaYConvierteUnaNotificacion() {
        when(springRepository.findByIdentificadorPublico("n-1")).thenReturn(Optional.empty());
        when(springRepository.save(any(NotificacionEntity.class))).thenAnswer(invocation -> {
            NotificacionEntity entity = invocation.getArgument(0);
            ReflectionTestUtils.setField(entity, "id", 7L);
            return entity;
        });

        Notificacion guardada = repository.guardar(crear(null, false));

        assertEquals(7L, guardada.getNotificacionId());
        assertEquals("n-1", guardada.getDto().getId());
        verify(springRepository).save(any(NotificacionEntity.class));
    }

    @Test
    void buscaPorUsuarioYAplicaLosFiltros() {
        NotificacionEntity entity = convertir(crear(3L, false));
        when(springRepository.buscarConFiltros("paciente-1", false, null, null))
                .thenReturn(List.of(entity));

        List<Notificacion> resultado = repository.buscar(
                new FiltroNotificacionDTO("paciente-1", false, null, null));

        assertEquals(1, resultado.size());
        assertEquals(3L, resultado.get(0).getNotificacionId());
    }

    @Test
    void marcaComoLeidaYDevuelveErrorSiNoExiste() {
        NotificacionEntity entity = convertir(crear(4L, false));
        when(springRepository.findById(4L)).thenReturn(Optional.of(entity));
        when(springRepository.save(entity)).thenReturn(entity);

        assertTrue(repository.marcarComoLeida(4L).getDto().isLeida());
        verify(springRepository).save(entity);
        when(springRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotificacionNoEncontradaException.class, () -> repository.marcarComoLeida(99L));
    }

    @Test
    void validaLosParametrosYEliminaTodo() {
        assertThrows(IllegalArgumentException.class,
                () -> repository.buscar(new FiltroNotificacionDTO(" ", null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> repository.marcarComoLeida(-1L));
        repository.limpiar();
        verify(springRepository).deleteAll();
    }

    @Test
    void convierteLaEntidadEnElDominio() {
        NotificacionEntity entity = convertir(crear(8L, true));

        assertEquals(8L, entity.aDominio().getNotificacionId());
        assertEquals("paciente-1", entity.aDominio().getDto().getDestinatarioId());
    }

    private static Notificacion crear(Long id, boolean leida) {
        NotificacionDTO dto = new NotificacionDTO(
                "n-1", id, "paciente-1", TipoNotificacion.RECORDATORIO_CITA,
                "Título", "Mensaje", LocalDateTime.parse("2026-11-07T10:00:00"), leida, "cita-1");
        return new Notificacion(id, dto, EstadoEnvio.ENVIADA, 2, 0);
    }

    private static NotificacionEntity convertir(Notificacion notificacion) {
        NotificacionEntity entity = new NotificacionEntity();
        entity.actualizarDesde(notificacion);
        ReflectionTestUtils.setField(entity, "id", notificacion.getNotificacionId());
        return entity;
    }
}
