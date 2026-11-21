package es.uclm.esi.iso2.sescam.citas.persistencia;

import es.uclm.esi.iso2.sescam.common.dto.EntradaListaEsperaDTO;
import es.uclm.esi.iso2.sescam.common.enums.EstadoEntradaEspera;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Implementación en memoria (DAO simulado) del repositorio de la lista de espera.
 */
public class ListaEsperaRepositoryInMemory implements IListaEsperaRepository {

    private final Map<String, EntradaListaEsperaDTO> almacenamiento = new ConcurrentHashMap<>();

    @Override
    public EntradaListaEsperaDTO guardar(EntradaListaEsperaDTO entrada) {
        if (entrada == null) {
            throw new IllegalArgumentException("La entrada de lista de espera no puede ser nula");
        }
        almacenamiento.put(entrada.idSolicitud(), entrada);
        return entrada;
    }

    @Override
    public Optional<EntradaListaEsperaDTO> buscarPorSolicitud(String idSolicitud) {
        return idSolicitud == null ? Optional.empty() : Optional.ofNullable(almacenamiento.get(idSolicitud));
    }

    @Override
    public List<EntradaListaEsperaDTO> buscarPorTipoPrueba(String tipoPruebaId) {
        return filtrar(e -> e.tipoPruebaId().equals(tipoPruebaId));
    }

    @Override
    public List<EntradaListaEsperaDTO> buscarPorEstado(EstadoEntradaEspera estado) {
        return filtrar(e -> e.estado() == estado);
    }

    private List<EntradaListaEsperaDTO> filtrar(Predicate<EntradaListaEsperaDTO> criterio) {
        return almacenamiento.values().stream().filter(criterio).toList();
    }
}
