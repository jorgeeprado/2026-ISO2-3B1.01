package es.uclm.esi.iso2.sescam.informes.presentacion;

import es.uclm.esi.iso2.sescam.common.dto.RegistroRealizacionDTO;
import es.uclm.esi.iso2.sescam.informes.dominio.RealizacionPruebasServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias :: RealizacionPruebasController (Capa de Presentación CU-10)")
class RealizacionPruebasControllerTest {

    @Mock
    private RealizacionPruebasServiceImpl serviceMock;

    private RealizacionPruebasController controller;

    @BeforeEach
    void setUp() {
        controller = new RealizacionPruebasController(serviceMock);
    }

    @Test
    @DisplayName("Debe delegar el registro de la prueba al servicio")
    void testRegistrarEjecucionPrueba() {
        RegistroRealizacionDTO dto = new RegistroRealizacionDTO();
        dto.setCitaId("C1");

        when(serviceMock.registrarRealizacion(dto, "PAC-1", "PRU-1", false)).thenReturn(dto);

        RegistroRealizacionDTO result = controller.registrarEjecucionPrueba(dto, "PAC-1", "PRU-1", false);

        assertNotNull(result);
        assertSame(dto, result);
        verify(serviceMock, times(1)).registrarRealizacion(dto, "PAC-1", "PRU-1", false);
    }

    @Test
    @DisplayName("Debe delegar la consulta por cita y por StudyInstanceUID al servicio")
    void testConsultasController() {
        RegistroRealizacionDTO dto = new RegistroRealizacionDTO();
        dto.setId("REG-1");

        when(serviceMock.consultarPorCitaId("CITA-1")).thenReturn(Optional.of(dto));
        when(serviceMock.consultarPorStudyInstanceUID("UID-1")).thenReturn(Optional.of(dto));

        assertTrue(controller.consultarRegistroPorCita("CITA-1").isPresent());
        assertTrue(controller.consultarRegistroPorStudyInstanceUID("UID-1").isPresent());

        verify(serviceMock, times(1)).consultarPorCitaId("CITA-1");
        verify(serviceMock, times(1)).consultarPorStudyInstanceUID("UID-1");
    }
}

