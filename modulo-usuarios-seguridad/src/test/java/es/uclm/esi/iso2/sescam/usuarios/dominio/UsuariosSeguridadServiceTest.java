package es.uclm.esi.iso2.sescam.usuarios.dominio;

import es.uclm.esi.iso2.sescam.common.dto.PacienteDTO;
import es.uclm.esi.iso2.sescam.common.interfaces.IAuditoriaService;
import es.uclm.esi.iso2.sescam.common.ports.ISisAdmisionClient;
import es.uclm.esi.iso2.sescam.usuarios.persistencia.IPacienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias :: UsuariosSeguridadService (Implementación IUsuariosSeguridadService - CU-12)")
class UsuariosSeguridadServiceTest {

    @Mock
    private IPacienteRepository pacienteRepositoryMock;

    @Mock
    private ISisAdmisionClient sisAdmisionClientMock;

    @Mock
    private IAuditoriaService auditoriaServiceMock;

    private UsuariosSeguridadService service;

    @BeforeEach
    void setUp() {
        service = new UsuariosSeguridadService(
                pacienteRepositoryMock,
                sisAdmisionClientMock,
                auditoriaServiceMock
        );
    }

    @Test
    @DisplayName("CU-12: Debe dar de alta a un paciente y persistirlo en el repositorio")
    void testDarAltaPaciente() {
        PacienteDTO nuevo = new PacienteDTO(
                null, "CIP-CLM-11223344", "11223344K", "Elena", "Vega",
                LocalDate.of(1991, 2, 2), "600112233", "elena@sescam.es", false
        );

        when(pacienteRepositoryMock.existePorDni("11223344K")).thenReturn(false);
        when(pacienteRepositoryMock.existePorCip("CIP-CLM-11223344")).thenReturn(false);
        when(pacienteRepositoryMock.guardar(any(PacienteDTO.class))).thenAnswer(inv -> inv.getArgument(0));

        PacienteDTO resultado = service.darAltaPaciente(nuevo);

        assertNotNull(resultado);
        assertNotNull(resultado.getId());
        assertTrue(resultado.getId().startsWith("PAC-"));
        assertTrue(resultado.isActivo());
        verify(pacienteRepositoryMock, times(1)).guardar(nuevo);
    }

    @Test
    @DisplayName("CU-12: Debe modificar los datos de un paciente existente")
    void testModificarPaciente() {
        PacienteDTO existente = new PacienteDTO(
                "PAC-001", "CIP-001", "12345678Z", "Elena", "Vega",
                LocalDate.of(1991, 2, 2), null, null, true
        );
        when(pacienteRepositoryMock.buscarPorIdOCip("PAC-001")).thenReturn(Optional.of(existente));
        when(pacienteRepositoryMock.guardar(any(PacienteDTO.class))).thenAnswer(inv -> inv.getArgument(0));

        PacienteDTO cambios = new PacienteDTO();
        cambios.setId("PAC-001");
        cambios.setTelefono("644556677");

        PacienteDTO modificado = service.modificarPaciente(cambios);

        assertNotNull(modificado);
        assertEquals("644556677", modificado.getTelefono());
        verify(pacienteRepositoryMock, times(1)).guardar(existente);
    }

    @Test
    @DisplayName("CU-12: RN-08 Debe ejecutar baja lógica marcando activo = false")
    void testDarBajaLogicaPaciente() {
        PacienteDTO existente = new PacienteDTO(
                "PAC-001", "CIP-001", "12345678Z", "Elena", "Vega",
                LocalDate.now(), null, null, true
        );
        when(pacienteRepositoryMock.buscarPorIdOCip("PAC-001")).thenReturn(Optional.of(existente));
        when(pacienteRepositoryMock.guardar(any(PacienteDTO.class))).thenAnswer(inv -> inv.getArgument(0));

        boolean resultado = service.darBajaLogicaPaciente("PAC-001");

        assertTrue(resultado);
        assertFalse(existente.isActivo(), "RN-08: El paciente debe quedar inactivo sin borrado físico");
        verify(pacienteRepositoryMock, times(1)).guardar(existente);
    }

    @Test
    @DisplayName("CU-12: Debe obtener un paciente localmente o vía SisAdmision")
    void testObtenerPaciente() {
        PacienteDTO paciente = new PacienteDTO("PAC-001", "CIP-001", "12345678Z", "Elena", "Vega", LocalDate.now(), null, null, true);
        when(pacienteRepositoryMock.buscarPorIdOCip("PAC-001")).thenReturn(Optional.of(paciente));

        PacienteDTO resultado = service.obtenerPaciente("PAC-001");

        assertNotNull(resultado);
        assertEquals("PAC-001", resultado.getId());
        verify(pacienteRepositoryMock, times(1)).buscarPorIdOCip("PAC-001");
    }

    @Test
    @DisplayName("Debe instanciarse correctamente con constructores sobrecargados")
    void testConstructoresSobrecargados() {
        UsuariosSeguridadService s1 = new UsuariosSeguridadService(pacienteRepositoryMock);
        assertNotNull(s1);

        UsuariosSeguridadService s2 = new UsuariosSeguridadService(pacienteRepositoryMock, sisAdmisionClientMock);
        assertNotNull(s2);
    }

    @Test
    @DisplayName("Debe lanzar UnsupportedOperationException en casos de uso de iteraciones posteriores (CU-18/CU-20)")
    void testMetodosFuturosNoSoportadosAun() {
        UnsupportedOperationException exSso = assertThrows(
                UnsupportedOperationException.class,
                () -> service.autenticarUsuario("admin", "token-sso")
        );
        assertTrue(exSso.getMessage().contains("CU-18"));

        UnsupportedOperationException exRbac = assertThrows(
                UnsupportedOperationException.class,
                () -> service.verificarPermiso("USU-01", "ACCION_PRUEBA")
        );
        assertTrue(exRbac.getMessage().contains("CU-18"));
    }
}
