package es.uclm.esi.iso2.sescam.usuarios;

import es.uclm.esi.iso2.sescam.common.dto.AuditoriaAccesoDTO;
import es.uclm.esi.iso2.sescam.common.enums.RolUsuario;
import es.uclm.esi.iso2.sescam.common.interfaces.IAuditoriaService;
import es.uclm.esi.iso2.sescam.usuarios.dominio.AuditoriaService;
import es.uclm.esi.iso2.sescam.usuarios.persistencia.AuditoriaRepositoryInMemory;
import es.uclm.esi.iso2.sescam.usuarios.persistencia.IAuditoriaRepository;
import es.uclm.esi.iso2.sescam.usuarios.presentacion.AuditoriaController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas de Integración y Humo :: Módulo 5 - Usuarios y Seguridad (CU-19 / RN-07 / RNF-06)")
class ModuloUsuariosSeguridadTest {

    @Test
    @DisplayName("Verificación de inicialización del módulo y constantes de roles RBAC")
    void testModuloUsuariosInitialized() {
        assertNotNull(RolUsuario.ADMIN_SISTEMA);
        assertNotNull(RolUsuario.ADMINISTRATIVO_ADMISION);
        assertNotNull(RolUsuario.MEDICO);
        assertNotNull(RolUsuario.PACIENTE);
    }

    @Test
    @DisplayName("Flujo Integrado E2E CU-19: Registro síncrono, filtrado, exportación e inmutabilidad estricta (RN-07 / RNF-06)")
    void testFlujoCompletoAuditoriaClinicaInmutable() {
        IAuditoriaRepository repository = new AuditoriaRepositoryInMemory();
        IAuditoriaService service = new AuditoriaService(repository);
        AuditoriaController controller = new AuditoriaController(service);

        LocalDateTime inicioJornada = LocalDateTime.now().minusMinutes(10);

        // 1. Simulación de accesos asistenciales síncronos sobre datos clínicos
        // Acción 1: Facultativo radiólogo consulta imagen DICOM
        controller.registrarAccesoClinico(
                "medico.radiologo", "PAC-101", "LECTURA", "SERIE_IMAGENES_DICOM", "10.20.1.10"
        );

        // Acción 2: Facultativo redacta y firma informe diagnóstico
        controller.registrarAccesoClinico(
                "medico.radiologo", "PAC-101", "FIRMA", "INFORME_DIAGNOSTICO", "10.20.1.10"
        );

        // Acción 3: Personal administrativo consulta antecedentes para citación
        controller.registrarAccesoClinico(
                "admision.central", "PAC-102", "LECTURA", "HISTORIA_CLINICA", "10.20.2.5"
        );

        // Acción 4: Paciente firma digitalmente consentimiento informado
        controller.registrarAccesoClinico(
                "paciente.titular", "PAC-101", "FIRMA", "CONSENTIMIENTO_INFORMADO", "85.60.10.4"
        );

        // 2. Verificación de almacenamiento síncrono y recuento acumulado
        assertEquals(4, repository.contar(), "Deben constar exactamente 4 trazas registradas síncronamente");

        // 3. Inspección médico-legal de accesos para el paciente PAC-101
        List<AuditoriaAccesoDTO> trazasPac101 = controller.consultarAuditorias(
                inicioJornada, LocalDateTime.now().plusMinutes(5), "PAC-101"
        );
        assertEquals(3, trazasPac101.size(), "PAC-101 debe tener 3 accesos registrados");

        // 4. Inspección de actividades por usuario y tipo de acción
        List<AuditoriaAccesoDTO> accionesMedico = controller.consultarAuditoriasPorUsuario("medico.radiologo");
        assertEquals(2, accionesMedico.size(), "El radiólogo debe tener 2 eventos registrados");

        List<AuditoriaAccesoDTO> firmas = controller.consultarAuditoriasPorAccion("FIRMA");
        assertEquals(2, firmas.size(), "Deben constar 2 acciones de tipo FIRMA");

        // 5. Exportación para auditoría externa / regulador sanitario (AEPD / SESCAM)
        String csv = controller.exportarAuditoriasCsv(inicioJornada, LocalDateTime.now().plusMinutes(5), "PAC-101");
        assertNotNull(csv);
        assertTrue(csv.startsWith("ID,TIMESTAMP,USUARIO_ID,ROL_USUARIO,PACIENTE_ID,ACCION,RECURSO,IP_ORIGEN\n"));
        assertTrue(csv.contains("PAC-101"));
        assertTrue(csv.contains("SERIE_IMAGENES_DICOM"));
        assertTrue(csv.contains("INFORME_DIAGNOSTICO"));
        assertTrue(csv.contains("CONSENTIMIENTO_INFORMADO"));

        // 6. Verificación estricta de RN-07 / RNF-06 (No repudio e inmutabilidad):
        // Intentar modificar o borrar cualquier traza debe quedar taxativamente bloqueado
        AuditoriaAccesoDTO traza = trazasPac101.get(0);
        boolean modificado = controller.intentarModificarRegistro(traza);
        assertFalse(modificado, "RN-07 exige bloquear cualquier intento de modificación de trazas");

        boolean eliminado = controller.intentarEliminarRegistro(traza.getId());
        assertFalse(eliminado, "RN-07 exige bloquear cualquier intento de borrado de trazas");

        assertThrows(UnsupportedOperationException.class, () -> repository.actualizar(traza));
        assertThrows(UnsupportedOperationException.class, () -> repository.eliminar(traza.getId()));
        assertThrows(UnsupportedOperationException.class, () -> repository.vaciar());
    }
}
