package es.uclm.esi.iso2.sescam.usuarios;

import es.uclm.esi.iso2.sescam.common.dto.PacienteDTO;
import es.uclm.esi.iso2.sescam.common.dto.UsuarioDTO;
import es.uclm.esi.iso2.sescam.common.enums.RolUsuario;
import es.uclm.esi.iso2.sescam.common.ports.ISsoClient;
import es.uclm.esi.iso2.sescam.usuarios.dominio.UsuariosSeguridadService;
import es.uclm.esi.iso2.sescam.usuarios.persistencia.PacienteRepositoryInMemory;
import es.uclm.esi.iso2.sescam.usuarios.persistencia.UsuarioRepositoryInMemory;
import es.uclm.esi.iso2.sescam.usuarios.presentacion.PacienteController;
import es.uclm.esi.iso2.sescam.usuarios.presentacion.UsuariosSeguridadController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas de Integración y Humo :: Módulo 5 - Usuarios y Seguridad (CU-12 / CU-18 / RBAC / RN-05 / RN-08)")
class ModuloUsuariosSeguridadTest {

    @Mock
    private ISsoClient ssoClientMock;

    private UsuarioRepositoryInMemory usuarioRepository;
    private PacienteRepositoryInMemory pacienteRepository;
    private UsuariosSeguridadService service;
    private UsuariosSeguridadController controller;
    private PacienteController pacienteController;

    @BeforeEach
    void setUp() {
        usuarioRepository = new UsuarioRepositoryInMemory();
        pacienteRepository = new PacienteRepositoryInMemory();
        service = new UsuariosSeguridadService(usuarioRepository, pacienteRepository, ssoClientMock, null, null);
        controller = new UsuariosSeguridadController(service);
        pacienteController = new PacienteController(service);
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

    @Test
    @DisplayName("Flujo E2E CU-12: Alta, modificación, consulta y baja lógica conforme a RN-08")
    void testFlujoCompletoGestionPacientesCu12() {
        // 1. Alta de paciente
        PacienteDTO nuevo = new PacienteDTO(
                null, null, "12345678Z", "Manuel", "García Prieto",
                LocalDate.of(1987, 7, 14), "611009988", "manuel@correo.es", false
        );

        PacienteDTO creado = pacienteController.darAltaPaciente(nuevo);
        assertNotNull(creado.getId(), "Debe asignarse un ID único");
        assertNotNull(creado.getCip(), "Debe autogenerarse el CIP del SESCAM");
        assertTrue(creado.isActivo(), "El paciente recién dado de alta debe figurar activo");

        // 2. Consulta y verificación de estado activo
        assertTrue(pacienteController.esPacienteActivo(creado.getId()));
        PacienteDTO consultado = pacienteController.obtenerPaciente(creado.getCip());
        assertNotNull(consultado);
        assertEquals("Manuel", consultado.getNombre());

        // 3. Modificación de datos de contacto
        PacienteDTO actualizacion = new PacienteDTO();
        actualizacion.setId(creado.getId());
        actualizacion.setTelefono("622334455");
        actualizacion.setEmail("manuel.garcia@sescam.es");

        PacienteDTO modificado = pacienteController.modificarPaciente(actualizacion);
        assertEquals("622334455", modificado.getTelefono());
        assertEquals("manuel.garcia@sescam.es", modificado.getEmail());
        assertEquals("Manuel", modificado.getNombre(), "Debe conservar los campos no alterados");

        // 4. Baja lógica conforme a RN-08
        boolean bajaExitosa = pacienteController.darBajaLogicaPaciente(creado.getId());
        assertTrue(bajaExitosa);
        assertFalse(pacienteController.esPacienteActivo(creado.getId()), "El paciente debe figurar inactivo tras la baja lógica");
    }
}
