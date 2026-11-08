package es.uclm.esi.iso2.sescam.common.interfaces.services;

import es.uclm.esi.iso2.sescam.common.interfaces.dto.PruebaDiagnosticaDTO;
import java.util.List;

public interface CatalogoService {
    PruebaDiagnosticaDTO registrarPrueba(PruebaDiagnosticaDTO prueba);
    PruebaDiagnosticaDTO obtenerPruebaPorId(String idPrueba);
    List<PruebaDiagnosticaDTO> listarTodasLasPruebas();
    boolean eliminarPrueba(String idPrueba);
}