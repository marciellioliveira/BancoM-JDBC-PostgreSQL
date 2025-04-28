package br.com.marcielli.bancom.dao;

import br.com.marcielli.bancom.entity.Cliente;
import br.com.marcielli.bancom.entity.User;
import br.com.marcielli.bancom.exception.ClienteNaoEncontradoException;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ClienteDAO {

    //Interface do JDBC
    private final DataSource dataSource;

    public ClienteDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Optional<Cliente> findByCpf(Long cpf) {
        String sql = "SELECT id, nome, cpf FROM clientes WHERE cpf = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, cpf);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Cliente cliente = new Cliente();
                    cliente.setId(rs.getLong("id"));
                    cliente.setNome(rs.getString("nome"));
                    cliente.setCpf(rs.getLong("cpf"));
                    return Optional.of(cliente);
                }
            }
        } catch (SQLException e) {
            throw new ClienteNaoEncontradoException("Erro ao buscar cliente por CPF: " + cpf +" - Exception:"+ e);
        }
        return Optional.empty();
    }

    public List<Cliente> findByNomeContainingIgnoreCase(String nome) {
        String sql = "SELECT id, nome, cpf FROM clientes WHERE LOWER(nome) LIKE LOWER(?)";
        List<Cliente> clientes = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + nome + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Cliente cliente = new Cliente();
                    cliente.setId(rs.getLong("id"));
                    cliente.setNome(rs.getString("nome"));
                    cliente.setCpf(rs.getLong("cpf"));
                    clientes.add(cliente);
                }
            }
        } catch (SQLException e) {
            throw new ClienteNaoEncontradoException("Erro ao buscar cliente por CPF: " + clientes +" - Exception:"+ e);
        }
        return clientes;
    }

    public Optional<Cliente> findByIdWithUser(Long clienteId) {
        String clienteSql = "SELECT id, nome, cpf FROM clientes WHERE id = ?";
        String userSql = "SELECT id, username, password FROM tb_users WHERE cliente_id = ?";

        try (Connection conn = dataSource.getConnection()) {
            // Busca o cliente
            Cliente cliente = null;
            try (PreparedStatement clientePs = conn.prepareStatement(clienteSql)) {
                clientePs.setLong(1, clienteId);
                try (ResultSet rs = clientePs.executeQuery()) {
                    if (rs.next()) {
                        cliente = new Cliente();
                        cliente.setId(rs.getLong("id"));
                        cliente.setNome(rs.getString("nome"));
                        cliente.setCpf(rs.getLong("cpf"));
                    }
                }
            }

            if (cliente == null) {
                return Optional.empty();
            }

            // Busca o user associado
            try (PreparedStatement userPs = conn.prepareStatement(userSql)) {
                userPs.setLong(1, clienteId);
                try (ResultSet rs = userPs.executeQuery()) {
                    if (rs.next()) {
                        User user = new User();
                        user.setId(rs.getInt("id"));
                        user.setUsername(rs.getString("username"));
                        user.setPassword(rs.getString("password"));
                        cliente.setUser(user);
                    }
                }
            }

            return Optional.of(cliente);
        } catch (SQLException e) {
            throw new ClienteNaoEncontradoException("Erro ao buscar cliente com user - Exception: "+e);
        }
    }
}
