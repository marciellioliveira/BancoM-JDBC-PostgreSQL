package br.com.marcielli.bancom.repository;

import br.com.marcielli.bancom.entity.Role;
import br.com.marcielli.bancom.repository.mappers.RoleRowMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
@Profile("cliente")
@Repository
public class RoleRepositoryJDBC {

    private final JdbcTemplate jdbcTemplate;

    public RoleRepositoryJDBC(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Role findByName(String name) {
        String sql = "SELECT id, name FROM roles WHERE name = ?";
        List<Role> roles = jdbcTemplate.query(sql, new RoleRowMapper(), name);

        if (roles.isEmpty()) {
            return null;  // Role não encontrada
        }
        return roles.getFirst();  // Retorna a primeira (e provavelmente única) role
    }


    public void save(Role role) {
        String sql = "INSERT INTO roles (name) VALUES (?)";
        jdbcTemplate.update(sql, role.getName());
    }

}
