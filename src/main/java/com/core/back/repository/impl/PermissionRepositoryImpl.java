package com.core.back.repository.impl;

import com.core.back.entity.PermissionEntity;
import com.core.back.repository.IPermissionRepository;
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
public class PermissionRepositoryImpl implements IPermissionRepository {

	private final JdbcTemplate jdbcTemplate;

	public PermissionRepositoryImpl(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private final RowMapper<PermissionEntity> permisoRowMapper = (ResultSet rs, int rowNum) -> {
		PermissionEntity permission = new PermissionEntity();
		permission.setPermissionId(rs.getLong("PERMISO_ID"));
		permission.setName(rs.getString("NOMBRE"));
		permission.setPerspective(rs.getString("PERSPECTIVA"));
		permission.setActive(rs.getInt("ESTADO") == 1);
		Timestamp fc = rs.getTimestamp("FECHA_CREACION");
		permission.setCreatedAt(fc != null ? fc.toLocalDateTime() : null);
		Timestamp fm = rs.getTimestamp("FECHA_MODIFICACION");
		permission.setUpdatedAt(fm != null ? fm.toLocalDateTime() : null);
		return permission;
	};

	@Override
	public Optional<PermissionEntity> findPermissionById(Long permissionId) {
		String sql = "SELECT * FROM PERMISO WHERE PERMISO_ID = ?";
		try {
			return Optional.ofNullable(jdbcTemplate.queryForObject(sql, permisoRowMapper, permissionId));
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}

	@Override
	public List<PermissionEntity> findPermissionsByRoleId(Long roleId) {
		String sql = "SELECT P.* FROM PERMISO P " + "INNER JOIN ROL_PERMISO RP ON P.PERMISO_ID = RP.PERMISO_ID "
				+ "WHERE RP.ROL_ID = ? AND P.ESTADO = 1";
		return jdbcTemplate.query(sql, permisoRowMapper, roleId);
	}

	@Override
	public void assignPermissionToRole(Long roleId, Long permissionId) {
		String sql = "INSERT INTO ROL_PERMISO (ROL_ID, PERMISO_ID, FECHA_CREACION) VALUES (?, ?, ?)";
		jdbcTemplate.update(sql, roleId, permissionId, Timestamp.valueOf(LocalDateTime.now()));
	}

	@Override
	public void removePermissionFromRole(Long roleId, Long permissionId) {
		String sql = "DELETE FROM ROL_PERMISO WHERE ROL_ID = ? AND PERMISO_ID = ?";
		jdbcTemplate.update(sql, roleId, permissionId);
	}

	@Override
	public void removeAllPermissionsFromRole(Long roleId) {
		String sql = "DELETE FROM ROL_PERMISO WHERE ROL_ID = ?";
		jdbcTemplate.update(sql, roleId);
	}

	@Override
	public PermissionEntity savePermission(PermissionEntity permiso) {
		String sql = "INSERT INTO PERMISO (NOMBRE, PERSPECTIVA, ESTADO, FECHA_CREACION) VALUES (?, ?, ?, ?)";

		KeyHolder keyHolder = new GeneratedKeyHolder();

		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sql, new String[] { "PERMISO_ID" });
			ps.setString(1, permiso.getName());
			ps.setString(2, permiso.getPerspective());
			ps.setInt(3, permiso.getActive() ? 1 : 0);
			ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
			return ps;
		}, keyHolder);

		if (keyHolder.getKey() != null) {
			permiso.setPermissionId(keyHolder.getKey().longValue());
		}
		return permiso;
	}

	@Override
	public void updatePermission(PermissionEntity permiso) {
		String sql = "UPDATE PERMISO SET NOMBRE = ?, PERSPECTIVA = ?, ESTADO = ?, FECHA_MODIFICACION = ? WHERE PERMISO_ID = ?";
		jdbcTemplate.update(sql, permiso.getName(), permiso.getPerspective(), permiso.getActive() ? 1 : 0,
				Timestamp.valueOf(LocalDateTime.now()), permiso.getPermissionId());
	}

	@Override
	public void deletePermission(Long permisoId) {
		// Borrado lógico
		String sql = "UPDATE PERMISO SET ESTADO = 0 WHERE PERMISO_ID = ?";
		jdbcTemplate.update(sql, permisoId);
	}

	@Override
    public List<PermissionEntity> findAllPermissions() {
        String sql = "SELECT * FROM PERMISO ORDER BY PERMISO_ID ASC";
        return jdbcTemplate.query(sql, permisoRowMapper);
    }
	
}