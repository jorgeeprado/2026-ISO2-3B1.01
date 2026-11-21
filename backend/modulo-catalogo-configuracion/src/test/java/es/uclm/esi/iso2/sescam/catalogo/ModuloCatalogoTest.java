package es.uclm.esi.iso2.sescam.catalogo;

import es.uclm.esi.iso2.sescam.common.enums.ModalidadPrueba;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ModuloCatalogoTest {

    @Test
    void testModuloCatalogoInitialized() {
        assertNotNull(ModalidadPrueba.RM);
    }
}

