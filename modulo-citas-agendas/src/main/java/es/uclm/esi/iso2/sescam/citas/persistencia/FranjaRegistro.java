package es.uclm.esi.iso2.sescam.citas.persistencia;

import es.uclm.esi.iso2.sescam.common.dto.AgendaMedicaDTO;
import es.uclm.esi.iso2.sescam.common.enums.TipoRecurso;

import java.util.Objects;

/**
 * Registro persistido de una franja de agenda: el DTO compartido más el tipo de recurso,
 * que {@link AgendaMedicaDTO} no contiene (D-011).
 */
public record FranjaRegistro(AgendaMedicaDTO franja, TipoRecurso tipoRecurso) {

    public FranjaRegistro {
        Objects.requireNonNull(franja, "La franja no puede ser nula");
        Objects.requireNonNull(tipoRecurso, "El tipo de recurso no puede ser nulo");
    }

    public String idAgenda() {
        return franja.idAgenda();
    }

    public String idRecurso() {
        return franja.idMedico();
    }
}
