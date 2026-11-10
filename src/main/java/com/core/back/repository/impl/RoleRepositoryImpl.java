package com.core.back.repository.impl;

import com.core.back.entity.RoleEntity;
import com.core.back.payload.RoleRequest;
import com.core.back.repository.IRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RoleRepositoryImpl implements IRoleRepository {

    private final JdbcTemplate jdbcTemplate;

    // Mapper para la Entidad
    private final RowMapper<RoleEntity> rolRowMapper = (ResultSet rs, int rowNum) -> {
        RoleEntity rol = new RoleEntity();
        rol.setRolId(rs.getLong("ROL_ID"));
        rol.setNombre(rs.getString("NOMBRE"));
        rol.setPerspectiva(rs.getString("PERSPECTIVA"));
        rol.setEstado(rs.getInt("ESTADO") == 1);
        
        Timestamp fc = rs.getTimestamp("FECHA_CREACION");
        rol.setFechaCreacion(fc != null ? fc.toLocalDateTime() : null);
        Timestamp fm = rs.getTimestamp("FECHA_MODIFICACION");
        rol.setFechaModificacion(fm != null ? fm.toLocalDateTime() : null);
        return rol;
    };

    // Mapper para el Payload (DTO)
    private final RowMapper<RoleRequest> rolRequestRowMapper = (ResultSet rs, int rowNum) -> {
        RoleRequest dto = new RoleRequest();
        dto.setRolId(rs.getLong("ROL_ID"));
        dto.setNombre(rs.getString("NOMBRE"));
        dto.setPerspectiva(rs.getString("PERSPECTIVA"));
        dto.setEstado(rs.getInt("ESTADO") == 1);
        return dto;
    };

    // ==========================================
    // NUEVOS MÉTODOS CRUD (Retornan RoleRequest)
    // ==========================================

    @Override
    public RoleRequest guardarRol(RoleRequest rolDto) {
        String sql = "INSERT INTO ROLES (NOMBRE, PERSPECTIVA, ESTADO, FECHA_CREACION) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            // El array "ROL_ID" le indica al driver JDBC que debe recuperar esa columna generada
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"ROL_ID"});
            ps.setString(1, rolDto.getNombre());
            ps.setString(2, rolDto.getPerspectiva());
            ps.setInt(3, (rolDto.getEstado() != null && rolDto.getEstado()) ? 1 : 0);
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            rolDto.setRolId(keyHolder.getKey().longValue());
        }
        return rolDto;
    }

    @Override
    public List<RoleRequest> obtenerTodos() {
        String sql = "SELECT * FROM ROLES ORDER BY ROL_ID ASC";
        return jdbcTemplate.query(sql, rolRequestRowMapper);
    }

    @Override
    public RoleRequest actualizarRol(Long id, RoleRequest rolDto) {
        String sql = "UPDATE ROLES SET NOMBRE = ?, PERSPECTIVA = ?, ESTADO = ?, FECHA_MODIFICACION = ? WHERE ROL_ID = ?";
        
        jdbcTemplate.update(sql, 
                rolDto.getNombre(), 
                rolDto.getPerspectiva(), 
                (rolDto.getEstado() != null && rolDto.getEstado()) ? 1 : 0, 
                Timestamp.valueOf(LocalDateTime.now()), 
                id
        );
        
        rolDto.setRolId(id);
        return rolDto;
    }

    @Override
    public void eliminarRol(Long id) {
        // Eliminación lógica: Actualizamos el estado a 0
        String sql = "UPDATE ROLES SET ESTADO = 0, FECHA_MODIFICACION = ? WHERE ROL_ID = ?";
        jdbcTemplate.update(sql, Timestamp.valueOf(LocalDateTime.now()), id);
    }

    // ==========================================
    // MÉTODOS ORIGINALES (Retornan RoleEntity)
    // ==========================================

    @Override
    public Optional<RoleEntity> buscarPorId(Long rolId) {
        String sql = "SELECT * FROM ROLES WHERE ROL_ID = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rolRowMapper, rolId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<RoleEntity> buscarPorNombre(String nombre) {
        String sql = "SELECT * FROM ROLES WHERE NOMBRE = ?";
        return jdbcTemplate.query(sql, rolRowMapper, nombre).stream().findFirst();
    }

    @Override
    public List<RoleEntity> buscarRolesPorUsuarioId(Long usuarioId) {
        String sql = "SELECT R.* FROM ROLES R " +
                     "INNER JOIN USUARIOS_ROLES UR ON R.ROL_ID = UR.ROL_ID " +
                     "WHERE UR.USUARIO_ID = ? AND R.ESTADO = 1";
        return jdbcTemplate.query(sql, rolRowMapper, usuarioId);
    }

    @Override
    public void asignarRolAUsuario(Long usuarioId, Long rolId) {
        String sql = "INSERT INTO USUARIOS_ROLES (USUARIO_ID, ROL_ID, FECHA_CREACION) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, usuarioId, rolId, Timestamp.valueOf(LocalDateTime.now()));
    }

    @Override
    public void removerRolDeUsuario(Long usuarioId, Long rolId) {
        String sql = "DELETE FROM USUARIOS_ROLES WHERE USUARIO_ID = ? AND ROL_ID = ?";
        jdbcTemplate.update(sql, usuarioId, rolId);
    }
}