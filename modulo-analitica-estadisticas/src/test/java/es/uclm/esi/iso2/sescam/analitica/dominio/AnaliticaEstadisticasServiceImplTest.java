package es.uclm.esi.iso2.sescam.analitica.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.uclm.esi.iso2.sescam.analitica.persistencia.IEstadisticasRepositorio;
import es.uclm.esi.iso2.sescam.common.dto.EstadisticaOperativaDTO;
import es.uclm.esi.iso2.sescam.common.dto.PruebaCatalogoDTO;
import es.uclm.esi.iso2.sescam.common.dto.SolicitudPruebaDTO;
import es.uclm.esi.iso2.sescam.common.enums.EstadoSolicitud;
import es.uclm.esi.iso2.sescam.common.enums.ModalidadPrueba;
import es.uclm.esi.iso2.sescam.common.enums.Prioridad;
import es.uclm.esi.iso2.sescam.common.interfaces.ICatalogoConfiguracionService;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
    void agregaEstadisticasOperativasConMediaPonderadaYValidaIntervalo() {
        LocalDate inicio = LocalDate.of(2026, 11, 1);
        LocalDate fin = LocalDate.of(2026, 11, 30);
        when(repositorio.buscarEstadisticasOperativas(inicio, fin)).thenReturn(List.of(
                new EstadisticaOperativaDTO(inicio, inicio.plusDays(2), 10, 8, 1, 2.0, 80.0, 3),
                new EstadisticaOperativaDTO(inicio.plusDays(3), fin, 30, 25, 2, 4.0, 90.0, 5)));

        EstadisticaOperativaDTO resultado = service.generarInformeOperativoAgendas(inicio, fin);

        assertEquals(40, resultado.getTotalCitasProgramadas());
        assertEquals(33, resultado.getTotalCitasRealizadas());
        assertEquals(3, resultado.getTotalCitasCanceladas());
        assertEquals(3.5, resultado.getTiempoMedioEsperaDias(), 0.0001);
        assertEquals(87.5, resultado.getPorcentajeOcupacionAgendas(), 0.0001);
        assertEquals(8, resultado.getSolicitudesPendientesEnListaEspera());
        assertThrows(IllegalArgumentException.class,
                () -> service.generarInformeOperativoAgendas(fin, inicio));
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
}
