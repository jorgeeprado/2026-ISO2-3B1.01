package es.uclm.esi.iso2.sescam.usuarios.dominio;

import es.uclm.esi.iso2.sescam.common.dto.AuditoriaAccesoDTO;
import es.uclm.esi.iso2.sescam.usuarios.persistencia.IAuditoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias :: AuditoriaService (CU-19 / RN-07 / RNF-06 / Inmutabilidad)")
class AuditoriaServiceTest {

    @Mock
    private IAuditoriaRepository auditoriaRepositoryMock;

    private AuditoriaService service;

    @BeforeEach
    void setUp() {
        service = new AuditoriaService(auditoriaRepositoryMock);
    }

    // =========================================================================
    // 1. Registro síncrono de eventos de auditoría clínica
    // =========================================================================

    @Test
    @DisplayName("Debe registrar síncronamente un acceso clínico con ID inmutable y timestamp")
    void testRegistrarAccesoClinicoExitoso() {
        service.registrarAccesoClinico(
                "medico.radiologo", "PAC-2026", "LECTURA", "HISTORIA_CLINICA", "10.0.0.15"
        );

        ArgumentCaptor<AuditoriaAccesoDTO> captor = ArgumentCaptor.forClass(AuditoriaAccesoDTO.class);
        verify(auditoriaRepositoryMock, times(1)).guardar(captor.capture());

        AuditoriaAccesoDTO guardado = captor.getValue();
        assertNotNull(guardado);
        assertNotNull(guardado.getId());
        assertTrue(guardado.getId().startsWith("AUD-"));
        assertNotNull(guardado.getTimestamp());
        assertEquals("medico.radiologo", guardado.getUsuarioId());
        assertEquals("MEDICO", guardado.getRolUsuario());
        assertEquals("PAC-2026", guardado.getPacienteIdAfectado());
        assertEquals("LECTURA", guardado.getAccion());
        assertEquals("HISTORIA_CLINICA", guardado.getRecursoAccedido());
        assertEquals("10.0.0.15", guardado.getIpOrigen());
    }

    @Test
    @DisplayName("Debe asignar valores por defecto a IP y Paciente si vienen nulos o en blanco")
    void testRegistrarAccesoClinicoValoresPorDefecto() {
        service.registrarAccesoClinico("admin.sistema", "   ", "EXPORTACION", "ESTADISTICAS_GLOBALES", null);

        ArgumentCaptor<AuditoriaAccesoDTO> captor = ArgumentCaptor.forClass(AuditoriaAccesoDTO.class);
        verify(auditoriaRepositoryMock, times(1)).guardar(captor.capture());

        AuditoriaAccesoDTO guardado = captor.getValue();
        assertEquals("ADMIN_SISTEMA", guardado.getRolUsuario());
        assertEquals("N/A", guardado.getPacienteIdAfectado());
        assertEquals("127.0.0.1", guardado.getIpOrigen());
    }

    @Test
    @DisplayName("Debe lanzar AuditoriaInvalidaException ante parámetros obligatorios nulos o vacíos")
    void testRegistrarAccesoClinicoParametrosInvalidos() {
        assertThrows(AuditoriaInvalidaException.class, () ->
                service.registrarAccesoClinico(null, "PAC-1", "LECTURA", "REC", "127.0.0.1"));

        assertThrows(AuditoriaInvalidaException.class, () ->
                service.registrarAccesoClinico("   ", "PAC-1", "LECTURA", "REC", "127.0.0.1"));

        assertThrows(AuditoriaInvalidaException.class, () ->
                service.registrarAccesoClinico("user1", "PAC-1", null, "REC", "127.0.0.1"));

        assertThrows(AuditoriaInvalidaException.class, () ->
                service.registrarAccesoClinico("user1", "PAC-1", "   ", "REC", "127.0.0.1"));

        assertThrows(AuditoriaInvalidaException.class, () ->
                service.registrarAccesoClinico("user1", "PAC-1", "LECTURA", null, "127.0.0.1"));

        assertThrows(AuditoriaInvalidaException.class, () ->
                service.registrarAccesoClinico("user1", "PAC-1", "LECTURA", "   ", "127.0.0.1"));

        verify(auditoriaRepositoryMock, never()).guardar(any());
    }

    @Test
    @DisplayName("Debe registrar acceso mediante DTO directo y autocompletar metadatos")
    void testRegistrarAccesoDtoDirecto() {
        when(auditoriaRepositoryMock.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        AuditoriaAccesoDTO dto = new AuditoriaAccesoDTO();
        dto.setUsuarioId("paciente.juan");
        dto.setAccion("FIRMA");
        dto.setRecursoAccedido("CONSENTIMIENTO_INFORMADO");

        AuditoriaAccesoDTO resultado = service.registrarAcceso(dto);

        assertNotNull(resultado);
        assertNotNull(resultado.getId());
        assertNotNull(resultado.getTimestamp());
        assertEquals("PACIENTE", resultado.getRolUsuario());
        assertEquals("N/A", resultado.getPacienteIdAfectado());
        assertEquals("127.0.0.1", resultado.getIpOrigen());
        verify(auditoriaRepositoryMock, times(1)).guardar(dto);
    }

    @Test
    @DisplayName("Debe fallar al registrar acceso mediante DTO nulo")
    void testRegistrarAccesoDtoNulo() {
        assertThrows(AuditoriaInvalidaException.class, () -> service.registrarAcceso(null));
        verify(auditoriaRepositoryMock, never()).guardar(any());
    }

    // =========================================================================
    // 2. Consulta y filtrado de trazas médico-legales
    // =========================================================================

    @Test
    @DisplayName("Debe consultar auditorías delegando en el repositorio con filtros válidos")
    void testConsultarAuditoriasExitoso() {
        LocalDateTime desde = LocalDateTime.of(2026, 11, 1, 0, 0);
        LocalDateTime hasta = LocalDateTime.of(2026, 11, 2, 23, 59);

        AuditoriaAccesoDTO traza = new AuditoriaAccesoDTO("A1", desde.plusHours(1), "u1", "MEDICO", "P1", "LECTURA", "REC", "IP");
        when(auditoriaRepositoryMock.buscar(desde, hasta, "P1")).thenReturn(List.of(traza));

        List<AuditoriaAccesoDTO> resultado = service.consultarAuditorias(desde, hasta, "P1");

        assertEquals(1, resultado.size());
        assertEquals("A1", resultado.get(0).getId());
        verify(auditoriaRepositoryMock, times(1)).buscar(desde, hasta, "P1");
    }

    @Test
    @DisplayName("Debe lanzar AuditoriaInvalidaException si la fecha 'desde' es posterior a 'hasta'")
    void testConsultarAuditoriasRangoInvalido() {
        LocalDateTime desde = LocalDateTime.of(2026, 11, 10, 0, 0);
        LocalDateTime hasta = LocalDateTime.of(2026, 11, 5, 0, 0);

        AuditoriaInvalidaException ex = assertThrows(AuditoriaInvalidaException.class,
                () -> service.consultarAuditorias(desde, hasta, null));
        assertTrue(ex.getMessage().contains("no puede ser posterior a 'hasta'"));
        verify(auditoriaRepositoryMock, never()).buscar(any(), any(), any());
    }

    @Test
    @DisplayName("Debe consultar auditorías por usuario o lanzar excepción si es nulo")
    void testConsultarAuditoriasPorUsuario() {
        when(auditoriaRepositoryMock.buscarPorUsuario("med1")).thenReturn(Collections.emptyList());

        List<AuditoriaAccesoDTO> resultado = service.consultarAuditoriasPorUsuario("med1");
        assertNotNull(resultado);
        verify(auditoriaRepositoryMock, times(1)).buscarPorUsuario("med1");

        assertThrows(AuditoriaInvalidaException.class, () -> service.consultarAuditoriasPorUsuario(null));
        assertThrows(AuditoriaInvalidaException.class, () -> service.consultarAuditoriasPorUsuario("   "));
    }

    @Test
    @DisplayName("Debe consultar auditorías por acción clínica o lanzar excepción si es nula")
    void testConsultarAuditoriasPorAccion() {
        when(auditoriaRepositoryMock.buscarPorAccion("FIRMA")).thenReturn(Collections.emptyList());

        List<AuditoriaAccesoDTO> resultado = service.consultarAuditoriasPorAccion("firma");
        assertNotNull(resultado);
        verify(auditoriaRepositoryMock, times(1)).buscarPorAccion("FIRMA");

        assertThrows(AuditoriaInvalidaException.class, () -> service.consultarAuditoriasPorAccion(null));
        assertThrows(AuditoriaInvalidaException.class, () -> service.consultarAuditoriasPorAccion("   "));
    }

    @Test
    @DisplayName("Debe consultar auditoría por ID o retornar Optional vacío ante ID nulo/blanco")
    void testConsultarAuditoriaPorId() {
        AuditoriaAccesoDTO traza = new AuditoriaAccesoDTO("AUD-1", LocalDateTime.now(), "u1", "ROL", "P1", "ACT", "REC", "IP");
        when(auditoriaRepositoryMock.buscarPorId("AUD-1")).thenReturn(Optional.of(traza));

        Optional<AuditoriaAccesoDTO> opt = service.consultarAuditoriaPorId("AUD-1");
        assertTrue(opt.isPresent());
        assertEquals("AUD-1", opt.get().getId());

        assertTrue(service.consultarAuditoriaPorId(null).isEmpty());
        assertTrue(service.consultarAuditoriaPorId("   ").isEmpty());
    }

    @Test
    @DisplayName("Debe contar las auditorías totales registradas")
    void testContarAuditorias() {
        when(auditoriaRepositoryMock.contar()).thenReturn(42L);
        assertEquals(42L, service.contarAuditorias());
        verify(auditoriaRepositoryMock, times(1)).contar();
    }

    // =========================================================================
    // 3. Exportación CSV para inspección regulatoria
    // =========================================================================

    @Test
    @DisplayName("Debe exportar trazas a formato CSV estructurado con cabecera y escape")
    void testExportarAuditoriasCsv() {
        LocalDateTime fecha = LocalDateTime.of(2026, 11, 19, 12, 30, 0);
        AuditoriaAccesoDTO traza1 = new AuditoriaAccesoDTO(
                "AUD-1", fecha, "dr.alvarez", "MEDICO", "PAC-777", "LECTURA", "INFORME_DICOM", "10.0.1.20"
        );
        AuditoriaAccesoDTO trazaConComas = new AuditoriaAccesoDTO(
                "AUD-2", fecha, "usuario,con,comas", "ROL", "PAC-888", "FIRMA", "REC", "127.0.0.1"
        );

        when(auditoriaRepositoryMock.buscar(null, null, null)).thenReturn(List.of(traza1, trazaConComas));

        String csv = service.exportarAuditoriasCsv(null, null, null);

        assertNotNull(csv);
        assertTrue(csv.startsWith("ID,TIMESTAMP,USUARIO_ID,ROL_USUARIO,PACIENTE_ID,ACCION,RECURSO,IP_ORIGEN\n"));
        assertTrue(csv.contains("AUD-1,2026-11-19T12:30:00,dr.alvarez,MEDICO,PAC-777,LECTURA,INFORME_DICOM,10.0.1.20"));
        assertTrue(csv.contains("\"usuario,con,comas\""));
    }

    // =========================================================================
    // 4. Bloqueo de mutaciones y borrados (RN-07 / RNF-06)
    // =========================================================================

    @Test
    @DisplayName("RN-07 / RNF-06: Debe interceptar y lanzar OperacionAuditoriaNoPermitidaException ante intento de modificación")
    void testBloqueoModificacionRegistro() {
        AuditoriaAccesoDTO dto = new AuditoriaAccesoDTO("AUD-X", LocalDateTime.now(), "u", "R", "P", "A", "REC", "IP");
        doThrow(new UnsupportedOperationException("RN-07: Bloqueo")).when(auditoriaRepositoryMock).actualizar(dto);

        OperacionAuditoriaNoPermitidaException ex = assertThrows(OperacionAuditoriaNoPermitidaException.class,
                () -> service.intentarModificarRegistro(dto));

        assertTrue(ex.getMessage().contains("Bloqueo médico-legal"));
        verify(auditoriaRepositoryMock, times(1)).actualizar(dto);
    }

    @Test
    @DisplayName("RN-07 / RNF-06: Debe interceptar y lanzar OperacionAuditoriaNoPermitidaException ante intento de borrado")
    void testBloqueoEliminacionRegistro() {
        doThrow(new UnsupportedOperationException("RN-07: Bloqueo")).when(auditoriaRepositoryMock).eliminar("AUD-X");

        OperacionAuditoriaNoPermitidaException ex = assertThrows(OperacionAuditoriaNoPermitidaException.class,
                () -> service.intentarEliminarRegistro("AUD-X"));

        assertTrue(ex.getMessage().contains("Bloqueo médico-legal"));
        verify(auditoriaRepositoryMock, times(1)).eliminar("AUD-X");
    }

    // =========================================================================
    // 5. Inferencia de roles de usuario
    // =========================================================================

    @Test
    @DisplayName("Debe resolver adecuadamente los roles según el prefijo del usuario")
    void testInferenciaRoles() {
        service.registrarAccesoClinico("medico.1", "P", "ACT", "REC", "IP");
        service.registrarAccesoClinico("admin.1", "P", "ACT", "REC", "IP");
        service.registrarAccesoClinico("admision.1", "P", "ACT", "REC", "IP");
        service.registrarAccesoClinico("paciente.1", "P", "ACT", "REC", "IP");
        service.registrarAccesoClinico("otro.1", "P", "ACT", "REC", "IP");

        ArgumentCaptor<AuditoriaAccesoDTO> captor = ArgumentCaptor.forClass(AuditoriaAccesoDTO.class);
        verify(auditoriaRepositoryMock, times(5)).guardar(captor.capture());

        List<AuditoriaAccesoDTO> valores = captor.getAllValues();
        assertEquals("MEDICO", valores.get(0).getRolUsuario());
        assertEquals("ADMIN_SISTEMA", valores.get(1).getRolUsuario());
        assertEquals("ADMINISTRATIVO_ADMISION", valores.get(2).getRolUsuario());
        assertEquals("PACIENTE", valores.get(3).getRolUsuario());
        assertEquals("USUARIO_AUTENTICADO", valores.get(4).getRolUsuario());
    }

    @Test
    @DisplayName("Constructor debe validar que el repositorio no sea nulo")
    void testConstructorValidaciones() {
        AuditoriaService servicioSimple = new AuditoriaService(auditoriaRepositoryMock);
        assertNotNull(servicioSimple);
        assertThrows(NullPointerException.class, () -> new AuditoriaService(null));
    }
}
