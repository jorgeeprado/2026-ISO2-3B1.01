package es.uclm.esi.iso2.sescam.usuarios.dominio;

import es.uclm.esi.iso2.sescam.common.dto.PacienteDTO;
import es.uclm.esi.iso2.sescam.common.dto.UsuarioDTO;
import es.uclm.esi.iso2.sescam.common.enums.RolUsuario;
import es.uclm.esi.iso2.sescam.common.interfaces.IAuditoriaService;
import es.uclm.esi.iso2.sescam.common.ports.ISsoClient;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias :: UsuariosSeguridadService (CU-18 / RBAC / RN-05 / ISsoClient)")
class UsuariosSeguridadServiceTest {

    @Mock
    private IUsuarioRepository usuarioRepositoryMock;

    @Mock
    private ISsoClient ssoClientMock;

    @Mock
    private IAuditoriaService auditoriaServiceMock;

    private UsuariosSeguridadService service;

    @BeforeEach
    void setUp() {
        service = new UsuariosSeguridadService(usuarioRepositoryMock, ssoClientMock, auditoriaServiceMock);
    }

    // =========================================================================
    // 1. Gestión de cuentas de usuario (Alta / Modificación / Consulta)
    // =========================================================================

    @Test
    @DisplayName("Debe registrar un nuevo usuario con rol asignado y auditar la creación")
    void testRegistrarUsuarioExitoso() {
        UsuarioDTO input = new UsuarioDTO(
                null, "pedro.fuente", "Pedro", "Fuente", "pfuente@sescam.es",
                RolUsuario.ADMINISTRATIVO_ADMISION, true
        );

        when(usuarioRepositoryMock.existePorUsername("pedro.fuente")).thenReturn(false);
        when(usuarioRepositoryMock.guardar(any(UsuarioDTO.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioDTO resultado = service.registrarUsuario(input);

        assertNotNull(resultado);
        assertNotNull(resultado.getId());
        assertTrue(resultado.getId().startsWith("USU-"));
        assertEquals("pedro.fuente", resultado.getUsername());
        assertEquals(RolUsuario.ADMINISTRATIVO_ADMISION, resultado.getRol());
        assertTrue(resultado.isActivo());

        verify(usuarioRepositoryMock, times(1)).guardar(input);
        verify(auditoriaServiceMock, times(1)).registrarAccesoClinico(
                eq("SISTEMA_SEGURIDAD"), eq(resultado.getId()), eq("CREACION_USUARIO"), any(), any()
        );
    }

    @Test
    @DisplayName("Debe lanzar UsuarioYaExisteException si el username ya está registrado")
    void testRegistrarUsuarioDuplicado() {
        UsuarioDTO input = new UsuarioDTO(null, "usuario.existente", "U", "E", null, RolUsuario.PACIENTE, true);
        when(usuarioRepositoryMock.existePorUsername("usuario.existente")).thenReturn(true);

        assertThrows(UsuarioYaExisteException.class, () -> service.registrarUsuario(input));
        verify(usuarioRepositoryMock, never()).guardar(any());
    }

    @Test
    @DisplayName("Debe lanzar UsuarioInvalidoException ante datos de usuario nulos o incompletos")
    void testRegistrarUsuarioValidaciones() {
        assertThrows(UsuarioInvalidoException.class, () -> service.registrarUsuario(null));

        UsuarioDTO sinUsername = new UsuarioDTO(null, "  ", "A", "B", null, RolUsuario.PACIENTE, true);
        assertThrows(UsuarioInvalidoException.class, () -> service.registrarUsuario(sinUsername));

        UsuarioDTO sinNombre = new UsuarioDTO(null, "user", "  ", "B", null, RolUsuario.PACIENTE, true);
        assertThrows(UsuarioInvalidoException.class, () -> service.registrarUsuario(sinNombre));

        UsuarioDTO sinApellidos = new UsuarioDTO(null, "user", "A", "  ", null, RolUsuario.PACIENTE, true);
        assertThrows(UsuarioInvalidoException.class, () -> service.registrarUsuario(sinApellidos));

        UsuarioDTO sinRol = new UsuarioDTO(null, "user", "A", "B", null, null, true);
        assertThrows(UsuarioInvalidoException.class, () -> service.registrarUsuario(sinRol));
    }

    @Test
    @DisplayName("Debe modificar los datos de un usuario existente")
    void testModificarUsuarioExitoso() {
        UsuarioDTO existente = new UsuarioDTO("USU-1", "user1", "Nombre", "Ap", "old@sescam.es", RolUsuario.PACIENTE, true);
        when(usuarioRepositoryMock.buscarPorId("USU-1")).thenReturn(Optional.of(existente));
        when(usuarioRepositoryMock.guardar(any(UsuarioDTO.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioDTO cambios = new UsuarioDTO("USU-1", "user1.nuevo", "NombreNuevo", "ApNuevo", "new@sescam.es", RolUsuario.MEDICO, true);
        when(usuarioRepositoryMock.buscarPorUsername("user1.nuevo")).thenReturn(Optional.empty());

        UsuarioDTO actualizado = service.modificarUsuario(cambios);

        assertNotNull(actualizado);
        assertEquals("user1.nuevo", actualizado.getUsername());
        assertEquals("NombreNuevo", actualizado.getNombre());
        assertEquals(RolUsuario.MEDICO, actualizado.getRol());
        verify(usuarioRepositoryMock, times(1)).guardar(existente);
    }

    @Test
    @DisplayName("Debe lanzar UsuarioNoEncontradoException al modificar usuario inexistente")
    void testModificarUsuarioNoEncontrado() {
        UsuarioDTO cambios = new UsuarioDTO("USU-INEXISTENTE", "u", "N", "A", null, RolUsuario.PACIENTE, true);
        when(usuarioRepositoryMock.buscarPorId("USU-INEXISTENTE")).thenReturn(Optional.empty());

        assertThrows(UsuarioNoEncontradoException.class, () -> service.modificarUsuario(cambios));
    }

    @Test
    @DisplayName("Debe lanzar UsuarioYaExisteException si la modificación de username colisiona con otro usuario")
    void testModificarUsuarioConflictoUsername() {
        UsuarioDTO u1 = new UsuarioDTO("USU-1", "user1", "N1", "A1", null, RolUsuario.PACIENTE, true);
        UsuarioDTO u2 = new UsuarioDTO("USU-2", "user2", "N2", "A2", null, RolUsuario.PACIENTE, true);

        when(usuarioRepositoryMock.buscarPorId("USU-1")).thenReturn(Optional.of(u1));
        when(usuarioRepositoryMock.buscarPorUsername("user2")).thenReturn(Optional.of(u2));

        UsuarioDTO cambios = new UsuarioDTO("USU-1", "user2", "N1", "A1", null, RolUsuario.PACIENTE, true);

        assertThrows(UsuarioYaExisteException.class, () -> service.modificarUsuario(cambios));
    }

    // =========================================================================
    // 2. Asignación de Roles y Cambio de Estado de Cuenta
    // =========================================================================

    @Test
    @DisplayName("Debe asignar un nuevo rol RBAC a un usuario existente")
    void testAsignarRolExitoso() {
        UsuarioDTO user = new UsuarioDTO("USU-005", "ines.ballesteros", "Inés", "Ballesteros", "i@sescam.es", RolUsuario.PACIENTE, true);
        when(usuarioRepositoryMock.buscarPorId("USU-005")).thenReturn(Optional.of(user));
        when(usuarioRepositoryMock.guardar(any(UsuarioDTO.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioDTO modificado = service.asignarRol("USU-005", RolUsuario.MEDICO);

        assertNotNull(modificado);
        assertEquals(RolUsuario.MEDICO, modificado.getRol());
        verify(usuarioRepositoryMock, times(1)).guardar(user);
        verify(auditoriaServiceMock, times(1)).registrarAccesoClinico(
                eq("SISTEMA_SEGURIDAD"), eq("USU-005"), eq("CAMBIO_ROL_MEDICO"), any(), any()
        );
    }

    @Test
    @DisplayName("Debe lanzar excepción al asignar rol con parámetros inválidos o usuario no encontrado")
    void testAsignarRolValidaciones() {
        assertThrows(IllegalArgumentException.class, () -> service.asignarRol(null, RolUsuario.MEDICO));
        assertThrows(IllegalArgumentException.class, () -> service.asignarRol("USU-1", null));

        when(usuarioRepositoryMock.buscarPorId("NO-EXISTE")).thenReturn(Optional.empty());
        when(usuarioRepositoryMock.buscarPorUsername("NO-EXISTE")).thenReturn(Optional.empty());
        assertThrows(UsuarioNoEncontradoException.class, () -> service.asignarRol("NO-EXISTE", RolUsuario.MEDICO));
    }

    @Test
    @DisplayName("Debe activar o desactivar una cuenta de usuario")
    void testCambiarEstadoUsuario() {
        UsuarioDTO user = new UsuarioDTO("USU-006", "marcos.diaz", "Marcos", "Díaz", "m@sescam.es", RolUsuario.ADMINISTRATIVO_ADMISION, true);
        when(usuarioRepositoryMock.buscarPorId("USU-006")).thenReturn(Optional.of(user));

        boolean suspendido = service.cambiarEstadoUsuario("USU-006", false);
        assertTrue(suspendido);
        assertFalse(user.isActivo());

        boolean reactivado = service.cambiarEstadoUsuario("USU-006", true);
        assertTrue(reactivado);
        assertTrue(user.isActivo());
    }

    @Test
    @DisplayName("Debe listar usuarios y filtrar por rol")
    void testListarUsuarios() {
        UsuarioDTO u1 = new UsuarioDTO("U1", "p1", "A", "A", null, RolUsuario.PACIENTE, true);
        when(usuarioRepositoryMock.listarTodos()).thenReturn(List.of(u1));
        when(usuarioRepositoryMock.listarPorRol(RolUsuario.PACIENTE)).thenReturn(List.of(u1));

        assertEquals(1, service.listarUsuarios().size());
        assertEquals(1, service.listarUsuariosPorRol(RolUsuario.PACIENTE).size());
        assertTrue(service.obtenerUsuarioPorId("U1").isEmpty());
        assertTrue(service.obtenerUsuarioPorUsername("p1").isEmpty());
    }

    // =========================================================================
    // 3. Matriz de Permisos RBAC y Herencia Funcional del Rol Médico (RN-05)
    // =========================================================================

    @Test
    @DisplayName("RBAC: Administrador del sistema tiene acceso pleno a cualquier acción")
    void testVerificarPermisoAdminSistema() {
        UsuarioDTO admin = new UsuarioDTO("USU-ADM", "admin", "Admin", "General", null, RolUsuario.ADMIN_SISTEMA, true);
        when(usuarioRepositoryMock.buscarPorId("USU-ADM")).thenReturn(Optional.of(admin));

        assertTrue(service.verificarPermiso("USU-ADM", "CONFIG_SISTEMA"));
        assertTrue(service.verificarPermiso("USU-ADM", "PRESCRIBIR_PRUEBA"));
        assertTrue(service.verificarPermiso("USU-ADM", "GESTION_ROLES"));
    }

    @Test
    @DisplayName("RBAC: Personal de Admisión posee permisos de citación y filiación pero no clínicos")
    void testVerificarPermisoAdministrativoAdmision() {
        UsuarioDTO adminis = new UsuarioDTO("USU-ADM-1", "admision", "Pedro", "López", null, RolUsuario.ADMINISTRATIVO_ADMISION, true);
        when(usuarioRepositoryMock.buscarPorId("USU-ADM-1")).thenReturn(Optional.of(adminis));

        assertTrue(service.verificarPermiso("USU-ADM-1", "ASIGNAR_CITA"));
        assertTrue(service.verificarPermiso("USU-ADM-1", "ALTA_PACIENTE"));
        assertTrue(service.verificarPermiso("USU-ADM-1", "ADMINISTRAR_AGENDAS"));
        assertFalse(service.verificarPermiso("USU-ADM-1", "PRESCRIBIR_PRUEBA"), "Personal administrativo no puede prescribir");
        assertFalse(service.verificarPermiso("USU-ADM-1", "FIRMAR_INFORME"), "Personal administrativo no puede firmar informes");
    }

    @Test
    @DisplayName("RBAC: Paciente solo posee acceso a sus casos de uso de autogestión")
    void testVerificarPermisoPaciente() {
        UsuarioDTO paciente = new UsuarioDTO("USU-PAC", "paciente", "Ana", "Ruiz", null, RolUsuario.PACIENTE, true);
        when(usuarioRepositoryMock.buscarPorId("USU-PAC")).thenReturn(Optional.of(paciente));

        assertTrue(service.verificarPermiso("USU-PAC", "CONSULTAR_CITA"));
        assertTrue(service.verificarPermiso("USU-PAC", "FIRMAR_CONSENTIMIENTO"));
        assertTrue(service.verificarPermiso("USU-PAC", "CHECKLIST_SEGURIDAD"));
        assertFalse(service.verificarPermiso("USU-PAC", "ASIGNAR_CITA"), "Paciente no tiene potestad para citar formalmente (RN-02)");
        assertFalse(service.verificarPermiso("USU-PAC", "PRESCRIBIR_PRUEBA"));
    }

    @Test
    @DisplayName("RBAC / RN-05: El Médico posee permisos clínicos Y hereda todos los permisos del Paciente")
    void testVerificarPermisoHerenciaMedicoRespectoAPaciente() {
        UsuarioDTO medico = new UsuarioDTO("USU-MED", "dr.prado", "Jorge", "Prado", null, RolUsuario.MEDICO, true);
        when(usuarioRepositoryMock.buscarPorId("USU-MED")).thenReturn(Optional.of(medico));

        // Permisos clínicos específicos del facultativo
        assertTrue(service.verificarPermiso("USU-MED", "PRESCRIBIR_PRUEBA"));
        assertTrue(service.verificarPermiso("USU-MED", "FIRMAR_INFORME"));
        assertTrue(service.verificarPermiso("USU-MED", "EMITIR_ALERTA_CRITICA"));

        // RN-05 / D-006: Herencia Funcional del Rol Paciente a título personal
        assertTrue(service.verificarPermiso("USU-MED", "CONSULTAR_CITA"), "RN-05 exige heredar permisos de PACIENTE");
        assertTrue(service.verificarPermiso("USU-MED", "FIRMAR_CONSENTIMIENTO"), "RN-05 exige heredar permisos de PACIENTE");
        assertTrue(service.verificarPermiso("USU-MED", "CONSULTAR_HISTORIAL"), "RN-05 exige heredar permisos de PACIENTE");

        // No tiene permisos de administración del sistema
        assertFalse(service.verificarPermiso("USU-MED", "CONFIG_SISTEMA"));
    }

    @Test
    @DisplayName("RBAC: Usuario inactivo o no encontrado retorna false en verificarPermiso")
    void testVerificarPermisoUsuarioInactivoONoEncontrado() {
        UsuarioDTO inactivo = new UsuarioDTO("USU-INACTIVO", "bloqueado", "B", "B", null, RolUsuario.ADMIN_SISTEMA, false);
        when(usuarioRepositoryMock.buscarPorId("USU-INACTIVO")).thenReturn(Optional.of(inactivo));
        when(usuarioRepositoryMock.buscarPorId("USU-NO-EXISTE")).thenReturn(Optional.empty());
        when(usuarioRepositoryMock.buscarPorUsername("USU-NO-EXISTE")).thenReturn(Optional.empty());

        assertFalse(service.verificarPermiso("USU-INACTIVO", "CONFIG_SISTEMA"));
        assertFalse(service.verificarPermiso("USU-NO-EXISTE", "CONSULTAR_CITA"));
        assertFalse(service.verificarPermiso(null, "CONSULTAR_CITA"));
        assertFalse(service.verificarPermiso("USU-INACTIVO", "  "));
    }

    @Test
    @DisplayName("RBAC: obtenerPermisosDeUsuario retorna los conjuntos correctos considerando herencia RN-05")
    void testObtenerPermisosDeUsuario() {
        UsuarioDTO medico = new UsuarioDTO("U-MED", "medico", "M", "M", null, RolUsuario.MEDICO, true);
        UsuarioDTO paciente = new UsuarioDTO("U-PAC", "pac", "P", "P", null, RolUsuario.PACIENTE, true);

        when(usuarioRepositoryMock.buscarPorId("U-MED")).thenReturn(Optional.of(medico));
        when(usuarioRepositoryMock.buscarPorId("U-PAC")).thenReturn(Optional.of(paciente));

        Set<String> permisosPac = service.obtenerPermisosDeUsuario("U-PAC");
        assertTrue(permisosPac.contains("CONSULTAR_CITA"));
        assertFalse(permisosPac.contains("PRESCRIBIR_PRUEBA"));

        Set<String> permisosMed = service.obtenerPermisosDeUsuario("U-MED");
        assertTrue(permisosMed.contains("PRESCRIBIR_PRUEBA"));
        // RN-05: Verificamos que el Set del médico contiene permisos de paciente
        assertTrue(permisosMed.contains("CONSULTAR_CITA"), "RN-05: El médico debe contener permisos heredados de paciente");
    }

    // =========================================================================
    // 4. Integración con SSO Corporativo (ISsoClient / RNF-04)
    // =========================================================================

    @Test
    @DisplayName("SSO: Autenticación federada exitosa con sincronización en repositorio")
    void testAutenticarUsuarioSsoExitoso() {
        UsuarioDTO federado = new UsuarioDTO(
                "USU-SSO-1", "carlos.dueña", "Carlos", "Dueña", "carlos@sescam.es",
                RolUsuario.ADMIN_SISTEMA, true
        );
        when(ssoClientMock.validarTokenFederado("token-jwt-valido")).thenReturn(federado);
        when(usuarioRepositoryMock.guardar(federado)).thenReturn(federado);

        UsuarioDTO autenticado = service.autenticarUsuario("carlos.dueña", "token-jwt-valido");

        assertNotNull(autenticado);
        assertEquals("carlos.dueña", autenticado.getUsername());
        verify(usuarioRepositoryMock, times(1)).guardar(federado);
        verify(auditoriaServiceMock, times(1)).registrarAccesoClinico(
                eq("SISTEMA_SEGURIDAD"), eq(federado.getId()), eq("LOGIN_SSO_EXITOSO"), any(), any()
        );
    }

    @Test
    @DisplayName("SSO: Debe lanzar AutenticacionException si el token federado es inválido")
    void testAutenticarUsuarioTokenInvalido() {
        when(ssoClientMock.validarTokenFederado("token-invalido")).thenReturn(null);

        assertThrows(AutenticacionException.class, () -> service.autenticarUsuario("carlos", "token-invalido"));
        verify(usuarioRepositoryMock, never()).guardar(any());
    }

    @Test
    @DisplayName("SSO: Debe lanzar AutenticacionException si la cuenta devuelta por SSO está inactiva")
    void testAutenticarUsuarioCuentaInactiva() {
        UsuarioDTO inactivo = new UsuarioDTO("U-1", "user1", "U", "U", null, RolUsuario.PACIENTE, false);
        when(ssoClientMock.validarTokenFederado("token-valido")).thenReturn(inactivo);

        assertThrows(AutenticacionException.class, () -> service.autenticarUsuario("user1", "token-valido"));
    }

    @Test
    @DisplayName("SSO: Debe lanzar AutenticacionException si el username no coincide con el token")
    void testAutenticarUsuarioUsernameDiscrepante() {
        UsuarioDTO usuarioToken = new UsuarioDTO("U-1", "otro.user", "O", "O", null, RolUsuario.PACIENTE, true);
        when(ssoClientMock.validarTokenFederado("token-valido")).thenReturn(usuarioToken);

        assertThrows(AutenticacionException.class, () -> service.autenticarUsuario("mi.user", "token-valido"));
    }

    @Test
    @DisplayName("SSO: Debe lanzar IllegalArgumentException con parámetros de autenticación vacíos")
    void testAutenticarUsuarioParametrosVacios() {
        assertThrows(IllegalArgumentException.class, () -> service.autenticarUsuario(null, "token"));
        assertThrows(IllegalArgumentException.class, () -> service.autenticarUsuario("user", null));
        assertThrows(IllegalArgumentException.class, () -> service.autenticarUsuario("  ", "token"));
    }

    @Test
    @DisplayName("SSO: Debe lanzar IllegalStateException si ISsoClient no fue inyectado")
    void testAutenticarSinSsoClient() {
        UsuariosSeguridadService s2 = new UsuariosSeguridadService(usuarioRepositoryMock);
        assertThrows(IllegalStateException.class, () -> s2.autenticarUsuario("user", "token"));
    }

    // =========================================================================
    // 5. Métodos diferidos de CU-12 y constructores
    // =========================================================================

    @Test
    @DisplayName("Debe lanzar UnsupportedOperationException para métodos de CU-12 en la Iteración 2")
    void testMetodosCu12Diferidos() {
        assertThrows(UnsupportedOperationException.class, () -> service.darAltaPaciente(new PacienteDTO()));
        assertThrows(UnsupportedOperationException.class, () -> service.modificarPaciente(new PacienteDTO()));
        assertThrows(UnsupportedOperationException.class, () -> service.darBajaLogicaPaciente("PAC-1"));
        assertThrows(UnsupportedOperationException.class, () -> service.obtenerPaciente("PAC-1"));
    }

    @Test
    @DisplayName("Debe inicializar correctamente constructores sobrecargados")
    void testConstructoresSobrecargados() {
        UsuariosSeguridadService s1 = new UsuariosSeguridadService(usuarioRepositoryMock);
        assertNotNull(s1);

        UsuariosSeguridadService s2 = new UsuariosSeguridadService(usuarioRepositoryMock, ssoClientMock);
        assertNotNull(s2);

        assertThrows(NullPointerException.class, () -> new UsuariosSeguridadService(null));
    }
}
