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
        String sql = "SELECT * FROM find_role_by_name_v1(?)";
        List<Role> roles = jdbcTemplate.query(sql, new RoleRowMapper(), name);

        if (roles.isEmpty()) {
            return null; // Role não encontrada
        }
        return roles.getFirst(); // Retorna a primeira
    }

    public void save(Role role) {
    	String sql = "SELECT insert_role_if_not_exists_v1(?, ?)";
    	jdbcTemplate.queryForObject(sql, Void.class, role.getId(), role.getName());
    }


}
