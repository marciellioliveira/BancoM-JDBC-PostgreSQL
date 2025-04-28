package br.com.marcielli.bancom.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.com.marcielli.bancom.testutils.BaseControllerTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;

public class ClienteControllerTest extends BaseControllerTest {

    public ClienteControllerTest(MockMvc mockMvc, DataSource dataSource) {
        super(mockMvc, dataSource);
    }

    @Test
    void shouldCreateAndGetCliente() throws Exception {
        String clienteJson = """
            {
                "nome": "John Doe",
                "cpf": 12345678900
            }
            """;

        mockMvc.perform(post("/clientes")
                        .contentType("application/json")
                        .content(clienteJson))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/clientes/12345678900")
                        .accept("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("John Doe"));
    }
}
