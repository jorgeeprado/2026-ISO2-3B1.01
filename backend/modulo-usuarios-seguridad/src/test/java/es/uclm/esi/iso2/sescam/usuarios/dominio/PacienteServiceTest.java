package es.uclm.esi.iso2.sescam.usuarios.dominio;

import es.uclm.esi.iso2.sescam.common.dto.PacienteDTO;
import es.uclm.esi.iso2.sescam.common.interfaces.IAuditoriaService;
import es.uclm.esi.iso2.sescam.common.ports.ISisAdmisionClient;
import es.uclm.esi.iso2.sescam.usuarios.persistencia.IPacienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias :: PacienteService (Capa de Dominio CU-12 / RN-08 / RN-07)")
class PacienteServiceTest {

    @Mock
    private IPacienteRepository pacienteRepositoryMock;

    @Mock
    private ISisAdmisionClient sisAdmisionClientMock;

    @Mock
    private IAuditoriaService auditoriaServiceMock;

    private PacienteService service;

    @BeforeEach
    void setUp() {
        service = new PacienteService(pacienteRepositoryMock, sisAdmisionClientMock, auditoriaServiceMock);
    }

    @Test
    @DisplayName("Debe dar de alta un paciente con datos completos y registrar auditoría síncrona (RN-07)")
    void testDarAltaPacienteExitosoConCipYDni() {
        PacienteDTO input = new PacienteDTO(
                null, "CIP-CLM-12345678Z", "12345678Z", "Ana", "García Moreno",
                LocalDate.of(1990, 5, 20), "600112233", "ana.garcia@example.com", false
        );

        when(pacienteRepositoryMock.existePorDni("12345678Z")).thenReturn(false);
        when(pacienteRepositoryMock.existePorCip("CIP-CLM-12345678Z")).thenReturn(false);
        when(pacienteRepositoryMock.guardar(any(PacienteDTO.class))).thenAnswer(inv -> inv.getArgument(0));

        PacienteDTO resultado = service.darAltaPaciente(input);

        assertNotNull(resultado);
        assertNotNull(resultado.getId(), "Debe autogenerarse un identificador de paciente");
        assertTrue(resultado.getId().startsWith("PAC-"));
        assertEquals("CIP-CLM-12345678Z", resultado.getCip());
        assertEquals("12345678Z", resultado.getDni());
        assertEquals("Ana", resultado.getNombre());
        assertTrue(resultado.isActivo(), "El paciente recién dado de alta debe quedar en estado activo");

        verify(pacienteRepositoryMock, times(1)).guardar(input);
        verify(auditoriaServiceMock, times(1)).registrarAccesoClinico(
                eq("ADMIN_ADMISION"), eq(resultado.getId()), eq("CREACION"), eq("HISTORIA_CLINICA"), any()
        );
    }

    @Test
    @DisplayName("Debe autogenerar CIP e ID si no se proporcionan en el alta")
    void testDarAltaPacienteAutogeneraCipEId() {
        PacienteDTO input = new PacienteDTO(
                null, null, "87654321X", "Carlos", "Pérez López",
                LocalDate.of(1985, 3, 15), "611223344", "carlos@example.com", false
        );

        when(pacienteRepositoryMock.existePorDni("87654321X")).thenReturn(false);
        when(pacienteRepositoryMock.guardar(any(PacienteDTO.class))).thenAnswer(inv -> inv.getArgument(0));

        PacienteDTO resultado = service.darAltaPaciente(input);

        assertNotNull(resultado.getId());
        assertTrue(resultado.getId().startsWith("PAC-"));
        assertNotNull(resultado.getCip());
        assertTrue(resultado.getCip().startsWith("CIP-CLM-"));
        assertTrue(resultado.isActivo());
    }

    @Test
    @DisplayName("Debe lanzar PacienteInvalidoException si el paciente es nulo")
    void testDarAltaPacienteNulo() {
        assertThrows(PacienteInvalidoException.class, () -> service.darAltaPaciente(null));
        verifyNoInteractions(pacienteRepositoryMock);
    }

    @Test
    @DisplayName("Debe lanzar PacienteInvalidoException si el nombre o apellidos están vacíos")
    void testDarAltaPacienteDatosDemograficosVacios() {
        PacienteDTO sinNombre = new PacienteDTO(null, null, "12345678Z", "  ", "García", LocalDate.of(1990, 1, 1), null, null, true);
        assertThrows(PacienteInvalidoException.class, () -> service.darAltaPaciente(sinNombre));

        PacienteDTO sinApellidos = new PacienteDTO(null, null, "12345678Z", "Ana", "  ", LocalDate.of(1990, 1, 1), null, null, true);
        assertThrows(PacienteInvalidoException.class, () -> service.darAltaPaciente(sinApellidos));

        PacienteDTO sinDni = new PacienteDTO(null, null, "  ", "Ana", "García", LocalDate.of(1990, 1, 1), null, null, true);
        assertThrows(PacienteInvalidoException.class, () -> service.darAltaPaciente(sinDni));
    }

    @Test
    @DisplayName("Debe lanzar PacienteInvalidoException si la fecha de nacimiento es futura")
    void testDarAltaPacienteFechaNacimientoFutura() {
        PacienteDTO conFechaFutura = new PacienteDTO(
                null, null, "12345678Z", "Ana", "García",
                LocalDate.now().plusDays(1), null, null, true
        );
        assertThrows(PacienteInvalidoException.class, () -> service.darAltaPaciente(conFechaFutura));
    }

    @Test
    @DisplayName("Debe lanzar PacienteYaExisteException si el DNI ya está registrado")
    void testDarAltaPacienteDniDuplicado() {
        PacienteDTO input = new PacienteDTO(null, null, "12345678Z", "Ana", "García", LocalDate.of(1990, 1, 1), null, null, true);
        when(pacienteRepositoryMock.existePorDni("12345678Z")).thenReturn(true);

        assertThrows(PacienteYaExisteException.class, () -> service.darAltaPaciente(input));
        verify(pacienteRepositoryMock, never()).guardar(any());
    }

    @Test
    @DisplayName("Debe lanzar PacienteYaExisteException si el CIP ya está registrado")
    void testDarAltaPacienteCipDuplicado() {
        PacienteDTO input = new PacienteDTO(null, "CIP-EXISTENTE", "12345678Z", "Ana", "García", LocalDate.of(1990, 1, 1), null, null, true);
        when(pacienteRepositoryMock.existePorDni("12345678Z")).thenReturn(false);
        when(pacienteRepositoryMock.existePorCip("CIP-EXISTENTE")).thenReturn(true);

        assertThrows(PacienteYaExisteException.class, () -> service.darAltaPaciente(input));
        verify(pacienteRepositoryMock, never()).guardar(any());
    }

    @Test
    @DisplayName("Debe modificar exitosamente los datos de un paciente existente y auditar")
    void testModificarPacienteExitoso() {
        PacienteDTO existente = new PacienteDTO(
                "PAC-001", "CIP-001", "11111111A", "Juan", "López",
                LocalDate.of(1980, 1, 1), "600000000", "juan@old.es", true
        );
        when(pacienteRepositoryMock.buscarPorIdOCip("PAC-001")).thenReturn(Optional.of(existente));
        when(pacienteRepositoryMock.guardar(any(PacienteDTO.class))).thenAnswer(inv -> inv.getArgument(0));

        PacienteDTO modificacion = new PacienteDTO();
        modificacion.setId("PAC-001");
        modificacion.setNombre("Juan Carlos");
        modificacion.setApellidos("López Martínez");
        modificacion.setTelefono("699887766");
        modificacion.setEmail("juancarlos@new.es");

        PacienteDTO resultado = service.modificarPaciente(modificacion);

        assertNotNull(resultado);
        assertEquals("Juan Carlos", resultado.getNombre());
        assertEquals("López Martínez", resultado.getApellidos());
        assertEquals("699887766", resultado.getTelefono());
        assertEquals("juancarlos@new.es", resultado.getEmail());
        assertEquals("11111111A", resultado.getDni()); // DNI preservado

        verify(pacienteRepositoryMock, times(1)).guardar(existente);
        verify(auditoriaServiceMock, times(1)).registrarAccesoClinico(
                eq("ADMIN_ADMISION"), eq("PAC-001"), eq("MODIFICACION"), eq("HISTORIA_CLINICA"), any()
        );
    }

    @Test
    @DisplayName("Debe lanzar PacienteNoEncontradoException al modificar un paciente no censado")
    void testModificarPacienteNoEncontrado() {
        PacienteDTO modificacion = new PacienteDTO();
        modificacion.setId("PAC-INEXISTENTE");
        when(pacienteRepositoryMock.buscarPorIdOCip("PAC-INEXISTENTE")).thenReturn(Optional.empty());

        assertThrows(PacienteNoEncontradoException.class, () -> service.modificarPaciente(modificacion));
    }

    @Test
    @DisplayName("Debe lanzar PacienteInvalidoException al modificar con paciente nulo o sin identificador")
    void testModificarPacienteInvalido() {
        assertThrows(PacienteInvalidoException.class, () -> service.modificarPaciente(null));

        PacienteDTO sinId = new PacienteDTO();
        assertThrows(PacienteInvalidoException.class, () -> service.modificarPaciente(sinId));
    }

    @Test
    @DisplayName("Debe lanzar PacienteYaExisteException si la modificación de DNI colisiona con otro paciente")
    void testModificarPacienteConflictoDniConOtro() {
        PacienteDTO p1 = new PacienteDTO("PAC-1", "CIP-1", "11111111A", "Uno", "Ap1", LocalDate.now(), null, null, true);
        PacienteDTO p2 = new PacienteDTO("PAC-2", "CIP-2", "22222222B", "Dos", "Ap2", LocalDate.now(), null, null, true);

        when(pacienteRepositoryMock.buscarPorIdOCip("PAC-1")).thenReturn(Optional.of(p1));
        when(pacienteRepositoryMock.buscarPorDni("22222222B")).thenReturn(Optional.of(p2));

        PacienteDTO cambio = new PacienteDTO();
        cambio.setId("PAC-1");
        cambio.setDni("22222222B");

        assertThrows(PacienteYaExisteException.class, () -> service.modificarPaciente(cambio));
    }

    @Test
    @DisplayName("Debe lanzar PacienteYaExisteException si la modificación de CIP colisiona con otro paciente")
    void testModificarPacienteConflictoCipConOtro() {
        PacienteDTO p1 = new PacienteDTO("PAC-1", "CIP-1", "11111111A", "Uno", "Ap1", LocalDate.now(), null, null, true);
        PacienteDTO p2 = new PacienteDTO("PAC-2", "CIP-2", "22222222B", "Dos", "Ap2", LocalDate.now(), null, null, true);

        when(pacienteRepositoryMock.buscarPorIdOCip("PAC-1")).thenReturn(Optional.of(p1));
        when(pacienteRepositoryMock.buscarPorCip("CIP-2")).thenReturn(Optional.of(p2));

        PacienteDTO cambio = new PacienteDTO();
        cambio.setId("PAC-1");
        cambio.setCip("CIP-2");

        assertThrows(PacienteYaExisteException.class, () -> service.modificarPaciente(cambio));
    }

    @Test
    @DisplayName("RN-08: Debe ejecutar baja lógica marcando activo = false sin eliminación física")
    void testDarBajaLogicaPacienteExitoso() {
        PacienteDTO existente = new PacienteDTO(
                "PAC-001", "CIP-001", "12345678Z", "Elena", "Ruiz",
                LocalDate.of(1995, 6, 10), "655443322", "elena@example.com", true
        );
        when(pacienteRepositoryMock.buscarPorIdOCip("PAC-001")).thenReturn(Optional.of(existente));
        when(pacienteRepositoryMock.guardar(any(PacienteDTO.class))).thenAnswer(inv -> inv.getArgument(0));

        boolean resultado = service.darBajaLogicaPaciente("PAC-001");

        assertTrue(resultado, "La baja lógica debe completarse exitosamente");
        assertFalse(existente.isActivo(), "El paciente debe tener activo = false en cumplimiento de RN-08");

        verify(pacienteRepositoryMock, times(1)).guardar(existente);
        verify(auditoriaServiceMock, times(1)).registrarAccesoClinico(
                eq("ADMIN_ADMISION"), eq("PAC-001"), eq("MODIFICACION"), eq("HISTORIA_CLINICA"), any()
        );
    }

    @Test
    @DisplayName("Debe retornar false al intentar dar de baja lógica a un paciente inexistente")
    void testDarBajaLogicaPacienteNoExiste() {
        when(pacienteRepositoryMock.buscarPorIdOCip("PAC-NO-EXISTE")).thenReturn(Optional.empty());

        boolean resultado = service.darBajaLogicaPaciente("PAC-NO-EXISTE");

        assertFalse(resultado);
        verify(pacienteRepositoryMock, never()).guardar(any());
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si el ID de baja lógica es nulo o vacío")
    void testDarBajaLogicaPacienteIdInvalido() {
        assertThrows(IllegalArgumentException.class, () -> service.darBajaLogicaPaciente(null));
        assertThrows(IllegalArgumentException.class, () -> service.darBajaLogicaPaciente("   "));
    }

    @Test
    @DisplayName("Debe obtener un paciente censado localmente y registrar auditoría de lectura (RN-07)")
    void testObtenerPacienteLocalmente() {
        PacienteDTO existente = new PacienteDTO(
                "PAC-001", "CIP-001", "12345678Z", "Mario", "Sanz",
                LocalDate.of(1982, 11, 4), "644332211", "mario@example.com", true
        );
        when(pacienteRepositoryMock.buscarPorIdOCip("PAC-001")).thenReturn(Optional.of(existente));

        PacienteDTO obtenido = service.obtenerPaciente("PAC-001");

        assertNotNull(obtenido);
        assertEquals("PAC-001", obtenido.getId());
        assertEquals("Mario", obtenido.getNombre());

        verify(auditoriaServiceMock, times(1)).registrarAccesoClinico(
                eq("ADMIN_ADMISION"), eq("PAC-001"), eq("LECTURA"), eq("HISTORIA_CLINICA"), any()
        );
    }

    @Test
    @DisplayName("Debe sincronizar con SisAdmision si el paciente no existe localmente pero está en el censo maestro")
    void testObtenerPacienteSincronizadoConSisAdmision() {
        when(pacienteRepositoryMock.buscarPorIdOCip("CIP-EXTERNO")).thenReturn(Optional.empty());

        PacienteDTO pacienteCensoMaestro = new PacienteDTO(
                null, "CIP-EXTERNO", "99887766A", "Lucía", "Blanco",
                LocalDate.of(1975, 8, 22), "677889900", "lucia@remoto.com", true
        );
        when(sisAdmisionClientMock.sincronizarCensoPaciente("CIP-EXTERNO")).thenReturn(pacienteCensoMaestro);
        when(pacienteRepositoryMock.guardar(any(PacienteDTO.class))).thenAnswer(inv -> inv.getArgument(0));

        PacienteDTO obtenido = service.obtenerPaciente("CIP-EXTERNO");

        assertNotNull(obtenido);
        assertEquals("Lucía", obtenido.getNombre());
        assertNotNull(obtenido.getId());
        assertTrue(obtenido.getId().startsWith("PAC-"));
        assertTrue(obtenido.isActivo());

        verify(sisAdmisionClientMock, times(1)).sincronizarCensoPaciente("CIP-EXTERNO");
        verify(pacienteRepositoryMock, times(1)).guardar(pacienteCensoMaestro);
    }

    @Test
    @DisplayName("Debe retornar null si el paciente no existe localmente ni en SisAdmision")
    void testObtenerPacienteInexistenteEnTodoElSistema() {
        when(pacienteRepositoryMock.buscarPorIdOCip("CIP-NADA")).thenReturn(Optional.empty());
        when(sisAdmisionClientMock.sincronizarCensoPaciente("CIP-NADA")).thenReturn(null);

        PacienteDTO obtenido = service.obtenerPaciente("CIP-NADA");

        assertNull(obtenido);
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si el ID a consultar es nulo o vacío")
    void testObtenerPacienteIdInvalido() {
        assertThrows(IllegalArgumentException.class, () -> service.obtenerPaciente(null));
        assertThrows(IllegalArgumentException.class, () -> service.obtenerPaciente("  "));
    }

    @Test
    @DisplayName("Debe listar pacientes activos o el censo completo")
    void testListarPacientes() {
        PacienteDTO pActivo = new PacienteDTO("P1", "C1", "D1", "N1", "A1", LocalDate.now(), null, null, true);
        PacienteDTO pInactivo = new PacienteDTO("P2", "C2", "D2", "N2", "A2", LocalDate.now(), null, null, false);

        when(pacienteRepositoryMock.listarActivos()).thenReturn(List.of(pActivo));
        when(pacienteRepositoryMock.listarTodos()).thenReturn(List.of(pActivo, pInactivo));

        List<PacienteDTO> activos = service.listarPacientes(true);
        assertEquals(1, activos.size());
        assertTrue(activos.get(0).isActivo());

        List<PacienteDTO> todos = service.listarPacientes(false);
        assertEquals(2, todos.size());
    }

    @Test
    @DisplayName("Una excepción en el servicio de auditoría no debe romper la operación de negocio")
    void testFalloEnAuditoriaNoRompeOperacion() {
        PacienteDTO input = new PacienteDTO(null, null, "33333333C", "Pedro", "Navarro", LocalDate.of(1992, 1, 1), null, null, true);
        when(pacienteRepositoryMock.guardar(any(PacienteDTO.class))).thenAnswer(inv -> inv.getArgument(0));
        doThrow(new RuntimeException("Fallo en base de datos de auditoría")).when(auditoriaServiceMock).registrarAccesoClinico(any(), any(), any(), any(), any());

        assertDoesNotThrow(() -> service.darAltaPaciente(input));
    }
}

