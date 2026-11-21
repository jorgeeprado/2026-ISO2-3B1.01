package es.uclm.esi.iso2.sescam.common.interfaces;

import es.uclm.esi.iso2.sescam.common.dto.EntradaListaEsperaDTO;
import es.uclm.esi.iso2.sescam.common.dto.SolicitudPruebaDTO;
import es.uclm.esi.iso2.sescam.common.enums.Prioridad;

import java.util.List;

/**
 * Contrato de gestión de las listas de espera (CU-15) para el Personal Administrativo de Admisión.
 * Hay una lista por tipo de prueba, ordenada según RN-01: prioridad clínica
 * ({@code URGENTE > PREFERENTE > ORDINARIO}) y, a igual prioridad, FIFO por fecha de prescripción.
 *
 * <p>Errores: {@link IllegalArgumentException} ante datos inválidos o solicitudes inexistentes;
 * {@link IllegalStateException} si la entrada no está en el estado requerido.</p>
 */
public interface GestionListaEsperaService {

    /**
     * Inscribe en la lista de su tipo de prueba una solicitud {@code PENDIENTE_CITACION}.
     * Copia su prioridad y su fecha de prescripción ({@code fechaSolicitud}). Falla si la solicitud
     * ya tiene entrada.
     */
    EntradaListaEsperaDTO inscribir(SolicitudPruebaDTO solicitud);

    /**
     * Cambia la prioridad clínica de una entrada {@code EN_ESPERA}, con motivo obligatorio.
     * Conserva la fecha de prescripción, por lo que la entrada se recoloca por antigüedad en el nuevo nivel.
     */
    EntradaListaEsperaDTO cambiarPrioridad(String idSolicitud, Prioridad nuevaPrioridad, String motivo);

    /**
     * Retira de la lista una entrada {@code EN_ESPERA}, con motivo obligatorio. La entrada no se borra.
     */
    EntradaListaEsperaDTO retirar(String idSolicitud, String motivo);

    /**
     * Devuelve las entradas {@code EN_ESPERA} de un tipo de prueba en el orden de RN-01.
     */
    List<EntradaListaEsperaDTO> consultarLista(String tipoPruebaId);

    /**
     * Devuelve la posición (empezando en 1) de una entrada {@code EN_ESPERA} dentro de su lista.
     */
    int consultarPosicion(String idSolicitud);

    /**
     * Obtiene la entrada de una solicitud, o {@code null} si no existe.
     */
    EntradaListaEsperaDTO obtenerEntrada(String idSolicitud);
}
