package es.uclm.esi.iso2.sescam.usuarios.persistencia;

import es.uclm.esi.iso2.sescam.common.dto.UsuarioDTO;
import es.uclm.esi.iso2.sescam.common.enums.RolUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias :: UsuarioRepositoryInMemory (Capa de Persistencia CU-18)")
class UsuarioRepositoryInMemoryTest {

    private UsuarioRepositoryInMemory repository;

    @BeforeEach
    void setUp() {
        repository = new UsuarioRepositoryInMemory();
    }

    @Test
    @DisplayName("Debe guardar y recuperar usuario por ID y por Username (insensible a mayúsculas)")
    void testGuardarYBuscar() {
        UsuarioDTO user = new UsuarioDTO(
                "USU-001", "carlos.dueña", "Carlos", "Dueña", "carlos@sescam.es",
                RolUsuario.ADMIN_SISTEMA, true
        );

        UsuarioDTO guardado = repository.guardar(user);
        assertNotNull(guardado);
        assertEquals("USU-001", guardado.getId());

        Optional<UsuarioDTO> porId = repository.buscarPorId("USU-001");
        assertTrue(porId.isPresent());
        assertEquals("carlos.dueña", porId.get().getUsername());

        Optional<UsuarioDTO> porUsername = repository.buscarPorUsername("CARLOS.DUEÑA");
        assertTrue(porUsername.isPresent());
        assertEquals("USU-001", porUsername.get().getId());
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException al intentar guardar usuario nulo o sin ID")
    void testGuardarInvalido() {
        assertThrows(IllegalArgumentException.class, () -> repository.guardar(null));

        UsuarioDTO sinId = new UsuarioDTO();
        assertThrows(IllegalArgumentException.class, () -> repository.guardar(sinId));

        UsuarioDTO idEspacios = new UsuarioDTO();
        idEspacios.setId("   ");
        assertThrows(IllegalArgumentException.class, () -> repository.guardar(idEspacios));
    }

    @Test
    @DisplayName("Debe listar todos los usuarios y filtrar por rol RBAC")
    void testListarTodosYPorRol() {
        UsuarioDTO u1 = new UsuarioDTO("U1", "medico1", "Juan", "López", "j@sescam.es", RolUsuario.MEDICO, true);
        UsuarioDTO u2 = new UsuarioDTO("U2", "medico2", "Sara", "Pérez", "s@sescam.es", RolUsuario.MEDICO, true);
        UsuarioDTO u3 = new UsuarioDTO("U3", "admin1", "Clara", "Roca", "c@sescam.es", RolUsuario.ADMIN_SISTEMA, true);

        repository.guardar(u1);
        repository.guardar(u2);
        repository.guardar(u3);

        List<UsuarioDTO> todos = repository.listarTodos();
        assertEquals(3, todos.size());

        List<UsuarioDTO> medicos = repository.listarPorRol(RolUsuario.MEDICO);
        assertEquals(2, medicos.size());

        List<UsuarioDTO> pacientes = repository.listarPorRol(RolUsuario.PACIENTE);
        assertTrue(pacientes.isEmpty());

        assertTrue(repository.listarPorRol(null).isEmpty());
    }

    @Test
    @DisplayName("Debe comprobar la existencia por username y por ID")
    void testExistePorUsernameYPorId() {
        UsuarioDTO u = new UsuarioDTO("U-100", "pablo.sanchez", "Pablo", "Sánchez", "p@sescam.es", RolUsuario.ADMINISTRATIVO_ADMISION, true);
        repository.guardar(u);

        assertTrue(repository.existePorUsername("pablo.sanchez"));
        assertTrue(repository.existePorUsername("PABLO.SANCHEZ"));
        assertFalse(repository.existePorUsername("desconocido"));
        assertFalse(repository.existePorUsername(null));
        assertFalse(repository.existePorUsername("   "));

        assertTrue(repository.existePorId("U-100"));
        assertFalse(repository.existePorId("U-999"));
        assertFalse(repository.existePorId(null));
        assertFalse(repository.existePorId("  "));
    }

    @Test
    @DisplayName("Debe vaciar el almacén en memoria al ejecutar limpiar()")
    void testLimpiar() {
        repository.guardar(new UsuarioDTO("U1", "u1", "U", "U", "u@sescam.es", RolUsuario.PACIENTE, true));
        assertEquals(1, repository.listarTodos().size());

        repository.limpiar();
        assertTrue(repository.listarTodos().isEmpty());
    }

    @Test
    @DisplayName("Búsquedas con parámetros nulos o en blanco devuelven Optional vacío")
    void testBusquedasVacias() {
        assertFalse(repository.buscarPorId(null).isPresent());
        assertFalse(repository.buscarPorId("  ").isPresent());
        assertFalse(repository.buscarPorUsername(null).isPresent());
        assertFalse(repository.buscarPorUsername("").isPresent());
    }
}
