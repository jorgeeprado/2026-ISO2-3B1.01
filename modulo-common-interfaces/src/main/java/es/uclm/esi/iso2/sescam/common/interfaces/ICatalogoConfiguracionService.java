package es.uclm.esi.iso2.sescam.common.interfaces;

import es.uclm.esi.iso2.sescam.common.dto.PruebaCatalogoDTO;
import java.util.List;

/**
 * Contrato de servicios del Módulo 6 (modulo-catalogo-configuracion).
 * Responsables: Pedro de la Fuente y Pablo Sánchez.
 * Casos de uso: CU-17.
 */
public interface ICatalogoConfiguracionService {

    /**
     * Obtiene la especificación de un tipo de prueba del catálogo por ID o código.
     */
    PruebaCatalogoDTO obtenerTipoPrueba(String tipoPruebaId);

    /**
     * Lista todos los tipos de prueba activos en el catálogo de radiodiagnóstico (RF-17).
     */
    List<PruebaCatalogoDTO> listarCatalogoActivo();

    /**
     * Crea o actualiza un tipo de prueba con sus parámetros técnicos y de citación (CU-17).
     */
    PruebaCatalogoDTO guardarTipoPrueba(PruebaCatalogoDTO prueba);

    /**
     * Da de baja lógica un tipo de prueba del catálogo.
     */
    boolean desactivarTipoPrueba(String tipoPruebaId);
}

