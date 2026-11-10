package es.uclm.esi.iso2.sescam.citas.persistencia;

import es.uclm.esi.iso2.sescam.common.dto.CitaDTO;
import es.uclm.esi.iso2.sescam.common.enums.EstadoCita;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del DAO simulado en memoria de las citas de CU-13 y del registro cita-turno.
 */
class CitaRepositoryInMemoryTest {

    @Test
    void guardaBuscaPorIdYPorSolicitud() {
        CitaRepositoryInMemory repo = new CitaRepositoryInMemory();
        CitaRegistro c1 = new CitaRegistro(cita("CITA-000001", "S1"), "T1", null, null);
        CitaRegistro c2 = new CitaRegistro(cita("CITA-000002", "S1"), "T2", "Salto de orden", null);
        CitaRegistro c3 = new CitaRegistro(cita("CITA-000003", "S2"), "T3", null, null);
        repo.guardar(c1);
        repo.guardar(c2);
        repo.guardar(c3);

        assertEquals("T2", repo.buscarPorId("CITA-000002").orElseThrow().idTurno());
        assertEquals("Salto de orden", repo.buscarPorId("CITA-000002").orElseThrow().motivoAsignacion());
        assertTrue(repo.buscarPorId("NO").isEmpty());
        assertTrue(repo.buscarPorId(null).isEmpty());
        assertEquals(2, repo.buscarPorSolicitud("S1").size());
        assertTrue(repo.buscarPorSolicitud("S9").isEmpty());
        assertThrows(IllegalArgumentException.class, () -> repo.guardar(null));
    }

    @Test
    void guardarReemplazaElRegistroDeLaMismaCita() {
        CitaRepositoryInMemory repo = new CitaRepositoryInMemory();
        repo.guardar(new CitaRegistro(cita("CITA-000001", "S1"), "T1", null, null));
        CitaDTO cancelada = cita("CITA-000001", "S1");
        cancelada.setEstado(EstadoCita.CANCELADA);
        repo.guardar(new CitaRegistro(cancelada, "T1", null, null));

        assertEquals(EstadoCita.CANCELADA, repo.buscarPorId("CITA-000001").orElseThrow().cita().getEstado());
        assertEquals(1, repo.buscarPorSolicitud("S1").size());
    }

    @Test
    void elRegistroGuardaYDevuelveCopiasDeLaCita() {
        CitaDTO original = cita("CITA-000001", "S1");
        CitaRegistro registro = new CitaRegistro(original, "T1", null, null);

        original.setEstado(EstadoCita.CANCELADA);
        CitaDTO devuelta = registro.cita();
        devuelta.setSalaId("otra-sala");

        assertEquals(EstadoCita.PROGRAMADA, registro.cita().getEstado());
        assertEquals("sala-1", registro.cita().getSalaId());
        assertNotSame(registro.cita(), registro.cita());
        assertEquals("CITA-000001", registro.idCita());
        assertEquals("S1", registro.idSolicitud());
    }

    @Test
    void elRegistroValidaCitaYTurno() {
        assertThrows(NullPointerException.class, () -> new CitaRegistro(null, "T1", null, null));
        assertThrows(IllegalArgumentException.class, () -> new CitaRegistro(cita(" ", "S1"), "T1", null, null));
        assertThrows(IllegalArgumentException.class, () -> new CitaRegistro(cita(null, "S1"), "T1", null, null));
        assertThrows(IllegalArgumentException.class, () -> new CitaRegistro(cita("C1", ""), "T1", null, null));
        assertThrows(IllegalArgumentException.class, () -> new CitaRegistro(cita("C1", null), "T1", null, null));
        assertThrows(IllegalArgumentException.class, () -> new CitaRegistro(cita("C1", "S1"), " ", null, null));
        assertThrows(IllegalArgumentException.class, () -> new CitaRegistro(cita("C1", "S1"), null, null, null));
    }

    private static CitaDTO cita(String id, String solicitud) {
        return new CitaDTO(id, solicitud, "pac-1", "TAC", LocalDateTime.of(2026, 11, 16, 9, 0), 30, "sala-1", null,
                EstadoCita.PROGRAMADA, null);
    }
}
