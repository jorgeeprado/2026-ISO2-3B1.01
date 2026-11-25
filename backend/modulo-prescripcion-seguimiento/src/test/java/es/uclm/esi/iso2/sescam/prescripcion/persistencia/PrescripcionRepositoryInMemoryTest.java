package es.uclm.esi.iso2.sescam.prescripcion.persistencia;

import es.uclm.esi.iso2.sescam.common.dto.SolicitudPruebaDTO;
import es.uclm.esi.iso2.sescam.common.enums.EstadoSolicitud;
import es.uclm.esi.iso2.sescam.common.enums.Prioridad;
import es.uclm.esi.iso2.sescam.prescripcion.dominio.Prescripcion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PrescripcionRepositoryInMemoryTest {

    private PrescripcionRepositoryInMemory repository;

    @BeforeEach
    void setUp() {
        repository = new PrescripcionRepositoryInMemory();
    }

    @Test
    void guardarBuscaListaYLimpiarSolicitudes() {
        Prescripcion prescripcion = prescripcion("PRES-1", "PAC-1", "MED-1");

        assertSame(prescripcion, repository.guardar(prescripcion));
        assertEquals(Optional.of(prescripcion), repository.buscarPorId("PRES-1"));
        assertEquals("PRES-1", repository.listarPorPaciente("PAC-1").get(0).getId());
        assertEquals("PRES-1", repository.listarPorMedico("MED-1").get(0).getId());
        assertEquals(1, repository.listarTodos().size());

        repository.limpiar();

        assertTrue(repository.listarTodos().isEmpty());
        assertTrue(repository.buscarPorId("PRES-1").isEmpty());
    }

    @Test
    void rechazaDatosInvalidosYGestionaIdentificadoresNulos() {
        assertThrows(IllegalArgumentException.class, () -> repository.guardar(null));
        assertThrows(IllegalArgumentException.class,
                () -> repository.guardar(prescripcion(" ", "PAC-1", "MED-1")));
        assertTrue(repository.buscarPorId(null).isEmpty());
        assertTrue(repository.buscarPorId("DESCONOCIDO").isEmpty());
        assertTrue(repository.listarPorPaciente(null).isEmpty());
        assertTrue(repository.listarPorMedico(null).isEmpty());
    }

    private Prescripcion prescripcion(String id, String pacienteId, String medicoId) {
        SolicitudPruebaDTO dto = new SolicitudPruebaDTO(id, pacienteId, medicoId, "TAC",
                Prioridad.ORDINARIO, "Control", "Seguimiento",
                LocalDateTime.of(2026, 1, 1, 9, 0), EstadoSolicitud.COMPLETADA, true, 6);
        return Prescripcion.desdeDTO(dto);
    }
}
