package es.uclm.esi.iso2.sescam.analitica;

import es.uclm.esi.iso2.sescam.common.dto.EstadisticaOperativaDTO;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class ModuloAnaliticaTest {

    @Test
    void testModuloAnaliticaInitialized() {
        EstadisticaOperativaDTO dto = new EstadisticaOperativaDTO(
                LocalDate.now(), LocalDate.now().plusDays(7), 10, 8, 2, 4.5, 85.0, 3);
        assertEquals(10, dto.getTotalCitasProgramadas());
        assertEquals(85.0, dto.getPorcentajeOcupacionAgendas());
    }
}

