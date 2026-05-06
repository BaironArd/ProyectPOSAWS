package com.pos.domain.model;

/**
 * Entidad de dominio para autenticación. Sin anotaciones de Spring ni JPA.
 */
public class Usuario {

    private Long id;
    private String usuario;
    private String passwordHash;
    private Rol rol;
    private boolean activo;

    public Usuario() {}

    public Usuario(Long id, String usuario, String passwordHash, Rol rol, boolean activo) {
        this.id = id;
        this.usuario = usuario;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.activo = activo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
