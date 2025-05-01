package br.com.marcielli.bancom.mappers;

import br.com.marcielli.bancom.entity.Cliente;
import br.com.marcielli.bancom.entity.Endereco;
import br.com.marcielli.bancom.entity.Role;
import br.com.marcielli.bancom.entity.User;
import org.springframework.jdbc.core.RowMapper;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class UserRowMapper implements RowMapper<User> {

    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        Set<Role> roles = new HashSet<>();

        user.setId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setUserAtivo(rs.getBoolean("user_ativo"));

        Cliente cliente = new Cliente();
        cliente.setId(rs.getLong("cliente_id"));
        cliente.setNome(rs.getString("nome"));
        cliente.setCpf(rs.getLong("cpf"));
        cliente.setClienteAtivo(rs.getBoolean("cliente_ativo"));

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
        cliente.setUser(user);
        user.setCliente(cliente);

        int roleId = rs.getInt("role_id");
        String roleName = rs.getString("role_name");
        if (roleId != 0 && roleName != null) {
            Role role = new Role();
            role.setId((long) roleId);
            role.setName(roleName);
            roles.add(role);
        }

        user.setRoles(roles);

        return user;
    }

}
