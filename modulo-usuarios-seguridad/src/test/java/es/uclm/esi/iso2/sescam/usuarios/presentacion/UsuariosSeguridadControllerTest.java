package es.uclm.esi.iso2.sescam.usuarios.presentacion;

import es.uclm.esi.iso2.sescam.common.dto.UsuarioDTO;
import es.uclm.esi.iso2.sescam.common.enums.RolUsuario;
import es.uclm.esi.iso2.sescam.common.interfaces.IUsuariosSeguridadService;
import es.uclm.esi.iso2.sescam.usuarios.dominio.UsuariosSeguridadService;
import es.uclm.esi.iso2.sescam.usuarios.persistencia.IUsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias :: UsuariosSeguridadController (Capa de Presentación CU-18)")
class UsuariosSeguridadControllerTest {

    @Mock
    private IUsuariosSeguridadService serviceInterfaceMock;

    @Mock
    private IUsuarioRepository usuarioRepositoryMock;

    private UsuariosSeguridadController controllerConInterface;
    private UsuariosSeguridadController controllerConcreto;
    private UsuariosSeguridadService serviceConcreto;

    @BeforeEach
    void setUp() {
        controllerConInterface = new UsuariosSeguridadController(serviceInterfaceMock);

        serviceConcreto = new UsuariosSeguridadService(usuarioRepositoryMock);
        controllerConcreto = new UsuariosSeguridadController(serviceConcreto);
    }

    @Test
    @DisplayName("Debe delegar la verificación de acceso a través del contrato IUsuariosSeguridadService")
    void testVerificarAcceso() {
        when(serviceInterfaceMock.verificarPermiso("USU-1", "PRESCRIBIR_PRUEBA")).thenReturn(true);

        boolean acceso = controllerConInterface.verificarAcceso("USU-1", "PRESCRIBIR_PRUEBA");

        assertTrue(acceso);
        verify(serviceInterfaceMock, times(1)).verificarPermiso("USU-1", "PRESCRIBIR_PRUEBA");
    }

    @Test
    @DisplayName("Debe delegar la autenticación SSO a través del contrato IUsuariosSeguridadService")
    void testAutenticarSso() {
        UsuarioDTO user = new UsuarioDTO("U1", "jorge.prado", "Jorge", "Prado", null, RolUsuario.MEDICO, true);
        when(serviceInterfaceMock.autenticarUsuario("jorge.prado", "token-jwt")).thenReturn(user);

        UsuarioDTO resultado = controllerConInterface.autenticarSso("jorge.prado", "token-jwt");

        assertNotNull(resultado);
        assertSame(user, resultado);
        verify(serviceInterfaceMock, times(1)).autenticarUsuario("jorge.prado", "token-jwt");
    }

    @Test
    @DisplayName("Debe procesar el ciclo de administración RBAC en el controlador concreto")
    void testOperacionesAdministracionRbac() {
        UsuarioDTO nuevo = new UsuarioDTO(null, "usuario.test", "Nombre", "Ap", "t@sescam.es", RolUsuario.PACIENTE, true);
        when(usuarioRepositoryMock.existePorUsername("usuario.test")).thenReturn(false);
        when(usuarioRepositoryMock.guardar(any(UsuarioDTO.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioDTO registrado = controllerConcreto.registrarUsuario(nuevo);
        assertNotNull(registrado.getId());

        when(usuarioRepositoryMock.buscarPorId(registrado.getId())).thenReturn(Optional.of(registrado));

        UsuarioDTO conRolMedico = controllerConcreto.asignarRol(registrado.getId(), RolUsuario.MEDICO);
        assertEquals(RolUsuario.MEDICO, conRolMedico.getRol());

        boolean suspendido = controllerConcreto.cambiarEstadoUsuario(registrado.getId(), false);
        assertTrue(suspendido);

        Optional<UsuarioDTO> consultado = controllerConcreto.consultarUsuario(registrado.getId());
        assertTrue(consultado.isPresent());

        Set<String> permisos = controllerConcreto.obtenerPermisosUsuario(registrado.getId());
        assertNotNull(permisos);

        when(usuarioRepositoryMock.listarPorRol(RolUsuario.MEDICO)).thenReturn(List.of(registrado));
        List<UsuarioDTO> medicos = controllerConcreto.listarUsuariosPorRol(RolUsuario.MEDICO);
        assertEquals(1, medicos.size());

        conRolMedico.setNombre("NombreModificado");
        UsuarioDTO modificado = controllerConcreto.modificarUsuario(conRolMedico);
        assertEquals("NombreModificado", modificado.getNombre());
    }

    @Test
    @DisplayName("Debe lanzar excepción si el servicio es nulo en el constructor")
    void testConstructorNuloLanzaExcepcion() {
        assertThrows(NullPointerException.class, () -> new UsuariosSeguridadController(null));
    }
}
