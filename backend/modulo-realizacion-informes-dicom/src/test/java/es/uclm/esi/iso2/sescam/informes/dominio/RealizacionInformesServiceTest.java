package es.uclm.esi.iso2.sescam.informes.dominio;

import es.uclm.esi.iso2.sescam.common.dto.ConsentimientoDTO;
import es.uclm.esi.iso2.sescam.common.dto.InformeRadiologicoDTO;
import es.uclm.esi.iso2.sescam.common.dto.RegistroRealizacionDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias :: RealizacionInformesService (Fachada PUD Módulo 3)")
class RealizacionInformesServiceTest {

    @Mock
    private ConsentimientoService consentimientoServiceMock;

    @Mock
    private RealizacionPruebasServiceImpl realizacionPruebasServiceMock;

    private RealizacionInformesService service;

    @BeforeEach
    void setUp() {
        service = new RealizacionInformesService(consentimientoServiceMock, realizacionPruebasServiceMock);
    }

    @Test
    @DisplayName("Debe delegar la firma de consentimiento en ConsentimientoService (CU-22)")
    void testFirmarConsentimientoInformadoDelega() {
        ConsentimientoDTO dto = new ConsentimientoDTO();
        when(consentimientoServiceMock.firmarConsentimiento(dto)).thenReturn(dto);

        ConsentimientoDTO result = service.firmarConsentimientoInformado(dto);

        assertSame(dto, result);
        verify(consentimientoServiceMock, times(1)).firmarConsentimiento(dto);
    }

    @Test
    @DisplayName("Debe delegar la validacion previa en ConsentimientoService (CU-22 / RN-04)")
    void testValidarConsentimientoPrevioDelega() {
        when(consentimientoServiceMock.validarConsentimientoPrevio("P1", "T1")).thenReturn(true);

        boolean result = service.validarConsentimientoPrevio("P1", "T1");

        assertTrue(result);
        verify(consentimientoServiceMock, times(1)).validarConsentimientoPrevio("P1", "T1");
    }

    @Test
    @DisplayName("Debe delegar el registro de realizacion de prueba en RealizacionPruebasServiceImpl (CU-10)")
    void testRegistrarRealizacionPruebaDelega() {
        RegistroRealizacionDTO dto = new RegistroRealizacionDTO();
        when(realizacionPruebasServiceMock.registrarRealizacionPrueba(dto)).thenReturn(dto);

        RegistroRealizacionDTO result = service.registrarRealizacionPrueba(dto);

        assertSame(dto, result);
        verify(realizacionPruebasServiceMock, times(1)).registrarRealizacionPrueba(dto);
    }

    @Test
    @DisplayName("Debe lanzar excepción si realizacionPruebasService no fue inyectado")
    void testRegistrarSinServicioInyectado() {
        RealizacionInformesService s2 = new RealizacionInformesService(consentimientoServiceMock);
        assertThrows(UnsupportedOperationException.class, () -> s2.registrarRealizacionPrueba(new RegistroRealizacionDTO()));
    }

    @Test
    @DisplayName("Debe lanzar UnsupportedOperationException para casos de uso de iteraciones posteriores (CU-09, CU-03)")
    void testMetodosFuturosNoSoportadosAun() {
        assertThrows(UnsupportedOperationException.class, () -> service.consultarHistorialPruebas("P1"));
        assertThrows(UnsupportedOperationException.class, () -> service.crearInforme(new InformeRadiologicoDTO()));
        assertThrows(UnsupportedOperationException.class, () -> service.consultarInforme("INF-1"));
        assertThrows(UnsupportedOperationException.class, () -> service.firmarInformeDigitalmente("INF-1", "MED-1", "H1"));
    }
}
