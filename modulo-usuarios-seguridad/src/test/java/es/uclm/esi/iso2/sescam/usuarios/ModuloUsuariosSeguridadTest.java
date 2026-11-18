package es.uclm.esi.iso2.sescam.usuarios;

import es.uclm.esi.iso2.sescam.common.dto.UsuarioDTO;
import es.uclm.esi.iso2.sescam.common.enums.RolUsuario;
import es.uclm.esi.iso2.sescam.common.ports.ISsoClient;
import es.uclm.esi.iso2.sescam.usuarios.dominio.UsuariosSeguridadService;
import es.uclm.esi.iso2.sescam.usuarios.persistencia.UsuarioRepositoryInMemory;
import es.uclm.esi.iso2.sescam.usuarios.presentacion.UsuariosSeguridadController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas de Integración y Humo :: Módulo 5 - Usuarios y Seguridad (CU-18 / RBAC / RN-05)")
class ModuloUsuariosSeguridadTest {

    @Mock
    private ISsoClient ssoClientMock;

    private UsuarioRepositoryInMemory repository;
    private UsuariosSeguridadService service;
    private UsuariosSeguridadController controller;

    @BeforeEach
    void setUp() {
        repository = new UsuarioRepositoryInMemory();
        service = new UsuariosSeguridadService(repository, ssoClientMock);
        controller = new UsuariosSeguridadController(service);
    }

    @Test
    @DisplayName("Verificación de inicialización y constantes de roles RBAC")
    void testModuloUsuariosInitialized() {
        assertNotNull(RolUsuario.ADMIN_SISTEMA);
        assertNotNull(RolUsuario.ADMINISTRATIVO_ADMISION);
        assertNotNull(RolUsuario.MEDICO);
        assertNotNull(RolUsuario.PACIENTE);
    }

    @Test
    @DisplayName("Flujo Integrado E2E CU-18: Login SSO, Asignación de Roles, RBAC y Herencia Médica (RN-05)")
    void testFlujoCompletoRbacYHerenciaMedico() {
        // 1. Autenticación inicial vía SSO federado corporativo (ISsoClient / RNF-04)
        UsuarioDTO userSso = new UsuarioDTO(
                "USU-101", "jorge.prado", "Jorge", "Prado Novillo", "jorge.prado@sescam.es",
                RolUsuario.PACIENTE, true
        );
        when(ssoClientMock.validarTokenFederado("jwt-token-valido")).thenReturn(userSso);

        UsuarioDTO autenticado = controller.autenticarSso("jorge.prado", "jwt-token-valido");
        assertNotNull(autenticado);
        assertEquals("USU-101", autenticado.getId());
        assertEquals(RolUsuario.PACIENTE, autenticado.getRol());

        // 2. Comprobación de permisos como PACIENTE: solo puede autogestionar
        assertTrue(controller.verificarAcceso("USU-101", "CONSULTAR_CITA"));
        assertTrue(controller.verificarAcceso("USU-101", "FIRMAR_CONSENTIMIENTO"));
        assertFalse(controller.verificarAcceso("USU-101", "PRESCRIBIR_PRUEBA"), "Paciente no puede prescribir");
        assertFalse(controller.verificarAcceso("USU-101", "ASIGNAR_CITA"), "Paciente no puede citar formalmente (RN-02)");

        // 3. Asignación de rol facultativo MEDICO (CU-18)
        UsuarioDTO conRolMedico = controller.asignarRol("USU-101", RolUsuario.MEDICO);
        assertEquals(RolUsuario.MEDICO, conRolMedico.getRol());

        // 4. Verificación de permisos de MEDICO y RN-05 (Herencia Funcional):
        // Permisos clínicos específicos del médico
        assertTrue(controller.verificarAcceso("USU-101", "PRESCRIBIR_PRUEBA"));
        assertTrue(controller.verificarAcceso("USU-101", "FIRMAR_INFORME"));
        assertTrue(controller.verificarAcceso("USU-101", "EMITIR_ALERTA_CRITICA"));

        // RN-05: El médico hereda automáticamente todos los permisos del rol PACIENTE a título personal
        assertTrue(controller.verificarAcceso("USU-101", "CONSULTAR_CITA"), "RN-05 exige heredar permisos de PACIENTE");
        assertTrue(controller.verificarAcceso("USU-101", "FIRMAR_CONSENTIMIENTO"), "RN-05 exige heredar permisos de PACIENTE");
        assertTrue(controller.verificarAcceso("USU-101", "CONSULTAR_HISTORIAL"), "RN-05 exige heredar permisos de PACIENTE");

        Set<String> permisosEfectivos = controller.obtenerPermisosUsuario("USU-101");
        assertTrue(permisosEfectivos.contains("PRESCRIBIR_PRUEBA"));
        assertTrue(permisosEfectivos.contains("CONSULTAR_CITA"));

        // 5. Asignación de rol ADMINISTRATIVO_ADMISION (CU-18 / RN-02)
        controller.asignarRol("USU-101", RolUsuario.ADMINISTRATIVO_ADMISION);
        assertTrue(controller.verificarAcceso("USU-101", "ASIGNAR_CITA"));
        assertTrue(controller.verificarAcceso("USU-101", "ADMINISTRAR_AGENDAS"));
        assertFalse(controller.verificarAcceso("USU-101", "PRESCRIBIR_PRUEBA"), "Administrativo no puede prescribir");

        // 6. Desactivación de cuenta: el usuario inactivo pierde inmediatamente todo acceso
        controller.cambiarEstadoUsuario("USU-101", false);
        assertFalse(controller.verificarAcceso("USU-101", "ASIGNAR_CITA"), "Usuario inactivo debe tener acceso denegado");
        assertFalse(controller.verificarAcceso("USU-101", "CONSULTAR_CITA"), "Usuario inactivo debe tener acceso denegado");
    }
}
