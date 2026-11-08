package es.uclm.esi.iso2.sescam.citas.dominio;

import es.uclm.esi.iso2.sescam.common.interfaces.dto.AgendaMedicaDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgendaServiceImplTest {

    private AgendaServiceImpl agendaService;

    @BeforeEach
    void setUp() {
        agendaService = new AgendaServiceImpl();
    }

    @Test
    void crearYObtenerAgenda() {
        AgendaMedicaDTO agenda = crearAgenda("agenda-1", "medico-1", LocalDate.of(2026, 11, 9));

        assertSame(agenda, agendaService.crearAgenda(agenda));
        assertEquals(agenda, agendaService.obtenerAgendaPorId("agenda-1"));
    }

    @Test
    void crearAgendaConIdExistenteReemplazaLaAgenda() {
        AgendaMedicaDTO original = crearAgenda("agenda-1", "medico-1", LocalDate.of(2026, 11, 9));
        AgendaMedicaDTO actualizada = crearAgenda("agenda-1", "medico-2", LocalDate.of(2026, 11, 10));
        agendaService.crearAgenda(original);

        agendaService.crearAgenda(actualizada);

        assertEquals(actualizada, agendaService.obtenerAgendaPorId("agenda-1"));
        assertEquals(1, agendaService.listarAgendasPorMedico("medico-2").size());
        assertTrue(agendaService.listarAgendasPorMedico("medico-1").isEmpty());
    }

    @Test
    void obtenerAgendaDevuelveNullSiNoExiste() {
        assertNull(agendaService.obtenerAgendaPorId("inexistente"));
    }

    @Test
    void listaAgendasPorMedicoYFecha() {
        LocalDate fecha = LocalDate.of(2026, 11, 9);
        AgendaMedicaDTO delMedico = crearAgenda("agenda-1", "medico-1", fecha);
        AgendaMedicaDTO otroMedico = crearAgenda("agenda-2", "medico-2", fecha);
        AgendaMedicaDTO otraFecha = crearAgenda("agenda-3", "medico-1", fecha.plusDays(1));
        agendaService.crearAgenda(delMedico);
        agendaService.crearAgenda(otroMedico);
        agendaService.crearAgenda(otraFecha);

        List<AgendaMedicaDTO> medicoAgendas = agendaService.listarAgendasPorMedico("medico-1");
        assertEquals(2, medicoAgendas.size());
        assertTrue(medicoAgendas.containsAll(List.of(delMedico, otraFecha)));
        List<AgendaMedicaDTO> agendasFecha = agendaService.listarAgendasPorFecha(fecha);
        assertEquals(2, agendasFecha.size());
        assertTrue(agendasFecha.containsAll(List.of(delMedico, otroMedico)));
        assertTrue(agendaService.listarAgendasPorFecha(fecha.plusDays(2)).isEmpty());
    }

    @Test
    void lasListasDevueltasNoPermitenModificarElAlmacenamiento() {
        agendaService.crearAgenda(crearAgenda("agenda-1", "medico-1", LocalDate.of(2026, 11, 9)));

        List<AgendaMedicaDTO> agendas = agendaService.listarAgendasPorMedico("medico-1");
        agendas.clear();

        assertEquals(1, agendaService.listarAgendasPorMedico("medico-1").size());
    }

    @Test
    void eliminarAgendaIndicaSiExistia() {
        agendaService.crearAgenda(crearAgenda("agenda-1", "medico-1", LocalDate.of(2026, 11, 9)));

        assertTrue(agendaService.eliminarAgenda("agenda-1"));
        assertFalse(agendaService.eliminarAgenda("agenda-1"));
        assertNull(agendaService.obtenerAgendaPorId("agenda-1"));
    }

    @Test
    void rechazaAgendaNulaEIntervaloNoPositivo() {
        assertThrows(IllegalArgumentException.class, () -> agendaService.crearAgenda(null));
        assertThrows(IllegalArgumentException.class,
                () -> agendaService.crearAgenda(new AgendaMedicaDTO(
                        "agenda-1", "medico-1", null, LocalDate.now(),
                        LocalTime.of(9, 0), LocalTime.of(10, 0), 0)));
        assertThrows(IllegalArgumentException.class,
                () -> agendaService.crearAgenda(new AgendaMedicaDTO(
                        "agenda-2", "medico-1", null, LocalDate.now(),
                        LocalTime.of(9, 0), LocalTime.of(10, 0), -5)));
    }

    @Test
    void rechazaParametrosDeConsultaNulosOVacios() {
        assertThrows(IllegalArgumentException.class, () -> agendaService.obtenerAgendaPorId(null));
        assertThrows(IllegalArgumentException.class, () -> agendaService.obtenerAgendaPorId(" "));
        assertThrows(IllegalArgumentException.class, () -> agendaService.listarAgendasPorMedico(null));
        assertThrows(IllegalArgumentException.class, () -> agendaService.listarAgendasPorMedico(" "));
        assertThrows(IllegalArgumentException.class, () -> agendaService.listarAgendasPorFecha(null));
        assertThrows(IllegalArgumentException.class, () -> agendaService.eliminarAgenda(null));
        assertThrows(IllegalArgumentException.class, () -> agendaService.eliminarAgenda(" "));
    }

    private AgendaMedicaDTO crearAgenda(String id, String idMedico, LocalDate fecha) {
        return new AgendaMedicaDTO(
                id,
                idMedico,
                null,
                fecha,
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                15
        );
    }
}
