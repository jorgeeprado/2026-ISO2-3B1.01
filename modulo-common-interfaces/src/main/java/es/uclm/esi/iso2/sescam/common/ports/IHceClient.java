package es.uclm.esi.iso2.sescam.common.ports;

import es.uclm.esi.iso2.sescam.common.dto.InformeRadiologicoDTO;

/**
 * Puerto de comunicación con la Historia Clínica Electrónica (HCE).
 * Conforme a estándares HL7 v2.x / FHIR R4 (DiagnosticReport, ServiceRequest).
 */
public interface IHceClient {

    /**
     * Publica un informe radiológico validado en la HCE del paciente.
     */
    boolean publicarInformeEnHce(InformeRadiologicoDTO informe);

    /**
     * Consulta antecedentes y alertas clínicas del paciente en el repositorio central.
     */
    String consultarAntecedentesAlergiasHce(String cipPaciente);
}
