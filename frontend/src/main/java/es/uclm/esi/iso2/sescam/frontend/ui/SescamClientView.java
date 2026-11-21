package es.uclm.esi.iso2.sescam.frontend.ui;

import es.uclm.esi.iso2.sescam.common.dto.UsuarioDTO;
import es.uclm.esi.iso2.sescam.common.enums.RolUsuario;

import java.util.Collections;
import java.util.List;

/**
 * Vista de interfaz de usuario de la capa Cliente (Frontend) del SESCAM (RNF-11).
 * Modela la interacción visual y los menús de navegación desacoplados según el rol del usuario,
 * preparada para clientes Web (HTML5/Angular/React), Escritorio (JavaFX) o Móvil.
 */
public class SescamClientView {

    public String renderEncabezado(UsuarioDTO usuario) {
        if (usuario == null) {
            return "[SESCAM - Sesión no iniciada]";
        }
        String nombreCompleto = (usuario.getNombre() != null ? usuario.getNombre() : "")
                + " " + (usuario.getApellidos() != null ? usuario.getApellidos() : "");
        String rolStr = usuario.getRol() != null ? usuario.getRol().name() : "SIN_ROL";
        return String.format("[SESCAM - Portal Radiodiagnóstico] Usuario: %s (%s) | Rol: %s",
                usuario.getUsername(), nombreCompleto.trim(), rolStr);
    }

    public List<String> obtenerOpcionesMenuPorRol(RolUsuario rol) {
        if (rol == null) {
            return Collections.emptyList();
        }
        switch (rol) {
            case PACIENTE:
                return List.of(
                        "1. Mis Citas y Solicitudes",
                        "2. Firma de Consentimiento y Checklist de Seguridad",
                        "3. Mis Informes e Imágenes Diagnósticas",
                        "4. Mis Notificaciones y Recordatorios",
                        "5. Encuestas de Calidad e Incidencias"
                );
            case MEDICO:
                return List.of(
                        "1. Prescribir Prueba Radiológica",
                        "2. Prescribir Seguimiento Periódico",
                        "3. Visor de Estudios DICOM e Informes",
                        "4. Registro de Realización Técnica",
                        "5. Emisión de Alertas por Hallazgo Crítico",
                        "6. Cuadro de Estadísticas Clínicas",
                        "7. Mis Citas Personales (Herencia Paciente RN-05)"
                );
            case ADMINISTRATIVO_ADMISION:
                return List.of(
                        "1. Asignación y Reprogramación de Citas",
                        "2. Administración de Agendas Diarias",
                        "3. Gestión de Listas de Espera y Prioridades",
                        "4. Reasignación Inteligente de Huecos (<48h)",
                        "5. Gestión de Altas y Bajas de Pacientes",
                        "6. Informes Operativos de Demoras y Ocupación"
                );
            case ADMIN_SISTEMA:
                return List.of(
                        "1. Catálogo de Pruebas y Protocolos",
                        "2. Administración de Usuarios y Roles RBAC",
                        "3. Auditoría Médico-Legal de Accesos (Inmutable)",
                        "4. Supervisión de Conectores Hospitalarios (HCE/PACS)",
                        "5. Cuadro de Mando y Estadísticas Globales"
                );
            default:
                return Collections.emptyList();
        }
    }

    public boolean esAccesoPermitidoEnMenu(RolUsuario rol, String opcion) {
        if (rol == null || opcion == null) {
            return false;
        }
        List<String> menu = obtenerOpcionesMenuPorRol(rol);
        return menu.stream().anyMatch(item -> item.toUpperCase().contains(opcion.toUpperCase()));
    }
}
