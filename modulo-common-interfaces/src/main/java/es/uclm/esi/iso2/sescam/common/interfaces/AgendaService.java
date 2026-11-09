package es.uclm.esi.iso2.sescam.common.interfaces.services;

import es.uclm.esi.iso2.sescam.common.interfaces.dto.AgendaMedicaDTO;

import java.time.LocalDate;
import java.util.List;

public interface AgendaService {
    AgendaMedicaDTO crearAgenda(AgendaMedicaDTO agenda);
    AgendaMedicaDTO obtenerAgendaPorId(String idAgenda);
    List<AgendaMedicaDTO> listarAgendasPorMedico(String idMedico);
    List<AgendaMedicaDTO> listarAgendasPorFecha(LocalDate fecha);
    boolean eliminarAgenda(String idAgenda);
}