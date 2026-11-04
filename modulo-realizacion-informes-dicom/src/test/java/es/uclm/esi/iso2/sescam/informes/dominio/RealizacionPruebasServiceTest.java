package es.uclm.esi.iso2.sescam.informes.dominio;

import es.uclm.esi.iso2.sescam.common.dto.RegistroRealizacionDTO;
import es.uclm.esi.iso2.sescam.common.ports.IPacsClient;
import es.uclm.esi.iso2.sescam.informes.persistencia.IRegistroRealizacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias :: RealizacionPruebasServiceImpl (CU-10 / RN-04 / RIS-PACS)")
class RealizacionPruebasServiceTest {

    @Mock
    private IRegistroRealizacionRepository registroRepositoryMock;

    @Mock
    private ConsentimientoService consentimientoServiceMock;

    @Mock
    private IPacsClient pacsClientMock;

    private RealizacionPruebasServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RealizacionPruebasServiceImpl(registroRepositoryMock, consentimientoServiceMock, pacsClientMock);
    }

    @Test
    @DisplayName("Debe registrar con éxito una prueba estándar sin contraste ni RM si PACS es válido")
    void testRegistroExitosoSinContrasteNiRM() {
        RegistroRealizacionDTO input = new RegistroRealizacionDTO();
        input.setCitaId("CITA-101");
        input.setMedicoRadiologoId("MED-007");
        input.setStudyInstanceUID("1.2.840.113619.2.55.1.12345");
        input.setContrasteAdministrado(false);
        input.setDosisRadiacionMgy(0.12);

        when(pacsClientMock.validarEstudioDicom(input.getStudyInstanceUID())).thenReturn(true);
        when(registroRepositoryMock.guardar(any(RegistroRealizacion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegistroRealizacionDTO resultado = service.registrarRealizacion(input, "PAC-01", "PRU-RX-TORAX", false);

        assertNotNull(resultado);
        assertNotNull(resultado.getId());
        assertTrue(resultado.getId().startsWith("REG-"));
        assertNotNull(resultado.getFechaHoraEjecucion());
        assertEquals("CITA-101", resultado.getCitaId());
        assertEquals("1.2.840.113619.2.55.1.12345", resultado.getStudyInstanceUID());
        assertFalse(resultado.isContrasteAdministrado());

        verify(pacsClientMock, times(1)).validarEstudioDicom(input.getStudyInstanceUID());
        verify(registroRepositoryMock, times(1)).guardar(any(RegistroRealizacion.class));
        verifyNoInteractions(consentimientoServiceMock);
    }

    @Test
    @DisplayName("Debe registrar con éxito una prueba con contraste si cuenta con consentimiento válido (RN-04)")
    void testRegistroExitosoConContrasteYConsentimientoValido() {
        RegistroRealizacionDTO input = new RegistroRealizacionDTO();
        input.setCitaId("CITA-202");
        input.setMedicoRadiologoId("MED-007");
        input.setStudyInstanceUID("1.2.840.113619.2.55.2.67890");
        input.setContrasteAdministrado(true);
        input.setDosisRadiacionMgy(15.4);

        when(pacsClientMock.validarEstudioDicom(input.getStudyInstanceUID())).thenReturn(true);
        when(consentimientoServiceMock.validarConsentimientoPrevio("PAC-01", "PRU-TAC-ABDOMEN")).thenReturn(true);
        when(registroRepositoryMock.guardar(any(RegistroRealizacion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegistroRealizacionDTO resultado = service.registrarRealizacion(input, "PAC-01", "PRU-TAC-ABDOMEN", false);

        assertNotNull(resultado);
        assertTrue(resultado.isContrasteAdministrado());
        assertTrue(resultado.isConsentimientoPrevioValidado());

        verify(consentimientoServiceMock, times(1)).validarConsentimientoPrevio("PAC-01", "PRU-TAC-ABDOMEN");
        verify(registroRepositoryMock, times(1)).guardar(any(RegistroRealizacion.class));
    }

    @Test
    @DisplayName("RN-04: Debe bloquear y lanzar ConsentimientoRequeridoException si se usa contraste sin consentimiento")
    void testRegistroFallidoConContrasteSinConsentimientoValido_RN04() {
        RegistroRealizacionDTO input = new RegistroRealizacionDTO();
        input.setCitaId("CITA-303");
        input.setMedicoRadiologoId("MED-007");
        input.setStudyInstanceUID("1.2.840.113619.2.55.3.11111");
        input.setContrasteAdministrado(true);
        input.setDosisRadiacionMgy(12.0);

        when(pacsClientMock.validarEstudioDicom(input.getStudyInstanceUID())).thenReturn(true);
        when(consentimientoServiceMock.validarConsentimientoPrevio("PAC-01", "PRU-TAC-TORAX")).thenReturn(false);

        ConsentimientoRequeridoException ex = assertThrows(ConsentimientoRequeridoException.class, () ->
                service.registrarRealizacion(input, "PAC-01", "PRU-TAC-TORAX", false)
        );

        assertTrue(ex.getMessage().contains("RN-04"));
        verify(registroRepositoryMock, never()).guardar(any());
    }

    @Test
    @DisplayName("RN-04: Debe bloquear y lanzar ConsentimientoRequeridoException si es RM sin consentimiento previo")
    void testRegistroFallidoResonanciaMagneticaSinConsentimiento_RN04() {
        RegistroRealizacionDTO input = new RegistroRealizacionDTO();
        input.setCitaId("CITA-404");
        input.setMedicoRadiologoId("MED-007");
        input.setStudyInstanceUID("1.2.840.113619.2.55.4.22222");
        input.setContrasteAdministrado(false); // RM sin contraste, pero RM exige checklist por campo magnético
        input.setDosisRadiacionMgy(0.0);

        when(pacsClientMock.validarEstudioDicom(input.getStudyInstanceUID())).thenReturn(true);
        when(consentimientoServiceMock.validarConsentimientoPrevio("PAC-01", "PRU-RM-CEREBRAL")).thenReturn(false);

        ConsentimientoRequeridoException ex = assertThrows(ConsentimientoRequeridoException.class, () ->
                service.registrarRealizacion(input, "PAC-01", "PRU-RM-CEREBRAL", true) // esRM = true
        );

        assertTrue(ex.getMessage().contains("RN-04"));
        verify(registroRepositoryMock, never()).guardar(any());
    }

    @Test
    @DisplayName("Debe lanzar EstudioDicomNoValidoException si el StudyInstanceUID no supera verificación en PACS")
    void testRegistroFallidoStudyInstanceUIDInvalidoPACS() {
        RegistroRealizacionDTO input = new RegistroRealizacionDTO();
        input.setCitaId("CITA-505");
        input.setMedicoRadiologoId("MED-007");
        input.setStudyInstanceUID("UID-INVALIDO-999");

        when(pacsClientMock.validarEstudioDicom("UID-INVALIDO-999")).thenReturn(false);

        EstudioDicomNoValidoException ex = assertThrows(EstudioDicomNoValidoException.class, () ->
                service.registrarRealizacion(input, "PAC-01", "PRU-RX", false)
        );

        assertTrue(ex.getMessage().contains("PACS"));
        verify(registroRepositoryMock, never()).guardar(any());
    }

    @Test
    @DisplayName("Debe persistir correctamente incidencias técnicas o clínicas documentadas")
    void testRegistroExitosoConIncidenciasTecnicas() {
        RegistroRealizacionDTO input = new RegistroRealizacionDTO();
        input.setCitaId("CITA-606");
        input.setMedicoRadiologoId("MED-007");
        input.setStudyInstanceUID("1.2.840.113619.2.55.6.33333");
        input.setContrasteAdministrado(false);
        input.setDosisRadiacionMgy(1.5);
        input.setIncidenciasTecnicasClinicas("Ligero artefacto de movimiento del paciente; no requirió repetición.");

        when(pacsClientMock.validarEstudioDicom(input.getStudyInstanceUID())).thenReturn(true);
        when(registroRepositoryMock.guardar(any(RegistroRealizacion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegistroRealizacionDTO resultado = service.registrarRealizacion(input, "PAC-01", "PRU-RX", false);

        assertNotNull(resultado);
        assertEquals("Ligero artefacto de movimiento del paciente; no requirió repetición.", resultado.getIncidenciasTecnicasClinicas());
        verify(registroRepositoryMock, times(1)).guardar(any(RegistroRealizacion.class));
    }

    @Test
    @DisplayName("Debe validar que los campos obligatorios no sean nulos ni vacíos")
    void testValidacionesParametrosRequeridos() {
        // Objeto nulo
        assertThrows(IllegalArgumentException.class, () -> service.registrarRealizacion(null, "P1", "T1", false));

        // Cita nula
        RegistroRealizacionDTO r1 = new RegistroRealizacionDTO("R1", null, "UID", null, "MED", false, 0.0, null, false);
        assertThrows(IllegalArgumentException.class, () -> service.registrarRealizacion(r1, "P1", "T1", false));

        // Medico nulo
        RegistroRealizacionDTO r2 = new RegistroRealizacionDTO("R1", "C1", "UID", null, "   ", false, 0.0, null, false);
        assertThrows(IllegalArgumentException.class, () -> service.registrarRealizacion(r2, "P1", "T1", false));

        // UID nulo
        RegistroRealizacionDTO r3 = new RegistroRealizacionDTO("R1", "C1", "", null, "MED", false, 0.0, null, false);
        assertThrows(IllegalArgumentException.class, () -> service.registrarRealizacion(r3, "P1", "T1", false));

        // Dosis negativa
        RegistroRealizacionDTO r4 = new RegistroRealizacionDTO("R1", "C1", "UID", null, "MED", false, -0.5, null, false);
        assertThrows(IllegalArgumentException.class, () -> service.registrarRealizacion(r4, "P1", "T1", false));
    }

    @Test
    @DisplayName("Debe delegar consultas por CitaId y StudyInstanceUID al repositorio")
    void testConsultasPorCitaYPorUID() {
        RegistroRealizacion entidad = new RegistroRealizacion("REG-1", "CITA-1", "UID-1", null, "MED-1", false, 0.5, null, false);

        when(registroRepositoryMock.buscarPorCitaId("CITA-1")).thenReturn(Optional.of(entidad));
        when(registroRepositoryMock.buscarPorStudyInstanceUID("UID-1")).thenReturn(Optional.of(entidad));

        Optional<RegistroRealizacionDTO> porCita = service.consultarPorCitaId("CITA-1");
        Optional<RegistroRealizacionDTO> porUid = service.consultarPorStudyInstanceUID("UID-1");

        assertTrue(porCita.isPresent());
        assertEquals("REG-1", porCita.get().getId());
        assertTrue(porUid.isPresent());
        assertEquals("REG-1", porUid.get().getId());
    }

    @Test
    @DisplayName("Debe validar sobrecarga registrarRealizacionPrueba(dto)")
    void testSobrecargaRegistrarRealizacionPrueba() {
        RegistroRealizacionDTO r = new RegistroRealizacionDTO("REG-SOBRECARGA", "CITA-S", "UID-S", null, "MED-1", false, 0.0, null, false);
        when(pacsClientMock.validarEstudioDicom("UID-S")).thenReturn(true);
        when(registroRepositoryMock.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        RegistroRealizacionDTO res = service.registrarRealizacionPrueba(r);
        assertNotNull(res);
        assertEquals("REG-SOBRECARGA", res.getId());
    }
}

