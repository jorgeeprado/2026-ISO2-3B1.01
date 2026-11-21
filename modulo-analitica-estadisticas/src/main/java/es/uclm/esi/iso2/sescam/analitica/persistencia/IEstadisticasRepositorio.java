package es.uclm.esi.iso2.sescam.analitica.persistencia;

import es.uclm.esi.iso2.sescam.common.dto.EstadisticaOperativaDTO;
import es.uclm.esi.iso2.sescam.common.dto.AgendaMedicaDTO;
import es.uclm.esi.iso2.sescam.common.dto.CitaDTO;
import es.uclm.esi.iso2.sescam.common.dto.SolicitudPruebaDTO;
import es.uclm.esi.iso2.sescam.common.enums.EstadoSolicitud;
import es.uclm.esi.iso2.sescam.common.enums.ModalidadPrueba;
import java.time.LocalDate;
import java.util.List;

/**
 * Puerto de persistencia para los datos consultados por el módulo de analítica.
 */
public interface IEstadisticasRepositorio {

    void guardarSolicitud(SolicitudPruebaDTO solicitud);

    List<SolicitudPruebaDTO> buscarSolicitudesPorMedico(String medicoId);

    List<SolicitudPruebaDTO> buscarSolicitudesPorModalidad(ModalidadPrueba modalidad);

    List<SolicitudPruebaDTO> buscarSolicitudesPorEstado(EstadoSolicitud estado);

    List<SolicitudPruebaDTO> buscarTodasLasSolicitudes();

    SolicitudPruebaDTO buscarSolicitudPorId(String solicitudId);

    List<SolicitudPruebaDTO> buscarSolicitudesEnPeriodo(LocalDate fechaInicio, LocalDate fechaFin);

    void registrarModalidadTipoPrueba(String tipoPruebaId, ModalidadPrueba modalidad);

    void guardarCita(CitaDTO cita);

    List<CitaDTO> buscarCitasEnPeriodo(LocalDate fechaInicio, LocalDate fechaFin);

    void guardarAgenda(AgendaMedicaDTO agenda);

    List<AgendaMedicaDTO> buscarAgendasEnPeriodo(LocalDate fechaInicio, LocalDate fechaFin);

    void guardarEstadisticaOperativa(EstadisticaOperativaDTO estadistica);

    List<EstadisticaOperativaDTO> buscarEstadisticasOperativas(LocalDate fechaInicio, LocalDate fechaFin);

    List<EstadisticaOperativaDTO> buscarTodasLasEstadisticasOperativas();
}
