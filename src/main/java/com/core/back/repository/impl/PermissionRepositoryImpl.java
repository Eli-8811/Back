package com.core.back.repository.impl;

import com.core.back.entity.PermissionEntity;
import com.core.back.repository.IPermissionRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Repository
public class PermissionRepositoryImpl implements IPermissionRepository {

	private final JdbcTemplate jdbcTemplate;

	public PermissionRepositoryImpl(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public PermissionEntity savePermission(PermissionEntity permission) {
		String sql = "INSERT INTO PERMISO (NOMBRE, PERSPECTIVA, ESTADO, FECHA_CREACION) "
				+ "VALUES (?, ?, ?, ?) RETURNING PERMISO_ID";
		Long permissionId = jdbcTemplate.queryForObject(
				sql,
				Long.class,
				permission.getName(),
				permission.getPerspective(),
				Boolean.TRUE.equals(permission.getActive()),
				Timestamp.valueOf(LocalDateTime.now()));
		permission.setPermissionId(permissionId);
		return permission;
	}
	
}