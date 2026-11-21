package es.uclm.esi.iso2.sescam.citas.dominio;

import es.uclm.esi.iso2.sescam.common.dto.CitaDTO;
import es.uclm.esi.iso2.sescam.common.enums.EstadoCita;
import es.uclm.esi.iso2.sescam.common.interfaces.CitaService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CitaServiceImpl implements CitaService {

    private final Map<String, CitaDTO> citasPorId = new HashMap<>();

    @Override
    public CitaDTO programarCita(CitaDTO cita) {
        validarCita(cita);
        if (citasPorId.containsKey(cita.getId())) {
            throw new IllegalArgumentException("Ya existe una cita con ese identificador");
        }

        CitaDTO almacenada = copiarCita(cita);
        citasPorId.put(almacenada.getId(), almacenada);
        return copiarCita(almacenada);
    }

    @Override
    public CitaDTO modificarCita(CitaDTO cita) {
        validarCita(cita);
        if (!citasPorId.containsKey(cita.getId())) {
            throw new IllegalArgumentException("No existe una cita con ese identificador");
        }

        CitaDTO almacenada = copiarCita(cita);
        citasPorId.put(almacenada.getId(), almacenada);
        return copiarCita(almacenada);
    }

    @Override
    public boolean cancelarCita(String idCita, String motivoCancelacion) {
        validarTextoObligatorio(idCita, "El identificador de la cita es obligatorio");
        validarTextoObligatorio(motivoCancelacion, "El motivo de cancelación es obligatorio");

        CitaDTO cita = citasPorId.get(idCita);
        if (cita == null) {
            return false;
        }

        cita.setEstado(EstadoCita.CANCELADA);
        cita.setMotivoCancelacion(motivoCancelacion);
        return true;
    }

    @Override
    public CitaDTO obtenerCitaPorId(String idCita) {
        validarTextoObligatorio(idCita, "El identificador de la cita es obligatorio");
        CitaDTO cita = citasPorId.get(idCita);
        return cita == null ? null : copiarCita(cita);
    }

    @Override
    public List<CitaDTO> listarCitasPorPaciente(String pacienteId) {
        validarTextoObligatorio(pacienteId, "El identificador del paciente es obligatorio");
        List<CitaDTO> citas = new ArrayList<>();
        for (CitaDTO cita : citasPorId.values()) {
            if (cita.getPacienteId().equals(pacienteId)) {
                citas.add(copiarCita(cita));
            }
        }
        return citas;
    }

    private void validarCita(CitaDTO cita) {
        if (cita == null) {
            throw new IllegalArgumentException("La cita no puede ser nula");
        }
        validarTextoObligatorio(cita.getId(), "El identificador de la cita es obligatorio");
        validarTextoObligatorio(cita.getSolicitudId(), "El identificador de la solicitud es obligatorio");
        validarTextoObligatorio(cita.getPacienteId(), "El identificador del paciente es obligatorio");
        validarTextoObligatorio(cita.getTipoPruebaId(), "El identificador del tipo de prueba es obligatorio");
        if (cita.getFechaHora() == null) {
            throw new IllegalArgumentException("La fecha y hora de la cita son obligatorias");
        }
        if (cita.getDuracionMinutos() <= 0) {
            throw new IllegalArgumentException("La duración de la cita debe ser mayor que cero");
        }
        validarTextoObligatorio(cita.getSalaId(), "El identificador de la sala es obligatorio");
        validarTextoObligatorio(cita.getMedicoRadiologoId(),
                "El identificador del médico o radiólogo es obligatorio");
        if (cita.getEstado() == null) {
            throw new IllegalArgumentException("El estado de la cita es obligatorio");
        }
    }

    private void validarTextoObligatorio(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
    }

    private CitaDTO copiarCita(CitaDTO cita) {
        return new CitaDTO(
                cita.getId(),
                cita.getSolicitudId(),
                cita.getPacienteId(),
                cita.getTipoPruebaId(),
                cita.getFechaHora(),
                cita.getDuracionMinutos(),
                cita.getSalaId(),
                cita.getMedicoRadiologoId(),
                cita.getEstado(),
                cita.getMotivoCancelacion()
        );
    }
}
