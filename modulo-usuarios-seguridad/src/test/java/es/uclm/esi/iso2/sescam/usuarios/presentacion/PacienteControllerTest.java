package es.uclm.esi.iso2.sescam.usuarios.presentacion;

import es.uclm.esi.iso2.sescam.common.dto.PacienteDTO;
import es.uclm.esi.iso2.sescam.common.interfaces.IUsuariosSeguridadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias :: PacienteController (Capa de Presentación CU-12)")
class PacienteControllerTest {

    @Mock
    private IUsuariosSeguridadService usuariosSeguridadServiceMock;

    private PacienteController controller;

    @BeforeEach
    void setUp() {
        controller = new PacienteController(usuariosSeguridadServiceMock);
    }

    @Test
    @DisplayName("Debe delegar el alta de paciente y retornar el DTO procesado")
    void testDarAltaPaciente() {
        PacienteDTO input = new PacienteDTO(null, null, "12345678Z", "Beatriz", "Vidal", LocalDate.of(1985, 5, 5), null, null, true);
        when(usuariosSeguridadServiceMock.darAltaPaciente(input)).thenReturn(input);

        PacienteDTO resultado = controller.darAltaPaciente(input);

        assertNotNull(resultado);
        assertSame(input, resultado);
        verify(usuariosSeguridadServiceMock, times(1)).darAltaPaciente(input);
    }

    @Test
    @DisplayName("Debe delegar la modificación de paciente y retornar el DTO actualizado")
    void testModificarPaciente() {
        PacienteDTO input = new PacienteDTO("PAC-1", "CIP-1", "12345678Z", "Beatriz", "Vidal", LocalDate.of(1985, 5, 5), null, null, true);
        when(usuariosSeguridadServiceMock.modificarPaciente(input)).thenReturn(input);

        PacienteDTO resultado = controller.modificarPaciente(input);

        assertNotNull(resultado);
        assertSame(input, resultado);
        verify(usuariosSeguridadServiceMock, times(1)).modificarPaciente(input);
    }

    @Test
    @DisplayName("Debe delegar la baja lógica de paciente (RN-08)")
    void testDarBajaLogicaPaciente() {
        when(usuariosSeguridadServiceMock.darBajaLogicaPaciente("PAC-001")).thenReturn(true);

        boolean resultado = controller.darBajaLogicaPaciente("PAC-001");

        assertTrue(resultado);
        verify(usuariosSeguridadServiceMock, times(1)).darBajaLogicaPaciente("PAC-001");
    }

    @Test
    @DisplayName("Debe delegar la obtención de paciente")
    void testObtenerPaciente() {
        PacienteDTO paciente = new PacienteDTO("PAC-001", "CIP-001", "12345678Z", "Beatriz", "Vidal", LocalDate.now(), null, null, true);
        when(usuariosSeguridadServiceMock.obtenerPaciente("PAC-001")).thenReturn(paciente);

        PacienteDTO resultado = controller.obtenerPaciente("PAC-001");

        assertNotNull(resultado);
        assertEquals("PAC-001", resultado.getId());
        verify(usuariosSeguridadServiceMock, times(1)).obtenerPaciente("PAC-001");
    }

    @Test
    @DisplayName("Debe evaluar correctamente si un paciente está activo")
    void testEsPacienteActivo() {
        PacienteDTO activo = new PacienteDTO("PAC-1", "CIP-1", "DNI-1", "A", "A", LocalDate.now(), null, null, true);
        PacienteDTO inactivo = new PacienteDTO("PAC-2", "CIP-2", "DNI-2", "B", "B", LocalDate.now(), null, null, false);

        when(usuariosSeguridadServiceMock.obtenerPaciente("PAC-1")).thenReturn(activo);
        when(usuariosSeguridadServiceMock.obtenerPaciente("PAC-2")).thenReturn(inactivo);
        when(usuariosSeguridadServiceMock.obtenerPaciente("PAC-INEXISTENTE")).thenReturn(null);

        assertTrue(controller.esPacienteActivo("PAC-1"), "Debe devolver true para paciente activo");
        assertFalse(controller.esPacienteActivo("PAC-2"), "Debe devolver false para paciente en baja lógica");
        assertFalse(controller.esPacienteActivo("PAC-INEXISTENTE"), "Debe devolver false para paciente inexistente");
        assertFalse(controller.esPacienteActivo(null), "Debe devolver false para identificador nulo");
        assertFalse(controller.esPacienteActivo("   "), "Debe devolver false para identificador vacío");
    }

    @Test
    @DisplayName("Debe lanzar excepción si el servicio es nulo en el constructor")
    void testConstructorNuloLanzaExcepcion() {
        assertThrows(NullPointerException.class, () -> new PacienteController(null));
    }
}
