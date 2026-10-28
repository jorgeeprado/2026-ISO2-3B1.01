package es.uclm.esi.iso2.sescam.common.interfaces;

import es.uclm.esi.iso2.sescam.common.dto.ConsentimientoDTO;
import es.uclm.esi.iso2.sescam.common.dto.InformeRadiologicoDTO;
import es.uclm.esi.iso2.sescam.common.dto.RegistroRealizacionDTO;
import java.util.List;

/**
 * Contrato de servicios del Módulo 3 (modulo-realizacion-informes-dicom).
 * Responsable: Jorge Prado Novillo.
 * Casos de uso: CU-03, CU-09, CU-10, CU-22.
 */
public interface IRealizacionInformesService {

    /**
     * Consulta el historial longitudinal de pruebas e informes validados de un paciente (CU-03 / RF-03).
     */
    List<InformeRadiologicoDTO> consultarHistorialPruebas(String pacienteId);

    /**
     * Registra un borrador de informe clínico asociado a una prueba (CU-09 / RF-09).
     */
    InformeRadiologicoDTO crearInforme(InformeRadiologicoDTO informe);

    /**
     * Consulta un informe radiológico por su identificador único.
     */
    InformeRadiologicoDTO consultarInforme(String informeId);

    /**
     * Valida y firma digitalmente un informe radiológico (CU-09 / RF-09).
     * Si contiene un hallazgo crítico inesperado, disparará alerta prioritaria (CU-24).
     */
    InformeRadiologicoDTO firmarInformeDigitalmente(String informeId, String medicoRadiologoId, String firmaDigitalHash);

    /**
     * Registra la realización efectiva de una prueba diagnóstica (CU-10 / RF-10).
     * Requiere verificación previa del consentimiento informado según RN-04 / CU-22.
     */
    RegistroRealizacionDTO registrarRealizacionPrueba(RegistroRealizacionDTO registro);

    /**
     * Recoge la firma digital del consentimiento informado y el checklist de seguridad (CU-22 / RF-22 / RN-04).
     */
    ConsentimientoDTO firmarConsentimientoInformado(ConsentimientoDTO consentimiento);

    /**
     * Verifica si un paciente cuenta con consentimiento y seguridad validados para una prueba (RN-04).
     */
    boolean validarConsentimientoPrevio(String pacienteId, String tipoPruebaId);
}
