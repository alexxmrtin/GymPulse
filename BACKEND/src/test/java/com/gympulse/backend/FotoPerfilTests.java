package com.gympulse.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.AbstractMockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class FotoPerfilTests {

    private static final byte[] PNG = {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52
    };

    @Autowired
    private MockMvc mvc;

    private record Cuenta(long id, String token) {
    }

    @Test
    void subeLeeYQuitaLaFoto() throws Exception {
        Cuenta cuenta = crearCuenta();

        mvc.perform(subir(cuenta, foto(PNG)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fotoActualizadaEn").isNotEmpty());

        mvc.perform(conToken(get("/usuario/{id}/foto", cuenta.id()), cuenta))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(PNG));

        mvc.perform(conToken(get("/usuario/{id}", cuenta.id()), cuenta))
                .andExpect(jsonPath("$.fotoActualizadaEn").isNotEmpty());

        mvc.perform(conToken(delete("/usuario/{id}/foto", cuenta.id()), cuenta))
                .andExpect(status().isNoContent());

        mvc.perform(conToken(get("/usuario/{id}/foto", cuenta.id()), cuenta))
                .andExpect(status().isNotFound());

        mvc.perform(conToken(get("/usuario/{id}", cuenta.id()), cuenta))
                .andExpect(jsonPath("$.fotoActualizadaEn").doesNotExist());
    }

    @Test
    void rechazaLoQueNoEsUnaImagenAunqueDigaQueLoEs() throws Exception {
        Cuenta cuenta = crearCuenta();

        mvc.perform(subir(cuenta, foto("no soy una imagen".getBytes())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(400));
    }

    @Test
    void exigeElCampoFoto() throws Exception {
        Cuenta cuenta = crearCuenta();

        mvc.perform(conToken(multipart(HttpMethod.PUT, "/usuario/{id}/foto", cuenta.id()), cuenta))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(400));
    }

    @Test
    void noDejaTocarLaFotoDeOtro() throws Exception {
        Cuenta duena = crearCuenta();
        Cuenta otra = crearCuenta();
        mvc.perform(subir(duena, foto(PNG))).andExpect(status().isOk());

        mvc.perform(conToken(get("/usuario/{id}/foto", duena.id()), otra))
                .andExpect(status().isForbidden());

        mvc.perform(conToken(multipart(HttpMethod.PUT, "/usuario/{id}/foto", duena.id()).file(foto(PNG)), otra))
                .andExpect(status().isForbidden());
    }

    @Test
    void exigeToken() throws Exception {
        Cuenta cuenta = crearCuenta();

        mvc.perform(get("/usuario/{id}/foto", cuenta.id()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void borraLaCuentaAunqueTengaFoto() throws Exception {
        Cuenta cuenta = crearCuenta();
        mvc.perform(subir(cuenta, foto(PNG))).andExpect(status().isOk());

        mvc.perform(conToken(delete("/usuario/{id}", cuenta.id()), cuenta))
                .andExpect(status().isNoContent());
    }

    private Cuenta crearCuenta() throws Exception {
        String nombre = "u" + UUID.randomUUID().toString().substring(0, 8);
        String email = nombre + "@gympulse.test";

        mvc.perform(post("/usuario")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"%s\",\"email\":\"%s\",\"password\":\"secreta\"}".formatted(nombre, email)))
                .andExpect(status().isAccepted());

        String respuesta = mvc.perform(post("/usuario/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"%s\",\"password\":\"secreta\"}".formatted(email)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Number id = JsonPath.read(respuesta, "$.usuario.id");
        return new Cuenta(id.longValue(), JsonPath.read(respuesta, "$.token"));
    }

    private MockMultipartFile foto(byte[] datos) {
        return new MockMultipartFile("foto", "foto.png", MediaType.IMAGE_PNG_VALUE, datos);
    }

    private MockMultipartHttpServletRequestBuilder subir(Cuenta cuenta, MockMultipartFile archivo) {
        return conToken(multipart(HttpMethod.PUT, "/usuario/{id}/foto", cuenta.id()).file(archivo), cuenta);
    }

    private <B extends AbstractMockHttpServletRequestBuilder<B>> B conToken(B peticion, Cuenta cuenta) {
        return peticion.header("Authorization", "Bearer " + cuenta.token());
    }
}
