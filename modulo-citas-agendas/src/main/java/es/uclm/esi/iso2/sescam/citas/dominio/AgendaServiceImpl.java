package es.uclm.esi.iso2.sescam.citas.dominio;

import es.uclm.esi.iso2.sescam.common.dto.AgendaMedicaDTO;
import es.uclm.esi.iso2.sescam.common.interfaces.AgendaService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgendaServiceImpl implements AgendaService {

    private final Map<String, AgendaMedicaDTO> agendasPorId = new HashMap<>();

    @Override
    public AgendaMedicaDTO crearAgenda(AgendaMedicaDTO agenda) {
        validarAgenda(agenda);
        agendasPorId.put(agenda.idAgenda(), agenda);
        return agenda;
    }

    @Override
    public AgendaMedicaDTO obtenerAgendaPorId(String idAgenda) {
        validarTextoObligatorio(idAgenda, "El identificador de la agenda es obligatorio");
        return agendasPorId.get(idAgenda);
    }

    @Override
    public List<AgendaMedicaDTO> listarAgendasPorMedico(String idMedico) {
        validarTextoObligatorio(idMedico, "El identificador del médico o recurso es obligatorio");
        List<AgendaMedicaDTO> agendas = new ArrayList<>();
        for (AgendaMedicaDTO agenda : agendasPorId.values()) {
            if (agenda.idMedico().equals(idMedico)) {
                agendas.add(agenda);
            }
        }
        return agendas;
    }

    @Override
    public List<AgendaMedicaDTO> listarAgendasPorFecha(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha de la agenda no puede ser nula");
        }
        List<AgendaMedicaDTO> agendas = new ArrayList<>();
        for (AgendaMedicaDTO agenda : agendasPorId.values()) {
            if (agenda.fecha().equals(fecha)) {
                agendas.add(agenda);
            }
        }
        return agendas;
    }

    @Override
    public boolean eliminarAgenda(String idAgenda) {
        validarTextoObligatorio(idAgenda, "El identificador de la agenda es obligatorio");
        return agendasPorId.remove(idAgenda) != null;
    }

    private void validarAgenda(AgendaMedicaDTO agenda) {
        if (agenda == null) {
            throw new IllegalArgumentException("La agenda no puede ser nula");
        }
        validarTextoObligatorio(agenda.idAgenda(), "El identificador de la agenda es obligatorio");
        validarTextoObligatorio(agenda.idMedico(), "El identificador del médico o recurso es obligatorio");
        if (agenda.fecha() == null) {
            throw new IllegalArgumentException("La fecha de la agenda no puede ser nula");
        }
        if (agenda.horaInicio() == null || agenda.horaFin() == null
                || !agenda.horaInicio().isBefore(agenda.horaFin())) {
            throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de fin");
        }
        if (agenda.intervaloMinutos() <= 0) {
            throw new IllegalArgumentException("El intervalo debe ser mayor que cero");
        }
    }

    private void validarTextoObligatorio(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
    }
}
