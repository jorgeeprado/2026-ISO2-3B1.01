package es.uclm.esi.iso2.sescam.notificaciones.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import es.uclm.esi.iso2.sescam.common.dto.IncidenciaDTO;
import es.uclm.esi.iso2.sescam.common.dto.NotificacionDTO;
import es.uclm.esi.iso2.sescam.common.enums.TipoNotificacion;
import es.uclm.esi.iso2.sescam.notificaciones.persistencia.INotificacionRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificacionesCalidadServiceTest {

    @Mock
    private INotificacionRepository repository;

    @Mock
    private ICanalNotificacion correo;

    @Mock
    private ICanalNotificacion sms;

    private NotificacionesCalidadService service;

    @BeforeEach
    void setUp() {
        lenient().when(repository.guardar(any())).thenAnswer(invocation -> invocation.getArgument(0));
        service = new NotificacionesCalidadService(
                repository,
                List.of(correo, sms),
                Clock.fixed(Instant.parse("2026-11-07T10:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void enviarRecordatorioCitaPersisteYDespachaPorTodosLosCanales() throws Exception {
        NotificacionDTO resultado = service.enviarRecordatorioCita("cita-1", "paciente-1");

        assertEquals("paciente-1", resultado.getDestinatarioId());
        assertEquals(TipoNotificacion.RECORDATORIO_CITA, resultado.getTipo());
        assertEquals("cita-1", resultado.getReferenciaEntidadId());
        assertEquals(Instant.parse("2026-11-07T10:00:00Z"),
                resultado.getFechaEmision().toInstant(ZoneOffset.UTC));
        assertTrue(resultado.getMensaje().contains("ayuno o al contraste"));

        ArgumentCaptor<NotificacionDTO> notificacionCaptor = ArgumentCaptor.forClass(NotificacionDTO.class);
        verify(correo).enviar(notificacionCaptor.capture());
        assertEquals(resultado.getId(), notificacionCaptor.getValue().getId());
        verify(sms).enviar(any(NotificacionDTO.class));

        ArgumentCaptor<Notificacion> registroCaptor = ArgumentCaptor.forClass(Notificacion.class);
        verify(repository, org.mockito.Mockito.times(2)).guardar(registroCaptor.capture());
        assertEquals(EstadoEnvio.PENDIENTE, registroCaptor.getAllValues().get(0).getEstadoEnvio());
        Notificacion finalizado = registroCaptor.getAllValues().get(1);
        assertEquals(EstadoEnvio.ENVIADA, finalizado.getEstadoEnvio());
        assertEquals(2, finalizado.getCanalesEnviados());
        assertEquals(0, finalizado.getCanalesFallidos());
    }

    @Test
    void notificarInformeDisponibleEmiteTipoYReferenciaCorrectos() throws Exception {
        NotificacionDTO resultado = service.notificarInformeDisponible("paciente-2", "informe-9");

        assertEquals(TipoNotificacion.INFORME_DISPONIBLE, resultado.getTipo());
        assertEquals("informe-9", resultado.getReferenciaEntidadId());
        verify(correo).enviar(any(NotificacionDTO.class));
        verify(sms).enviar(any(NotificacionDTO.class));
    }

    @Test
    void falloDeUnCanalPersisteEstadoParcialYPropagaElError() throws Exception {
        doThrow(new NotificacionDeliveryException("No disponible"))
                .when(sms).enviar(any(NotificacionDTO.class));

        NotificacionDispatchException error = assertThrows(
                NotificacionDispatchException.class,
                () -> service.enviarRecordatorioCita("cita-1", "paciente-1"));

        assertEquals(EstadoEnvio.PARCIAL, error.getEstadoEnvio());
        assertTrue(error.getNotificacionId() != null && !error.getNotificacionId().isBlank());
        ArgumentCaptor<Notificacion> registroCaptor = ArgumentCaptor.forClass(Notificacion.class);
        verify(repository, org.mockito.Mockito.times(2)).guardar(registroCaptor.capture());
        Notificacion finalizado = registroCaptor.getAllValues().get(1);
        assertEquals(EstadoEnvio.PARCIAL, finalizado.getEstadoEnvio());
        assertEquals(1, finalizado.getCanalesEnviados());
        assertEquals(1, finalizado.getCanalesFallidos());
    }

    @Test
    void falloDeTodosLosCanalesPersisteEstadoFallidoYPropagaElError() throws Exception {
        doThrow(new NotificacionDeliveryException("No disponible"))
                .when(correo).enviar(any(NotificacionDTO.class));
        doThrow(new NotificacionDeliveryException("No disponible"))
                .when(sms).enviar(any(NotificacionDTO.class));

        NotificacionDispatchException error = assertThrows(
                NotificacionDispatchException.class,
                () -> service.notificarInformeDisponible("paciente-1", "informe-1"));

        assertEquals(EstadoEnvio.FALLIDA, error.getEstadoEnvio());
        ArgumentCaptor<Notificacion> registroCaptor = ArgumentCaptor.forClass(Notificacion.class);
        verify(repository, org.mockito.Mockito.times(2)).guardar(registroCaptor.capture());
        assertEquals(EstadoEnvio.FALLIDA, registroCaptor.getAllValues().get(1).getEstadoEnvio());
    }

    @Test
    void rechazaIdentificadoresVaciosAntesDePersistirODespachar() {
        assertThrows(IllegalArgumentException.class, () -> service.enviarRecordatorioCita(" ", "paciente-1"));
        assertThrows(IllegalArgumentException.class, () -> service.notificarInformeDisponible("paciente-1", null));
        verifyNoInteractions(repository, correo, sms);
    }

    @Test
    void consultarNotificacionesDevuelveLosDtoDelRepositorio() {
        Notificacion notificacion = new Notificacion(
                new NotificacionDTO("n-1", "paciente-1", TipoNotificacion.RECORDATORIO_CITA,
                        "Título", "Mensaje", java.time.LocalDateTime.now(), false, "cita-1"),
                EstadoEnvio.ENVIADA,
                2,
                0);
        when(repository.buscarPorDestinatario("paciente-1")).thenReturn(List.of(notificacion));

        List<NotificacionDTO> resultados = service.consultarNotificaciones("paciente-1");

        assertEquals(1, resultados.size());
        assertEquals("n-1", resultados.get(0).getId());
        verify(repository).buscarPorDestinatario("paciente-1");
    }

    @Test
    void rechazaConsultaSinDestinatario() {
        assertThrows(IllegalArgumentException.class, () -> service.consultarNotificaciones(""));
        verifyNoInteractions(repository);
    }

    @Test
    void constructorRechazaCanalesSinConfigurar() {
        assertThrows(IllegalArgumentException.class,
                () -> new NotificacionesCalidadService(repository, List.of(), Clock.systemUTC()));
    }

    @Test
    void operacionesDeOtrosCasosDeUsoSiguenExplicitamentePendientes() {
        assertThrows(UnsupportedOperationException.class,
                () -> service.emitirAlertaHallazgoCritico("medico-1", "informe-1", "detalle"));
        assertThrows(UnsupportedOperationException.class,
                () -> service.registrarIncidenciaOEncuesta((IncidenciaDTO) null));
    }
}
