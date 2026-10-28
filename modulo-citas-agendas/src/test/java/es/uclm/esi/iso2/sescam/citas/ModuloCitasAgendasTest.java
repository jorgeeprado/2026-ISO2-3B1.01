package es.uclm.esi.iso2.sescam.citas;

import es.uclm.esi.iso2.sescam.common.enums.EstadoCita;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ModuloCitasAgendasTest {

    @Test
    void testModuloCitasInitialized() {
        assertNotNull(EstadoCita.PROGRAMADA);
    }
}
