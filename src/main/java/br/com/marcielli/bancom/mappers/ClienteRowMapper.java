package br.com.marcielli.bancom.mappers;

import br.com.marcielli.bancom.configuracao.AdminInitializer;
import br.com.marcielli.bancom.entity.Cliente;
import br.com.marcielli.bancom.entity.Endereco;
import br.com.marcielli.bancom.entity.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ClienteRowMapper implements RowMapper<Cliente> {

	private static final Logger logger = LoggerFactory.getLogger(ClienteRowMapper.class);    
	
    @Override
    public Cliente mapRow(ResultSet rs, int rowNum) throws SQLException {
    	    	
        Cliente cliente = new Cliente();        
        cliente.setId(rs.getLong("cliente_id"));
        cliente.setNome(rs.getString("nome"));
        cliente.setCpf(rs.getLong("cpf")); 
        cliente.setClienteAtivo(rs.getBoolean("cliente_ativo"));

        // User
        User user = new User();
        user.setId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username")); 
        user.setPassword(rs.getString("password")); 
        user.setUserAtivo(rs.getBoolean("user_ativo"));
        user.setCliente(cliente); 
        cliente.setUser(user);

        // Endereco
        Endereco endereco = new Endereco();
        endereco.setId(rs.getLong("endereco_id"));
        endereco.setCep(rs.getString("cep"));
        endereco.setCidade(rs.getString("cidade"));
        endereco.setEstado(rs.getString("estado"));
        endereco.setRua(rs.getString("rua"));
        endereco.setNumero(rs.getString("numero"));
        endereco.setBairro(rs.getString("bairro"));
        endereco.setComplemento(rs.getString("complemento"));
        endereco.setCliente(cliente); 
        cliente.setEndereco(endereco);
        
        return cliente;
    }
}
