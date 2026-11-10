package es.uclm.esi.iso2.sescam.common.enums;

/**
 * Estado de una entrada en la lista de espera (CU-15). Es independiente del estado
 * de la prescripción ({@link EstadoSolicitud}), que pertenece al módulo de prescripción (D-017).
 */
public enum EstadoEntradaEspera {
    /** La solicitud espera hueco y participa en el orden de RN-01. */
    EN_ESPERA,
    /** La citación administrativa (CU-13) ha consolidado una cita para la solicitud. */
    CITADA,
    /** La solicitud se ha retirado de la lista con un motivo; no se borra. */
    RETIRADA
}
