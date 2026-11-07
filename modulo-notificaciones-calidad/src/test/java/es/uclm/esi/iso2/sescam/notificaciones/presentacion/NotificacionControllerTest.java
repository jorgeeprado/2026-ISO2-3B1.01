package es.uclm.esi.iso2.sescam.notificaciones.presentacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.uclm.esi.iso2.sescam.common.dto.NotificacionDTO;
import es.uclm.esi.iso2.sescam.common.enums.TipoNotificacion;
import es.uclm.esi.iso2.sescam.common.interfaces.INotificacionesCalidadService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificacionControllerTest {

    @Mock
    private INotificacionesCalidadService service;

    private NotificacionController controller;

    @BeforeEach
    void setUp() {
        controller = new NotificacionController(service);
    }

    @Test
    void enviaRecordatorioDelegandoEnElServicio() {
        NotificacionDTO esperado = crearDto("cita-1", TipoNotificacion.RECORDATORIO_CITA);
        when(service.enviarRecordatorioCita("cita-1", "paciente-1")).thenReturn(esperado);

        assertEquals(esperado, controller.enviarRecordatorioCita("cita-1", "paciente-1"));
        verify(service).enviarRecordatorioCita("cita-1", "paciente-1");
    }

    @Test
    void notificaInformeDelegandoEnElServicio() {
        NotificacionDTO esperado = crearDto("informe-1", TipoNotificacion.INFORME_DISPONIBLE);
        when(service.notificarInformeDisponible("paciente-1", "informe-1")).thenReturn(esperado);

        assertEquals(esperado, controller.notificarInformeDisponible("paciente-1", "informe-1"));
        verify(service).notificarInformeDisponible("paciente-1", "informe-1");
    }

    @Test
    void consultaNotificacionesDelegandoEnElServicio() {
        List<NotificacionDTO> esperado = List.of(crearDto("cita-1", TipoNotificacion.RECORDATORIO_CITA));
        when(service.consultarNotificaciones("paciente-1")).thenReturn(esperado);

        assertEquals(esperado, controller.consultarNotificaciones("paciente-1"));
        verify(service).consultarNotificaciones("paciente-1");
    }

    @Test
    void requiereServicio() {
        assertThrows(NullPointerException.class, () -> new NotificacionController(null));
    }

    private static NotificacionDTO crearDto(String referenciaId, TipoNotificacion tipo) {
        return new NotificacionDTO(
                "n-1", "paciente-1", tipo, "Título", "Mensaje",
                java.time.LocalDateTime.now(), false, referenciaId);
    }
}
