package com.pos.infrastructure.adapter.out.persistence;

import com.pos.domain.model.Rol;
import com.pos.domain.model.Usuario;
import com.pos.domain.port.out.UsuarioRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UsuarioJpaAdapter implements UsuarioRepository {

    private final UsuarioJpaRepository jpaRepository;

    public UsuarioJpaAdapter(UsuarioJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Usuario> findByUsuario(String usuario) {
        return jpaRepository.findByUsuario(usuario).map(e -> new Usuario(
                e.getId(),
                e.getUsuario(),
                e.getPasswordHash(),
                Rol.valueOf(e.getRol().name()),
                e.isActivo()
        ));
    }
}
