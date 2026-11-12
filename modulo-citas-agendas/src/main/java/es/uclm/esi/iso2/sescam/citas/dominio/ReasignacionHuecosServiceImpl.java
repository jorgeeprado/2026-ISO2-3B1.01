package es.uclm.esi.iso2.sescam.citas.dominio;

import es.uclm.esi.iso2.sescam.common.dto.CitaDTO;
import es.uclm.esi.iso2.sescam.common.dto.EntradaListaEsperaDTO;
import es.uclm.esi.iso2.sescam.common.dto.TurnoAgendaDTO;
import es.uclm.esi.iso2.sescam.common.enums.EstadoTurno;
import es.uclm.esi.iso2.sescam.common.interfaces.CandidatosListaEsperaService;
import es.uclm.esi.iso2.sescam.common.interfaces.CitacionAdministrativaService;
import es.uclm.esi.iso2.sescam.common.interfaces.DisponibilidadAgendaService;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Servicio de dominio del CU-23 para reasignar un turno libre en las siguientes 48 horas. */
public class ReasignacionHuecosServiceImpl {

    static final int SIN_LIMITE = Integer.MAX_VALUE;

    private final DisponibilidadAgendaService disponibilidadAgendaService;
    private final CandidatosListaEsperaService candidatosListaEsperaService;
    private final CitacionAdministrativaService citacionAdministrativaService;
    private final Clock reloj;

    public ReasignacionHuecosServiceImpl(DisponibilidadAgendaService disponibilidadAgendaService,
                                         CandidatosListaEsperaService candidatosListaEsperaService,
                                         CitacionAdministrativaService citacionAdministrativaService,
                                         Clock reloj) {
        this.disponibilidadAgendaService = Objects.requireNonNull(disponibilidadAgendaService,
                "El servicio de disponibilidad de agendas no puede ser nulo");
        this.candidatosListaEsperaService = Objects.requireNonNull(candidatosListaEsperaService,
                "El servicio de candidatos de lista de espera no puede ser nulo");
        this.citacionAdministrativaService = Objects.requireNonNull(citacionAdministrativaService,
                "El servicio de citación administrativa no puede ser nulo");
        this.reloj = Objects.requireNonNull(reloj, "El reloj no puede ser nulo");
    }

    /**
     * Reasigna el turno al primer candidato compatible según RN-01.
     * La cita se persiste y se consolida a través del servicio oficial de CU-13.
     *
     * @return la cita creada, o vacío si el turno no está en la ventana estricta de 48 horas
     *         o no hay solicitudes compatibles en espera
     */
    public Optional<CitaDTO> reasignarHuecoLiberado(String idTurno, String idSolicitudCancelada) {
        validarTexto(idTurno, "El identificador del turno es obligatorio");
        validarTexto(idSolicitudCancelada, "El identificador de la solicitud cancelada es obligatorio");
        TurnoAgendaDTO turno = disponibilidadAgendaService.obtenerTurno(idTurno);
        if (turno == null) {
            throw new IllegalArgumentException("No existe el turno: " + idTurno);
        }
        if (turno.estado() != EstadoTurno.LIBRE) {
            throw new IllegalStateException("Solo se reasignan turnos libres; estado actual: " + turno.estado());
        }
        if (!estaEnVentanaDeReasignacion(turno)) {
            return Optional.empty();
        }

        List<EntradaListaEsperaDTO> candidatos = candidatosListaEsperaService
                .proponerCandidatos(idTurno, SIN_LIMITE);
        if (candidatos == null) {
            throw new IllegalStateException("El servicio de lista de espera devolvió candidatos nulos");
        }
        if (candidatos.isEmpty()) {
            return Optional.empty();
        }

        EntradaListaEsperaDTO seleccionado = null;
        for (EntradaListaEsperaDTO candidato : candidatos) {
            if (candidato == null || candidato.idSolicitud() == null || candidato.idSolicitud().isBlank()) {
                throw new IllegalStateException("La lista contiene un candidato sin identificador de solicitud válido");
            }
            if (!idSolicitudCancelada.equals(candidato.idSolicitud())) {
                seleccionado = candidato;
                break;
            }
        }
        if (seleccionado == null) {
            return Optional.empty();
        }

        String motivo = "CU-23: reasignación urgente tras cancelar la solicitud " + idSolicitudCancelada;
        CitaDTO cita = citacionAdministrativaService.asignarCita(seleccionado.idSolicitud(), idTurno, motivo);
        if (cita == null) {
            throw new IllegalStateException("El servicio de citación no devolvió la cita reasignada");
        }
        return Optional.of(cita);
    }

    private boolean estaEnVentanaDeReasignacion(TurnoAgendaDTO turno) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        LocalDateTime inicioTurno = LocalDateTime.of(turno.fecha(), turno.horaInicio());
        return inicioTurno.isAfter(ahora) && inicioTurno.isBefore(ahora.plusHours(48));
    }

    private static void validarTexto(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
    }
}