package es.uclm.esi.iso2.sescam.prescripcion.persistencia;

import es.uclm.esi.iso2.sescam.common.dto.SolicitudPruebaDTO;
import es.uclm.esi.iso2.sescam.common.enums.EstadoSolicitud;
import es.uclm.esi.iso2.sescam.common.enums.Prioridad;
import es.uclm.esi.iso2.sescam.prescripcion.dominio.Prescripcion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PrescripcionRepositoryInMemoryTest {

    private PrescripcionRepositoryInMemory repository;

    @BeforeEach
    void setUp() {
        repository = new PrescripcionRepositoryInMemory();
    }

    @Test
    void guardaYConsultaPorIdPacienteYMedico() {
        Prescripcion prescripcion = prescripcion("PRES-1", "PAC-1", "MED-1",
                EstadoSolicitud.PENDIENTE_CITACION);

        assertSame(prescripcion, repository.guardar(prescripcion));
        assertEquals(prescripcion, repository.buscarPorId("PRES-1").orElseThrow());
        assertEquals(List.of(prescripcion), repository.listarPorPaciente("PAC-1"));
        assertEquals(List.of(prescripcion), repository.listarPorMedico("MED-1"));
        assertEquals(List.of(prescripcion), repository.listarTodos());
    }

    @Test
    void devuelveVaciosParaIDsDesconocidosYRechazaGuardadoInvalido() {
        assertThrows(IllegalArgumentException.class, () -> repository.guardar(null));
        assertThrows(IllegalArgumentException.class,
                () -> repository.guardar(prescripcion(" ", "PAC-1", "MED-1",
                        EstadoSolicitud.PENDIENTE_CITACION)));
        assertTrue(repository.buscarPorId(null).isEmpty());
        assertTrue(repository.buscarPorId("DESCONOCIDO").isEmpty());
        assertTrue(repository.listarPorPaciente(null).isEmpty());
        assertTrue(repository.listarPorMedico(null).isEmpty());
        assertTrue(repository.listarPorPaciente("PAC-SIN-DATOS").isEmpty());
        assertTrue(repository.listarPorMedico("MED-SIN-DATOS").isEmpty());
        assertTrue(repository.listarTodos().isEmpty());
    }

    @Test
    void limpiarEliminaTodasLasSolicitudes() {
        repository.guardar(prescripcion("PRES-1", "PAC-1", "MED-1",
                EstadoSolicitud.COMPLETADA));

        repository.limpiar();

        assertTrue(repository.listarTodos().isEmpty());
        assertTrue(repository.buscarPorId("PRES-1").isEmpty());
    }

    private Prescripcion prescripcion(String id, String pacienteId, String medicoId,
                                      EstadoSolicitud estado) {
        SolicitudPruebaDTO dto = new SolicitudPruebaDTO(id, pacienteId, medicoId, "TAC",
                Prioridad.ORDINARIO, "Control", "Seguimiento",
                LocalDateTime.of(2026, 1, 1, 9, 0), estado, true, 6);
        return Prescripcion.desdeDTO(dto);
    }
}
