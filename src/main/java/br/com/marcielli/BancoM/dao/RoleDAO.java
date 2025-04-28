package br.com.marcielli.bancom.dao;

import br.com.marcielli.bancom.entity.Role;
import br.com.marcielli.bancom.exception.RolePersistenceException;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class RoleDAO {

    //Interface do JDBC
    private final DataSource dataSource;

    public RoleDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Optional<Role> findByName(String name) {
        String sql = "SELECT id, name FROM roles WHERE name = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Role role = new Role();
                    role.setId(rs.getLong("id"));
                    role.setName(rs.getString("name"));
                    return Optional.of(role);
                }
            }
        } catch (SQLException e) {
            throw new RolePersistenceException("Erro ao buscar role por nome: " + name +" - Excessão: "+e);
        }
        return Optional.empty();
    }
}
