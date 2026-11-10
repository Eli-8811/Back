package com.core.back.repository.impl;

import com.core.back.entity.PermissionEntity;
import com.core.back.repository.IPermissionRepository;
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
public class PermissionRepositoryImpl implements IPermissionRepository {

	private final JdbcTemplate jdbcTemplate;

	private final RowMapper<PermissionEntity> permisoRowMapper = (ResultSet rs, int rowNum) -> {
		PermissionEntity permiso = new PermissionEntity();
		permiso.setPermisoId(rs.getLong("PERMISO_ID"));
		permiso.setNombre(rs.getString("NOMBRE"));
		permiso.setPerspectiva(rs.getString("PERSPECTIVA"));
		permiso.setEstado(rs.getInt("ESTADO") == 1);

		Timestamp fc = rs.getTimestamp("FECHA_CREACION");
		permiso.setFechaCreacion(fc != null ? fc.toLocalDateTime() : null);
		Timestamp fm = rs.getTimestamp("FECHA_MODIFICACION");
		permiso.setFechaModificacion(fm != null ? fm.toLocalDateTime() : null);
		return permiso;
	};

	@Override
	public Optional<PermissionEntity> buscarPorId(Long permisoId) {
		String sql = "SELECT * FROM PERMISOS WHERE PERMISO_ID = ?";
		try {
			return Optional.ofNullable(jdbcTemplate.queryForObject(sql, permisoRowMapper, permisoId));
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}

	@Override
	public List<PermissionEntity> buscarPermisosPorRolId(Long rolId) {
		String sql = "SELECT P.* FROM PERMISOS P " + "INNER JOIN ROLES_PERMISOS RP ON P.PERMISO_ID = RP.PERMISO_ID "
				+ "WHERE RP.ROL_ID = ? AND P.ESTADO = 1";
		return jdbcTemplate.query(sql, permisoRowMapper, rolId);
	}

	@Override
	public void asignarPermisoARol(Long rolId, Long permisoId) {
		String sql = "INSERT INTO ROLES_PERMISOS (ROL_ID, PERMISO_ID, FECHA_CREACION) VALUES (?, ?, ?)";
		jdbcTemplate.update(sql, rolId, permisoId, Timestamp.valueOf(LocalDateTime.now()));
	}

	@Override
	public void removerPermisoDeRol(Long rolId, Long permisoId) {
		String sql = "DELETE FROM ROLES_PERMISOS WHERE ROL_ID = ? AND PERMISO_ID = ?";
		jdbcTemplate.update(sql, rolId, permisoId);
	}

	@Override
	public void eliminarPermisosDeRol(Long rolId) {
		String sql = "DELETE FROM ROLES_PERMISOS WHERE ROL_ID = ?";
		jdbcTemplate.update(sql, rolId);
	}

	@Override
	public PermissionEntity guardar(PermissionEntity permiso) {
		String sql = "INSERT INTO PERMISOS (NOMBRE, PERSPECTIVA, ESTADO, FECHA_CREACION) VALUES (?, ?, ?, ?)";

		KeyHolder keyHolder = new GeneratedKeyHolder();

		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sql, new String[] { "PERMISO_ID" });
			ps.setString(1, permiso.getNombre());
			ps.setString(2, permiso.getPerspectiva());
			ps.setInt(3, permiso.getEstado() ? 1 : 0);
			ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
			return ps;
		}, keyHolder);

		if (keyHolder.getKey() != null) {
			permiso.setPermisoId(keyHolder.getKey().longValue());
		}
		return permiso;
	}

	@Override
	public void actualizar(PermissionEntity permiso) {
		String sql = "UPDATE PERMISOS SET NOMBRE = ?, PERSPECTIVA = ?, ESTADO = ?, FECHA_MODIFICACION = ? WHERE PERMISO_ID = ?";
		jdbcTemplate.update(sql, permiso.getNombre(), permiso.getPerspectiva(), permiso.getEstado() ? 1 : 0,
				Timestamp.valueOf(LocalDateTime.now()), permiso.getPermisoId());
	}

	@Override
	public void eliminar(Long permisoId) {
		// Borrado lógico
		String sql = "UPDATE PERMISOS SET ESTADO = 0 WHERE PERMISO_ID = ?";
		jdbcTemplate.update(sql, permisoId);
	}

	@Override
    public List<PermissionEntity> obtenerTodos() {
        String sql = "SELECT * FROM PERMISOS ORDER BY PERMISO_ID ASC";
        return jdbcTemplate.query(sql, permisoRowMapper);
    }
	
}