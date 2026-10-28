package es.uclm.esi.iso2.sescam.common.dto;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO para la consolidación de métricas de ocupación, demoras y actividad asistencial (RF-11, RF-16, RF-21).
 */
public class EstadisticaOperativaDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private int totalCitasProgramadas;
    private int totalCitasRealizadas;
    private int totalCitasCanceladas;
    private double tiempoMedioEsperaDias;
    private double porcentajeOcupacionAgendas;
    private int solicitudesPendientesEnListaEspera;

    public EstadisticaOperativaDTO() {}

    public EstadisticaOperativaDTO(LocalDate fechaInicio, LocalDate fechaFin, int totalCitasProgramadas,
                                  int totalCitasRealizadas, int totalCitasCanceladas,
                                  double tiempoMedioEsperaDias, double porcentajeOcupacionAgendas,
                                  int solicitudesPendientesEnListaEspera) {
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.totalCitasProgramadas = totalCitasProgramadas;
        this.totalCitasRealizadas = totalCitasRealizadas;
        this.totalCitasCanceladas = totalCitasCanceladas;
        this.tiempoMedioEsperaDias = tiempoMedioEsperaDias;
        this.porcentajeOcupacionAgendas = porcentajeOcupacionAgendas;
        this.solicitudesPendientesEnListaEspera = solicitudesPendientesEnListaEspera;
    }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public int getTotalCitasProgramadas() { return totalCitasProgramadas; }
    public void setTotalCitasProgramadas(int totalCitasProgramadas) { this.totalCitasProgramadas = totalCitasProgramadas; }

    public int getTotalCitasRealizadas() { return totalCitasRealizadas; }
    public void setTotalCitasRealizadas(int totalCitasRealizadas) { this.totalCitasRealizadas = totalCitasRealizadas; }

    public int getTotalCitasCanceladas() { return totalCitasCanceladas; }
    public void setTotalCitasCanceladas(int totalCitasCanceladas) { this.totalCitasCanceladas = totalCitasCanceladas; }

    public double getTiempoMedioEsperaDias() { return tiempoMedioEsperaDias; }
    public void setTiempoMedioEsperaDias(double tiempoMedioEsperaDias) { this.tiempoMedioEsperaDias = tiempoMedioEsperaDias; }

    public double getPorcentajeOcupacionAgendas() { return porcentajeOcupacionAgendas; }
    public void setPorcentajeOcupacionAgendas(double porcentajeOcupacionAgendas) { this.porcentajeOcupacionAgendas = porcentajeOcupacionAgendas; }

    public int getSolicitudesPendientesEnListaEspera() { return solicitudesPendientesEnListaEspera; }
    public void setSolicitudesPendientesEnListaEspera(int solicitudesPendientesEnListaEspera) { this.solicitudesPendientesEnListaEspera = solicitudesPendientesEnListaEspera; }
}
