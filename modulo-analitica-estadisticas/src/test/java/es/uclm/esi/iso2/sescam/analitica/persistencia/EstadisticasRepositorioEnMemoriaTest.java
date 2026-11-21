package es.uclm.esi.iso2.sescam.analitica.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.uclm.esi.iso2.sescam.common.dto.AgendaMedicaDTO;
import es.uclm.esi.iso2.sescam.common.dto.CitaDTO;
import es.uclm.esi.iso2.sescam.common.dto.EstadisticaOperativaDTO;
import es.uclm.esi.iso2.sescam.common.dto.SolicitudPruebaDTO;
import es.uclm.esi.iso2.sescam.common.enums.EstadoCita;
import es.uclm.esi.iso2.sescam.common.enums.EstadoSolicitud;
import es.uclm.esi.iso2.sescam.common.enums.ModalidadPrueba;
import es.uclm.esi.iso2.sescam.common.enums.Prioridad;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EstadisticasRepositorioEnMemoriaTest {

    private EstadisticasRepositorioEnMemoria repositorio;

    @BeforeEach
    void crearRepositorio() {
        repositorio = new EstadisticasRepositorioEnMemoria();
    }

    @Test
    void guardaYFiltraSolicitudesPorFacultativoEstadoYModalidad() {
        SolicitudPruebaDTO rx = solicitud("1", "medico-1", "rx-id", EstadoSolicitud.CITADA);
        SolicitudPruebaDTO rm = solicitud("2", "medico-2", "rm-id", EstadoSolicitud.COMPLETADA);
        repositorio.guardarSolicitud(rx);
        repositorio.guardarSolicitud(rm);
        repositorio.registrarModalidadTipoPrueba("rx-id", ModalidadPrueba.RX);
        repositorio.registrarModalidadTipoPrueba("rm-id", ModalidadPrueba.RM);

        assertEquals(List.of("1"), ids(repositorio.buscarSolicitudesPorMedico("medico-1")));
        assertEquals(List.of("1"), ids(repositorio.buscarSolicitudesPorEstado(EstadoSolicitud.CITADA)));
        assertEquals(List.of("2"), ids(repositorio.buscarSolicitudesPorModalidad(ModalidadPrueba.RM)));
        assertEquals(2, repositorio.buscarTodasLasSolicitudes().size());
    }

    @Test
    void actualizaPorIdYDevuelveCopiasAisladas() {
        SolicitudPruebaDTO inicial = solicitud("1", "medico-1", "rx-id", EstadoSolicitud.CITADA);
        repositorio.guardarSolicitud(inicial);
        inicial.setMedicoPrescriptorId("medico-alterado");

        SolicitudPruebaDTO recuperada = repositorio.buscarSolicitudesPorMedico("medico-1").get(0);
        assertNotSame(inicial, recuperada);
        recuperada.setEstado(EstadoSolicitud.CANCELADA);
        assertEquals(EstadoSolicitud.CITADA,
                repositorio.buscarSolicitudesPorMedico("medico-1").get(0).getEstado());

        repositorio.guardarSolicitud(solicitud("1", "medico-2", "rx-id", EstadoSolicitud.COMPLETADA));
        assertEquals(0, repositorio.buscarSolicitudesPorMedico("medico-1").size());
        assertEquals(1, repositorio.buscarSolicitudesPorMedico("medico-2").size());
    }

    @Test
    void validaDatosDeEntradaParaSolicitudesYModalidades() {
        assertThrows(NullPointerException.class, () -> repositorio.guardarSolicitud(null));
        assertThrows(IllegalArgumentException.class,
                () -> repositorio.guardarSolicitud(solicitud(" ", "medico-1", "rx", EstadoSolicitud.CITADA)));
        assertThrows(IllegalArgumentException.class, () -> repositorio.buscarSolicitudesPorMedico(" "));
        assertThrows(NullPointerException.class, () -> repositorio.buscarSolicitudesPorModalidad(null));
        assertThrows(NullPointerException.class, () -> repositorio.buscarSolicitudesPorEstado(null));
        assertThrows(IllegalArgumentException.class,
                () -> repositorio.registrarModalidadTipoPrueba(" ", ModalidadPrueba.RX));
        assertThrows(NullPointerException.class,
                () -> repositorio.registrarModalidadTipoPrueba("rx", null));
    }

    @Test
    void guardaYFiltraEstadisticasOperativasPorIntervalo() {
        LocalDate desde = LocalDate.of(2026, 11, 1);
        repositorio.guardarEstadisticaOperativa(
                new EstadisticaOperativaDTO(desde, desde.plusDays(4), 10, 8, 1, 2.0, 80.0, 3));
        repositorio.guardarEstadisticaOperativa(
                new EstadisticaOperativaDTO(desde.plusDays(10), desde.plusDays(12), 5, 5, 0, 1.0, 100.0, 0));

        List<EstadisticaOperativaDTO> filtradas =
                repositorio.buscarEstadisticasOperativas(desde.plusDays(4), desde.plusDays(5));

        assertEquals(1, filtradas.size());
        assertEquals(10, filtradas.get(0).getTotalCitasProgramadas());
        filtradas.get(0).setTotalCitasProgramadas(99);
        assertEquals(10, repositorio.buscarTodasLasEstadisticasOperativas().get(0).getTotalCitasProgramadas());
    }

    @Test
    void guardaYFiltraCitasAgendasYSolicitudesPorPeriodo() {
        LocalDate dia = LocalDate.of(2026, 11, 9);
        SolicitudPruebaDTO solicitud = solicitud("s1", "medico-1", "rx-id", EstadoSolicitud.CITADA);
        solicitud.setFechaSolicitud(dia.atTime(8, 0));
        repositorio.guardarSolicitud(solicitud);
        repositorio.guardarSolicitud(solicitud("s2", "medico-1", "rx-id", EstadoSolicitud.PENDIENTE_CITACION));
        CitaDTO cita = new CitaDTO("c1", "s1", "p1", "rx-id", dia.atTime(10, 0),
                30, "sala-1", "medico-1", EstadoCita.PROGRAMADA, null);
        repositorio.guardarCita(cita);
        repositorio.guardarAgenda(new AgendaMedicaDTO(
                "a1", "sala-1", "rx-id", dia, LocalTime.of(8, 0), LocalTime.of(12, 0), 30));

        assertEquals(1, repositorio.buscarCitasEnPeriodo(dia, dia).size());
        assertEquals(1, repositorio.buscarAgendasEnPeriodo(dia, dia).size());
        assertEquals(List.of("s1"), repositorio.buscarSolicitudesEnPeriodo(dia, dia).stream()
                .map(SolicitudPruebaDTO::getId).toList());
        assertEquals("s1", repositorio.buscarSolicitudPorId("s1").getId());
        assertNull(repositorio.buscarSolicitudPorId("no-existe"));
        assertThrows(IllegalArgumentException.class, () -> repositorio.buscarCitasEnPeriodo(dia, dia.minusDays(1)));
        assertThrows(IllegalArgumentException.class,
                () -> repositorio.guardarAgenda(new AgendaMedicaDTO(
                        "a2", "sala-1", "rx-id", dia, LocalTime.of(8, 0), LocalTime.of(9, 0), 0)));
        assertThrows(NullPointerException.class, () -> repositorio.guardarCita(null));
        assertThrows(IllegalArgumentException.class,
                () -> repositorio.guardarCita(new CitaDTO(
                        " ", "s1", "p1", "rx-id", LocalDateTime.now(), 30,
                        "sala-1", "medico-1", EstadoCita.PROGRAMADA, null)));
    }

    @Test
    void rechazaEstadisticasEIntervalosInvalidos() {
        assertThrows(NullPointerException.class, () -> repositorio.guardarEstadisticaOperativa(null));
        assertThrows(IllegalArgumentException.class,
                () -> repositorio.guardarEstadisticaOperativa(
                        new EstadisticaOperativaDTO(null, LocalDate.now(), 0, 0, 0, 0, 0, 0)));
        assertThrows(IllegalArgumentException.class,
                () -> repositorio.buscarEstadisticasOperativas(LocalDate.now(), LocalDate.now().minusDays(1)));
        assertThrows(NullPointerException.class,
                () -> repositorio.buscarEstadisticasOperativas(null, LocalDate.now()));
    }

    private static List<String> ids(List<SolicitudPruebaDTO> solicitudes) {
        return solicitudes.stream().map(SolicitudPruebaDTO::getId).toList();
    }

    private static SolicitudPruebaDTO solicitud(
            String id, String medicoId, String tipoId, EstadoSolicitud estado) {
        return new SolicitudPruebaDTO(
                id, "paciente", medicoId, tipoId, Prioridad.ORDINARIO, null, null,
                LocalDate.of(2026, 11, 1).atStartOfDay(), estado, false, null);
    }
}
