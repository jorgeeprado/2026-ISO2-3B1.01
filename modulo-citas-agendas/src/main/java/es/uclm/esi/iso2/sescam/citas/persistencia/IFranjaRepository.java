package es.uclm.esi.iso2.sescam.citas.persistencia;

import es.uclm.esi.iso2.sescam.common.enums.TipoRecurso;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistencia de franjas de agenda (CU-14).
 */
public interface IFranjaRepository {

    /** Guarda o reemplaza la franja con el mismo identificador. */
    FranjaRegistro guardar(FranjaRegistro registro);

    Optional<FranjaRegistro> buscarPorId(String idAgenda);

    List<FranjaRegistro> buscarPorRecursoYFecha(TipoRecurso tipoRecurso, String idRecurso, LocalDate fecha);

    List<FranjaRegistro> buscarPorRecurso(TipoRecurso tipoRecurso, String idRecurso);

    List<FranjaRegistro> buscarPorFecha(LocalDate fecha);

    /** @return true si la franja existía y se ha eliminado */
    boolean eliminar(String idAgenda);
}
