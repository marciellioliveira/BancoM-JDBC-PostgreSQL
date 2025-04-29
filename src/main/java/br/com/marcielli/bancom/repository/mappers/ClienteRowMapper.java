package br.com.marcielli.bancom.repository.mappers;

import br.com.marcielli.bancom.entity.Cliente;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ClienteRowMapper implements RowMapper<Cliente> {

    @Override
    public Cliente mapRow(ResultSet rs, int rowNum) throws SQLException {
        Cliente cliente = new Cliente();
        cliente.setId(rs.getLong("id"));
        cliente.setNome(rs.getString("nome"));
        cliente.setCpf(rs.getLong("cpf"));
        cliente.setClienteAtivo(rs.getBoolean("cliente_ativo"));

        // Caso o cliente tenha um endereço associado, você pode mapear também.
        // Isso depende de como você estrutura a consulta e o relacionamento entre as tabelas.

        return cliente;
    }
}
