package com.pos.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String usuario;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RolEnum rol;

    @Column(nullable = false)
    private boolean activo = true;

    public enum RolEnum { CAJERO, ADMIN }

    public UsuarioEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public RolEnum getRol() { return rol; }
    public void setRol(RolEnum rol) { this.rol = rol; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
