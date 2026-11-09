package es.uclm.esi.iso2.sescam.analitica.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.uclm.esi.iso2.sescam.analitica.persistencia.IEstadisticasRepositorio;
import es.uclm.esi.iso2.sescam.common.dto.AgendaMedicaDTO;
import es.uclm.esi.iso2.sescam.common.dto.CitaDTO;
import es.uclm.esi.iso2.sescam.common.dto.EstadisticaOperativaDTO;
import es.uclm.esi.iso2.sescam.common.dto.PruebaCatalogoDTO;
import es.uclm.esi.iso2.sescam.common.dto.SolicitudPruebaDTO;
import es.uclm.esi.iso2.sescam.common.enums.EstadoSolicitud;
import es.uclm.esi.iso2.sescam.common.enums.EstadoCita;
import es.uclm.esi.iso2.sescam.common.enums.ModalidadPrueba;
import es.uclm.esi.iso2.sescam.common.enums.Prioridad;
import es.uclm.esi.iso2.sescam.common.interfaces.ICatalogoConfiguracionService;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnaliticaEstadisticasServiceImplTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 9, 12, 0);

    @Mock
    private IEstadisticasRepositorio repositorio;

    @Mock
    private ICatalogoConfiguracionService catalogo;

    @Mock
    private Clock clock;

    @InjectMocks
    private AnaliticaEstadisticasServiceImpl service;

    @BeforeEach
    void configurarReloj() {
        lenient().when(clock.instant()).thenReturn(AHORA.toInstant(ZoneOffset.UTC));
        lenient().when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    }

    @Test
    void calculaEsperaMediaDeSolicitudesPendientesYCitada() {
        List<SolicitudPruebaDTO> solicitudes = List.of(
                solicitud("1", "RX", Prioridad.URGENTE, EstadoSolicitud.PENDIENTE_CITACION,
                        AHORA.minusDays(10), false),
                solicitud("2", "RM", Prioridad.PREFERENTE, EstadoSolicitud.CITADA,
                        AHORA.minusDays(4), false),
                solicitud("3", "RX", Prioridad.ORDINARIO, EstadoSolicitud.COMPLETADA,
                        AHORA.minusDays(20), false));
        prepararCatalogo(solicitudes, ModalidadPrueba.RX, ModalidadPrueba.RM);
        when(repositorio.buscarSolicitudesPorMedico("medico-1")).thenReturn(solicitudes);

        Map<String, Object> resultado = service.consultarEstadisticasPrescripcionMedico("medico-1");

        assertEquals(3, resultado.get("totalSolicitudes"));
        assertEquals(7.0, (double) resultado.get("tiempoMedioEsperaDias"), 0.0001);
        @SuppressWarnings("unchecked")
        Map<String, Double> porPrioridad =
                (Map<String, Double>) resultado.get("tiempoMedioEsperaDiasPorPrioridad");
        assertEquals(10.0, porPrioridad.get("URGENTE"), 0.0001);
        assertEquals(4.0, porPrioridad.get("PREFERENTE"), 0.0001);
        assertEquals(0.0, porPrioridad.get("ORDINARIO"), 0.0001);
    }

    @Test
    void calculaAdherenciaDelCeroAlCienPorCiento() {
        when(repositorio.buscarSolicitudesPorMedico("sin-realizar")).thenReturn(List.of(
                solicitud("1", "RX", Prioridad.ORDINARIO, EstadoSolicitud.PENDIENTE_CITACION,
                        AHORA.minusDays(1), true)));
        when(repositorio.buscarSolicitudesPorMedico("parcial")).thenReturn(List.of(
                solicitud("2", "RX", Prioridad.ORDINARIO, EstadoSolicitud.COMPLETADA,
                        AHORA.minusDays(1), true),
                solicitud("3", "RX", Prioridad.ORDINARIO, EstadoSolicitud.CITADA,
                        AHORA.minusDays(1), true)));
        when(repositorio.buscarSolicitudesPorMedico("completo")).thenReturn(List.of(
                solicitud("4", "RX", Prioridad.ORDINARIO, EstadoSolicitud.COMPLETADA,
                        AHORA.minusDays(1), true),
                solicitud("5", "RX", Prioridad.ORDINARIO, EstadoSolicitud.COMPLETADA,
                        AHORA.minusDays(1), true)));
        when(catalogo.obtenerTipoPrueba("RX"))
                .thenReturn(tipoPrueba("RX", ModalidadPrueba.RX));

        assertEquals(0.0, tasaAdherencia("sin-realizar"));
        assertEquals(50.0, tasaAdherencia("parcial"));
        assertEquals(100.0, tasaAdherencia("completo"));
    }

    @Test
    void cuentaYCalculaPorcentajeDeUsoDeCadaModalidad() {
        List<SolicitudPruebaDTO> solicitudes = List.of(
                solicitud("1", "RX", Prioridad.URGENTE, EstadoSolicitud.COMPLETADA,
                        AHORA.minusDays(1), false),
                solicitud("2", "RX", Prioridad.PREFERENTE, EstadoSolicitud.CITADA,
                        AHORA.minusDays(1), false),
                solicitud("3", "RM", Prioridad.ORDINARIO, EstadoSolicitud.PENDIENTE_CITACION,
                        AHORA.minusDays(1), false));
        prepararCatalogo(solicitudes, ModalidadPrueba.RX, ModalidadPrueba.RM);
        when(repositorio.buscarSolicitudesPorMedico("medico-1")).thenReturn(solicitudes);

        Map<String, Object> resultado = service.consultarEstadisticasPrescripcionMedico("medico-1");
        @SuppressWarnings("unchecked")
        Map<String, Integer> conteos = (Map<String, Integer>) resultado.get("solicitudesPorModalidad");
        @SuppressWarnings("unchecked")
        Map<String, Double> porcentajes =
                (Map<String, Double>) resultado.get("porcentajeSolicitudesPorModalidad");

        assertEquals(2, conteos.get("RX"));
        assertEquals(1, conteos.get("RM"));
        assertEquals(0, conteos.get("US"));
        assertEquals(200.0 / 3.0, porcentajes.get("RX"), 0.0001);
        assertEquals(100.0 / 3.0, porcentajes.get("RM"), 0.0001);
        verify(repositorio).registrarModalidadTipoPrueba("RX", ModalidadPrueba.RX);
        verify(repositorio).registrarModalidadTipoPrueba("RM", ModalidadPrueba.RM);
    }

    @Test
    void facultativoSinSolicitudesRecibeMetricasEnCero() {
        when(repositorio.buscarSolicitudesPorMedico("medico-vacio")).thenReturn(List.of());

        Map<String, Object> resultado = service.consultarEstadisticasPrescripcionMedico("medico-vacio");

        assertEquals(0, resultado.get("totalSolicitudes"));
        assertEquals(0.0, resultado.get("tiempoMedioEsperaDias"));
        assertEquals(0.0, resultado.get("tasaAdherenciaSeguimientoPorcentaje"));
        @SuppressWarnings("unchecked")
        Map<String, Integer> modalidades = (Map<String, Integer>) resultado.get("solicitudesPorModalidad");
        assertEquals(0, modalidades.values().stream().mapToInt(Integer::intValue).sum());
    }

    @Test
    void rechazaIdentificadoresInvalidosYDatosInconsistentes() {
        assertThrows(IllegalArgumentException.class, () -> service.consultarEstadisticasPrescripcionMedico(" "));
        verify(repositorio, never()).buscarSolicitudesPorMedico(" ");

        when(repositorio.buscarSolicitudesPorMedico("medico-1")).thenReturn(null);
        assertThrows(NullPointerException.class,
                () -> service.consultarEstadisticasPrescripcionMedico("medico-1"));

        when(repositorio.buscarSolicitudesPorMedico("medico-2")).thenReturn(List.of(
                solicitud("1", "desconocido", Prioridad.URGENTE,
                        EstadoSolicitud.COMPLETADA, AHORA.minusDays(1), false)));
        when(catalogo.obtenerTipoPrueba("desconocido")).thenReturn(null);
        assertThrows(IllegalStateException.class,
                () -> service.consultarEstadisticasPrescripcionMedico("medico-2"));
    }

    @Test
    void rechazaSolicitudEnEsperaSinFechaValida() {
        SolicitudPruebaDTO sinFecha = solicitud(
                "1", "RX", Prioridad.URGENTE, EstadoSolicitud.PENDIENTE_CITACION, null, false);
        when(repositorio.buscarSolicitudesPorMedico("medico-1")).thenReturn(List.of(sinFecha));

        assertThrows(IllegalStateException.class,
                () -> service.consultarEstadisticasPrescripcionMedico("medico-1"));
    }

    @Test
    void calculaOcupacionDemoraCancelacionesYListaDeEsperaEnElPeriodo() {
        LocalDate inicio = LocalDate.of(2026, 11, 1);
        LocalDate fin = LocalDate.of(2026, 11, 30);
        SolicitudPruebaDTO primera = solicitud("s1", "RX", Prioridad.ORDINARIO,
                EstadoSolicitud.CITADA, inicio.atTime(8, 0), false);
        SolicitudPruebaDTO segunda = solicitud("s2", "RX", Prioridad.ORDINARIO,
                EstadoSolicitud.CITADA, inicio.plusDays(1).atTime(10, 0), false);
        when(repositorio.buscarCitasEnPeriodo(inicio, fin)).thenReturn(List.of(
                cita("c1", "s1", inicio.plusDays(2).atTime(8, 0), EstadoCita.REALIZADA),
                cita("c2", "s2", inicio.plusDays(5).atTime(10, 0), EstadoCita.CONFIRMADA),
                cita("c3", "s1", inicio.plusDays(6).atTime(9, 0), EstadoCita.CANCELADA)));
        when(repositorio.buscarAgendasEnPeriodo(inicio, fin)).thenReturn(List.of(
                agenda("a1", inicio.plusDays(2), LocalTime.of(8, 0), LocalTime.of(10, 0), 60),
                agenda("a2", inicio.plusDays(5), LocalTime.of(8, 0), LocalTime.of(10, 0), 60)));
        when(repositorio.buscarSolicitudesEnPeriodo(inicio, fin)).thenReturn(List.of(
                primera, segunda,
                solicitud("s3", "RX", Prioridad.URGENTE, EstadoSolicitud.PENDIENTE_CITACION,
                        inicio.plusDays(3).atStartOfDay(), false)));
        when(repositorio.buscarSolicitudPorId("s1")).thenReturn(primera);
        when(repositorio.buscarSolicitudPorId("s2")).thenReturn(segunda);

        EstadisticaOperativaDTO resultado = service.generarInformeOperativoAgendas(inicio, fin);

        assertEquals(3, resultado.getTotalCitasProgramadas());
        assertEquals(1, resultado.getTotalCitasRealizadas());
        assertEquals(1, resultado.getTotalCitasCanceladas());
        assertEquals(3.0, resultado.getTiempoMedioEsperaDias(), 0.0001);
        assertEquals(50.0, resultado.getPorcentajeOcupacionAgendas(), 0.0001);
        assertEquals(100.0 / 3.0, resultado.getPorcentajeCancelaciones(), 0.0001);
        assertEquals(1, resultado.getSolicitudesPendientesEnListaEspera());
        verify(repositorio).guardarEstadisticaOperativa(resultado);
    }

    @Test
    void devuelveMetricasCeroCuandoNoHayAgendaNiCitas() {
        LocalDate inicio = LocalDate.of(2026, 11, 1);
        LocalDate fin = LocalDate.of(2026, 11, 30);
        when(repositorio.buscarCitasEnPeriodo(inicio, fin)).thenReturn(List.of());
        when(repositorio.buscarAgendasEnPeriodo(inicio, fin)).thenReturn(List.of());
        when(repositorio.buscarSolicitudesEnPeriodo(inicio, fin)).thenReturn(List.of());

        EstadisticaOperativaDTO resultado = service.generarInformeOperativoAgendas(inicio, fin);

        assertNotNull(resultado);
        assertEquals(0.0, resultado.getPorcentajeOcupacionAgendas());
        assertEquals(0.0, resultado.getTiempoMedioEsperaDias());
        assertEquals(0.0, resultado.getPorcentajeCancelaciones());
        assertEquals(0, resultado.getTotalCitasProgramadas());
    }

    @Test
    void calculaCienPorCienDeOcupacionYDeCancelaciones() {
        LocalDate inicio = LocalDate.of(2026, 11, 1);
        LocalDate fin = inicio;
        SolicitudPruebaDTO solicitudRealizada = solicitud("s1", "RX", Prioridad.URGENTE,
                EstadoSolicitud.CITADA, inicio.minusDays(1).atStartOfDay(), false);
        when(repositorio.buscarCitasEnPeriodo(inicio, fin)).thenReturn(
                List.of(cita("c1", "s1", inicio.atTime(8, 0), EstadoCita.CONFIRMADA)),
                List.of(cita("c2", "s1", inicio.atTime(8, 0), EstadoCita.CANCELADA)));
        when(repositorio.buscarAgendasEnPeriodo(inicio, fin)).thenReturn(List.of(
                agenda("a1", inicio, LocalTime.of(8, 0), LocalTime.of(9, 0), 60)));
        when(repositorio.buscarSolicitudesEnPeriodo(inicio, fin)).thenReturn(List.of());
        when(repositorio.buscarSolicitudPorId("s1")).thenReturn(solicitudRealizada);

        EstadisticaOperativaDTO ocupacionTotal = service.generarInformeOperativoAgendas(inicio, fin);
        assertEquals(100.0, ocupacionTotal.getPorcentajeOcupacionAgendas(), 0.0001);

        EstadisticaOperativaDTO cancelacionesTotales = service.generarInformeOperativoAgendas(inicio, fin);
        assertEquals(100.0, cancelacionesTotales.getPorcentajeCancelaciones(), 0.0001);
        assertEquals(0.0, cancelacionesTotales.getPorcentajeOcupacionAgendas());
    }

    @Test
    void rechazaRangoDeFechasInvalidoSinConsultarRepositorio() {
        LocalDate inicio = LocalDate.of(2026, 11, 30);
        LocalDate fin = LocalDate.of(2026, 11, 1);
        assertThrows(IllegalArgumentException.class,
                () -> service.generarInformeOperativoAgendas(inicio, fin));
        assertThrows(NullPointerException.class,
                () -> service.generarInformeOperativoAgendas(null, inicio));
        verify(repositorio, never()).buscarCitasEnPeriodo(inicio, fin);
    }

    @Test
    void rechazaDatosOperativosNulosDevueltosPorElRepositorio() {
        LocalDate inicio = LocalDate.of(2026, 11, 1);
        LocalDate fin = inicio.plusDays(1);
        when(repositorio.buscarCitasEnPeriodo(inicio, fin)).thenReturn(Arrays.asList((CitaDTO) null));
        when(repositorio.buscarAgendasEnPeriodo(inicio, fin)).thenReturn(List.of());
        when(repositorio.buscarSolicitudesEnPeriodo(inicio, fin)).thenReturn(List.of());

        assertThrows(NullPointerException.class,
                () -> service.generarInformeOperativoAgendas(inicio, fin));
    }

    @Test
    void generaCuadroGlobalAunqueNoHayaEstadisticas() {
        when(repositorio.buscarTodasLasEstadisticasOperativas()).thenReturn(List.of());
        when(repositorio.buscarTodasLasSolicitudes()).thenReturn(List.of());

        Map<String, Object> resultado = service.generarCuadroMandoGlobal();

        assertEquals(0, resultado.get("totalSolicitudes"));
        assertEquals(0, resultado.get("totalCitasProgramadas"));
        assertEquals(0.0, resultado.get("tiempoMedioEsperaDias"));
        assertEquals(0.0, resultado.get("porcentajeOcupacionAgendas"));
    }

    private double tasaAdherencia(String medicoId) {
        return (double) service.consultarEstadisticasPrescripcionMedico(medicoId)
                .get("tasaAdherenciaSeguimientoPorcentaje");
    }

    private void prepararCatalogo(List<SolicitudPruebaDTO> solicitudes, ModalidadPrueba... modalidades) {
        for (int i = 0; i < modalidades.length; i++) {
            String tipoId = i == 0 ? "RX" : "RM";
            when(catalogo.obtenerTipoPrueba(tipoId)).thenReturn(tipoPrueba(tipoId, modalidades[i]));
        }
        when(repositorio.buscarSolicitudesPorMedico("medico-1")).thenReturn(solicitudes);
    }

    private static PruebaCatalogoDTO tipoPrueba(String id, ModalidadPrueba modalidad) {
        return new PruebaCatalogoDTO(id, id, id, modalidad, 10, false, false, null, false, true);
    }

    private static SolicitudPruebaDTO solicitud(
            String id, String tipoId, Prioridad prioridad, EstadoSolicitud estado,
            LocalDateTime fecha, boolean seguimiento) {
        return new SolicitudPruebaDTO(
                id, "paciente-" + id, "medico-1", tipoId, prioridad, null, null,
                fecha, estado, seguimiento, seguimiento ? 12 : null);
    }

    private static CitaDTO cita(String id, String solicitudId, LocalDateTime fecha, EstadoCita estado) {
        return new CitaDTO(id, solicitudId, "paciente", "RX", fecha, 30,
                "sala-1", "medico-1", estado, null);
    }

    private static AgendaMedicaDTO agenda(
            String id, LocalDate fecha, LocalTime inicio, LocalTime fin, int intervalo) {
        return new AgendaMedicaDTO(id, "sala-1", "RX", fecha, inicio, fin, intervalo);
    }
}
