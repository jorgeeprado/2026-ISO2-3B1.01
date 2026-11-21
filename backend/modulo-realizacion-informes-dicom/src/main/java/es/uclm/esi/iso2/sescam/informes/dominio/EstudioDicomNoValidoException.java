package es.uclm.esi.iso2.sescam.informes.dominio;

/**
 * Excepción de negocio lanzada cuando el identificador de estudio DICOM (StudyInstanceUID)
 * no existe o no supera la validación en el servidor central RIS/PACS.
 */
public class EstudioDicomNoValidoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EstudioDicomNoValidoException(String mensaje) {
        super(mensaje);
    }
}

