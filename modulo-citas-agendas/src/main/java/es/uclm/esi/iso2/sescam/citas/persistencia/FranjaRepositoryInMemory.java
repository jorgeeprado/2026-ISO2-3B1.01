package es.uclm.esi.iso2.sescam.citas.persistencia;

import es.uclm.esi.iso2.sescam.common.enums.TipoRecurso;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Implementación en memoria (DAO simulado) del repositorio de franjas.
 */
public class FranjaRepositoryInMemory implements IFranjaRepository {

    private final Map<String, FranjaRegistro> almacenamiento = new ConcurrentHashMap<>();

    @Override
    public FranjaRegistro guardar(FranjaRegistro registro) {
        if (registro == null) {
            throw new IllegalArgumentException("La franja no puede ser nula");
        }
        almacenamiento.put(registro.idAgenda(), registro);
        return registro;
    }

    @Override
    public Optional<FranjaRegistro> buscarPorId(String idAgenda) {
        return idAgenda == null ? Optional.empty() : Optional.ofNullable(almacenamiento.get(idAgenda));
    }

    @Override
    public List<FranjaRegistro> buscarPorRecursoYFecha(TipoRecurso tipoRecurso, String idRecurso, LocalDate fecha) {
        return filtrar(r -> r.tipoRecurso() == tipoRecurso
                && r.idRecurso().equals(idRecurso)
                && r.franja().fecha().equals(fecha));
    }

    @Override
    public List<FranjaRegistro> buscarPorRecurso(TipoRecurso tipoRecurso, String idRecurso) {
        return filtrar(r -> r.tipoRecurso() == tipoRecurso && r.idRecurso().equals(idRecurso));
    }

    @Override
    public List<FranjaRegistro> buscarPorFecha(LocalDate fecha) {
        return filtrar(r -> r.franja().fecha().equals(fecha));
    }

    @Override
    public boolean eliminar(String idAgenda) {
        return idAgenda != null && almacenamiento.remove(idAgenda) != null;
    }

    private List<FranjaRegistro> filtrar(Predicate<FranjaRegistro> criterio) {
        return almacenamiento.values().stream().filter(criterio).toList();
    }
}
