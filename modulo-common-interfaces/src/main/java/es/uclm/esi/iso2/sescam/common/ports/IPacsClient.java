package es.uclm.esi.iso2.sescam.common.ports;

/**
 * Puerto de integración con la plataforma RIS/PACS (DICOM 3.0 / WADO-RS / QIDO-RS).
 * Satisface RNF-02 y RNF-03.
 */
public interface IPacsClient {

    /**
     * Vincula y verifica la existencia de un StudyInstanceUID en el PACS central.
     */
    boolean validarEstudioDicom(String studyInstanceUID);

    /**
     * Recupera los metadatos o serie de imágenes de un estudio DICOM.
     */
    byte[] recuperarSerieDicom(String studyInstanceUID);
}
