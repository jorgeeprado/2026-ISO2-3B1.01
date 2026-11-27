package es.uclm.esi.iso2.sescam.notificaciones.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import org.springframework.beans.factory.support.StaticListableBeanFactory;

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
                () -> service.registrarIncidenciaOEncuesta((IncidenciaDTO) null));
    }

    // --- CU-24 / RN-06: alerta prioritaria por hallazgo radiológico crítico ---

    @Test
    void emitirAlertaHallazgoCriticoNotificaAlPrescriptorPorTodosLosCanales() throws Exception {
        NotificacionDTO resultado = service.emitirAlertaHallazgoCritico(
                "medico-7", "informe-3", "  Neumotórax a tensión  ");

        assertEquals("medico-7", resultado.getDestinatarioId());
        assertEquals(TipoNotificacion.ALERTA_HALLAZGO_CRITICO, resultado.getTipo());
        assertEquals("informe-3", resultado.getReferenciaEntidadId());
        assertTrue(resultado.getTitulo().startsWith("ALERTA PRIORITARIA"));
        assertTrue(resultado.getMensaje().contains("informe-3"));
        assertTrue(resultado.getMensaje().contains("Detalle: Neumotórax a tensión."));
        assertEquals(Instant.parse("2026-11-07T10:00:00Z"),
                resultado.getFechaEmision().toInstant(ZoneOffset.UTC));
        assertFalse(resultado.isLeida());

        ArgumentCaptor<NotificacionDTO> enviada = ArgumentCaptor.forClass(NotificacionDTO.class);
        verify(correo).enviar(enviada.capture());
        assertEquals(resultado.getId(), enviada.getValue().getId());
        assertEquals(TipoNotificacion.ALERTA_HALLAZGO_CRITICO, enviada.getValue().getTipo());
        verify(sms).enviar(any(NotificacionDTO.class));

        ArgumentCaptor<Notificacion> registros = ArgumentCaptor.forClass(Notificacion.class);
        verify(repository, org.mockito.Mockito.times(2)).guardar(registros.capture());
        assertEquals(EstadoEnvio.PENDIENTE, registros.getAllValues().get(0).getEstadoEnvio());
        Notificacion finalizada = registros.getAllValues().get(1);
        assertEquals(EstadoEnvio.ENVIADA, finalizada.getEstadoEnvio());
        assertEquals(2, finalizada.getCanalesEnviados());
        assertEquals(0, finalizada.getCanalesFallidos());
    }

    @Test
    void alertaHallazgoCriticoConFalloDeCanalQuedaParcialYPropagaElError() throws Exception {
        doThrow(new NotificacionDeliveryException("SMS caído"))
                .when(sms).enviar(any(NotificacionDTO.class));

        NotificacionDispatchException error = assertThrows(
                NotificacionDispatchException.class,
                () -> service.emitirAlertaHallazgoCritico("medico-7", "informe-3", "Disección aórtica"));

        assertEquals(EstadoEnvio.PARCIAL, error.getEstadoEnvio());
        verify(correo).enviar(any(NotificacionDTO.class));
        ArgumentCaptor<Notificacion> registros = ArgumentCaptor.forClass(Notificacion.class);
        verify(repository, org.mockito.Mockito.times(2)).guardar(registros.capture());
        Notificacion finalizada = registros.getAllValues().get(1);
        assertEquals(EstadoEnvio.PARCIAL, finalizada.getEstadoEnvio());
        assertEquals(TipoNotificacion.ALERTA_HALLAZGO_CRITICO, finalizada.getDto().getTipo());
    }

    @Test
    void alertaHallazgoCriticoSinNingunCanalEntregadoQuedaFallida() throws Exception {
        doThrow(new NotificacionDeliveryException("Correo caído"))
                .when(correo).enviar(any(NotificacionDTO.class));
        doThrow(new NotificacionDeliveryException("SMS caído"))
                .when(sms).enviar(any(NotificacionDTO.class));

        NotificacionDispatchException error = assertThrows(
                NotificacionDispatchException.class,
                () -> service.emitirAlertaHallazgoCritico("medico-7", "informe-3", "Hemorragia"));

        assertEquals(EstadoEnvio.FALLIDA, error.getEstadoEnvio());
    }

    @Test
    void alertaHallazgoCriticoRechazaDatosIncompletosSinPersistirNiDespachar() {
        assertThrows(IllegalArgumentException.class,
                () -> service.emitirAlertaHallazgoCritico(null, "informe-3", "detalle"));
        assertThrows(IllegalArgumentException.class,
                () -> service.emitirAlertaHallazgoCritico("medico-7", " ", "detalle"));
        assertThrows(IllegalArgumentException.class,
                () -> service.emitirAlertaHallazgoCritico("medico-7", "informe-3", ""));
        assertThrows(IllegalArgumentException.class,
                () -> service.emitirAlertaHallazgoCritico("medico-7", "informe-3", null));
        verifyNoInteractions(repository, correo, sms);
    }

    @Test
    void alertaHallazgoCriticoRecortaElDetalleDemasiadoLargoSinPerderElAviso() throws Exception {
        String limite = "x".repeat(NotificacionesCalidadService.LONGITUD_MAXIMA_DETALLE);

        NotificacionDTO exacta = service.emitirAlertaHallazgoCritico("medico-7", "informe-3", limite);
        NotificacionDTO recortada = service.emitirAlertaHallazgoCritico("medico-7", "informe-4", limite + "x");

        assertTrue(exacta.getMensaje().contains("Detalle: " + limite + "."));
        assertTrue(recortada.getMensaje().contains("Detalle: " + limite.substring(1) + "…."));
        assertFalse(recortada.getMensaje().contains(limite));
        verify(correo, org.mockito.Mockito.times(2)).enviar(any(NotificacionDTO.class));
    }

    @Test
    void alertaHallazgoCriticoSinCanalesConfiguradosFallaSinPersistir() {
        NotificacionesCalidadService sinCanales = new NotificacionesCalidadService(
                repository, new StaticListableBeanFactory().getBeanProvider(ICanalNotificacion.class),
                Clock.systemUTC());

        assertThrows(IllegalStateException.class,
                () -> sinCanales.emitirAlertaHallazgoCritico("medico-7", "informe-3", "detalle"));
        verifyNoInteractions(repository);
    }
}
