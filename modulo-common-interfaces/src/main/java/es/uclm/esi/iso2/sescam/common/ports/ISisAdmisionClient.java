package es.uclm.esi.iso2.sescam.common.ports;

import es.uclm.esi.iso2.sescam.common.dto.PacienteDTO;

/**
 * Puerto de sincronización con el sistema maestro de filiación y censo hospitalario (SisAdmision).
 */
public interface ISisAdmisionClient {

    /**
     * Sincroniza datos demográficos del censo maestro mediante mensaje HL7 ADT.
     */
    PacienteDTO sincronizarCensoPaciente(String cip);
}

