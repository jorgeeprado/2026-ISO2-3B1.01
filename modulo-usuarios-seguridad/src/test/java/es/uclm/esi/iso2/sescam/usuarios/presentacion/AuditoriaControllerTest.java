package es.uclm.esi.iso2.sescam.usuarios.presentacion;

import es.uclm.esi.iso2.sescam.common.dto.AuditoriaAccesoDTO;
import es.uclm.esi.iso2.sescam.common.interfaces.IAuditoriaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias :: AuditoriaController (Capa de Presentación CU-19)")
class AuditoriaControllerTest {

    @Mock
    private IAuditoriaService auditoriaServiceMock;

    private AuditoriaController controller;

    @BeforeEach
    void setUp() {
        controller = new AuditoriaController(auditoriaServiceMock);
    }

    @Test
    @DisplayName("Debe delegar el registro de acceso clínico en IAuditoriaService")
    void testRegistrarAccesoClinico() {
        controller.registrarAccesoClinico(
                "medico.juan", "PAC-123", "FIRMA", "INFORME_DICOM", "192.168.0.50"
        );

        verify(auditoriaServiceMock, times(1)).registrarAccesoClinico(
                "medico.juan", "PAC-123", "FIRMA", "INFORME_DICOM", "192.168.0.50"
        );
    }

    @Test
    @DisplayName("Debe delegar la consulta de auditorías en IAuditoriaService")
    void testConsultarAuditorias() {
        LocalDateTime desde = LocalDateTime.of(2026, 11, 1, 0, 0);
        LocalDateTime hasta = LocalDateTime.of(2026, 11, 30, 23, 59);

        AuditoriaAccesoDTO dto = new AuditoriaAccesoDTO("A1", desde, "u1", "ROL", "P1", "ACT", "REC", "IP");
        when(auditoriaServiceMock.consultarAuditorias(desde, hasta, "P1")).thenReturn(List.of(dto));

        List<AuditoriaAccesoDTO> resultado = controller.consultarAuditorias(desde, hasta, "P1");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("A1", resultado.get(0).getId());
        verify(auditoriaServiceMock, times(1)).consultarAuditorias(desde, hasta, "P1");
    }

    @Test
    @DisplayName("Debe exportar trazas a CSV a través de IAuditoriaService")
    void testExportarAuditoriasCsv() {
        LocalDateTime fecha = LocalDateTime.of(2026, 11, 19, 10, 0, 0);
        AuditoriaAccesoDTO dto = new AuditoriaAccesoDTO("A-99", fecha, "dr.test", "MEDICO", "PAC-9", "LECTURA", "DICOM", "127.0.0.1");
        when(auditoriaServiceMock.consultarAuditorias(null, null, null)).thenReturn(List.of(dto));

        String csv = controller.exportarAuditoriasCsv(null, null, null);

        assertNotNull(csv);
        assertTrue(csv.contains("ID,TIMESTAMP,USUARIO_ID,ROL_USUARIO,PACIENTE_ID,ACCION,RECURSO,IP_ORIGEN"));
        assertTrue(csv.contains("A-99,2026-11-19T10:00:00,dr.test,MEDICO,PAC-9,LECTURA,DICOM,127.0.0.1"));
    }

    @Test
    @DisplayName("Debe consultar trazas por usuario")
    void testConsultarAuditoriasPorUsuario() {
        AuditoriaAccesoDTO d1 = new AuditoriaAccesoDTO("A1", LocalDateTime.now(), "u1", "MEDICO", "P1", "LECTURA", "REC", "IP");
        AuditoriaAccesoDTO d2 = new AuditoriaAccesoDTO("A2", LocalDateTime.now(), "u2", "MEDICO", "P2", "FIRMA", "REC", "IP");

        when(auditoriaServiceMock.consultarAuditorias(null, null, null)).thenReturn(List.of(d1, d2));

        List<AuditoriaAccesoDTO> porUsuario = controller.consultarAuditoriasPorUsuario("u1");
        assertEquals(1, porUsuario.size());
        assertEquals("A1", porUsuario.get(0).getId());

        assertTrue(controller.consultarAuditoriasPorUsuario(null).isEmpty());
    }

    @Test
    @DisplayName("Debe consultar trazas por acción clínica")
    void testConsultarAuditoriasPorAccion() {
        AuditoriaAccesoDTO d1 = new AuditoriaAccesoDTO("A1", LocalDateTime.now(), "u1", "MEDICO", "P1", "LECTURA", "REC", "IP");
        AuditoriaAccesoDTO d2 = new AuditoriaAccesoDTO("A2", LocalDateTime.now(), "u2", "MEDICO", "P2", "FIRMA", "REC", "IP");

        when(auditoriaServiceMock.consultarAuditorias(null, null, null)).thenReturn(List.of(d1, d2));

        List<AuditoriaAccesoDTO> porAccion = controller.consultarAuditoriasPorAccion("FIRMA");
        assertEquals(1, porAccion.size());
        assertEquals("A2", porAccion.get(0).getId());

        assertTrue(controller.consultarAuditoriasPorAccion(null).isEmpty());
    }

    @Test
    @DisplayName("RN-07 / RNF-06: Debe bloquear modificación y eliminación devolviendo false")
    void testBloqueoModificacionYEliminacion() {
        AuditoriaAccesoDTO dto = new AuditoriaAccesoDTO("A1", LocalDateTime.now(), "u", "R", "P", "A", "REC", "IP");

        assertFalse(controller.intentarModificarRegistro(dto));
        assertFalse(controller.intentarEliminarRegistro("A1"));
    }

    @Test
    @DisplayName("Debe fallar al instanciar con servicio nulo")
    void testConstructorNulo() {
        assertThrows(NullPointerException.class, () -> new AuditoriaController(null));
    }
}
