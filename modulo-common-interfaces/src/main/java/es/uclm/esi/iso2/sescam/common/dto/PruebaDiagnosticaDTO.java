package es.uclm.esi.iso2.sescam.common.dto;

public record PruebaDiagnosticaDTO(
    String idPrueba,
    String nombre,
    String modalidad, // Ej: "TAC", "RM", "US", "Rx"
    int duracionMinutos,
    boolean requiereContraste
) {}
