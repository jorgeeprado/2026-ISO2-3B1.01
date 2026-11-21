package es.uclm.esi.iso2.sescam.citas.dominio;

import es.uclm.esi.iso2.sescam.common.dto.CitaDTO;
import es.uclm.esi.iso2.sescam.common.enums.EstadoCita;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CitaServiceImplTest {

    private CitaServiceImpl citaService;

    @BeforeEach
    void setUp() {
        citaService = new CitaServiceImpl();
    }

    @Test
    void programarYObtenerCita() {
        CitaDTO cita = crearCita("cita-1", "paciente-1");

        CitaDTO programada = citaService.programarCita(cita);

        assertNotSame(cita, programada);
        assertEquals(cita.getId(), programada.getId());
        assertEquals(cita.getPacienteId(), citaService.obtenerCitaPorId("cita-1").getPacienteId());
    }

    @Test
    void programarCitaRechazaIdDuplicado() {
        citaService.programarCita(crearCita("cita-1", "paciente-1"));

        assertThrows(IllegalArgumentException.class,
                () -> citaService.programarCita(crearCita("cita-1", "paciente-2")));
        assertEquals("paciente-1", citaService.obtenerCitaPorId("cita-1").getPacienteId());
    }

    @Test
    void modificarCitaActualizaLaExistente() {
        citaService.programarCita(crearCita("cita-1", "paciente-1"));
        CitaDTO modificada = crearCita("cita-1", "paciente-2");

        CitaDTO resultado = citaService.modificarCita(modificada);

        assertEquals("paciente-2", resultado.getPacienteId());
        assertEquals("paciente-2", citaService.obtenerCitaPorId("cita-1").getPacienteId());
    }

    @Test
    void modificarCitaInexistenteFalla() {
        assertThrows(IllegalArgumentException.class,
                () -> citaService.modificarCita(crearCita("cita-desconocida", "paciente-1")));
    }

    @Test
    void cancelarCitaActualizaEstadoYMotivo() {
        citaService.programarCita(crearCita("cita-1", "paciente-1"));

        assertTrue(citaService.cancelarCita("cita-1", "Paciente solicita cancelación"));

        CitaDTO cancelada = citaService.obtenerCitaPorId("cita-1");
        assertEquals(EstadoCita.CANCELADA, cancelada.getEstado());
        assertEquals("Paciente solicita cancelación", cancelada.getMotivoCancelacion());
    }

    @Test
    void cancelarCitaInexistenteDevuelveFalse() {
        assertFalse(citaService.cancelarCita("cita-desconocida", "Motivo"));
    }

    @Test
    void obtenerCitaInexistenteDevuelveNull() {
        assertNull(citaService.obtenerCitaPorId("cita-desconocida"));
    }

    @Test
    void listarCitasPorPacienteSoloDevuelveSusCitas() {
        CitaDTO primera = crearCita("cita-1", "paciente-1");
        CitaDTO segunda = crearCita("cita-2", "paciente-1");
        citaService.programarCita(primera);
        citaService.programarCita(crearCita("cita-3", "paciente-2"));
        citaService.programarCita(segunda);

        List<CitaDTO> citas = citaService.listarCitasPorPaciente("paciente-1");

        assertEquals(2, citas.size());
        assertTrue(citas.stream().allMatch(cita -> "paciente-1".equals(cita.getPacienteId())));
        assertTrue(citas.stream().map(CitaDTO::getId).toList().containsAll(List.of("cita-1", "cita-2")));
        citas.clear();
        assertEquals(2, citaService.listarCitasPorPaciente("paciente-1").size());
    }

    @Test
    void noPermiteModificarLasCitasAlmacenadasDesdeElExterior() {
        CitaDTO cita = crearCita("cita-1", "paciente-1");
        citaService.programarCita(cita);

        cita.setPacienteId("paciente-alterado");
        citaService.obtenerCitaPorId("cita-1").setPacienteId("paciente-alterado");

        assertEquals("paciente-1", citaService.obtenerCitaPorId("cita-1").getPacienteId());
    }

    @Test
    void validaLosParametrosDeLosMetodos() {
        assertThrows(IllegalArgumentException.class, () -> citaService.programarCita(null));
        assertThrows(IllegalArgumentException.class, () -> citaService.modificarCita(null));
        assertThrows(IllegalArgumentException.class, () -> citaService.obtenerCitaPorId(null));
        assertThrows(IllegalArgumentException.class, () -> citaService.obtenerCitaPorId(" "));
        assertThrows(IllegalArgumentException.class, () -> citaService.listarCitasPorPaciente(null));
        assertThrows(IllegalArgumentException.class, () -> citaService.listarCitasPorPaciente(" "));
        assertThrows(IllegalArgumentException.class, () -> citaService.cancelarCita(null, "Motivo"));
        assertThrows(IllegalArgumentException.class, () -> citaService.cancelarCita("cita-1", null));
        assertThrows(IllegalArgumentException.class, () -> citaService.cancelarCita(" ", "Motivo"));
        assertThrows(IllegalArgumentException.class, () -> citaService.cancelarCita("cita-1", " "));
    }

    @Test
    void validaLosCamposObligatoriosYDuracionDeLaCita() {
        List<Consumer<CitaDTO>> invalidadores = List.of(
                cita -> cita.setId(null),
                cita -> cita.setId(" "),
                cita -> cita.setSolicitudId(null),
                cita -> cita.setSolicitudId(" "),
                cita -> cita.setPacienteId(null),
                cita -> cita.setPacienteId(" "),
                cita -> cita.setTipoPruebaId(null),
                cita -> cita.setTipoPruebaId(" "),
                cita -> cita.setFechaHora(null),
                cita -> cita.setDuracionMinutos(0),
                cita -> cita.setDuracionMinutos(-1),
                cita -> cita.setSalaId(null),
                cita -> cita.setSalaId(" "),
                cita -> cita.setMedicoRadiologoId(null),
                cita -> cita.setMedicoRadiologoId(" "),
                cita -> cita.setEstado(null)
        );

        for (Consumer<CitaDTO> invalidar : invalidadores) {
            CitaDTO citaInvalida = crearCita("cita-1", "paciente-1");
            invalidar.accept(citaInvalida);
            assertThrows(IllegalArgumentException.class, () -> citaService.programarCita(citaInvalida));
        }

        assertTrue(citaService.listarCitasPorPaciente("paciente-1").isEmpty());
    }

    private CitaDTO crearCita(String id, String pacienteId) {
        return new CitaDTO(
                id,
                "solicitud-1",
                pacienteId,
                "TAC",
                LocalDateTime.of(2026, 11, 9, 10, 0),
                30,
                "sala-1",
                "medico-1",
                EstadoCita.PROGRAMADA,
                null
        );
    }

}
