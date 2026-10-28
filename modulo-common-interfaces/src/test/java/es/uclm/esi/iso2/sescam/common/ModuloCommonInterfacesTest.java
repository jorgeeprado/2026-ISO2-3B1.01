package es.uclm.esi.iso2.sescam.common;

import es.uclm.esi.iso2.sescam.common.dto.PacienteDTO;
import es.uclm.esi.iso2.sescam.common.enums.Prioridad;
import es.uclm.esi.iso2.sescam.common.enums.RolUsuario;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class ModuloCommonInterfacesTest {

    @Test
    void testPacienteDTOCreation() {
        PacienteDTO p = new PacienteDTO("P-001", "CIP123456", "12345678Z", "Juan", "García",
                LocalDate.of(1980, 5, 12), "600112233", "juan@example.com", true);

        assertEquals("P-001", p.getId());
        assertEquals("CIP123456", p.getCip());
        assertEquals("Juan", p.getNombre());
        assertTrue(p.isActivo());
    }

    @Test
    void testEnumsValues() {
        assertEquals(3, Prioridad.values().length);
        assertEquals(4, RolUsuario.values().length);
        assertNotNull(Prioridad.valueOf("URGENTE"));
        assertNotNull(RolUsuario.valueOf("MEDICO"));
    }
}

