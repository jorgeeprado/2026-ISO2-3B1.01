package es.uclm.esi.iso2.sescam.citas.dominio;

import es.uclm.esi.iso2.sescam.citas.persistencia.IListaEsperaRepository;
import es.uclm.esi.iso2.sescam.citas.persistencia.ListaEsperaRepositoryInMemory;
import es.uclm.esi.iso2.sescam.common.dto.AgendaMedicaDTO;
import es.uclm.esi.iso2.sescam.common.dto.EntradaListaEsperaDTO;
import es.uclm.esi.iso2.sescam.common.dto.SolicitudPruebaDTO;
import es.uclm.esi.iso2.sescam.common.dto.TurnoAgendaDTO;
import es.uclm.esi.iso2.sescam.common.enums.EstadoEntradaEspera;
import es.uclm.esi.iso2.sescam.common.enums.EstadoSolicitud;
import es.uclm.esi.iso2.sescam.common.enums.EstadoTurno;
import es.uclm.esi.iso2.sescam.common.enums.Prioridad;
import es.uclm.esi.iso2.sescam.common.interfaces.AgendaService;
import es.uclm.esi.iso2.sescam.common.interfaces.CandidatosListaEsperaService;
import es.uclm.esi.iso2.sescam.common.interfaces.DisponibilidadAgendaService;
import es.uclm.esi.iso2.sescam.common.interfaces.GestionListaEsperaService;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Controlador de dominio del CU-15: listas de espera por tipo de prueba ordenadas según RN-01
 * y propuesta de candidatos para los huecos libres de las agendas de CU-14 (D-020).
 * No ocupa turnos: la asignación formal del hueco corresponde a CU-13 (RN-02).
 */
public class ListaEsperaServiceImpl implements GestionListaEsperaService, CandidatosListaEsperaService {

    private static final OrdenListaEspera ORDEN_RN01 = new OrdenListaEspera();

    private final IListaEsperaRepository listaEsperaRepository;
    private final DisponibilidadAgendaService disponibilidadAgendaService;
    private final AgendaService agendaService;
    private final Clock reloj;

    public ListaEsperaServiceImpl(DisponibilidadAgendaService disponibilidadAgendaService,
                                  AgendaService agendaService) {
        this(new ListaEsperaRepositoryInMemory(), disponibilidadAgendaService, agendaService,
                Clock.systemDefaultZone());
    }

    public ListaEsperaServiceImpl(IListaEsperaRepository listaEsperaRepository,
                                  DisponibilidadAgendaService disponibilidadAgendaService,
                                  AgendaService agendaService,
                                  Clock reloj) {
        this.listaEsperaRepository = Objects.requireNonNull(listaEsperaRepository,
                "El repositorio de la lista de espera no puede ser nulo");
        this.disponibilidadAgendaService = Objects.requireNonNull(disponibilidadAgendaService,
                "El servicio de disponibilidad de agendas no puede ser nulo");
        this.agendaService = Objects.requireNonNull(agendaService, "El servicio de agendas no puede ser nulo");
        this.reloj = Objects.requireNonNull(reloj, "El reloj no puede ser nulo");
    }

    // ------------------------------------------------------------------ GestionListaEsperaService

    @Override
    public EntradaListaEsperaDTO inscribir(SolicitudPruebaDTO solicitud) {
        if (solicitud == null) {
            throw new IllegalArgumentException("La solicitud no puede ser nula");
        }
        validarTexto(solicitud.getId(), "El identificador de la solicitud es obligatorio");
        validarTexto(solicitud.getPacienteId(), "El identificador del paciente es obligatorio");
        validarTexto(solicitud.getTipoPruebaId(), "El identificador del tipo de prueba es obligatorio");
        if (solicitud.getPrioridad() == null) {
            throw new IllegalArgumentException("La prioridad clínica es obligatoria");
        }
        if (solicitud.getFechaSolicitud() == null) {
            throw new IllegalArgumentException("La fecha de prescripción es obligatoria");
        }
        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE_CITACION) {
            throw new IllegalArgumentException("Solo se inscriben solicitudes pendientes de citación");
        }
        if (listaEsperaRepository.buscarPorSolicitud(solicitud.getId()).isPresent()) {
            throw new IllegalArgumentException("La solicitud ya está inscrita en la lista de espera: "
                    + solicitud.getId());
        }
        EntradaListaEsperaDTO entrada = new EntradaListaEsperaDTO(
                solicitud.getId(),
                solicitud.getPacienteId(),
                solicitud.getTipoPruebaId(),
                solicitud.getPrioridad(),
                solicitud.getFechaSolicitud(),
                LocalDateTime.now(reloj),
                EstadoEntradaEspera.EN_ESPERA,
                null,
                null);
        return listaEsperaRepository.guardar(entrada);
    }

    @Override
    public EntradaListaEsperaDTO cambiarPrioridad(String idSolicitud, Prioridad nuevaPrioridad, String motivo) {
        if (nuevaPrioridad == null) {
            throw new IllegalArgumentException("La nueva prioridad clínica es obligatoria");
        }
        validarTexto(motivo, "El motivo del cambio de prioridad es obligatorio");
        EntradaListaEsperaDTO entrada = buscarExistente(idSolicitud);
        exigirEstado(entrada, EstadoEntradaEspera.EN_ESPERA);
        return listaEsperaRepository.guardar(copiar(entrada, nuevaPrioridad, EstadoEntradaEspera.EN_ESPERA,
                null, motivo));
    }

    @Override
    public EntradaListaEsperaDTO retirar(String idSolicitud, String motivo) {
        validarTexto(motivo, "El motivo de la retirada es obligatorio");
        EntradaListaEsperaDTO entrada = buscarExistente(idSolicitud);
        exigirEstado(entrada, EstadoEntradaEspera.EN_ESPERA);
        return listaEsperaRepository.guardar(copiar(entrada, entrada.prioridad(), EstadoEntradaEspera.RETIRADA,
                null, motivo));
    }

    @Override
    public List<EntradaListaEsperaDTO> consultarLista(String tipoPruebaId) {
        validarTexto(tipoPruebaId, "El identificador del tipo de prueba es obligatorio");
        return ordenarEnEspera(listaEsperaRepository.buscarPorTipoPrueba(tipoPruebaId), Integer.MAX_VALUE);
    }

    @Override
    public int consultarPosicion(String idSolicitud) {
        EntradaListaEsperaDTO entrada = buscarExistente(idSolicitud);
        exigirEstado(entrada, EstadoEntradaEspera.EN_ESPERA);
        List<EntradaListaEsperaDTO> lista = consultarLista(entrada.tipoPruebaId());
        int posicion = 1;
        for (EntradaListaEsperaDTO e : lista) {
            if (e.idSolicitud().equals(idSolicitud)) {
                return posicion;
            }
            posicion++;
        }
        throw new IllegalStateException("La entrada no figura en la lista de su tipo de prueba: " + idSolicitud);
    }

    @Override
    public EntradaListaEsperaDTO obtenerEntrada(String idSolicitud) {
        validarTexto(idSolicitud, "El identificador de la solicitud es obligatorio");
        return listaEsperaRepository.buscarPorSolicitud(idSolicitud).orElse(null);
    }

    // ------------------------------------------------------------------ CandidatosListaEsperaService

    @Override
    public List<EntradaListaEsperaDTO> proponerCandidatos(String idTurno, int maximo) {
        validarTexto(idTurno, "El identificador del turno es obligatorio");
        if (maximo <= 0) {
            throw new IllegalArgumentException("El número máximo de candidatos debe ser positivo");
        }
        TurnoAgendaDTO turno = disponibilidadAgendaService.obtenerTurno(idTurno);
        if (turno == null) {
            throw new IllegalArgumentException("No existe el turno: " + idTurno);
        }
        if (turno.estado() != EstadoTurno.LIBRE) {
            throw new IllegalStateException("Solo se proponen candidatos para turnos libres; estado actual: "
                    + turno.estado());
        }
        AgendaMedicaDTO franja = agendaService.obtenerAgendaPorId(turno.idAgenda());
        if (franja == null) {
            throw new IllegalStateException("No existe la franja del turno: " + turno.idAgenda());
        }
        String pruebaAsociada = franja.idPruebaAsociada();
        List<EntradaListaEsperaDTO> compatibles = (pruebaAsociada == null || pruebaAsociada.isBlank())
                ? listaEsperaRepository.buscarPorEstado(EstadoEntradaEspera.EN_ESPERA)
                : listaEsperaRepository.buscarPorTipoPrueba(pruebaAsociada);
        return ordenarEnEspera(compatibles, maximo);
    }

    @Override
    public EntradaListaEsperaDTO marcarCitada(String idSolicitud, String idCita) {
        validarTexto(idCita, "El identificador de la cita es obligatorio");
        EntradaListaEsperaDTO entrada = buscarExistente(idSolicitud);
        exigirEstado(entrada, EstadoEntradaEspera.EN_ESPERA);
        return listaEsperaRepository.guardar(copiar(entrada, entrada.prioridad(), EstadoEntradaEspera.CITADA,
                idCita, entrada.motivo()));
    }

    @Override
    public EntradaListaEsperaDTO reincorporar(String idSolicitud, String motivo) {
        validarTexto(motivo, "El motivo de la reincorporación es obligatorio");
        EntradaListaEsperaDTO entrada = buscarExistente(idSolicitud);
        exigirEstado(entrada, EstadoEntradaEspera.CITADA);
        return listaEsperaRepository.guardar(copiar(entrada, entrada.prioridad(), EstadoEntradaEspera.EN_ESPERA,
                null, motivo));
    }

    // ------------------------------------------------------------------ Auxiliares

    private EntradaListaEsperaDTO buscarExistente(String idSolicitud) {
        validarTexto(idSolicitud, "El identificador de la solicitud es obligatorio");
        return listaEsperaRepository.buscarPorSolicitud(idSolicitud)
                .orElseThrow(() -> new IllegalArgumentException(
                        "La solicitud no está en la lista de espera: " + idSolicitud));
    }

    private static void exigirEstado(EntradaListaEsperaDTO entrada, EstadoEntradaEspera requerido) {
        if (entrada.estado() != requerido) {
            throw new IllegalStateException("La entrada " + entrada.idSolicitud() + " debe estar en estado "
                    + requerido + "; estado actual: " + entrada.estado());
        }
    }

    /** Filtra las entradas EN_ESPERA, las ordena según RN-01 y devuelve como máximo {@code maximo}. */
    private static List<EntradaListaEsperaDTO> ordenarEnEspera(List<EntradaListaEsperaDTO> entradas, int maximo) {
        return entradas.stream()
                .filter(e -> e.estado() == EstadoEntradaEspera.EN_ESPERA)
                .sorted(ORDEN_RN01)
                .limit(maximo)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /** Copia una entrada conservando su fecha de prescripción y de inscripción (RN-01). */
    private static EntradaListaEsperaDTO copiar(EntradaListaEsperaDTO e, Prioridad prioridad,
                                               EstadoEntradaEspera estado, String idCita, String motivo) {
        return new EntradaListaEsperaDTO(e.idSolicitud(), e.pacienteId(), e.tipoPruebaId(), prioridad,
                e.fechaPrescripcion(), e.fechaInscripcion(), estado, idCita, motivo);
    }

    private static void validarTexto(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
    }
}
