package es.uclm.esi.iso2.sescam.usuarios.dominio;

import es.uclm.esi.iso2.sescam.common.dto.PacienteDTO;
import es.uclm.esi.iso2.sescam.common.dto.UsuarioDTO;
import es.uclm.esi.iso2.sescam.common.enums.RolUsuario;
import es.uclm.esi.iso2.sescam.common.interfaces.IAuditoriaService;
import es.uclm.esi.iso2.sescam.common.ports.ISisAdmisionClient;
import es.uclm.esi.iso2.sescam.common.ports.ISsoClient;
import es.uclm.esi.iso2.sescam.usuarios.persistencia.IPacienteRepository;
import es.uclm.esi.iso2.sescam.usuarios.persistencia.IUsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias :: UsuariosSeguridadService (CU-12 / CU-18 / RBAC / RN-05 / ISsoClient)")
class UsuariosSeguridadServiceTest {

    @Mock
    private IUsuarioRepository usuarioRepositoryMock;

    @Mock
    private ISsoClient ssoClientMock;

    @Mock
    private IPacienteRepository pacienteRepositoryMock;

    @Mock
    private ISisAdmisionClient sisAdmisionClientMock;

    @Mock
    private IAuditoriaService auditoriaServiceMock;

    private UsuariosSeguridadService service;

    @BeforeEach
    void setUp() {
        service = new UsuariosSeguridadService(usuarioRepositoryMock, ssoClientMock, auditoriaServiceMock);
    }

    // =========================================================================
    // 1. Gestión de cuentas de usuario (Alta / Modificación / Consulta) - CU-18
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
    @DisplayName("Debe lanzar UsuarioYaExisteException al registrar username duplicado")
    void testRegistrarUsuarioDuplicado() {
        UsuarioDTO input = new UsuarioDTO("U1", "jorge.prado", "Jorge", "Prado", null, RolUsuario.PACIENTE, true);
        when(usuarioRepositoryMock.existePorUsername("jorge.prado")).thenReturn(true);

        assertThrows(UsuarioYaExisteException.class, () -> service.registrarUsuario(input));
        verify(usuarioRepositoryMock, never()).guardar(any());
    }

    @Test
    @DisplayName("Debe validar campos requeridos del usuario al registrar")
    void testRegistrarUsuarioValidaciones() {
        assertThrows(UsuarioInvalidoException.class, () -> service.registrarUsuario(null));

        UsuarioDTO sinUsername = new UsuarioDTO("U1", "", "Nombre", "Apellidos", null, RolUsuario.PACIENTE, true);
        assertThrows(UsuarioInvalidoException.class, () -> service.registrarUsuario(sinUsername));

        UsuarioDTO sinNombre = new UsuarioDTO("U1", "user", "  ", "Apellidos", null, RolUsuario.PACIENTE, true);
        assertThrows(UsuarioInvalidoException.class, () -> service.registrarUsuario(sinNombre));

        UsuarioDTO sinApellidos = new UsuarioDTO("U1", "user", "Nombre", "", null, RolUsuario.PACIENTE, true);
        assertThrows(UsuarioInvalidoException.class, () -> service.registrarUsuario(sinApellidos));

        UsuarioDTO sinRol = new UsuarioDTO("U1", "user", "Nombre", "Apellidos", null, null, true);
        assertThrows(UsuarioInvalidoException.class, () -> service.registrarUsuario(sinRol));
    }

    @Test
    @DisplayName("Debe modificar datos de un usuario existente")
    void testModificarUsuarioExitoso() {
        UsuarioDTO existente = new UsuarioDTO("USU-1", "antiguo", "Pedro", "Fuente", "p@sescam.es", RolUsuario.PACIENTE, true);
        when(usuarioRepositoryMock.buscarPorId("USU-1")).thenReturn(Optional.of(existente));
        when(usuarioRepositoryMock.guardar(any(UsuarioDTO.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioDTO cambios = new UsuarioDTO("USU-1", "nuevo.user", "Pedro", "Fuente Mod", "nuevo@sescam.es", RolUsuario.MEDICO, true);
        when(usuarioRepositoryMock.buscarPorUsername("nuevo.user")).thenReturn(Optional.empty());

        UsuarioDTO modificado = service.modificarUsuario(cambios);

        assertNotNull(modificado);
        assertEquals("nuevo.user", modificado.getUsername());
        assertEquals("nuevo@sescam.es", modificado.getEmail());
        assertEquals(RolUsuario.MEDICO, modificado.getRol());
        verify(usuarioRepositoryMock, times(1)).guardar(existente);
    }

    @Test
    @DisplayName("Debe lanzar UsuarioNoEncontradoException al modificar usuario inexistente")
    void testModificarUsuarioNoEncontrado() {
        when(usuarioRepositoryMock.buscarPorId("INEXISTENTE")).thenReturn(Optional.empty());

        UsuarioDTO dto = new UsuarioDTO("INEXISTENTE", "user", "A", "B", null, RolUsuario.PACIENTE, true);
        assertThrows(UsuarioNoEncontradoException.class, () -> service.modificarUsuario(dto));
    }

    @Test
    @DisplayName("Debe consultar usuarios por ID, username y listar por rol")
    void testConsultasYFiltrosUsuarios() {
        UsuarioDTO u1 = new UsuarioDTO("U-1", "user1", "N1", "A1", null, RolUsuario.MEDICO, true);
        when(usuarioRepositoryMock.buscarPorId("U-1")).thenReturn(Optional.of(u1));
        when(usuarioRepositoryMock.buscarPorUsername("user1")).thenReturn(Optional.of(u1));
        when(usuarioRepositoryMock.listarPorRol(RolUsuario.MEDICO)).thenReturn(List.of(u1));
        when(usuarioRepositoryMock.listarTodos()).thenReturn(List.of(u1));

        assertTrue(service.obtenerUsuarioPorId("U-1").isPresent());
        assertTrue(service.obtenerUsuarioPorUsername("user1").isPresent());
        assertEquals(1, service.listarUsuariosPorRol(RolUsuario.MEDICO).size());
        assertEquals(1, service.listarUsuarios().size());

        assertTrue(service.obtenerUsuarioPorId(null).isEmpty());
        assertTrue(service.obtenerUsuarioPorUsername(" ").isEmpty());
    }

    // =========================================================================
    // 2. Asignación de Roles y Estado de Cuentas (CU-18)
    // =========================================================================

    @Test
    @DisplayName("Debe asignar y cambiar el rol de un usuario existente")
    void testAsignarRolUsuario() {
        UsuarioDTO user = new UsuarioDTO("U-1", "user1", "N", "A", null, RolUsuario.PACIENTE, true);
        when(usuarioRepositoryMock.buscarPorId("U-1")).thenReturn(Optional.of(user));
        when(usuarioRepositoryMock.guardar(any(UsuarioDTO.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioDTO resultado = service.asignarRol("U-1", RolUsuario.MEDICO);

        assertEquals(RolUsuario.MEDICO, resultado.getRol());
        verify(usuarioRepositoryMock, times(1)).guardar(user);
    }

    @Test
    @DisplayName("Debe activar y suspender/desactivar la cuenta de un usuario")
    void testCambiarEstadoUsuario() {
        UsuarioDTO user = new UsuarioDTO("U-1", "user1", "N", "A", null, RolUsuario.PACIENTE, true);
        when(usuarioRepositoryMock.buscarPorId("U-1")).thenReturn(Optional.of(user));
        when(usuarioRepositoryMock.guardar(any(UsuarioDTO.class))).thenAnswer(inv -> inv.getArgument(0));

        boolean baja = service.cambiarEstadoUsuario("U-1", false);
        assertTrue(baja);
        assertFalse(user.isActivo());

        boolean reactivacion = service.cambiarEstadoUsuario("U-1", true);
        assertTrue(reactivacion);
        assertTrue(user.isActivo());
    }

    @Test
    @DisplayName("Debe validar parámetros nulos en asignación de roles y cambio de estado")
    void testValidacionParametrosRolYEstado() {
        assertThrows(IllegalArgumentException.class, () -> service.asignarRol(null, RolUsuario.PACIENTE));
        assertThrows(IllegalArgumentException.class, () -> service.asignarRol("U-1", null));
        assertThrows(IllegalArgumentException.class, () -> service.cambiarEstadoUsuario("", true));
    }

    // =========================================================================
    // 3. Matriz de Control de Acceso RBAC y Herencia Médica (RN-05)
    // =========================================================================

    @Test
    @DisplayName("RBAC Paciente: Solo debe tener permisos autorizados para el paciente")
    void testPermisosRolPaciente() {
        UsuarioDTO paciente = new UsuarioDTO("PAC-1", "paciente", "P", "P", null, RolUsuario.PACIENTE, true);
        when(usuarioRepositoryMock.buscarPorId("PAC-1")).thenReturn(Optional.of(paciente));

        assertTrue(service.verificarPermiso("PAC-1", "CONSULTAR_CITA"));
        assertTrue(service.verificarPermiso("PAC-1", "FIRMAR_CONSENTIMIENTO"));
        assertTrue(service.verificarPermiso("PAC-1", "CHECKLIST_SEGURIDAD"));

        assertFalse(service.verificarPermiso("PAC-1", "PRESCRIBIR_PRUEBA"));
        assertFalse(service.verificarPermiso("PAC-1", "ASIGNAR_CITA"));
        assertFalse(service.verificarPermiso("PAC-1", "ADMIN_USUARIOS"));
    }

    @Test
    @DisplayName("RN-05: El rol MEDICO debe poseer permisos clínicos y HEREDAR los de PACIENTE")
    void testHerenciaFuncionalRolMedico() {
        UsuarioDTO medico = new UsuarioDTO("MED-1", "dr.pablo", "Pablo", "Sanchez", null, RolUsuario.MEDICO, true);
        when(usuarioRepositoryMock.buscarPorId("MED-1")).thenReturn(Optional.of(medico));

        // Permisos propios del médico
        assertTrue(service.verificarPermiso("MED-1", "PRESCRIBIR_PRUEBA"));
        assertTrue(service.verificarPermiso("MED-1", "FIRMAR_INFORME"));
        assertTrue(service.verificarPermiso("MED-1", "EMITIR_ALERTA_CRITICA"));

        // RN-05: Herencia estricta de permisos del rol PACIENTE
        assertTrue(service.verificarPermiso("MED-1", "CONSULTAR_CITA"), "Debe heredar de PACIENTE");
        assertTrue(service.verificarPermiso("MED-1", "SOLICITAR_CAMBIO_CITA"), "Debe heredar de PACIENTE");
        assertTrue(service.verificarPermiso("MED-1", "FIRMAR_CONSENTIMIENTO"), "Debe heredar de PACIENTE");
        assertTrue(service.verificarPermiso("MED-1", "CONSULTAR_HISTORIAL"), "Debe heredar de PACIENTE");

        // Permisos ajenos
        assertFalse(service.verificarPermiso("MED-1", "CONFIG_SISTEMA"));

        Set<String> permisosTotales = service.obtenerPermisosDeUsuario("MED-1");
        assertTrue(permisosTotales.contains("PRESCRIBIR_PRUEBA"));
        assertTrue(permisosTotales.contains("CONSULTAR_CITA"));
    }

    @Test
    @DisplayName("RBAC Administrativo de Admisión: Solo tareas de gestión de agendas y citas")
    void testPermisosRolAdministrativoAdmision() {
        UsuarioDTO adminAdm = new UsuarioDTO("ADM-1", "carlos.adm", "Carlos", "D", null, RolUsuario.ADMINISTRATIVO_ADMISION, true);
        when(usuarioRepositoryMock.buscarPorId("ADM-1")).thenReturn(Optional.of(adminAdm));

        assertTrue(service.verificarPermiso("ADM-1", "ASIGNAR_CITA"));
        assertTrue(service.verificarPermiso("ADM-1", "ADMINISTRAR_AGENDAS"));
        assertTrue(service.verificarPermiso("ADM-1", "GESTION_LISTA_ESPERA"));
        assertTrue(service.verificarPermiso("ADM-1", "ALTA_PACIENTE"));

        assertFalse(service.verificarPermiso("ADM-1", "PRESCRIBIR_PRUEBA"));
        assertFalse(service.verificarPermiso("ADM-1", "FIRMAR_INFORME"));
    }

    @Test
    @DisplayName("RBAC Administrador del Sistema: Acceso pleno al sistema")
    void testPermisosRolAdminSistema() {
        UsuarioDTO sysAdmin = new UsuarioDTO("SYS-1", "admin", "Root", "Admin", null, RolUsuario.ADMIN_SISTEMA, true);
        when(usuarioRepositoryMock.buscarPorId("SYS-1")).thenReturn(Optional.of(sysAdmin));

        assertTrue(service.verificarPermiso("SYS-1", "CONFIG_SISTEMA"));
        assertTrue(service.verificarPermiso("SYS-1", "GESTION_ROLES"));
        assertTrue(service.verificarPermiso("SYS-1", "ASIGNAR_CITA"));
        assertTrue(service.verificarPermiso("SYS-1", "CUALQUIER_ACCION_FUTURA"));
    }

    @Test
    @DisplayName("RBAC: Un usuario inactivo o inexistente no debe tener ningún permiso")
    void testVerificarPermisoUsuarioInactivoOInexistente() {
        UsuarioDTO inactivo = new UsuarioDTO("U-OFF", "off", "O", "O", null, RolUsuario.ADMIN_SISTEMA, false);
        when(usuarioRepositoryMock.buscarPorId("U-OFF")).thenReturn(Optional.of(inactivo));
        when(usuarioRepositoryMock.buscarPorId("NO_EXISTE")).thenReturn(Optional.empty());

        assertFalse(service.verificarPermiso("U-OFF", "CONFIG_SISTEMA"));
        assertFalse(service.verificarPermiso("NO_EXISTE", "CONSULTAR_CITA"));
        assertFalse(service.verificarPermiso(null, "CONSULTAR_CITA"));
        assertFalse(service.verificarPermiso("U-OFF", null));
        assertTrue(service.obtenerPermisosDeUsuario("U-OFF").isEmpty());
    }

    // =========================================================================
    // 4. Autenticación Federada SSO Corporativo (CU-18 / CU-20 / ISsoClient)
    // =========================================================================

    @Test
    @DisplayName("SSO: Debe autenticar y sincronizar usuario con token corporativo válido")
    void testAutenticarUsuarioSsoExitoso() {
        UsuarioDTO ssoUser = new UsuarioDTO(
                "SSO-99", "jorge.prado", "Jorge", "Prado Novillo", "jprado@sescam.es",
                RolUsuario.PACIENTE, true
        );
        when(ssoClientMock.validarTokenFederado("token-jwt-sescam-valido")).thenReturn(ssoUser);
        when(usuarioRepositoryMock.guardar(any(UsuarioDTO.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioDTO autenticado = service.autenticarUsuario("jorge.prado", "token-jwt-sescam-valido");

        assertNotNull(autenticado);
        assertEquals("jorge.prado", autenticado.getUsername());
        assertEquals(RolUsuario.PACIENTE, autenticado.getRol());
        assertTrue(autenticado.isActivo());
        verify(usuarioRepositoryMock, times(1)).guardar(ssoUser);
        verify(auditoriaServiceMock, times(1)).registrarAccesoClinico(
                eq("SISTEMA_SEGURIDAD"), eq(ssoUser.getId()), eq("LOGIN_SSO_EXITOSO"), any(), any()
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
    // 5. Métodos de CU-12 cuando no está inyectado IPacienteRepository
    // =========================================================================

    @Test
    @DisplayName("Debe lanzar UnsupportedOperationException para métodos de CU-12 si no se inyectó pacienteRepository")
    void testMetodosCu12SinRepositorio() {
        assertThrows(UnsupportedOperationException.class, () -> service.darAltaPaciente(new PacienteDTO()));
        assertThrows(UnsupportedOperationException.class, () -> service.modificarPaciente(new PacienteDTO()));
        assertThrows(UnsupportedOperationException.class, () -> service.darBajaLogicaPaciente("PAC-1"));
        assertThrows(UnsupportedOperationException.class, () -> service.obtenerPaciente("PAC-1"));
    }

    // =========================================================================
    // 6. Pruebas de CU-12 (Pacientes) cuando se inyecta IPacienteRepository
    // =========================================================================

    @Test
    @DisplayName("CU-12: Debe dar de alta a un paciente y persistirlo en el repositorio")
    void testDarAltaPaciente() {
        UsuariosSeguridadService servicePacientes = new UsuariosSeguridadService(
                pacienteRepositoryMock, sisAdmisionClientMock, auditoriaServiceMock
        );

        PacienteDTO nuevo = new PacienteDTO(
                null, "CIP-CLM-11223344", "11223344K", "Elena", "Vega",
                LocalDate.of(1991, 2, 2), "600112233", "elena@sescam.es", false
        );

        when(pacienteRepositoryMock.existePorDni("11223344K")).thenReturn(false);
        when(pacienteRepositoryMock.existePorCip("CIP-CLM-11223344")).thenReturn(false);
        when(pacienteRepositoryMock.guardar(any(PacienteDTO.class))).thenAnswer(inv -> inv.getArgument(0));

        PacienteDTO resultado = servicePacientes.darAltaPaciente(nuevo);

        assertNotNull(resultado);
        assertNotNull(resultado.getId());
        assertTrue(resultado.getId().startsWith("PAC-"));
        assertTrue(resultado.isActivo());
        verify(pacienteRepositoryMock, times(1)).guardar(nuevo);
    }

    @Test
    @DisplayName("CU-12: Debe modificar los datos de un paciente existente")
    void testModificarPaciente() {
        UsuariosSeguridadService servicePacientes = new UsuariosSeguridadService(
                pacienteRepositoryMock, sisAdmisionClientMock, auditoriaServiceMock
        );

        PacienteDTO existente = new PacienteDTO(
                "PAC-001", "CIP-001", "12345678Z", "Elena", "Vega",
                LocalDate.of(1991, 2, 2), null, null, true
        );
        when(pacienteRepositoryMock.buscarPorIdOCip("PAC-001")).thenReturn(Optional.of(existente));
        when(pacienteRepositoryMock.guardar(any(PacienteDTO.class))).thenAnswer(inv -> inv.getArgument(0));

        PacienteDTO cambios = new PacienteDTO();
        cambios.setId("PAC-001");
        cambios.setTelefono("644556677");

        PacienteDTO modificado = servicePacientes.modificarPaciente(cambios);

        assertNotNull(modificado);
        assertEquals("644556677", modificado.getTelefono());
        verify(pacienteRepositoryMock, times(1)).guardar(existente);
    }

    @Test
    @DisplayName("CU-12: RN-08 Debe ejecutar baja lógica marcando activo = false")
    void testDarBajaLogicaPaciente() {
        UsuariosSeguridadService servicePacientes = new UsuariosSeguridadService(
                pacienteRepositoryMock, sisAdmisionClientMock, auditoriaServiceMock
        );

        PacienteDTO existente = new PacienteDTO(
                "PAC-001", "CIP-001", "12345678Z", "Elena", "Vega",
                LocalDate.now(), null, null, true
        );
        when(pacienteRepositoryMock.buscarPorIdOCip("PAC-001")).thenReturn(Optional.of(existente));
        when(pacienteRepositoryMock.guardar(any(PacienteDTO.class))).thenAnswer(inv -> inv.getArgument(0));

        boolean resultado = servicePacientes.darBajaLogicaPaciente("PAC-001");

        assertTrue(resultado);
        assertFalse(existente.isActivo(), "RN-08: El paciente debe quedar inactivo sin borrado físico");
        verify(pacienteRepositoryMock, times(1)).guardar(existente);
    }

    @Test
    @DisplayName("CU-12: Debe obtener un paciente localmente o vía SisAdmision")
    void testObtenerPaciente() {
        UsuariosSeguridadService servicePacientes = new UsuariosSeguridadService(
                pacienteRepositoryMock, sisAdmisionClientMock, auditoriaServiceMock
        );

        PacienteDTO paciente = new PacienteDTO("PAC-001", "CIP-001", "12345678Z", "Elena", "Vega", LocalDate.now(), null, null, true);
        when(pacienteRepositoryMock.buscarPorIdOCip("PAC-001")).thenReturn(Optional.of(paciente));

        PacienteDTO resultado = servicePacientes.obtenerPaciente("PAC-001");

        assertNotNull(resultado);
        assertEquals("PAC-001", resultado.getId());
        verify(pacienteRepositoryMock, times(1)).buscarPorIdOCip("PAC-001");
    }

    @Test
    @DisplayName("Debe lanzar UnsupportedOperationException en métodos de CU-18 si solo se inyectó pacienteRepository")
    void testMetodosCu18SinUsuarioRepository() {
        UsuariosSeguridadService servicePacientes = new UsuariosSeguridadService(pacienteRepositoryMock);

        UnsupportedOperationException exSso = assertThrows(
                UnsupportedOperationException.class,
                () -> servicePacientes.autenticarUsuario("admin", "token-sso")
        );
        assertTrue(exSso.getMessage().contains("CU-18"));

        UnsupportedOperationException exRbac = assertThrows(
                UnsupportedOperationException.class,
                () -> servicePacientes.verificarPermiso("USU-01", "ACCION_PRUEBA")
        );
        assertTrue(exRbac.getMessage().contains("CU-18"));
    }

    @Test
    @DisplayName("Debe inicializar correctamente constructores sobrecargados")
    void testConstructoresSobrecargados() {
        UsuariosSeguridadService s1 = new UsuariosSeguridadService(usuarioRepositoryMock);
        assertNotNull(s1);

        UsuariosSeguridadService s2 = new UsuariosSeguridadService(usuarioRepositoryMock, ssoClientMock);
        assertNotNull(s2);

        UsuariosSeguridadService s3 = new UsuariosSeguridadService(pacienteRepositoryMock);
        assertNotNull(s3);

        UsuariosSeguridadService s4 = new UsuariosSeguridadService(pacienteRepositoryMock, sisAdmisionClientMock);
        assertNotNull(s4);

        assertThrows(NullPointerException.class, () -> new UsuariosSeguridadService((IUsuarioRepository) null));
        assertThrows(NullPointerException.class, () -> new UsuariosSeguridadService((IPacienteRepository) null));
    }
}
