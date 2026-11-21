package es.uclm.esi.iso2.sescam.informes.persistencia;

import es.uclm.esi.iso2.sescam.informes.dominio.RegistroRealizacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias :: RegistroRealizacionRepositoryInMemory")
class RegistroRealizacionRepositoryInMemoryTest {

    private RegistroRealizacionRepositoryInMemory repository;

    @BeforeEach
    void setUp() {
        repository = new RegistroRealizacionRepositoryInMemory();
        repository.limpiar();
    }

    @Test
    @DisplayName("Debe guardar y recuperar por ID, citaId y StudyInstanceUID")
    void testGuardarYBuscar() {
        RegistroRealizacion r = new RegistroRealizacion(
                "REG-01", "CITA-01", "1.2.3.4.5", LocalDateTime.now(),
                "MED-01", true, 5.0, "Sin incidencias", true
        );

        repository.guardar(r);

        Optional<RegistroRealizacion> porId = repository.buscarPorId("REG-01");
        assertTrue(porId.isPresent());
        assertEquals("CITA-01", porId.get().getCitaId());

        Optional<RegistroRealizacion> porCita = repository.buscarPorCitaId("CITA-01");
        assertTrue(porCita.isPresent());
        assertEquals("REG-01", porCita.get().getId());

        Optional<RegistroRealizacion> porUid = repository.buscarPorStudyInstanceUID("1.2.3.4.5");
        assertTrue(porUid.isPresent());
        assertEquals("REG-01", porUid.get().getId());
    }

    @Test
    @DisplayName("Debe listar todos los registros y retornar lista vacía si no hay")
    void testListarTodos() {
        assertEquals(0, repository.listarTodos().size());

        RegistroRealizacion r1 = new RegistroRealizacion("R1", "C1", "UID1", null, "M1", false, 0.0, null, false);
        RegistroRealizacion r2 = new RegistroRealizacion("R2", "C2", "UID2", null, "M2", false, 0.0, null, false);

        repository.guardar(r1);
        repository.guardar(r2);

        List<RegistroRealizacion> todos = repository.listarTodos();
        assertEquals(2, todos.size());
    }

    @Test
    @DisplayName("Debe retornar Optional.empty() para búsquedas con parámetros nulos o inexistentes")
    void testBusquedasNulas() {
        assertTrue(repository.buscarPorId(null).isEmpty());
        assertTrue(repository.buscarPorCitaId(null).isEmpty());
        assertTrue(repository.buscarPorStudyInstanceUID(null).isEmpty());
        assertTrue(repository.buscarPorId("NO_EXISTE").isEmpty());
    }

    @Test
    @DisplayName("Debe lanzar excepción si el registro a guardar es nulo")
    void testGuardarNulo() {
        assertThrows(IllegalArgumentException.class, () -> repository.guardar(null));
    }
}

