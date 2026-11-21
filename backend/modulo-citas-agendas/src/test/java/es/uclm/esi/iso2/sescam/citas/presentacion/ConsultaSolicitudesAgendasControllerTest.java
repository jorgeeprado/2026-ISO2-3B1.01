package es.uclm.esi.iso2.sescam.citas.presentacion;

import es.uclm.esi.iso2.sescam.common.dto.SolicitudPruebaDTO;
import es.uclm.esi.iso2.sescam.common.dto.TurnoAgendaDTO;
import es.uclm.esi.iso2.sescam.common.enums.TipoRecurso;
import es.uclm.esi.iso2.sescam.common.interfaces.ConsultaSolicitudesAgendasService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultaSolicitudesAgendasControllerTest {

	@Mock
	private ConsultaSolicitudesAgendasService consultaService;

	@Test
	void constructorExigeServicio() {
		assertThrows(NullPointerException.class, () -> new ConsultaSolicitudesAgendasController(null));
	}

	@Test
	void delegaConsultaDeSolicitudesYAgendas() {
		ConsultaSolicitudesAgendasController controlador = new ConsultaSolicitudesAgendasController(consultaService);
		LocalDate fecha = LocalDate.of(2026, 11, 20);
		List<SolicitudPruebaDTO> solicitudes = List.of();
		List<TurnoAgendaDTO> turnos = List.of();
		when(consultaService.consultarSolicitudesPorMedico("MED-1")).thenReturn(solicitudes);
		when(consultaService.consultarAgendasDisponibles(TipoRecurso.SALA, "SALA-1", fecha)).thenReturn(turnos);

		assertEquals(solicitudes, controlador.consultarSolicitudesPorMedico("MED-1"));
		assertEquals(turnos, controlador.consultarAgendasDisponibles(TipoRecurso.SALA, "SALA-1", fecha));

		verify(consultaService).consultarSolicitudesPorMedico("MED-1");
		verify(consultaService).consultarAgendasDisponibles(TipoRecurso.SALA, "SALA-1", fecha);
	}
}
