package es.uclm.esi.iso2.sescam.informes.presentacion;

import es.uclm.esi.iso2.sescam.common.dto.ConsentimientoDTO;
import es.uclm.esi.iso2.sescam.common.interfaces.IRealizacionInformesService;
import java.util.Objects;

/**
 * Controlador de presentación para la gestión del consentimiento informado del paciente (CU-22).
 * Conecta las vistas de paciente (portal web / app móvil) con la capa de dominio de radiodiagnóstico.
 */
public class ConsentimientoController {

    private final IRealizacionInformesService realizacionInformesService;

    public ConsentimientoController(IRealizacionInformesService realizacionInformesService) {
        this.realizacionInformesService = Objects.requireNonNull(realizacionInformesService, "El servicio de realizacion no puede ser nulo");
    }

    /**
     * Procesa la solicitud de firma digital del paciente con el checklist de seguridad cumplimentado.
     *
     * @param consentimiento DTO recibido desde la vista del paciente.
     * @return El consentimiento validado y firmado.
     */
    public ConsentimientoDTO procesarFirmaConsentimiento(ConsentimientoDTO consentimiento) {
        return realizacionInformesService.firmarConsentimientoInformado(consentimiento);
    }

    /**
     * Consulta si el paciente cuenta con el consentimiento y seguridad requeridos para acceder a la sala técnica.
     *
     * @param pacienteId   Identificador del paciente.
     * @param tipoPruebaId Identificador del tipo de prueba radiológica.
     * @return true si la prueba puede realizarse conforme a RN-04; false si está bloqueada.
     */
    public boolean verificarConsentimientoPrevio(String pacienteId, String tipoPruebaId) {
        return realizacionInformesService.validarConsentimientoPrevio(pacienteId, tipoPruebaId);
    }
}

