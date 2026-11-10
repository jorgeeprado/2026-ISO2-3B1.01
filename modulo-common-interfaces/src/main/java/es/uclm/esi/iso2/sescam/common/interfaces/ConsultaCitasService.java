package es.uclm.esi.iso2.sescam.common.interfaces;

import es.uclm.esi.iso2.sescam.common.dto.CitaDTO;

import java.util.List;

/**
 * Contrato de consulta de las citas gestionadas por la citación administrativa (CU-13), de solo lectura.
 * Pensado para clientes que no consolidan citas (CU-01, CU-08, CU-23 y otros módulos).
 * Devuelve copias: modificar el resultado no altera las citas almacenadas.
 */
public interface ConsultaCitasService {

    /**
     * Obtiene una cita por su identificador, o {@code null} si no existe.
     */
    CitaDTO obtenerCita(String idCita);

    /**
     * Devuelve todas las citas de una solicitud, canceladas incluidas, en orden de creación.
     */
    List<CitaDTO> listarCitasPorSolicitud(String idSolicitud);
}
