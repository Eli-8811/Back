package com.core.back.repository.impl;

import com.core.back.payload.RoleRequest;
import com.core.back.repository.IRoleRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class RoleRepositoryImpl implements IRoleRepository {

    private final JdbcTemplate jdbcTemplate;

    public RoleRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<RoleRequest> rolRequestRowMapper = (ResultSet rs, int rowNum) -> {
        RoleRequest dto = new RoleRequest();
        dto.setRoleId(rs.getLong("ROL_ID"));
        dto.setName(rs.getString("NOMBRE"));
        dto.setPerspective(rs.getString("PERSPECTIVA"));
        dto.setActive(rs.getBoolean("ESTADO"));
        return dto;
    };

    @Override
    public RoleRequest saveRole(RoleRequest roleRequest) {
        String sql = "INSERT INTO ROL (NOMBRE, PERSPECTIVA, ESTADO, FECHA_CREACION) " +
                "VALUES (?, ?, ?, ?) RETURNING ROL_ID";
        Long roleId = jdbcTemplate.queryForObject(
                sql,
                Long.class,
                roleRequest.getName(),
                roleRequest.getPerspective(),
                Boolean.TRUE.equals(roleRequest.getActive()),
                Timestamp.valueOf(LocalDateTime.now())
        );
        roleRequest.setRoleId(roleId);
        return roleRequest;
    }

    @Override
    public List<RoleRequest> findAllRoles() {
        String sql = "SELECT * FROM ROL ORDER BY ROL_ID ASC";
        return jdbcTemplate.query(sql, rolRequestRowMapper);
    }

}