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
    void constructorRechazaServicioNulo() {
        assertThrows(NullPointerException.class, () -> new PrescripcionController(null));
    }

    @Test
    void delegaPrescripcionPuntualYPeriodica() {
        SolicitudPruebaDTO puntual = new SolicitudPruebaDTO();
        SolicitudPruebaDTO periodica = new SolicitudPruebaDTO();
        when(service.prescribirPrueba(puntual)).thenReturn(puntual);
        when(service.prescribirSeguimientoPeriodico(periodica, 6)).thenReturn(periodica);

        assertSame(puntual, controller.prescribirPrueba(puntual));
        assertSame(periodica, controller.prescribirSeguimientoPeriodico(periodica, 6));
        verify(service).prescribirPrueba(puntual);
        verify(service).prescribirSeguimientoPeriodico(periodica, 6);
    }

    @Test
    void delegaConsultasYListado() {
        SolicitudPruebaDTO solicitud = new SolicitudPruebaDTO();
        when(service.consultarSolicitud("PRES-1")).thenReturn(solicitud);
        when(service.listarSolicitudesPorPaciente("PAC-1")).thenReturn(List.of(solicitud));
        when(service.listarSolicitudesPorMedico("MED-1")).thenReturn(List.of(solicitud));

        assertSame(solicitud, controller.consultarSolicitud("PRES-1"));
        assertEquals(List.of(solicitud), controller.listarSolicitudesPorPaciente("PAC-1"));
        assertEquals(List.of(solicitud), controller.listarSolicitudesPorMedico("MED-1"));
    }

    @Test
    void delegaAutogeneracionDelSiguienteCiclo() {
        SolicitudPruebaDTO siguiente = new SolicitudPruebaDTO();
        when(service.autogenerarSiguienteCicloSeguimiento("SEGUIMIENTO-1")).thenReturn(siguiente);

        assertSame(siguiente,
                controller.autogenerarSiguienteCicloSeguimiento("SEGUIMIENTO-1"));
        verify(service).autogenerarSiguienteCicloSeguimiento("SEGUIMIENTO-1");
    }
}
