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
 * - Iteración 12 (v0.4.0): CU-10 (Realización de prueba, incidencias e integración PACS).
 * - Iteraciones posteriores: CU-09 (Informes/DICOM), CU-03 (Historial longitudinal).
 */
public class RealizacionInformesService implements IRealizacionInformesService {

    private final ConsentimientoService consentimientoService;
    private final RealizacionPruebasServiceImpl realizacionPruebasService;

    public RealizacionInformesService(ConsentimientoService consentimientoService) {
        this(consentimientoService, null);
    }

    public RealizacionInformesService(ConsentimientoService consentimientoService,
                                      RealizacionPruebasServiceImpl realizacionPruebasService) {
        this.consentimientoService = Objects.requireNonNull(consentimientoService, "El servicio de consentimiento no puede ser nulo");
        this.realizacionPruebasService = realizacionPruebasService;
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
    public RegistroRealizacionDTO registrarRealizacionPrueba(RegistroRealizacionDTO registro) {
        if (realizacionPruebasService == null) {
            throw new UnsupportedOperationException("El servicio de realización de pruebas no ha sido inyectado");
        }
        return realizacionPruebasService.registrarRealizacionPrueba(registro);
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
}
