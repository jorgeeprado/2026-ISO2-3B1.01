package es.uclm.esi.iso2.sescam.informes.dominio;

import es.uclm.esi.iso2.sescam.common.dto.ConsentimientoDTO;
import es.uclm.esi.iso2.sescam.common.dto.InformeRadiologicoDTO;
import es.uclm.esi.iso2.sescam.common.dto.RegistroRealizacionDTO;
import es.uclm.esi.iso2.sescam.common.interfaces.IRealizacionInformesService;
import java.util.List;
import java.util.Objects;

/**
 * Implementación principal del contrato IRealizacionInformesService para el Módulo 3.
 * Evoluciona de forma incremental según el Proceso Unificado de Desarrollo (PUD):
 * - Iteración 11 (v0.4.0): CU-22 (Consentimiento informado y checklist de seguridad).
 * - Iteraciones posteriores: CU-10 (Realización de prueba), CU-09 (Informes/DICOM), CU-03 (Historial).
 */
public class RealizacionInformesService implements IRealizacionInformesService {

    private final ConsentimientoService consentimientoService;

    public RealizacionInformesService(ConsentimientoService consentimientoService) {
        this.consentimientoService = Objects.requireNonNull(consentimientoService, "El servicio de consentimiento no puede ser nulo");
    }

    @Override
    public ConsentimientoDTO firmarConsentimientoInformado(ConsentimientoDTO consentimiento) {
        return consentimientoService.firmarConsentimiento(consentimiento);
    }

    @Override
    public boolean validarConsentimientoPrevio(String pacienteId, String tipoPruebaId) {
        return consentimientoService.validarConsentimientoPrevio(pacienteId, tipoPruebaId);
    }

    @Override
    public List<InformeRadiologicoDTO> consultarHistorialPruebas(String pacienteId) {
        throw new UnsupportedOperationException("El caso de uso CU-03 está planificado para su desarrollo en la Iteración 16.");
    }

    @Override
    public InformeRadiologicoDTO crearInforme(InformeRadiologicoDTO informe) {
        throw new UnsupportedOperationException("El caso de uso CU-09 está planificado para su desarrollo en la Iteración 15.");
    }

    @Override
    public InformeRadiologicoDTO consultarInforme(String informeId) {
        throw new UnsupportedOperationException("El caso de uso CU-09 está planificado para su desarrollo en la Iteración 15.");
    }

    @Override
    public InformeRadiologicoDTO firmarInformeDigitalmente(String informeId, String medicoRadiologoId, String firmaDigitalHash) {
        throw new UnsupportedOperationException("El caso de uso CU-09 está planificado para su desarrollo en la Iteración 15.");
    }

    @Override
    public RegistroRealizacionDTO registrarRealizacionPrueba(RegistroRealizacionDTO registro) {
        throw new UnsupportedOperationException("El caso de uso CU-10 está planificado para su desarrollo en la Iteración 12.");
    }
}

