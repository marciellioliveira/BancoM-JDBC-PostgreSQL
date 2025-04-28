package br.com.marcielli.bancom.dao;

import br.com.marcielli.bancom.entity.Role;
import br.com.marcielli.bancom.entity.User;
import br.com.marcielli.bancom.exception.ClienteNaoEncontradoException;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Repository
public class UserDAO {

    //Interface do JDBC
    private final DataSource dataSource;

    public UserDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Optional<User> findByIdWithRoles(Integer userId) {
        String userSql = "SELECT id, username, password FROM tb_users WHERE id = ?";
        String rolesSql = "SELECT r.id, r.name FROM roles r JOIN tb_users_roles ur ON r.id = ur.role_id WHERE ur.user_id = ?";

        try (Connection conn = dataSource.getConnection()) {
            // Busca o usuário
            User user = null;
            try (PreparedStatement userPs = conn.prepareStatement(userSql)) {
                userPs.setInt(1, userId);
                try (ResultSet rs = userPs.executeQuery()) {
                    if (rs.next()) {
                        user = new User();
                        user.setId(rs.getInt("id"));
                        user.setUsername(rs.getString("username"));
                        user.setPassword(rs.getString("password"));
                    }
                }
            }

            if (user == null) {
                return Optional.empty();
            }

            // Busca as roles
            Set<Role> roles = new HashSet<>();
            try (PreparedStatement rolesPs = conn.prepareStatement(rolesSql)) {
                rolesPs.setInt(1, userId);
                try (ResultSet rs = rolesPs.executeQuery()) {
                    while (rs.next()) {
                        Role role = new Role();
                        role.setId(rs.getLong("id"));
                        role.setName(rs.getString("name"));
                        roles.add(role);
                    }
                }
            }

            user.setRoles(roles);
            return Optional.of(user);
        } catch (SQLException e) {
            throw new ClienteNaoEncontradoException("Erro ao buscar usuário - Exception: "+e);
        }
    }
}
