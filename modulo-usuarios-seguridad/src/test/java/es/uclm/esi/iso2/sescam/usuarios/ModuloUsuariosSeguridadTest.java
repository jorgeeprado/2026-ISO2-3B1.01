package es.uclm.esi.iso2.sescam.usuarios;

import es.uclm.esi.iso2.sescam.common.dto.PacienteDTO;
import es.uclm.esi.iso2.sescam.common.enums.RolUsuario;
import es.uclm.esi.iso2.sescam.usuarios.dominio.UsuariosSeguridadService;
import es.uclm.esi.iso2.sescam.usuarios.persistencia.PacienteRepositoryInMemory;
import es.uclm.esi.iso2.sescam.usuarios.presentacion.PacienteController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas de Integración y Humo :: Módulo 5 - Usuarios y Seguridad (CU-12 / RN-08)")
class ModuloUsuariosSeguridadTest {

    private PacienteRepositoryInMemory repository;
    private UsuariosSeguridadService service;
    private PacienteController controller;

    @BeforeEach
    void setUp() {
        repository = new PacienteRepositoryInMemory();
        service = new UsuariosSeguridadService(repository);
        controller = new PacienteController(service);
    }

    @Test
    @DisplayName("Verificación de inicialización y enums del módulo")
    void testModuloUsuariosInitialized() {
        assertNotNull(RolUsuario.ADMIN_SISTEMA);
        assertNotNull(RolUsuario.ADMINISTRATIVO_ADMISION);
        assertNotNull(RolUsuario.MEDICO);
        assertNotNull(RolUsuario.PACIENTE);
    }

    @Test
    @DisplayName("Flujo E2E CU-12: Alta, modificación, consulta y baja lógica conforme a RN-08")
    void testFlujoCompletoGestionPacientesCu12() {
        // 1. Alta de paciente
        PacienteDTO nuevo = new PacienteDTO(
                null, null, "12345678Z", "Manuel", "García Prieto",
                LocalDate.of(1987, 7, 14), "611009988", "manuel@correo.es", false
        );

        PacienteDTO creado = controller.darAltaPaciente(nuevo);
        assertNotNull(creado.getId(), "Debe asignarse un ID único");
        assertNotNull(creado.getCip(), "Debe autogenerarse el CIP del SESCAM");
        assertTrue(creado.isActivo(), "El paciente recién dado de alta debe figurar activo");

        // 2. Consulta y verificación de estado activo
        assertTrue(controller.esPacienteActivo(creado.getId()));
        PacienteDTO consultado = controller.obtenerPaciente(creado.getCip());
        assertNotNull(consultado);
        assertEquals("Manuel", consultado.getNombre());

        // 3. Modificación de datos de contacto
        consultado.setTelefono("655112233");
        consultado.setEmail("manuel.nuevo@correo.es");
        PacienteDTO modificado = controller.modificarPaciente(consultado);
        assertEquals("655112233", modificado.getTelefono());
        assertEquals("manuel.nuevo@correo.es", modificado.getEmail());

        // 4. Baja lógica (RN-08): El paciente no se elimina físicamente de la base de datos
        boolean bajaConsolidada = controller.darBajaLogicaPaciente(creado.getId());
        assertTrue(bajaConsolidada, "La baja lógica debe reportar éxito");

        // Verificación de estado inactivo mediante el controlador
        assertFalse(controller.esPacienteActivo(creado.getId()), "El paciente ya no debe figurar activo");

        // Verificación médico-legal (RN-08): El registro sigue existiendo en el repositorio pero con activo = false
        PacienteDTO enBaja = repository.buscarPorId(creado.getId()).orElse(null);
        assertNotNull(enBaja, "RN-08 prohíbe el borrado físico de la historia clínica");
        assertFalse(enBaja.isActivo(), "El campo activo debe persistir como false");
        assertEquals(1, repository.listarTodos().size(), "El expediente permanece en el censo total");
        assertEquals(0, repository.listarActivos().size(), "No debe figurar en el censo de pacientes activos");
    }
}
