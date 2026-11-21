package es.uclm.esi.iso2.sescam.usuarios.persistencia;

import es.uclm.esi.iso2.sescam.common.dto.AuditoriaAccesoDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias :: AuditoriaRepositoryInMemory (Append-only / RN-07 / RNF-06)")
class AuditoriaRepositoryInMemoryTest {

    private AuditoriaRepositoryInMemory repository;

    @BeforeEach
    void setUp() {
        repository = new AuditoriaRepositoryInMemory();
    }

    @Test
    @DisplayName("Debe guardar un registro y recuperarlo por ID con copia defensiva")
    void testGuardarYBuscarPorId() {
        LocalDateTime ahora = LocalDateTime.now();
        AuditoriaAccesoDTO dto = new AuditoriaAccesoDTO(
                "AUD-001", ahora, "medico.radiologo", "MEDICO",
                "PAC-100", "LECTURA", "HISTORIA_CLINICA", "192.168.1.10"
        );

        AuditoriaAccesoDTO guardado = repository.guardar(dto);

        assertNotNull(guardado);
        assertEquals("AUD-001", guardado.getId());
        assertEquals("medico.radiologo", guardado.getUsuarioId());
        assertEquals(1, repository.contar());

        Optional<AuditoriaAccesoDTO> opt = repository.buscarPorId("AUD-001");
        assertTrue(opt.isPresent());
        assertEquals("PAC-100", opt.get().getPacienteIdAfectado());
    }

    @Test
    @DisplayName("Debe fallar al guardar si el registro o el ID es nulo")
    void testGuardarRegistroNuloOIdNulo() {
        assertThrows(NullPointerException.class, () -> repository.guardar(null));

        AuditoriaAccesoDTO sinId = new AuditoriaAccesoDTO();
        sinId.setUsuarioId("user1");
        assertThrows(NullPointerException.class, () -> repository.guardar(sinId));
    }

    @Test
    @DisplayName("Debe fallar al guardar si el ID ya existe en el log")
    void testGuardarIdDuplicado() {
        AuditoriaAccesoDTO dto1 = new AuditoriaAccesoDTO(
                "AUD-DUP", LocalDateTime.now(), "medico1", "MEDICO",
                "PAC-1", "LECTURA", "INFORME_DICOM", "127.0.0.1"
        );
        repository.guardar(dto1);

        AuditoriaAccesoDTO dto2 = new AuditoriaAccesoDTO(
                "AUD-DUP", LocalDateTime.now(), "medico2", "MEDICO",
                "PAC-2", "FIRMA", "INFORME_DICOM", "127.0.0.1"
        );

        assertThrows(IllegalArgumentException.class, () -> repository.guardar(dto2));
    }

    @Test
    @DisplayName("Debe filtrar registros por rango de fechas (desde / hasta)")
    void testBuscarFiltroDesdeHasta() {
        LocalDateTime base = LocalDateTime.of(2026, 11, 1, 10, 0);

        repository.guardar(new AuditoriaAccesoDTO("A1", base.minusDays(5), "u1", "MEDICO", "P1", "LECTURA", "REC", "IP"));
        repository.guardar(new AuditoriaAccesoDTO("A2", base, "u2", "MEDICO", "P2", "LECTURA", "REC", "IP"));
        repository.guardar(new AuditoriaAccesoDTO("A3", base.plusDays(5), "u3", "MEDICO", "P3", "LECTURA", "REC", "IP"));

        List<AuditoriaAccesoDTO> enVentana = repository.buscar(base.minusDays(1), base.plusDays(1), null);
        assertEquals(1, enVentana.size());
        assertEquals("A2", enVentana.get(0).getId());

        List<AuditoriaAccesoDTO> soloDesde = repository.buscar(base, null, null);
        assertEquals(2, soloDesde.size());

        List<AuditoriaAccesoDTO> soloHasta = repository.buscar(null, base, null);
        assertEquals(2, soloHasta.size());
    }

    @Test
    @DisplayName("Debe filtrar registros por pacienteId")
    void testBuscarFiltroPaciente() {
        repository.guardar(new AuditoriaAccesoDTO("A1", LocalDateTime.now(), "u1", "MEDICO", "PAC-AAA", "LECTURA", "REC", "IP"));
        repository.guardar(new AuditoriaAccesoDTO("A2", LocalDateTime.now(), "u2", "MEDICO", "PAC-BBB", "LECTURA", "REC", "IP"));

        List<AuditoriaAccesoDTO> pacienteA = repository.buscar(null, null, "PAC-AAA");
        assertEquals(1, pacienteA.size());
        assertEquals("A1", pacienteA.get(0).getId());

        List<AuditoriaAccesoDTO> todos = repository.buscar(null, null, "");
        assertEquals(2, todos.size());
    }

    @Test
    @DisplayName("Debe filtrar registros por usuario y por acción")
    void testBuscarPorUsuarioYAccion() {
        repository.guardar(new AuditoriaAccesoDTO("A1", LocalDateTime.now(), "dr.garcia", "MEDICO", "P1", "LECTURA", "REC", "IP"));
        repository.guardar(new AuditoriaAccesoDTO("A2", LocalDateTime.now(), "dr.garcia", "MEDICO", "P2", "FIRMA", "REC", "IP"));
        repository.guardar(new AuditoriaAccesoDTO("A3", LocalDateTime.now(), "admin1", "ADMIN", "P3", "EXPORTACION", "REC", "IP"));

        List<AuditoriaAccesoDTO> porUsuario = repository.buscarPorUsuario("dr.garcia");
        assertEquals(2, porUsuario.size());

        List<AuditoriaAccesoDTO> usuarioVacio = repository.buscarPorUsuario(null);
        assertTrue(usuarioVacio.isEmpty());

        List<AuditoriaAccesoDTO> porAccion = repository.buscarPorAccion("FIRMA");
        assertEquals(1, porAccion.size());
        assertEquals("A2", porAccion.get(0).getId());

        List<AuditoriaAccesoDTO> accionVacia = repository.buscarPorAccion("   ");
        assertTrue(accionVacia.isEmpty());
    }

    @Test
    @DisplayName("Debe listar todos los registros y permitir contar")
    void testContarYListarTodos() {
        assertEquals(0, repository.contar());
        assertTrue(repository.listarTodos().isEmpty());

        repository.guardar(new AuditoriaAccesoDTO("A1", LocalDateTime.now(), "u1", "ROL", "P1", "ACT", "REC", "IP"));
        assertEquals(1, repository.contar());
        assertEquals(1, repository.listarTodos().size());
    }

    @Test
    @DisplayName("Debe buscar ID inexistente o nulo retornando Optional vacío")
    void testBuscarPorIdInexistente() {
        assertTrue(repository.buscarPorId(null).isEmpty());
        assertTrue(repository.buscarPorId("   ").isEmpty());
        assertTrue(repository.buscarPorId("NO_EXISTE").isEmpty());
    }

    // =========================================================================
    // Cumplimiento estricto de RN-07 / RNF-06 (Prohibición de mutaciones)
    // =========================================================================

    @Test
    @DisplayName("RN-07 / RNF-06: Debe lanzar UnsupportedOperationException al intentar actualizar un registro")
    void testProhibicionActualizar() {
        AuditoriaAccesoDTO dto = new AuditoriaAccesoDTO("A1", LocalDateTime.now(), "u1", "ROL", "P1", "ACT", "REC", "IP");
        repository.guardar(dto);

        UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class,
                () -> repository.actualizar(dto));
        assertTrue(ex.getMessage().contains("RN-07"), "El mensaje debe indicar la infracción de la regla RN-07");
        assertTrue(ex.getMessage().contains("UPDATE"));
    }

    @Test
    @DisplayName("RN-07 / RNF-06: Debe lanzar UnsupportedOperationException al intentar eliminar un registro")
    void testProhibicionEliminar() {
        AuditoriaAccesoDTO dto = new AuditoriaAccesoDTO("A1", LocalDateTime.now(), "u1", "ROL", "P1", "ACT", "REC", "IP");
        repository.guardar(dto);

        UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class,
                () -> repository.eliminar("A1"));
        assertTrue(ex.getMessage().contains("RN-07"), "El mensaje debe indicar la infracción de la regla RN-07");
        assertTrue(ex.getMessage().contains("DELETE"));
    }

    @Test
    @DisplayName("RN-07 / RNF-06: Debe lanzar UnsupportedOperationException al intentar vaciar el log de auditoría")
    void testProhibicionVaciar() {
        UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class,
                () -> repository.vaciar());
        assertTrue(ex.getMessage().contains("TRUNCATE/DELETE"));
    }

    @Test
    @DisplayName("Debe preservar la inmutabilidad interna frente a modificaciones del objeto original (copia defensiva)")
    void testDefensiveCopyInmutabilidad() {
        AuditoriaAccesoDTO dto = new AuditoriaAccesoDTO(
                "A1", LocalDateTime.now(), "u1", "ROL", "P1", "LECTURA", "REC", "IP"
        );
        repository.guardar(dto);

        // Mutar el DTO original externamente
        dto.setAccion("ACCION_MUTADA");
        dto.setUsuarioId("HACKER");

        // El registro persistido en el repositorio no debe verse afectado
        AuditoriaAccesoDTO recuperado = repository.buscarPorId("A1").orElseThrow();
        assertEquals("LECTURA", recuperado.getAccion());
        assertEquals("u1", recuperado.getUsuarioId());
    }

    @Test
    @DisplayName("Las colecciones retornadas deben ser inmutables")
    void testListaRetornadaEsInmutable() {
        repository.guardar(new AuditoriaAccesoDTO("A1", LocalDateTime.now(), "u1", "ROL", "P1", "ACT", "REC", "IP"));
        List<AuditoriaAccesoDTO> lista = repository.listarTodos();

        AuditoriaAccesoDTO intruso = new AuditoriaAccesoDTO("A2", LocalDateTime.now(), "u2", "ROL", "P2", "ACT", "REC", "IP");
        assertThrows(UnsupportedOperationException.class, () -> lista.add(intruso));
    }
}
