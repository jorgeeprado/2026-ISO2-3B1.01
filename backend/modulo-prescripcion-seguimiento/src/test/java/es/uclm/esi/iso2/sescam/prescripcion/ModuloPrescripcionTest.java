package es.uclm.esi.iso2.sescam.prescripcion;

import es.uclm.esi.iso2.sescam.common.enums.EstadoSolicitud;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ModuloPrescripcionTest {

    @Test
    void testModuloPrescripcionInitialized() {
        assertNotNull(EstadoSolicitud.PENDIENTE_CITACION);
    }
}

