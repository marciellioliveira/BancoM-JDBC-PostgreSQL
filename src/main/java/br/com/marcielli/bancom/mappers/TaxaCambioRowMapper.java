package br.com.marcielli.bancom.mappers;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class TaxaCambioRowMapper implements RowMapper<BigDecimal> {

	@Override
    public BigDecimal mapRow(ResultSet rs, int rowNum) throws SQLException {
        return rs.getBigDecimal("taxa");
    }

}
