package es.uclm.esi.iso2.sescam.citas.presentacion;

import es.uclm.esi.iso2.sescam.common.dto.EntradaListaEsperaDTO;
import es.uclm.esi.iso2.sescam.common.dto.SolicitudPruebaDTO;
import es.uclm.esi.iso2.sescam.common.dto.TurnoAgendaDTO;
import es.uclm.esi.iso2.sescam.common.enums.Prioridad;
import es.uclm.esi.iso2.sescam.common.enums.TipoRecurso;
import es.uclm.esi.iso2.sescam.common.interfaces.CandidatosListaEsperaService;
import es.uclm.esi.iso2.sescam.common.interfaces.DisponibilidadAgendaService;
import es.uclm.esi.iso2.sescam.common.interfaces.GestionListaEsperaService;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Controlador de presentación del CU-15 para el Personal Administrativo de Admisión.
 * Gestiona las listas de espera y, para un hueco libre de las agendas de CU-14,
 * muestra los candidatos en el orden de RN-01. La asignación del hueco se hace en CU-13.
 */
public class ListaEsperaController {

    private final GestionListaEsperaService gestionListaEsperaService;
    private final CandidatosListaEsperaService candidatosListaEsperaService;
    private final DisponibilidadAgendaService disponibilidadAgendaService;

    public ListaEsperaController(GestionListaEsperaService gestionListaEsperaService,
                                 CandidatosListaEsperaService candidatosListaEsperaService,
                                 DisponibilidadAgendaService disponibilidadAgendaService) {
        this.gestionListaEsperaService = Objects.requireNonNull(gestionListaEsperaService,
                "El servicio de gestión de la lista de espera no puede ser nulo");
        this.candidatosListaEsperaService = Objects.requireNonNull(candidatosListaEsperaService,
                "El servicio de candidatos de la lista de espera no puede ser nulo");
        this.disponibilidadAgendaService = Objects.requireNonNull(disponibilidadAgendaService,
                "El servicio de disponibilidad de agendas no puede ser nulo");
    }

    public EntradaListaEsperaDTO inscribir(SolicitudPruebaDTO solicitud) {
        return gestionListaEsperaService.inscribir(solicitud);
    }

    public EntradaListaEsperaDTO cambiarPrioridad(String idSolicitud, Prioridad nuevaPrioridad, String motivo) {
        return gestionListaEsperaService.cambiarPrioridad(idSolicitud, nuevaPrioridad, motivo);
    }

    public EntradaListaEsperaDTO retirar(String idSolicitud, String motivo) {
        return gestionListaEsperaService.retirar(idSolicitud, motivo);
    }

    public List<EntradaListaEsperaDTO> consultarLista(String tipoPruebaId) {
        return gestionListaEsperaService.consultarLista(tipoPruebaId);
    }

    public int consultarPosicion(String idSolicitud) {
        return gestionListaEsperaService.consultarPosicion(idSolicitud);
    }

    public EntradaListaEsperaDTO obtenerEntrada(String idSolicitud) {
        return gestionListaEsperaService.obtenerEntrada(idSolicitud);
    }

    public List<TurnoAgendaDTO> consultarHuecosLibres(TipoRecurso tipoRecurso, String idRecurso, LocalDate fecha) {
        return disponibilidadAgendaService.consultarHuecosLibres(tipoRecurso, idRecurso, fecha);
    }

    public List<EntradaListaEsperaDTO> proponerCandidatos(String idTurno, int maximo) {
        return candidatosListaEsperaService.proponerCandidatos(idTurno, maximo);
    }
}
