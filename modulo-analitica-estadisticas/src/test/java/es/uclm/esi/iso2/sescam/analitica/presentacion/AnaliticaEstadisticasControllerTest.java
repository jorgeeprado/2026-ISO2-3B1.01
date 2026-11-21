package es.uclm.esi.iso2.sescam.analitica.presentacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.uclm.esi.iso2.sescam.common.dto.EstadisticaOperativaDTO;
import es.uclm.esi.iso2.sescam.common.interfaces.IAnaliticaEstadisticasService;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnaliticaEstadisticasControllerTest {

    @Mock
    private IAnaliticaEstadisticasService service;

    @InjectMocks
    private AnaliticaEstadisticasController controller;

    private final LocalDate inicio = LocalDate.of(2026, 11, 1);
    private final LocalDate fin = LocalDate.of(2026, 11, 30);

    @Test
    void validaYDelegaConsultasDeEstadisticas() {
        when(service.consultarEstadisticasPrescripcionMedico("medico-1"))
                .thenReturn(Map.of("totalSolicitudes", 2));
        when(service.generarInformeOperativoAgendas(inicio, fin))
                .thenReturn(new EstadisticaOperativaDTO(inicio, fin, 2, 1, 0, 1.0, 50.0, 1));
        when(service.generarCuadroMandoGlobal()).thenReturn(Map.of("totalCitasProgramadas", 2));

        assertEquals(Map.of("totalSolicitudes", 2),
                controller.consultarEstadisticasPrescripcionMedico("medico-1"));
        assertEquals(2, controller.generarInformeOperativoAgendas(inicio, fin).getTotalCitasProgramadas());
        assertEquals(Map.of("totalCitasProgramadas", 2), controller.generarCuadroMandoGlobal());
        verify(service).consultarEstadisticasPrescripcionMedico("medico-1");
        verify(service).generarInformeOperativoAgendas(inicio, fin);
        verify(service).generarCuadroMandoGlobal();
    }

    @Test
    void rechazaParametrosInvalidosSinDelegar() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.consultarEstadisticasPrescripcionMedico(null));
        assertThrows(IllegalArgumentException.class,
                () -> controller.consultarEstadisticasPrescripcionMedico(" "));
        assertThrows(NullPointerException.class,
                () -> controller.generarInformeOperativoAgendas(null, fin));
        assertThrows(IllegalArgumentException.class,
                () -> controller.generarInformeOperativoAgendas(fin, inicio));
    }
}
