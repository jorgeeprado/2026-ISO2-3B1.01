package es.uclm.esi.iso2.sescam.notificaciones;

import es.uclm.esi.iso2.sescam.common.enums.TipoNotificacion;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ModuloNotificacionesTest {

    @Test
    void testModuloNotificacionesInitialized() {
        assertNotNull(TipoNotificacion.ALERTA_HALLAZGO_CRITICO);
    }
}
