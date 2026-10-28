package es.uclm.esi.iso2.sescam.common.dto;

import es.uclm.esi.iso2.sescam.common.enums.RolUsuario;
import java.io.Serializable;

/**
 * DTO para la representación de usuarios del sistema y control de acceso (RBAC).
 */
public class UsuarioDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String username;
    private String nombre;
    private String apellidos;
    private String email;
    private RolUsuario rol;
    private boolean activo;

    public UsuarioDTO() {
    }

    public UsuarioDTO(String id, String username, String nombre, String apellidos, String email, RolUsuario rol, boolean activo) {
        this.id = id;
        this.username = username;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.rol = rol;
        this.activo = activo;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public RolUsuario getRol() { return rol; }
    public void setRol(RolUsuario rol) { this.rol = rol; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
