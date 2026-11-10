package es.uclm.esi.iso2.sescam.usuarios.persistencia;

import es.uclm.esi.iso2.sescam.common.dto.PacienteDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias :: PacienteRepositoryInMemory (Capa de Persistencia / Simulación DAO)")
class PacienteRepositoryInMemoryTest {

    private PacienteRepositoryInMemory repository;

    @BeforeEach
    void setUp() {
        repository = new PacienteRepositoryInMemory();
    }

    @Test
    @DisplayName("Debe guardar y recuperar un paciente por su ID")
    void testGuardarYBuscarPorId() {
        PacienteDTO p = new PacienteDTO(
                "PAC-001", "CIP-001", "12345678Z", "Laura", "Gómez",
                LocalDate.of(1993, 4, 12), "600123456", "laura@example.es", true
        );

        PacienteDTO guardado = repository.guardar(p);
        assertNotNull(guardado);
        assertEquals("PAC-001", guardado.getId());

        Optional<PacienteDTO> encontrado = repository.buscarPorId("PAC-001");
        assertTrue(encontrado.isPresent());
        assertEquals("Laura", encontrado.get().getNombre());
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si el paciente o su ID son nulos o vacíos")
    void testGuardarInvalido() {
        assertThrows(IllegalArgumentException.class, () -> repository.guardar(null));

        PacienteDTO sinId = new PacienteDTO();
        sinId.setId(null);
        assertThrows(IllegalArgumentException.class, () -> repository.guardar(sinId));

        PacienteDTO idEspacios = new PacienteDTO();
        idEspacios.setId("   ");
        assertThrows(IllegalArgumentException.class, () -> repository.guardar(idEspacios));
    }

    @Test
    @DisplayName("Debe buscar correctamente por CIP de forma insensible a mayúsculas")
    void testBuscarPorCip() {
        PacienteDTO p = new PacienteDTO(
                "PAC-002", "CIP-CLM-9988", "88776655B", "David", "Soria",
                LocalDate.of(1988, 8, 8), null, null, true
        );
        repository.guardar(p);

        Optional<PacienteDTO> encontrado = repository.buscarPorCip("cip-clm-9988");
        assertTrue(encontrado.isPresent());
        assertEquals("David", encontrado.get().getNombre());

        assertFalse(repository.buscarPorCip("CIP-INEXISTENTE").isPresent());
        assertFalse(repository.buscarPorCip(null).isPresent());
        assertFalse(repository.buscarPorCip("  ").isPresent());
    }

    @Test
    @DisplayName("Debe buscar correctamente por DNI")
    void testBuscarPorDni() {
        PacienteDTO p = new PacienteDTO(
                "PAC-003", "CIP-003", "77665544C", "Sara", "Molina",
                LocalDate.of(2000, 10, 10), null, null, true
        );
        repository.guardar(p);

        Optional<PacienteDTO> encontrado = repository.buscarPorDni("77665544c");
        assertTrue(encontrado.isPresent());
        assertEquals("Sara", encontrado.get().getNombre());

        assertFalse(repository.buscarPorDni("00000000T").isPresent());
        assertFalse(repository.buscarPorDni(null).isPresent());
        assertFalse(repository.buscarPorDni("").isPresent());
    }

    @Test
    @DisplayName("Debe buscar por ID o CIP indistintamente")
    void testBuscarPorIdOCip() {
        PacienteDTO p = new PacienteDTO(
                "PAC-004", "CIP-004", "11223344D", "Pablo", "Sanz",
                LocalDate.of(1978, 12, 1), null, null, true
        );
        repository.guardar(p);

        assertTrue(repository.buscarPorIdOCip("PAC-004").isPresent());
        assertTrue(repository.buscarPorIdOCip("CIP-004").isPresent());
        assertFalse(repository.buscarPorIdOCip("DESCONOCIDO").isPresent());
        assertFalse(repository.buscarPorIdOCip(null).isPresent());
        assertFalse(repository.buscarPorIdOCip("  ").isPresent());
    }

    @Test
    @DisplayName("Debe diferenciar entre listar todos y listar únicamente activos (RN-08)")
    void testListarTodosYListarActivos() {
        PacienteDTO pActivo = new PacienteDTO("P1", "C1", "D1", "A", "A", LocalDate.now(), null, null, true);
        PacienteDTO pBajaLogica = new PacienteDTO("P2", "C2", "D2", "B", "B", LocalDate.now(), null, null, false);

        repository.guardar(pActivo);
        repository.guardar(pBajaLogica);

        List<PacienteDTO> todos = repository.listarTodos();
        assertEquals(2, todos.size(), "Listar todos debe devolver ambos registros");

        List<PacienteDTO> activos = repository.listarActivos();
        assertEquals(1, activos.size(), "Listar activos solo debe incluir pacientes con activo = true");
        assertEquals("P1", activos.get(0).getId());
    }

    @Test
    @DisplayName("Debe comprobar correctamente la existencia por CIP o DNI")
    void testExistePorCipYPorDni() {
        PacienteDTO p = new PacienteDTO("P10", "CIP-10", "55443322E", "Raquel", "Alba", LocalDate.now(), null, null, true);
        repository.guardar(p);

        assertTrue(repository.existePorCip("CIP-10"));
        assertTrue(repository.existePorCip("cip-10"));
        assertFalse(repository.existePorCip("CIP-NO"));
        assertFalse(repository.existePorCip(null));
        assertFalse(repository.existePorCip("   "));

        assertTrue(repository.existePorDni("55443322E"));
        assertTrue(repository.existePorDni("55443322e"));
        assertFalse(repository.existePorDni("99999999Z"));
        assertFalse(repository.existePorDni(null));
        assertFalse(repository.existePorDni(""));
    }

    @Test
    @DisplayName("Debe vaciar el almacén al ejecutar limpiar()")
    void testLimpiar() {
        repository.guardar(new PacienteDTO("P1", "C1", "D1", "A", "A", LocalDate.now(), null, null, true));
        assertEquals(1, repository.listarTodos().size());

        repository.limpiar();
        assertTrue(repository.listarTodos().isEmpty());
    }
}

