package br.com.marcielli.bancom.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.com.marcielli.bancom.testutils.BaseControllerTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;

public class UserControllerTest extends BaseControllerTest {

    public UserControllerTest(MockMvc mockMvc, DataSource dataSource) {
        super(mockMvc, dataSource);
    }

    @Test
    void shouldCreateAndGetUser() throws Exception {
        // Cria um usuário via API
        String userJson = """
            {
                "username": "john_doe",
                "password": "secret123"
            }
            """;

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(userJson))
                .andExpect(status().isCreated());

        // Busca o usuário criado
        mockMvc.perform(get("/users/john_doe")
                        .accept("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john_doe"));
    }
}
