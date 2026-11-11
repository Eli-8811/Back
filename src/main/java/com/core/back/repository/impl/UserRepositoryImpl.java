package com.core.back.repository.impl;

import com.core.back.entity.PermissionEntity;
import com.core.back.entity.RoleEntity;
import com.core.back.entity.UserEntity;
import com.core.back.repository.IUserRepository;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class UserRepositoryImpl implements IUserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<UserEntity> usuarioRowMapper = (ResultSet rs, int rowNum) -> {
        UserEntity usuario = new UserEntity();
        usuario.setUserId(rs.getLong("USUARIO_ID"));
        usuario.setFirstName(rs.getString("NOMBRE"));
        usuario.setLastName(rs.getString("APELLIDOS"));
        usuario.setAge(rs.getObject("EDAD") != null ? rs.getInt("EDAD") : null);
        usuario.setGender(rs.getObject("GENERO") != null ? rs.getInt("GENERO") : null);
        usuario.setUsername(rs.getString("USERNAME"));
        usuario.setPassword(rs.getString("PASSWORD"));
        usuario.setEmail(rs.getString("CORREO"));
        usuario.setActive(rs.getInt("ESTADO") == 1);
        Timestamp fc = rs.getTimestamp("FECHA_CREACION");
        usuario.setCreatedAt(fc != null ? fc.toLocalDateTime() : null);
        Timestamp fm = rs.getTimestamp("FECHA_MODIFICACION");
        usuario.setUpdatedAt(fm != null ? fm.toLocalDateTime() : null);
        return usuario;
    };

    @Override
    public UserEntity saveUser(UserEntity usuario) {
        String sql = "INSERT INTO USUARIOS (NOMBRE, APELLIDOS, EDAD, GENERO, USERNAME, PASSWORD, CORREO, ESTADO, FECHA_CREACION) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime ahora = LocalDateTime.now();
        usuario.setCreatedAt(ahora);

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"USUARIO_ID"});
            ps.setString(1, usuario.getFirstName());
            ps.setString(2, usuario.getLastName());
            ps.setObject(3, usuario.getAge());
            ps.setObject(4, usuario.getGender());
            ps.setString(5, usuario.getUsername());
            ps.setString(6, usuario.getPassword());
            ps.setString(7, usuario.getEmail());
            ps.setInt(8, (usuario.getActive() != null && usuario.getActive()) ? 1 : 0);
            ps.setTimestamp(9, Timestamp.valueOf(ahora));
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            usuario.setUserId(keyHolder.getKey().longValue());
        }
        return usuario;
    }

    @Override
    public void assignRoleToUser(Long usuarioId, Long rolId) {
        String sql = "INSERT INTO USUARIOS_ROLES (USUARIO_ID, ROL_ID, FECHA_CREACION) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, usuarioId, rolId, Timestamp.valueOf(LocalDateTime.now()));
    }
    
    @Override
    public UserEntity updateUser(UserEntity usuario) {
        String sql = "UPDATE USUARIOS SET NOMBRE = ?, APELLIDOS = ?, EDAD = ?, GENERO = ?, CORREO = ?, FECHA_MODIFICACION = ? WHERE USUARIO_ID = ?";
        LocalDateTime ahora = LocalDateTime.now();
        usuario.setUpdatedAt(ahora);

        jdbcTemplate.update(sql, usuario.getFirstName(), usuario.getLastName(), usuario.getAge(), usuario.getGender(), usuario.getEmail(), Timestamp.valueOf(ahora), usuario.getUserId());
        return usuario;
    }

    @Override
    public Optional<UserEntity> findUserById(Long usuarioId) {
        String sql = "SELECT * FROM USUARIOS WHERE USUARIO_ID = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, usuarioRowMapper, usuarioId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<UserEntity> findUserByEmail(String correo) {
        String sql = "SELECT * FROM USUARIOS WHERE CORREO = ?";
        return jdbcTemplate.query(sql, usuarioRowMapper, correo).stream().findFirst();
    }

    @Override
    public Optional<UserEntity> findUserByUsername(String username) {
        String sql = "SELECT * FROM USUARIOS WHERE USERNAME = ?";
        return jdbcTemplate.query(sql, usuarioRowMapper, username).stream().findFirst();
    }

    @Override
    public Optional<List<UserEntity>> findActiveUsers(Boolean estado) {
        // Se agregan los campos de ESTADO, FECHA_CREACION y MODIFICACION para Roles y Permisos
        String sql = "SELECT U.*, " +
                     "R.ROL_ID, R.NOMBRE AS ROL_NOMBRE, R.PERSPECTIVA AS ROL_PERSPECTIVA, R.ESTADO AS ROL_ESTADO, R.FECHA_CREACION AS ROL_FC, R.FECHA_MODIFICACION AS ROL_FM, " +
                     "P.PERMISO_ID, P.NOMBRE AS PERMISO_NOMBRE, P.PERSPECTIVA AS PERMISO_PERSPECTIVA, P.ESTADO AS PERMISO_ESTADO, P.FECHA_CREACION AS PERMISO_FC, P.FECHA_MODIFICACION AS PERMISO_FM " +
                     "FROM USUARIOS U " +
                     "LEFT JOIN USUARIOS_ROLES UR ON U.USUARIO_ID = UR.USUARIO_ID " +
                     "LEFT JOIN ROLES R ON UR.ROL_ID = R.ROL_ID " +
                     "LEFT JOIN ROLES_PERMISOS RP ON R.ROL_ID = RP.ROL_ID " +
                     "LEFT JOIN PERMISOS P ON RP.PERMISO_ID = P.PERMISO_ID " +
                     "WHERE U.ESTADO = ?";

        return Optional.ofNullable(jdbcTemplate.query(sql, rs -> {
            Map<Long, UserEntity> map = new LinkedHashMap<>();
            while (rs.next()) {
                Long uId = rs.getLong("USUARIO_ID");
                
                // 1. Poblado Usuario
                UserEntity usuario = map.computeIfAbsent(uId, id -> {
                    try {
                        UserEntity u = new UserEntity();
                        u.setUserId(id);
                        u.setFirstName(rs.getString("NOMBRE"));
                        u.setLastName(rs.getString("APELLIDOS"));
                        u.setAge(rs.getObject("EDAD", Integer.class));
                        u.setGender(rs.getObject("GENERO", Integer.class));
                        u.setUsername(rs.getString("USERNAME"));
                        u.setPassword(rs.getString("PASSWORD"));
                        u.setEmail(rs.getString("CORREO"));
                        u.setActive(rs.getInt("ESTADO") == 1);
                        u.setCreatedAt(rs.getTimestamp("FECHA_CREACION") != null ? rs.getTimestamp("FECHA_CREACION").toLocalDateTime() : null);
                        u.setUpdatedAt(rs.getTimestamp("FECHA_MODIFICACION") != null ? rs.getTimestamp("FECHA_MODIFICACION").toLocalDateTime() : null);
                        u.setRoles(new ArrayList<>());
                        return u;
                    } catch (Exception e) { return null; }
                });

                // 2. Procesar Rol
                Long rId = rs.getObject("ROL_ID", Long.class);
                if (rId != null && usuario.getRoles().stream().noneMatch(r -> r.getRoleId().equals(rId))) {
                    RoleEntity rol = new RoleEntity();
                    rol.setRoleId(rId);
                    rol.setName(rs.getString("ROL_NOMBRE"));
                    rol.setPerspective(rs.getString("ROL_PERSPECTIVA"));
                    rol.setActive(rs.getInt("ROL_ESTADO") == 1);
                    rol.setCreatedAt(rs.getTimestamp("ROL_FC") != null ? rs.getTimestamp("ROL_FC").toLocalDateTime() : null);
                    rol.setUpdatedAt(rs.getTimestamp("ROL_FM") != null ? rs.getTimestamp("ROL_FM").toLocalDateTime() : null);
                    rol.setPermissions(new ArrayList<>());
                    usuario.getRoles().add(rol);
                }

                // 3. Procesar Permiso
                Long pId = rs.getObject("PERMISO_ID", Long.class);
                if (pId != null) {
                    RoleEntity rol = usuario.getRoles().stream().filter(r -> r.getRoleId().equals(rId)).findFirst().orElse(null);
                    if (rol != null && rol.getPermissions().stream().noneMatch(p -> p.getPermissionId().equals(pId))) {
                        PermissionEntity permiso = new PermissionEntity();
                        permiso.setPermissionId(pId);
                        permiso.setName(rs.getString("PERMISO_NOMBRE"));
                        permiso.setPerspective(rs.getString("PERMISO_PERSPECTIVA"));
                        permiso.setActive(rs.getInt("PERMISO_ESTADO") == 1);
                        permiso.setCreatedAt(rs.getTimestamp("PERMISO_FC") != null ? rs.getTimestamp("PERMISO_FC").toLocalDateTime() : null);
                        permiso.setUpdatedAt(rs.getTimestamp("PERMISO_FM") != null ? rs.getTimestamp("PERMISO_FM").toLocalDateTime() : null);
                        rol.getPermissions().add(permiso);
                    }
                }
            }
            return new ArrayList<>(map.values());
        }, estado ? 1 : 0));
    }
    
    @Override
    public boolean existsUserById(Long usuarioId) {
        String sql = "SELECT COUNT(*) FROM USUARIOS WHERE USUARIO_ID = ?";
        Integer c = jdbcTemplate.queryForObject(sql, Integer.class, usuarioId);
        return c != null && c > 0;
    }

    @Override
    public boolean existsUserByEmail(String correo) {
        String sql = "SELECT COUNT(*) FROM USUARIOS WHERE CORREO = ?";
        Integer c = jdbcTemplate.queryForObject(sql, Integer.class, correo);
        return c != null && c > 0;
    }

    @Override
    public boolean existsUserByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM USUARIOS WHERE USERNAME = ?";
        Integer c = jdbcTemplate.queryForObject(sql, Integer.class, username);
        return c != null && c > 0;
    }

    @Override
    public void deleteUserById(Long usuarioId) {
        String sql = "DELETE FROM USUARIOS WHERE USUARIO_ID = ?";
        jdbcTemplate.update(sql, usuarioId);
    }
    
    @Override
    public boolean existsRoleById(Long rolId) {
        String sql = "SELECT COUNT(*) FROM ROLES WHERE ROL_ID = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, rolId);
        return count != null && count > 0;
    }
    
}