package es.uclm.esi.iso2.sescam.common.interfaces;

import es.uclm.esi.iso2.sescam.common.dto.IncidenciaDTO;
import es.uclm.esi.iso2.sescam.common.dto.NotificacionDTO;
import java.util.List;

/**
 * Contrato de servicios del Módulo 4 (modulo-notificaciones-calidad).
 * Responsable: Marcos Díaz-Pavón Romero.
 * Casos de uso: CU-02, CU-04, CU-24.
 */
public interface INotificacionesCalidadService {

    /**
     * Emite un recordatorio de cita o instrucciones de preparación previa al paciente (CU-02 / RF-02).
     */
    NotificacionDTO enviarRecordatorioCita(String citaId, String pacienteId);

    /**
     * Emite aviso de disponibilidad de informe diagnóstico validado (CU-02 / RF-02).
     */
    NotificacionDTO notificarInformeDisponible(String pacienteId, String informeId);

    /**
     * Emite una notificación prioritaria inmediata al médico prescriptor por hallazgo crítico (CU-24 / RF-24).
     */
    NotificacionDTO emitirAlertaHallazgoCritico(String medicoPrescriptorId, String informeId, String detalleAlerta);

    /**
     * Consulta las notificaciones recibidas por un usuario destinatario.
     */
    List<NotificacionDTO> consultarNotificaciones(String usuarioId);

    /**
     * Registra una incidencia asistencial o una encuesta de satisfacción cumplimentada (CU-04 / RF-04).
     */
    IncidenciaDTO registrarIncidenciaOEncuesta(IncidenciaDTO incidencia);
}

