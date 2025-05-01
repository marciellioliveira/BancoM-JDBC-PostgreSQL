package br.com.marcielli.bancom.dao;

import br.com.marcielli.bancom.entity.Role;
import br.com.marcielli.bancom.mappers.RoleRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RoleDao {

    private final JdbcTemplate jdbcTemplate;

    public RoleDao(JdbcTemplate jdbcTemplate) {
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
