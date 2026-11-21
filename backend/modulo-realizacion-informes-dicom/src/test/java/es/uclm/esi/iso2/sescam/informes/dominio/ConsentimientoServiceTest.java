package es.uclm.esi.iso2.sescam.informes.dominio;

import es.uclm.esi.iso2.sescam.common.dto.ConsentimientoDTO;
import es.uclm.esi.iso2.sescam.informes.persistencia.IConsentimientoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias :: ConsentimientoService (CU-22 / RN-04)")
class ConsentimientoServiceTest {

    @Mock
    private IConsentimientoRepository repositoryMock;

    private ConsentimientoService service;

    @BeforeEach
    void setUp() {
        service = new ConsentimientoService(repositoryMock);
    }

    @Test
    @DisplayName("Debe firmar y registrar exitosamente un consentimiento con checklist de seguridad completo")
    void testFirmarConsentimientoExitoso() {
        ConsentimientoDTO input = new ConsentimientoDTO();
        input.setPacienteId("PAC-001");
        input.setTipoPruebaId("PRU-TAC-01");
        input.setChecklistSeguridadCompletado(true);
        input.setAlergiaContrasteYodado(false);
        input.setPortaMarcapasosOImplanteMetalico(false);
        input.setValorFiltradoGlomerular(85.5);

        when(repositoryMock.guardar(any(ConsentimientoDTO.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConsentimientoDTO resultado = service.firmarConsentimiento(input);

        assertNotNull(resultado);
        assertTrue(resultado.isFirmado(), "El consentimiento debe quedar marcado como firmado");
        assertNotNull(resultado.getId(), "Debe autogenerarse un identificador si no se suministró");
        assertTrue(resultado.getId().startsWith("CONS-"));
        assertNotNull(resultado.getFechaFirma(), "Debe asignarse fecha y hora de la firma");
        assertNotNull(resultado.getFirmaDigitalHash(), "Debe generarse un hash criptográfico de firma digital");

        verify(repositoryMock, times(1)).guardar(input);
    }

    @Test
    @DisplayName("Debe lanzar excepción si el objeto consentimiento es nulo")
    void testFirmarConsentimientoNulo() {
        assertThrows(IllegalArgumentException.class, () -> service.firmarConsentimiento(null));
        verifyNoInteractions(repositoryMock);
    }

    @Test
    @DisplayName("Debe lanzar excepción si el pacienteId es nulo o vacío")
    void testFirmarConsentimientoPacienteInvalido() {
        ConsentimientoDTO c1 = new ConsentimientoDTO();
        c1.setPacienteId(null);
        c1.setTipoPruebaId("PRU-01");
        c1.setChecklistSeguridadCompletado(true);
        assertThrows(IllegalArgumentException.class, () -> service.firmarConsentimiento(c1));

        ConsentimientoDTO c2 = new ConsentimientoDTO();
        c2.setPacienteId("   ");
        c2.setTipoPruebaId("PRU-01");
        c2.setChecklistSeguridadCompletado(true);
        assertThrows(IllegalArgumentException.class, () -> service.firmarConsentimiento(c2));

        verifyNoInteractions(repositoryMock);
    }

    @Test
    @DisplayName("Debe lanzar excepción si el tipoPruebaId es nulo o vacío")
    void testFirmarConsentimientoPruebaInvalida() {
        ConsentimientoDTO c = new ConsentimientoDTO();
        c.setPacienteId("PAC-01");
        c.setTipoPruebaId(null);
        c.setChecklistSeguridadCompletado(true);

        assertThrows(IllegalArgumentException.class, () -> service.firmarConsentimiento(c));
        verifyNoInteractions(repositoryMock);
    }

    @Test
    @DisplayName("Debe lanzar excepción si se intenta firmar sin haber completado el checklist de seguridad")
    void testFirmarConsentimientoChecklistIncompleto() {
        ConsentimientoDTO c = new ConsentimientoDTO();
        c.setPacienteId("PAC-01");
        c.setTipoPruebaId("PRU-01");
        c.setChecklistSeguridadCompletado(false); // No completado

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.firmarConsentimiento(c));
        assertTrue(ex.getMessage().contains("cuestionario de seguridad"));
        verifyNoInteractions(repositoryMock);
    }

    @Test
    @DisplayName("Debe validar exitosamente si el paciente cuenta con consentimiento apto y sin contraindicaciones")
    void testValidarConsentimientoPrevioExitoso() {
        ConsentimientoDTO c = new ConsentimientoDTO(
                "CONS-1", "PAC-01", "PRU-01", LocalDateTime.now(),
                true, true, false, false, 90.0, "hash12345"
        );

        when(repositoryMock.buscarPorPacienteYPrueba("PAC-01", "PRU-01")).thenReturn(Optional.of(c));

        boolean apto = service.validarConsentimientoPrevio("PAC-01", "PRU-01");

        assertTrue(apto, "El paciente debe estar validado para la realización de la prueba");
        verify(repositoryMock, times(1)).buscarPorPacienteYPrueba("PAC-01", "PRU-01");
    }

    @Test
    @DisplayName("Debe bloquear (false) si no consta ningún consentimiento previo registrado")
    void testValidarConsentimientoPrevioNoExiste() {
        when(repositoryMock.buscarPorPacienteYPrueba("PAC-01", "PRU-01")).thenReturn(Optional.empty());

        boolean apto = service.validarConsentimientoPrevio("PAC-01", "PRU-01");

        assertFalse(apto, "Debe bloquear si no existe consentimiento previo");
    }

    @Test
    @DisplayName("Debe bloquear si los parámetros de búsqueda son nulos")
    void testValidarConsentimientoPrevioParametrosNulos() {
        assertFalse(service.validarConsentimientoPrevio(null, "PRU-01"));
        assertFalse(service.validarConsentimientoPrevio("PAC-01", null));
        verifyNoInteractions(repositoryMock);
    }

    @Test
    @DisplayName("Debe bloquear si el consentimiento no está firmado o le falta el hash de firma")
    void testValidarConsentimientoPrevioIncompleto() {
        ConsentimientoDTO c = new ConsentimientoDTO(
                "CONS-1", "PAC-01", "PRU-01", LocalDateTime.now(),
                false, true, false, false, 90.0, "hash12345"
        );
        when(repositoryMock.buscarPorPacienteYPrueba("PAC-01", "PRU-01")).thenReturn(Optional.of(c));
        assertFalse(service.validarConsentimientoPrevio("PAC-01", "PRU-01"));

        c.setFirmado(true);
        c.setFirmaDigitalHash(null);
        assertFalse(service.validarConsentimientoPrevio("PAC-01", "PRU-01"));
    }

    @Test
    @DisplayName("RN-04: Debe bloquear la realización si el paciente porta marcapasos o implante metálico")
    void testValidarConsentimientoPrevioContraindicacionMarcapasos() {
        ConsentimientoDTO c = new ConsentimientoDTO(
                "CONS-1", "PAC-01", "PRU-RM", LocalDateTime.now(),
                true, true, false, true, 80.0, "hash12345"
        );
        when(repositoryMock.buscarPorPacienteYPrueba("PAC-01", "PRU-RM")).thenReturn(Optional.of(c));

        boolean apto = service.validarConsentimientoPrevio("PAC-01", "PRU-RM");

        assertFalse(apto, "RN-04 exige bloquear si existe contraindicación de marcapasos o implante metálico");
    }

    @Test
    @DisplayName("RN-04: Debe bloquear la realización si el paciente tiene filtrado glomerular severo (<30)")
    void testValidarConsentimientoPrevioFiltradoGlomerularBajo() {
        ConsentimientoDTO c = new ConsentimientoDTO(
                "CONS-1", "PAC-01", "PRU-TAC-CONTRASTE", LocalDateTime.now(),
                true, true, false, false, 24.5, "hash12345"
        );
        when(repositoryMock.buscarPorPacienteYPrueba("PAC-01", "PRU-TAC-CONTRASTE")).thenReturn(Optional.of(c));

        boolean apto = service.validarConsentimientoPrevio("PAC-01", "PRU-TAC-CONTRASTE");

        assertFalse(apto, "RN-04 exige bloquear si el filtrado glomerular < 30 ml/min por riesgo de nefrotoxicidad");
    }

    @Test
    @DisplayName("Debe consultar correctamente el consentimiento registrado")
    void testConsultarConsentimiento() {
        ConsentimientoDTO c = new ConsentimientoDTO();
        c.setId("CONS-123");
        when(repositoryMock.buscarPorPacienteYPrueba("PAC-01", "PRU-01")).thenReturn(Optional.of(c));

        Optional<ConsentimientoDTO> result = service.consultarConsentimiento("PAC-01", "PRU-01");

        assertTrue(result.isPresent());
        assertEquals("CONS-123", result.get().getId());
    }
}

