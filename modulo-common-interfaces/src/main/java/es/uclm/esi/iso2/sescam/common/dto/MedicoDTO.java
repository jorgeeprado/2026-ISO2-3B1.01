package es.uclm.esi.iso2.sescam.common.dto;

import java.io.Serializable;

/**
 * DTO para el profesional facultativo (prescriptor o radiólogo).
 */
public class MedicoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String numeroColegiado;
    private String nombre;
    private String apellidos;
    private String especialidad;
    private String servicio;
    private String email;

    public MedicoDTO() {}

    public MedicoDTO(String id, String numeroColegiado, String nombre, String apellidos, String especialidad, String servicio, String email) {
        this.id = id;
        this.numeroColegiado = numeroColegiado;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.especialidad = especialidad;
        this.servicio = servicio;
        this.email = email;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNumeroColegiado() { return numeroColegiado; }
    public void setNumeroColegiado(String numeroColegiado) { this.numeroColegiado = numeroColegiado; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }

    public String getServicio() { return servicio; }
    public void setServicio(String servicio) { this.servicio = servicio; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
