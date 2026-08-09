package com.gympulse.backend.helpers;

import org.springframework.security.oauth2.jwt.Jwt;

/*
 Saca el id del usuario del token ya validado. El id viaja en el claim estandar "sub", que el JWT obliga a que sea texto, y por eso hay que convertirlo a Long.
 Existe para que los controladores no repitan esa conversion en cada endpoint, y sobre todo para que el id venga SIEMPRE del token firmado y nunca de un parametro que el cliente pueda cambiar a su antojo.
*/
public final class UsuarioAutenticado {

    private UsuarioAutenticado() {
    }

    public static Long id(Jwt token) {
        return Long.valueOf(token.getSubject());
    }
}
