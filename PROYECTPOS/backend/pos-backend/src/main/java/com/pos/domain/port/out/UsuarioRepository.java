package com.pos.domain.port.out;

import com.pos.domain.model.Usuario;
import java.util.Optional;

public interface UsuarioRepository {
    Optional<Usuario> findByUsuario(String usuario);
}
