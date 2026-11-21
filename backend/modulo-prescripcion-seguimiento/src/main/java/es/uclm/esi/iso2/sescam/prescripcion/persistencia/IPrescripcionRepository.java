package es.uclm.esi.iso2.sescam.prescripcion.persistencia;

import es.uclm.esi.iso2.sescam.prescripcion.dominio.Prescripcion;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistencia para prescripciones de pruebas diagnósticas.
 */
public interface IPrescripcionRepository {

    Prescripcion guardar(Prescripcion prescripcion);

    Optional<Prescripcion> buscarPorId(String id);

    List<Prescripcion> listarPorPaciente(String pacienteId);

    List<Prescripcion> listarPorMedico(String medicoId);

    List<Prescripcion> listarTodos();

    void limpiar();
}
