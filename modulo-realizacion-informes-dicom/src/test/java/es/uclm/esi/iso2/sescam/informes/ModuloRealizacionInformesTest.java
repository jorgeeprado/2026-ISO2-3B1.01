package es.uclm.esi.iso2.sescam.informes;

import es.uclm.esi.iso2.sescam.common.enums.EstadoInforme;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ModuloRealizacionInformesTest {

    @Test
    void testModuloInformesInitialized() {
        assertNotNull(EstadoInforme.FIRMADO);
    }
}

