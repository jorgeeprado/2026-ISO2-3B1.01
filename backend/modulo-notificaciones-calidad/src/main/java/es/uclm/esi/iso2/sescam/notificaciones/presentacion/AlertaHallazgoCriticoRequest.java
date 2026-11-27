package es.uclm.esi.iso2.sescam.notificaciones.presentacion;

/**
 * Cuerpo de la petición REST de CU-24 (emitir alerta por hallazgo radiológico crítico).
 */
public record AlertaHallazgoCriticoRequest(
        String medicoPrescriptorId,
        String informeId,
        String detalleAlerta) {
}
