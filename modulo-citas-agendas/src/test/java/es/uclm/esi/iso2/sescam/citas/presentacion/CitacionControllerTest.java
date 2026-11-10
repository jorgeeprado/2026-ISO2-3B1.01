package es.uclm.esi.iso2.sescam.citas.presentacion;

import es.uclm.esi.iso2.sescam.common.dto.CitaDTO;
import es.uclm.esi.iso2.sescam.common.interfaces.CitacionAdministrativaService;
import es.uclm.esi.iso2.sescam.common.interfaces.ConsultaCitasService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Pruebas del controlador de presentación del CU-13 con los servicios de dominio simulados.
 */
@ExtendWith(MockitoExtension.class)
class CitacionControllerTest {

    @Mock
    private CitacionAdministrativaService citacion;
    @Mock
    private ConsultaCitasService consulta;

    private CitacionController controller;
    private CitaDTO cita;

    @BeforeEach
    void setUp() {
        controller = new CitacionController(citacion, consulta);
        cita = new CitaDTO();
    }

    @Test
    void rechazaServiciosNulos() {
        assertThrows(NullPointerException.class, () -> new CitacionController(null, consulta));
        assertThrows(NullPointerException.class, () -> new CitacionController(citacion, null));
    }

    @Test
    void delegaAsignacionReprogramacionYCancelacion() {
        when(citacion.asignarCita("S1", "T1", "m")).thenReturn(cita);
        when(citacion.reprogramarCita("CITA-000001", "T2", "m")).thenReturn(cita);
        when(citacion.cancelarCita("CITA-000001", "m")).thenReturn(cita);

        assertSame(cita, controller.asignarCita("S1", "T1", "m"));
        assertSame(cita, controller.reprogramarCita("CITA-000001", "T2", "m"));
        assertSame(cita, controller.cancelarCita("CITA-000001", "m"));
    }

    @Test
    void delegaLasConsultas() {
        when(consulta.obtenerCita("CITA-000001")).thenReturn(cita);
        when(consulta.listarCitasPorSolicitud("S1")).thenReturn(List.of(cita));

        assertSame(cita, controller.obtenerCita("CITA-000001"));
        assertEquals(List.of(cita), controller.listarCitasPorSolicitud("S1"));
    }
}
