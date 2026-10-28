package es.uclm.esi.iso2.sescam.common.interfaces;

import es.uclm.esi.iso2.sescam.common.dto.EstadisticaOperativaDTO;
import java.time.LocalDate;
import java.util.Map;

/**
 * Contrato de analítica y cuadros de mando del Módulo 7 (modulo-analitica-estadisticas).
 * Responsable: Javier Gómez-Lobo Ramírez.
 * Casos de uso: CU-11, CU-16, CU-21.
 */
public interface IAnaliticaEstadisticasService {

    /**
     * Genera métricas clínicas para facultativos prescriptores (CU-11 / RF-11).
     */
    Map<String, Object> consultarEstadisticasPrescripcionMedico(String medicoId);

    /**
     * Genera reportes operativos sobre ocupación, demoras y cancelaciones para Admisión (CU-16 / RF-16).
     */
    EstadisticaOperativaDTO generarInformeOperativoAgendas(LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Consolida el cuadro de mando de estadísticas globales del servicio para Dirección y Administradores (CU-21 / RF-21).
     */
    Map<String, Object> generarCuadroMandoGlobal();
}
