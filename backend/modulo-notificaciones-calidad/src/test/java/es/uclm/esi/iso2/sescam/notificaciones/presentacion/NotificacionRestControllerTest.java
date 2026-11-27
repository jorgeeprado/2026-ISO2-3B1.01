package es.uclm.esi.iso2.sescam.notificaciones.presentacion;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import es.uclm.esi.iso2.sescam.common.dto.FiltroNotificacionDTO;
import es.uclm.esi.iso2.sescam.common.dto.NotificacionDTO;
import es.uclm.esi.iso2.sescam.common.enums.TipoNotificacion;
import es.uclm.esi.iso2.sescam.common.interfaces.INotificacionesCalidadService;
import es.uclm.esi.iso2.sescam.notificaciones.dominio.EstadoEnvio;
import es.uclm.esi.iso2.sescam.notificaciones.dominio.NotificacionDeliveryException;
import es.uclm.esi.iso2.sescam.notificaciones.dominio.NotificacionDispatchException;
import es.uclm.esi.iso2.sescam.notificaciones.dominio.NotificacionNoEncontradaException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class NotificacionRestControllerTest {

    @Mock
    private INotificacionesCalidadService service;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new NotificacionController(service))
                .setControllerAdvice(new NotificacionExceptionHandler())
                .build();
    }

    @Test
    void getFiltraNotificacionesYDevuelveJson() throws Exception {
        NotificacionDTO dto = crearDto();
        when(service.consultarNotificacionesPorUsuario(any(FiltroNotificacionDTO.class)))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/notificaciones")
                        .param("usuarioId", "paciente-1")
                        .param("leida", "false")
                        .param("fechaDesde", "2026-11-01T00:00:00")
                        .param("fechaHasta", "2026-11-07T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("n-1"))
                .andExpect(jsonPath("$[0].notificacionId").value(12))
                .andExpect(jsonPath("$[0].leida").value(false));
    }

    @Test
    void putMarcaComoLeidaYDevuelve404CuandoNoExiste() throws Exception {
        NotificacionDTO dto = crearDto();
        dto.setLeida(true);
        when(service.marcarComoLeida(12L)).thenReturn(dto);

        mockMvc.perform(put("/api/notificaciones/12/leida"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leida").value(true));

        when(service.marcarComoLeida(99L)).thenThrow(new NotificacionNoEncontradaException(99L));
        mockMvc.perform(put("/api/notificaciones/99/leida"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("No existe la notificación con id 99"));
    }

    @Test
    void fechaMalFormadaDevuelve400() throws Exception {
        mockMvc.perform(get("/api/notificaciones")
                        .param("usuarioId", "paciente-1")
                        .param("fechaDesde", "no-es-una-fecha"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postEmiteAlertaDeHallazgoCriticoYDevuelve201() throws Exception {
        NotificacionDTO dto = crearDto();
        dto.setTipo(TipoNotificacion.ALERTA_HALLAZGO_CRITICO);
        when(service.emitirAlertaHallazgoCritico("medico-7", "informe-3", "Hemorragia"))
                .thenReturn(dto);

        mockMvc.perform(postAlerta())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("ALERTA_HALLAZGO_CRITICO"))
                .andExpect(jsonPath("$.id").value("n-1"));
    }

    @Test
    void postAlertaConDatosInvalidosDevuelve400() throws Exception {
        when(service.emitirAlertaHallazgoCritico("medico-7", "informe-3", "Hemorragia"))
                .thenThrow(new IllegalArgumentException("informeId es obligatorio"));

        mockMvc.perform(postAlerta())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("informeId es obligatorio"));
        mockMvc.perform(post("/api/notificaciones/alertas-hallazgo-critico"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postAlertaConFalloDeCanalDevuelve502ConElEstado() throws Exception {
        when(service.emitirAlertaHallazgoCritico("medico-7", "informe-3", "Hemorragia"))
                .thenThrow(new NotificacionDispatchException(
                        "n-9", EstadoEnvio.PARCIAL, new NotificacionDeliveryException("SMS caido")));

        mockMvc.perform(postAlerta())
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.notificacionId").value("n-9"))
                .andExpect(jsonPath("$.estadoEnvio").value("PARCIAL"));
    }

    @Test
    void postAlertaSinCanalesConfiguradosDevuelve503() throws Exception {
        when(service.emitirAlertaHallazgoCritico("medico-7", "informe-3", "Hemorragia"))
                .thenThrow(new IllegalStateException("No hay canales configurados"));

        mockMvc.perform(postAlerta())
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("No hay canales configurados"));
    }

    @Test
    void controladorRechazaPeticionDeAlertaNula() {
        NotificacionController controller = new NotificacionController(service);

        assertThrows(IllegalArgumentException.class,
                () -> controller.emitirAlertaHallazgoCritico(null));
        verifyNoInteractions(service);
    }

    private static MockHttpServletRequestBuilder postAlerta() {
        return post("/api/notificaciones/alertas-hallazgo-critico")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"medicoPrescriptorId\":\"medico-7\",\"informeId\":\"informe-3\","
                        + "\"detalleAlerta\":\"Hemorragia\"}");
    }

    private static NotificacionDTO crearDto() {
        return new NotificacionDTO(
                "n-1",
                12L,
                "paciente-1",
                TipoNotificacion.RECORDATORIO_CITA,
                "Recordatorio",
                "Mensaje",
                LocalDateTime.parse("2026-11-07T10:00:00"),
                false,
                "cita-1");
    }
}
