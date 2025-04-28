package br.com.marcielli.bancom.testutils;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class BaseControllerTest {

    private DataSource dataSource;
    protected MockMvc mockMvc;

    public BaseControllerTest(MockMvc mockMvc, DataSource dataSource) {
        this.mockMvc = mockMvc;
        this.dataSource = dataSource;
    }

    @BeforeEach
    void cleanup() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            conn.createStatement().execute("DELETE FROM tb_users");
            conn.createStatement().execute("DELETE FROM clientes");
        }
    }

}
