package es.uclm.esi.iso2.sescam.catalogo.dominio;

import es.uclm.esi.iso2.sescam.common.interfaces.dto.PruebaDiagnosticaDTO;
import es.uclm.esi.iso2.sescam.common.interfaces.services.CatalogoService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CatalogoServiceImpl implements CatalogoService {

    private final Map<String, PruebaDiagnosticaDTO> pruebasPorId;

    public CatalogoServiceImpl() {
        this(new HashMap<>());
    }

    CatalogoServiceImpl(Map<String, PruebaDiagnosticaDTO> pruebasPorId) {
        this.pruebasPorId = Objects.requireNonNull(pruebasPorId, "El almacenamiento no puede ser nulo");
    }

    @Override
    public PruebaDiagnosticaDTO registrarPrueba(PruebaDiagnosticaDTO prueba) {
        if (prueba == null) {
            throw new IllegalArgumentException("La prueba diagnóstica no puede ser nula");
        }
        String idPrueba = prueba.idPrueba();
        if (idPrueba == null || idPrueba.isBlank()) {
            throw new IllegalArgumentException("El identificador de la prueba es obligatorio");
        }

        pruebasPorId.put(idPrueba, prueba);
        return prueba;
    }

    @Override
    public PruebaDiagnosticaDTO obtenerPruebaPorId(String idPrueba) {
        return pruebasPorId.get(idPrueba);
    }

    @Override
    public List<PruebaDiagnosticaDTO> listarTodasLasPruebas() {
        return new ArrayList<>(pruebasPorId.values());
    }

    @Override
    public boolean eliminarPrueba(String idPrueba) {
        return pruebasPorId.remove(idPrueba) != null;
    }
}
