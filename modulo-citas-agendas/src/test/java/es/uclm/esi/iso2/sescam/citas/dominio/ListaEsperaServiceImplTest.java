package es.uclm.esi.iso2.sescam.citas.dominio;

import es.uclm.esi.iso2.sescam.common.dto.ListaEsperaDTO;
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

class ListaEsperaServiceImplTest {

    private ListaEsperaServiceImpl listaEsperaService;

    @BeforeEach
    void setUp() {
        listaEsperaService = new ListaEsperaServiceImpl();
    }

    @Test
    void registraYObtieneUnaEntradaPorId() {
        ListaEsperaDTO entrada = crearEntrada("entrada-1", "paciente-1", "TAC-1");

        ListaEsperaDTO registrada = listaEsperaService.registrarEnListaEspera(entrada);

        assertNotSame(entrada, registrada);
        assertEquals(entrada.getId(), registrada.getId());
        assertEquals(entrada.getPacienteId(),
                listaEsperaService.obtenerEntradaPorId("entrada-1").getPacienteId());
    }

    @Test
    void registrarEntradaDuplicadaFallaYConservaLaOriginal() {
        ListaEsperaDTO original = crearEntrada("entrada-1", "paciente-1", "TAC-1");
        listaEsperaService.registrarEnListaEspera(original);

        assertThrows(IllegalArgumentException.class,
                () -> listaEsperaService.registrarEnListaEspera(
                        crearEntrada("entrada-1", "paciente-2", "RM-1")));
        assertEquals("paciente-1",
                listaEsperaService.obtenerEntradaPorId("entrada-1").getPacienteId());
    }

    @Test
    void eliminarEntradaIndicaSiExistia() {
        listaEsperaService.registrarEnListaEspera(crearEntrada("entrada-1", "paciente-1", "TAC-1"));

        assertTrue(listaEsperaService.eliminarDeListaEspera("entrada-1"));
        assertFalse(listaEsperaService.eliminarDeListaEspera("entrada-1"));
        assertNull(listaEsperaService.obtenerEntradaPorId("entrada-1"));
    }

    @Test
    void obtenerEntradaInexistenteDevuelveNull() {
        assertNull(listaEsperaService.obtenerEntradaPorId("inexistente"));
    }

    @Test
    void listaEntradasPorTipoDePruebaYPaciente() {
        ListaEsperaDTO primera = crearEntrada("entrada-1", "paciente-1", "TAC-1");
        ListaEsperaDTO segunda = crearEntrada("entrada-2", "paciente-1", "RM-1");
        ListaEsperaDTO tercera = crearEntrada("entrada-3", "paciente-2", "TAC-1");
        listaEsperaService.registrarEnListaEspera(primera);
        listaEsperaService.registrarEnListaEspera(segunda);
        listaEsperaService.registrarEnListaEspera(tercera);

        List<ListaEsperaDTO> porTipo = listaEsperaService.listarPorTipoPrueba("TAC-1");
        List<ListaEsperaDTO> porPaciente = listaEsperaService.listarPorPaciente("paciente-1");

        assertEquals(2, porTipo.size());
        assertTrue(porTipo.stream().map(ListaEsperaDTO::getId)
                .toList().containsAll(List.of("entrada-1", "entrada-3")));
        assertEquals(2, porPaciente.size());
        assertTrue(porPaciente.stream().map(ListaEsperaDTO::getId)
                .toList().containsAll(List.of("entrada-1", "entrada-2")));
        assertTrue(listaEsperaService.listarPorTipoPrueba("sin-resultados").isEmpty());
        assertTrue(listaEsperaService.listarPorPaciente("sin-resultados").isEmpty());
    }

    @Test
    void lasListasDevueltasSonIndependientesDelAlmacenamiento() {
        listaEsperaService.registrarEnListaEspera(crearEntrada("entrada-1", "paciente-1", "TAC-1"));

        List<ListaEsperaDTO> resultado = listaEsperaService.listarPorPaciente("paciente-1");
        resultado.get(0).setEstado("CANCELADA");
        resultado.clear();

        assertEquals("PENDIENTE",
                listaEsperaService.obtenerEntradaPorId("entrada-1").getEstado());
        assertEquals(1, listaEsperaService.listarPorPaciente("paciente-1").size());
    }

    @Test
    void noPermiteCambiosExternosEnLaEntradaAlmacenada() {
        ListaEsperaDTO entrada = crearEntrada("entrada-1", "paciente-1", "TAC-1");
        listaEsperaService.registrarEnListaEspera(entrada);

        entrada.setPacienteId("paciente-alterado");
        listaEsperaService.obtenerEntradaPorId("entrada-1").setEstado("CANCELADA");

        ListaEsperaDTO almacenada = listaEsperaService.obtenerEntradaPorId("entrada-1");
        assertEquals("paciente-1", almacenada.getPacienteId());
        assertEquals("PENDIENTE", almacenada.getEstado());
    }

    @Test
    void rechazaEntradaNulaYCamposObligatoriosInvalidos() {
        assertThrows(IllegalArgumentException.class,
                () -> listaEsperaService.registrarEnListaEspera(null));

        List<Consumer<ListaEsperaDTO>> invalidadores = List.of(
                entrada -> entrada.setId(null),
                entrada -> entrada.setId(" "),
                entrada -> entrada.setPacienteId(null),
                entrada -> entrada.setPacienteId(" "),
                entrada -> entrada.setTipoPruebaId(null),
                entrada -> entrada.setTipoPruebaId(" "),
                entrada -> entrada.setFechaInscripcion(null),
                entrada -> entrada.setEstado(null),
                entrada -> entrada.setEstado(" ")
        );
        for (Consumer<ListaEsperaDTO> invalidar : invalidadores) {
            ListaEsperaDTO entrada = crearEntrada("entrada-1", "paciente-1", "TAC-1");
            invalidar.accept(entrada);
            assertThrows(IllegalArgumentException.class,
                    () -> listaEsperaService.registrarEnListaEspera(entrada));
        }
        assertTrue(listaEsperaService.listarPorPaciente("paciente-1").isEmpty());
    }

    @Test
    void rechazaParametrosNulosOVaciosEnLosMetodos() {
        assertThrows(IllegalArgumentException.class, () -> listaEsperaService.eliminarDeListaEspera(null));
        assertThrows(IllegalArgumentException.class, () -> listaEsperaService.eliminarDeListaEspera(" "));
        assertThrows(IllegalArgumentException.class, () -> listaEsperaService.obtenerEntradaPorId(null));
        assertThrows(IllegalArgumentException.class, () -> listaEsperaService.obtenerEntradaPorId(" "));
        assertThrows(IllegalArgumentException.class, () -> listaEsperaService.listarPorTipoPrueba(null));
        assertThrows(IllegalArgumentException.class, () -> listaEsperaService.listarPorTipoPrueba(" "));
        assertThrows(IllegalArgumentException.class, () -> listaEsperaService.listarPorPaciente(null));
        assertThrows(IllegalArgumentException.class, () -> listaEsperaService.listarPorPaciente(" "));
    }

    private ListaEsperaDTO crearEntrada(String id, String pacienteId, String tipoPruebaId) {
        return new ListaEsperaDTO(
                id,
                pacienteId,
                tipoPruebaId,
                LocalDateTime.of(2026, 11, 9, 10, 0),
                "PENDIENTE"
        );
    }
}
