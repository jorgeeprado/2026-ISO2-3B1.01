package es.uclm.esi.iso2.sescam.common.dto;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO que encapsula los datos demográficos y clínicos básicos del paciente.
 */
public class PacienteDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String cip; // Código de Identificación del Paciente (SESCAM)
    private String dni;
    private String nombre;
    private String apellidos;
    private LocalDate fechaNacimiento;
    private String telefono;
    private String email;
    private boolean activo; // Baja lógica conforme a RN-08

    public PacienteDTO() {}

    public PacienteDTO(String id, String cip, String dni, String nombre, String apellidos,
                       LocalDate fechaNacimiento, String telefono, String email, boolean activo) {
        this.id = id;
        this.cip = cip;
        this.dni = dni;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.fechaNacimiento = fechaNacimiento;
        this.telefono = telefono;
        this.email = email;
        this.activo = activo;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCip() { return cip; }
    public void setCip(String cip) { this.cip = cip; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}

