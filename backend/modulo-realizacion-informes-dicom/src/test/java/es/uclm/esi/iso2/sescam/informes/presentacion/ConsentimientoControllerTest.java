package es.uclm.esi.iso2.sescam.informes.presentacion;

import es.uclm.esi.iso2.sescam.common.dto.ConsentimientoDTO;
import es.uclm.esi.iso2.sescam.common.interfaces.IRealizacionInformesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias :: ConsentimientoController (Capa de Presentación CU-22)")
class ConsentimientoControllerTest {

    @Mock
    private IRealizacionInformesService serviceMock;

    private ConsentimientoController controller;

    @BeforeEach
    void setUp() {
        controller = new ConsentimientoController(serviceMock);
    }

    @Test
    @DisplayName("Debe delegar la firma del consentimiento al servicio y retornar el DTO procesado")
    void testProcesarFirmaConsentimiento() {
        ConsentimientoDTO input = new ConsentimientoDTO();
        input.setPacienteId("PAC-100");
        input.setTipoPruebaId("PRU-TAC");

        when(serviceMock.firmarConsentimientoInformado(input)).thenReturn(input);

        ConsentimientoDTO resultado = controller.procesarFirmaConsentimiento(input);

        assertNotNull(resultado);
        assertSame(input, resultado);
        verify(serviceMock, times(1)).firmarConsentimientoInformado(input);
    }

    @Test
    @DisplayName("Debe verificar la existencia de consentimiento previo mediante el servicio")
    void testVerificarConsentimientoPrevio() {
        when(serviceMock.validarConsentimientoPrevio("PAC-100", "PRU-TAC")).thenReturn(true);

        boolean apto = controller.verificarConsentimientoPrevio("PAC-100", "PRU-TAC");

        assertTrue(apto);
        verify(serviceMock, times(1)).validarConsentimientoPrevio("PAC-100", "PRU-TAC");
    }
}

