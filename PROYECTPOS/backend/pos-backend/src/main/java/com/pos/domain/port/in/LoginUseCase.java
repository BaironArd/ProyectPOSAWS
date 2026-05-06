package com.pos.domain.port.in;

import com.pos.domain.model.SesionToken;

public interface LoginUseCase {
    SesionToken login(String usuario, String contrasena);
}
