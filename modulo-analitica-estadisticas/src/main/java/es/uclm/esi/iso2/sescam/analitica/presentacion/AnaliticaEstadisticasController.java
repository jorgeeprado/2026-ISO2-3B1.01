package es.uclm.esi.iso2.sescam.analitica.presentacion;

import es.uclm.esi.iso2.sescam.common.dto.EstadisticaOperativaDTO;
import es.uclm.esi.iso2.sescam.common.interfaces.IAnaliticaEstadisticasService;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;

/**
 * Punto de entrada de presentación para consultas de analítica.
 *
 * <p>El módulo no declara un framework web; esta clase mantiene el límite de
 * presentación sin introducir una dependencia de transporte no configurada.</p>
 */
public class AnaliticaEstadisticasController {

    private final IAnaliticaEstadisticasService service;

    public AnaliticaEstadisticasController(IAnaliticaEstadisticasService service) {
        this.service = Objects.requireNonNull(service, "El servicio de analítica no puede ser nulo");
    }

    public Map<String, Object> consultarEstadisticasPrescripcionMedico(String medicoId) {
        validarIdentificador(medicoId, "medicoId");
        return service.consultarEstadisticasPrescripcionMedico(medicoId);
    }

    public EstadisticaOperativaDTO generarInformeOperativoAgendas(
            LocalDate fechaInicio, LocalDate fechaFin) {
        validarIntervalo(fechaInicio, fechaFin);
        return service.generarInformeOperativoAgendas(fechaInicio, fechaFin);
    }

    public Map<String, Object> generarCuadroMandoGlobal() {
        return service.generarCuadroMandoGlobal();
    }

    private static void validarIdentificador(String valor, String nombre) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(nombre + " es obligatorio");
        }
    }

    private static void validarIntervalo(LocalDate fechaInicio, LocalDate fechaFin) {
        Objects.requireNonNull(fechaInicio, "La fecha de inicio no puede ser nula");
        Objects.requireNonNull(fechaFin, "La fecha de fin no puede ser nula");
        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
    }
}
