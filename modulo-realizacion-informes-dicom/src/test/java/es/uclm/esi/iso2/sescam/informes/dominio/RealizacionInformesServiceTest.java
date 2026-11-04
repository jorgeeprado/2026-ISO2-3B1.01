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

    private RealizacionInformesService service;

    @BeforeEach
    void setUp() {
        service = new RealizacionInformesService(consentimientoServiceMock);
    }

    @Test
    @DisplayName("Debe delegar la firma de consentimiento en ConsentimientoService")
    void testFirmarConsentimientoInformadoDelega() {
        ConsentimientoDTO dto = new ConsentimientoDTO();
        when(consentimientoServiceMock.firmarConsentimiento(dto)).thenReturn(dto);

        ConsentimientoDTO result = service.firmarConsentimientoInformado(dto);

        assertSame(dto, result);
        verify(consentimientoServiceMock, times(1)).firmarConsentimiento(dto);
    }

    @Test
    @DisplayName("Debe delegar la validacion previa en ConsentimientoService")
    void testValidarConsentimientoPrevioDelega() {
        when(consentimientoServiceMock.validarConsentimientoPrevio("P1", "T1")).thenReturn(true);

        boolean result = service.validarConsentimientoPrevio("P1", "T1");

        assertTrue(result);
        verify(consentimientoServiceMock, times(1)).validarConsentimientoPrevio("P1", "T1");
    }

    @Test
    @DisplayName("Debe lanzar UnsupportedOperationException para casos de uso de iteraciones posteriores")
    void testMetodosFuturosNoSoportadosAun() {
        assertThrows(UnsupportedOperationException.class, () -> service.consultarHistorialPruebas("P1"));
        assertThrows(UnsupportedOperationException.class, () -> service.crearInforme(new InformeRadiologicoDTO()));
        assertThrows(UnsupportedOperationException.class, () -> service.consultarInforme("INF-1"));
        assertThrows(UnsupportedOperationException.class, () -> service.firmarInformeDigitalmente("INF-1", "MED-1", "H1"));
        assertThrows(UnsupportedOperationException.class, () -> service.registrarRealizacionPrueba(new RegistroRealizacionDTO()));
    }
}

