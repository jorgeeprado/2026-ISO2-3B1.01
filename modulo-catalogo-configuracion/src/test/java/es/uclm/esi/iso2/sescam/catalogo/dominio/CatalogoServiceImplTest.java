package es.uclm.esi.iso2.sescam.catalogo.dominio;

import es.uclm.esi.iso2.sescam.common.interfaces.dto.PruebaDiagnosticaDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CatalogoServiceImplTest {

    @Mock
    private Map<String, PruebaDiagnosticaDTO> almacenamiento;

    private CatalogoServiceImpl catalogoService;

    @BeforeEach
    void setUp() {
        catalogoService = new CatalogoServiceImpl();
    }

    @Test
    void registrarPruebaGuardaYDevuelveLaPrueba() {
        PruebaDiagnosticaDTO prueba = crearPrueba("TAC-001", "TAC craneal");

        PruebaDiagnosticaDTO registrada = catalogoService.registrarPrueba(prueba);

        assertSame(prueba, registrada);
        assertEquals(prueba, catalogoService.obtenerPruebaPorId("TAC-001"));
    }

    @Test
    void registrarPruebaActualizaLaPruebaConElMismoId() {
        PruebaDiagnosticaDTO inicial = crearPrueba("TAC-001", "TAC inicial");
        PruebaDiagnosticaDTO actualizada = crearPrueba("TAC-001", "TAC actualizado");
        catalogoService.registrarPrueba(inicial);

        catalogoService.registrarPrueba(actualizada);

        assertEquals(actualizada, catalogoService.obtenerPruebaPorId("TAC-001"));
        assertEquals(1, catalogoService.listarTodasLasPruebas().size());
    }

    @Test
    void obtenerPruebaPorIdDevuelveNullCuandoNoExiste() {
        assertNull(catalogoService.obtenerPruebaPorId("inexistente"));
    }

    @Test
    void listarTodasLasPruebasDevuelveUnaCopiaConTodasLasPruebas() {
        PruebaDiagnosticaDTO tac = crearPrueba("TAC-001", "TAC craneal");
        PruebaDiagnosticaDTO rm = crearPrueba("RM-001", "RM lumbar");
        catalogoService.registrarPrueba(tac);
        catalogoService.registrarPrueba(rm);

        List<PruebaDiagnosticaDTO> pruebas = catalogoService.listarTodasLasPruebas();

        assertEquals(2, pruebas.size());
        assertTrue(pruebas.containsAll(List.of(tac, rm)));
        pruebas.clear();
        assertEquals(2, catalogoService.listarTodasLasPruebas().size());
    }

    @Test
    void eliminarPruebaIndicaSiLaPruebaExistia() {
        catalogoService.registrarPrueba(crearPrueba("TAC-001", "TAC craneal"));

        assertTrue(catalogoService.eliminarPrueba("TAC-001"));
        assertFalse(catalogoService.eliminarPrueba("TAC-001"));
        assertNull(catalogoService.obtenerPruebaPorId("TAC-001"));
    }

    @Test
    void registrarPruebaRechazaPruebaNulaOIdVacio() {
        assertThrows(IllegalArgumentException.class, () -> catalogoService.registrarPrueba(null));
        assertThrows(IllegalArgumentException.class,
                () -> catalogoService.registrarPrueba(crearPrueba(null, "TAC craneal")));
        assertThrows(IllegalArgumentException.class,
                () -> catalogoService.registrarPrueba(crearPrueba(" ", "TAC craneal")));
        assertTrue(catalogoService.listarTodasLasPruebas().isEmpty());
    }

    @Test
    void registrarPruebaGuardaLaPruebaEnElMapConSuId() {
        CatalogoServiceImpl servicioConAlmacenamientoSimulado = new CatalogoServiceImpl(almacenamiento);
        PruebaDiagnosticaDTO prueba = crearPrueba("TAC-001", "TAC craneal");

        PruebaDiagnosticaDTO registrada = servicioConAlmacenamientoSimulado.registrarPrueba(prueba);

        assertSame(prueba, registrada);
        verify(almacenamiento).put("TAC-001", prueba);
    }

    private PruebaDiagnosticaDTO crearPrueba(String id, String nombre) {
        return new PruebaDiagnosticaDTO(id, nombre, "TAC", 30, false);
    }
}
