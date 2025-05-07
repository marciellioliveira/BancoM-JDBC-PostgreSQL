package br.com.marcielli.bancom.mappers;

import br.com.marcielli.bancom.entity.Cliente;
import br.com.marcielli.bancom.entity.Endereco;
import br.com.marcielli.bancom.entity.User;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class UserRowMapper implements RowMapper<User> {

	@Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setUserAtivo(rs.getBoolean("user_ativo"));

        Cliente cliente = new Cliente();
        long clienteId = rs.getLong("cliente_id");
        cliente.setId(clienteId);
        cliente.setNome(rs.getString("nome"));
        
        String cpf = rs.getString("cpf");
        Long cpfLong = null;
        
        if (cpf != null && !cpf.trim().isEmpty()) {
            // Remove pontos e traços, se existirem
            cpf = cpf.replaceAll("[.-]", "").trim();
            
            // Verifica se depois de limpar, só restaram números
            if (!cpf.isEmpty() && cpf.matches("\\d+")) {
                cpfLong = Long.valueOf(cpf);
            }
        }
        
        // Só seta se cpfLong não for nulo
        if (cpfLong != null) {
            cliente.setCpf(cpfLong);
        }
        
  
        cliente.setClienteAtivo(rs.getBoolean("cliente_ativo"));
        user.setCliente(cliente);

        Endereco endereco = new Endereco();
        endereco.setId(rs.getLong("endereco_id"));
        endereco.setRua(rs.getString("rua"));
        endereco.setNumero(rs.getString("numero"));
        endereco.setBairro(rs.getString("bairro"));
        endereco.setCidade(rs.getString("cidade"));
        endereco.setEstado(rs.getString("estado"));
        endereco.setComplemento(rs.getString("complemento"));
        endereco.setCep(rs.getString("cep"));
        cliente.setEndereco(endereco);

        return user;
    }

}
