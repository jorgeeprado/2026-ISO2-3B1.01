package es.uclm.esi.iso2.sescam.citas.presentacion;

import es.uclm.esi.iso2.sescam.citas.dominio.ReasignacionHuecosServiceImpl;
import es.uclm.esi.iso2.sescam.common.dto.CitaDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReasignacionHuecosControllerTest {

    @Mock
    private ReasignacionHuecosServiceImpl servicio;

    @Test
    void constructorExigeServicio() {
        assertThrows(NullPointerException.class, () -> new ReasignacionHuecosController(null));
    }

    @Test
    void delegaLaReasignacionDelHueco() {
        ReasignacionHuecosController controlador = new ReasignacionHuecosController(servicio);
        Optional<CitaDTO> resultado = Optional.of(new CitaDTO());
        when(servicio.reasignarHuecoLiberado("F1#11:00", "SOL-CANCELADA")).thenReturn(resultado);

        assertEquals(resultado, controlador.reasignarHuecoLiberado("F1#11:00", "SOL-CANCELADA"));
        verify(servicio).reasignarHuecoLiberado("F1#11:00", "SOL-CANCELADA");
    }
}