package com.core.back.repository.impl;

import com.core.back.entity.RoleEntity;
import com.core.back.payload.RoleRequest;
import com.core.back.repository.IRoleRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class RoleRepositoryImpl implements IRoleRepository {

    private final JdbcTemplate jdbcTemplate;

    public RoleRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<RoleEntity> rolRowMapper = (ResultSet rs, int rowNum) -> {
        RoleEntity rol = new RoleEntity();
        rol.setRoleId(rs.getLong("ROL_ID"));
        rol.setName(rs.getString("NOMBRE"));
        rol.setPerspective(rs.getString("PERSPECTIVA"));
        rol.setActive(rs.getBoolean("ESTADO"));
        Timestamp fc = rs.getTimestamp("FECHA_CREACION");
        rol.setCreatedAt(fc != null ? fc.toLocalDateTime() : null);
        Timestamp fm = rs.getTimestamp("FECHA_MODIFICACION");
        rol.setUpdatedAt(fm != null ? fm.toLocalDateTime() : null);
        return rol;
    };

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

    @Override
    public RoleRequest updateRole(Long id, RoleRequest roleRequest) {
        String sql = "UPDATE ROL SET NOMBRE = ?, PERSPECTIVA = ?, ESTADO = ?, FECHA_MODIFICACION = ? WHERE ROL_ID = ?";
        
        jdbcTemplate.update(sql, 
                roleRequest.getName(), 
                roleRequest.getPerspective(), 
                (roleRequest.getActive() != null && roleRequest.getActive()) ? 1 : 0, 
                Timestamp.valueOf(LocalDateTime.now()), 
                id
        );
        
        roleRequest.setRoleId(id);
        return roleRequest;
    }

    @Override
    public void deleteRole(Long id) {
        String sql = "UPDATE ROL SET ESTADO = 0, FECHA_MODIFICACION = ? WHERE ROL_ID = ?";
        jdbcTemplate.update(sql, Timestamp.valueOf(LocalDateTime.now()), id);
    }

    @Override
    public Optional<RoleEntity> findRoleById(Long roleId) {
        String sql = "SELECT * FROM ROL WHERE ROL_ID = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rolRowMapper, roleId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<RoleEntity> findRoleByName(String name) {
        String sql = "SELECT * FROM ROL WHERE NOMBRE = ?";
        return jdbcTemplate.query(sql, rolRowMapper, name).stream().findFirst();
    }

    @Override
    public List<RoleEntity> findRolesByUserId(Long userId) {
        String sql = "SELECT R.* FROM ROL R " +
                     "INNER JOIN USUARIO_ROL UR ON R.ROL_ID = UR.ROL_ID " +
                     "WHERE UR.USUARIO_ID = ? AND R.ESTADO = 1";
        return jdbcTemplate.query(sql, rolRowMapper, userId);
    }

    @Override
    public void assignRoleToUser(Long userId, Long roleId) {
        String sql = "INSERT INTO USUARIO_ROL (USUARIO_ID, ROL_ID, FECHA_CREACION) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, userId, roleId, Timestamp.valueOf(LocalDateTime.now()));
    }

    @Override
    public void removeRoleFromUser(Long userId, Long roleId) {
        String sql = "DELETE FROM USUARIO_ROL WHERE USUARIO_ID = ? AND ROL_ID = ?";
        jdbcTemplate.update(sql, userId, roleId);
    }

}