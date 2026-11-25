package es.uclm.esi.iso2.sescam.prescripcion.presentacion;

import es.uclm.esi.iso2.sescam.common.dto.SolicitudPruebaDTO;
import es.uclm.esi.iso2.sescam.common.interfaces.IPrescripcionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrescripcionControllerTest {

    @Mock
    private IPrescripcionService service;

    private PrescripcionController controller;

    @BeforeEach
    void setUp() {
        controller = new PrescripcionController(service);
    }

    @Test
    void rechazaServicioNulo() {
        assertThrows(NullPointerException.class, () -> new PrescripcionController(null));
    }

    @Test
    void exponeLaConsultaPorIdentificador() {
        SolicitudPruebaDTO solicitud = new SolicitudPruebaDTO();
        when(service.consultarSolicitud("PRES-1")).thenReturn(solicitud);

        assertSame(solicitud, controller.consultarSolicitud("PRES-1"));
        verify(service).consultarSolicitud("PRES-1");
    }

    @Test
    void exponeLosListadosPorPacienteYMedico() {
        SolicitudPruebaDTO solicitud = new SolicitudPruebaDTO();
        when(service.listarSolicitudesPorPaciente("PAC-1")).thenReturn(List.of(solicitud));
        when(service.listarSolicitudesPorMedico("MED-1")).thenReturn(List.of(solicitud));

        assertEquals(List.of(solicitud), controller.listarSolicitudesPorPaciente("PAC-1"));
        assertEquals(List.of(solicitud), controller.listarSolicitudesPorMedico("MED-1"));
        verify(service).listarSolicitudesPorPaciente("PAC-1");
        verify(service).listarSolicitudesPorMedico("MED-1");
    }

    @Test
    void exponeLasOperacionesDePrescripcionSinAlterarElContrato() {
        SolicitudPruebaDTO solicitud = new SolicitudPruebaDTO();
        when(service.prescribirPrueba(solicitud)).thenReturn(solicitud);
        when(service.prescribirSeguimientoPeriodico(solicitud, 6)).thenReturn(solicitud);
        when(service.autogenerarSiguienteCicloSeguimiento("PRES-1")).thenReturn(solicitud);

        assertSame(solicitud, controller.prescribirPrueba(solicitud));
        assertSame(solicitud, controller.prescribirSeguimientoPeriodico(solicitud, 6));
        assertSame(solicitud, controller.autogenerarSiguienteCicloSeguimiento("PRES-1"));
    }
}
