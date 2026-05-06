package com.pos.infrastructure.config;

import com.pos.infrastructure.adapter.out.persistence.UsuarioEntity;
import com.pos.infrastructure.adapter.out.persistence.UsuarioJpaRepository;
import com.pos.infrastructure.security.JwtService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Inicializa los usuarios de prueba con hashes BCrypt correctos al arrancar.
 * Reemplaza los hashes hardcodeados en data.sql que pueden ser incorrectos.
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private final UsuarioJpaRepository usuarioRepository;
    private final JwtService jwtService;

    public DataInitializer(UsuarioJpaRepository usuarioRepository, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
    }

    @Override
    public void run(ApplicationArguments args) {
        // Solo crear si no existen (evitar duplicados en reinicios)
        crearUsuarioSiNoExiste("cajero01", "1234", UsuarioEntity.RolEnum.CAJERO);
        crearUsuarioSiNoExiste("cajero02", "1234", UsuarioEntity.RolEnum.CAJERO);
        crearUsuarioSiNoExiste("admin01", "admin123", UsuarioEntity.RolEnum.ADMIN);
    }

    private void crearUsuarioSiNoExiste(String usuario, String password, UsuarioEntity.RolEnum rol) {
        if (usuarioRepository.findByUsuario(usuario).isEmpty()) {
            UsuarioEntity entity = new UsuarioEntity();
            entity.setUsuario(usuario);
            entity.setPasswordHash(jwtService.encodePassword(password));
            entity.setRol(rol);
            entity.setActivo(true);
            usuarioRepository.save(entity);
        }
    }
}
