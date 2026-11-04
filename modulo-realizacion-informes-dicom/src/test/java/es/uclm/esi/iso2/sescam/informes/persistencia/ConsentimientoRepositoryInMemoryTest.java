package es.uclm.esi.iso2.sescam.informes.persistencia;

import es.uclm.esi.iso2.sescam.common.dto.ConsentimientoDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias :: ConsentimientoRepositoryInMemory")
class ConsentimientoRepositoryInMemoryTest {

    private ConsentimientoRepositoryInMemory repository;

    @BeforeEach
    void setUp() {
        repository = new ConsentimientoRepositoryInMemory();
        repository.limpiar();
    }

    @Test
    @DisplayName("Debe guardar y recuperar por ID un consentimiento")
    void testGuardarYBuscarPorId() {
        ConsentimientoDTO dto = new ConsentimientoDTO(
                "CONS-01", "PAC-01", "PRU-01", LocalDateTime.now(),
                true, true, false, false, 80.0, "hashABC"
        );

        repository.guardar(dto);

        Optional<ConsentimientoDTO> encontrado = repository.buscarPorId("CONS-01");
        assertTrue(encontrado.isPresent());
        assertEquals("PAC-01", encontrado.get().getPacienteId());
        assertEquals("PRU-01", encontrado.get().getTipoPruebaId());
    }

    @Test
    @DisplayName("Debe buscar correctamente por pacienteId y tipoPruebaId")
    void testBuscarPorPacienteYPrueba() {
        ConsentimientoDTO dto = new ConsentimientoDTO(
                "CONS-02", "PAC-02", "PRU-TAC", LocalDateTime.now(),
                true, true, false, false, 75.0, "hashXYZ"
        );
        repository.guardar(dto);

        Optional<ConsentimientoDTO> result = repository.buscarPorPacienteYPrueba("PAC-02", "PRU-TAC");
        assertTrue(result.isPresent());
        assertEquals("CONS-02", result.get().getId());

        assertTrue(repository.buscarPorPacienteYPrueba("PAC-02", "OTRA_PRUEBA").isEmpty());
        assertTrue(repository.buscarPorPacienteYPrueba(null, "PRU-TAC").isEmpty());
        assertTrue(repository.buscarPorPacienteYPrueba("PAC-02", null).isEmpty());
    }

    @Test
    @DisplayName("Debe listar todos los consentimientos de un paciente")
    void testListarPorPaciente() {
        ConsentimientoDTO c1 = new ConsentimientoDTO("C1", "PAC-10", "P1", LocalDateTime.now(), true, true, false, false, 90.0, "h1");
        ConsentimientoDTO c2 = new ConsentimientoDTO("C2", "PAC-10", "P2", LocalDateTime.now(), true, true, false, false, 90.0, "h2");
        ConsentimientoDTO c3 = new ConsentimientoDTO("C3", "PAC-99", "P1", LocalDateTime.now(), true, true, false, false, 90.0, "h3");

        repository.guardar(c1);
        repository.guardar(c2);
        repository.guardar(c3);

        List<ConsentimientoDTO> list = repository.listarPorPaciente("PAC-10");
        assertEquals(2, list.size());
        assertTrue(repository.listarPorPaciente(null).isEmpty());
    }

    @Test
    @DisplayName("Debe verificar si existe consentimiento válido")
    void testExisteConsentimientoValido() {
        ConsentimientoDTO cValido = new ConsentimientoDTO("C1", "PAC-A", "PRU-A", LocalDateTime.now(), true, true, false, false, 90.0, "h1");
        ConsentimientoDTO cInvalido = new ConsentimientoDTO("C2", "PAC-B", "PRU-B", LocalDateTime.now(), false, true, false, false, 90.0, "h2");

        repository.guardar(cValido);
        repository.guardar(cInvalido);

        assertTrue(repository.existeConsentimientoValido("PAC-A", "PRU-A"));
        assertFalse(repository.existeConsentimientoValido("PAC-B", "PRU-B"));
        assertFalse(repository.existeConsentimientoValido("PAC-NO", "PRU-NO"));
    }

    @Test
    @DisplayName("Debe lanzar excepción al intentar guardar nulo")
    void testGuardarNulo() {
        assertThrows(IllegalArgumentException.class, () -> repository.guardar(null));
    }
}

