package com.core.back.repository.impl;

import com.core.back.entity.PermissionEntity;
import com.core.back.entity.RoleEntity;
import com.core.back.entity.UserEntity;
import com.core.back.repository.IUserRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

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

    private final RowMapper<UserEntity> userRowMapper = (resultSet, rowNumber) -> {
        UserEntity user = new UserEntity();
        user.setUserId(resultSet.getLong("USUARIO_ID"));
        user.setFirstName(resultSet.getString("NOMBRE"));
        user.setLastName(resultSet.getString("APELLIDOS"));
        user.setAge(resultSet.getObject("EDAD") != null ? resultSet.getInt("EDAD") : null);
        user.setGender(resultSet.getObject("GENERO") != null ? resultSet.getInt("GENERO") : null);
        user.setUsername(resultSet.getString("USERNAME"));
        user.setPassword(resultSet.getString("PASSWORD"));
        user.setEmail(resultSet.getString("CORREO"));
        user.setActive(resultSet.getInt("ESTADO") == 1);
        Timestamp createdAt = resultSet.getTimestamp("FECHA_CREACION");
        user.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);
        Timestamp updatedAt = resultSet.getTimestamp("FECHA_MODIFICACION");
        user.setUpdatedAt(updatedAt != null ? updatedAt.toLocalDateTime() : null);
        return user;
    };

    @Override
    public UserEntity saveUser(UserEntity user) {
        String sql = "INSERT INTO USUARIO (NOMBRE, APELLIDOS, EDAD, GENERO, USERNAME, PASSWORD, CORREO, ESTADO) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING USUARIO_ID";

        Long userId = jdbcTemplate.queryForObject(
                sql,
                Long.class,
                user.getFirstName(),
                user.getLastName(),
                user.getAge(),
                user.getGender(),
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                Boolean.TRUE.equals(user.getActive()));
        user.setUserId(userId);
        return user;
    }

    @Override
    public void assignRoleToUser(Long userId, Long roleId) {
        String sql = "INSERT INTO USUARIO_ROL (USUARIO_ID, ROL_ID) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, roleId);
    }
    
    @Override
    public UserEntity updateUser(UserEntity user) {
        String sql = "UPDATE USUARIO SET NOMBRE = ?, APELLIDOS = ?, EDAD = ?, GENERO = ?, CORREO = ?, FECHA_MODIFICACION = ? WHERE USUARIO_ID = ?";
        LocalDateTime now = LocalDateTime.now();
        user.setUpdatedAt(now);

        jdbcTemplate.update(sql, user.getFirstName(), user.getLastName(), user.getAge(), user.getGender(), user.getEmail(), Timestamp.valueOf(now), user.getUserId());
        return user;
    }

    @Override
    public Optional<UserEntity> findUserById(Long userId) {
        String sql = "SELECT * FROM USUARIO WHERE USUARIO_ID = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, userRowMapper, userId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<UserEntity> findUserByEmail(String email) {
        String sql = "SELECT * FROM USUARIO WHERE CORREO = ?";
        return jdbcTemplate.query(sql, userRowMapper, email).stream().findFirst();
    }

    @Override
    public Optional<UserEntity> findUserByUsername(String username) {
        String sql = "SELECT * FROM USUARIO WHERE USERNAME = ?";
        return jdbcTemplate.query(sql, userRowMapper, username).stream().findFirst();
    }

    @Override
    public Optional<List<UserEntity>> findActiveUsers(Boolean active) {
        String sql = "SELECT U.*, " +
                     "R.ROL_ID, R.NOMBRE AS ROL_NOMBRE, R.PERSPECTIVA AS ROL_PERSPECTIVA, R.ESTADO AS ROL_ESTADO, R.FECHA_CREACION AS ROL_FC, R.FECHA_MODIFICACION AS ROL_FM, " +
                     "P.PERMISO_ID, P.NOMBRE AS PERMISO_NOMBRE, P.PERSPECTIVA AS PERMISO_PERSPECTIVA, P.ESTADO AS PERMISO_ESTADO, P.FECHA_CREACION AS PERMISO_FC, P.FECHA_MODIFICACION AS PERMISO_FM " +
                     "FROM USUARIO U " +
                     "LEFT JOIN USUARIO_ROL UR ON U.USUARIO_ID = UR.USUARIO_ID " +
                     "LEFT JOIN ROLES R ON UR.ROL_ID = R.ROL_ID " +
                     "LEFT JOIN ROL_PERMISO RP ON R.ROL_ID = RP.ROL_ID " +
                     "LEFT JOIN PERMISO P ON RP.PERMISO_ID = P.PERMISO_ID " +
                     "WHERE U.ESTADO = ?";

        return Optional.ofNullable(jdbcTemplate.query(sql, resultSet -> {
            Map<Long, UserEntity> usersById = new LinkedHashMap<>();
            while (resultSet.next()) {
                Long userId = resultSet.getLong("USUARIO_ID");
                
                UserEntity user = usersById.computeIfAbsent(userId, mappedUserId -> {
                    try {
                        UserEntity mappedUser = new UserEntity();
                        mappedUser.setUserId(mappedUserId);
                        mappedUser.setFirstName(resultSet.getString("NOMBRE"));
                        mappedUser.setLastName(resultSet.getString("APELLIDOS"));
                        mappedUser.setAge(resultSet.getObject("EDAD", Integer.class));
                        mappedUser.setGender(resultSet.getObject("GENERO", Integer.class));
                        mappedUser.setUsername(resultSet.getString("USERNAME"));
                        mappedUser.setPassword(resultSet.getString("PASSWORD"));
                        mappedUser.setEmail(resultSet.getString("CORREO"));
                        mappedUser.setActive(resultSet.getInt("ESTADO") == 1);
                        Timestamp createdAt = resultSet.getTimestamp("FECHA_CREACION");
                        mappedUser.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);
                        Timestamp updatedAt = resultSet.getTimestamp("FECHA_MODIFICACION");
                        mappedUser.setUpdatedAt(updatedAt != null ? updatedAt.toLocalDateTime() : null);
                        mappedUser.setRoles(new ArrayList<>());
                        return mappedUser;
                    } catch (Exception exception) {
                        return null;
                    }
                });

                Long roleId = resultSet.getObject("ROL_ID", Long.class);
                if (roleId != null && user.getRoles().stream().noneMatch(role -> role.getRoleId().equals(roleId))) {
                    RoleEntity role = new RoleEntity();
                    role.setRoleId(roleId);
                    role.setName(resultSet.getString("ROL_NOMBRE"));
                    role.setPerspective(resultSet.getString("ROL_PERSPECTIVA"));
                    role.setActive(resultSet.getInt("ROL_ESTADO") == 1);
                    Timestamp roleCreatedAt = resultSet.getTimestamp("ROL_FC");
                    role.setCreatedAt(roleCreatedAt != null ? roleCreatedAt.toLocalDateTime() : null);
                    Timestamp roleUpdatedAt = resultSet.getTimestamp("ROL_FM");
                    role.setUpdatedAt(roleUpdatedAt != null ? roleUpdatedAt.toLocalDateTime() : null);
                    role.setPermissions(new ArrayList<>());
                    user.getRoles().add(role);
                }

                Long permissionId = resultSet.getObject("PERMISO_ID", Long.class);
                if (permissionId != null) {
                    RoleEntity userRole = user.getRoles().stream()
                            .filter(role -> role.getRoleId().equals(roleId))
                            .findFirst()
                            .orElse(null);
                    if (userRole != null && userRole.getPermissions().stream()
                            .noneMatch(permission -> permission.getPermissionId().equals(permissionId))) {
                        PermissionEntity permission = new PermissionEntity();
                        permission.setPermissionId(permissionId);
                        permission.setName(resultSet.getString("PERMISO_NOMBRE"));
                        permission.setPerspective(resultSet.getString("PERMISO_PERSPECTIVA"));
                        permission.setActive(resultSet.getInt("PERMISO_ESTADO") == 1);
                        Timestamp permissionCreatedAt = resultSet.getTimestamp("PERMISO_FC");
                        permission.setCreatedAt(permissionCreatedAt != null
                                ? permissionCreatedAt.toLocalDateTime()
                                : null);
                        Timestamp permissionUpdatedAt = resultSet.getTimestamp("PERMISO_FM");
                        permission.setUpdatedAt(permissionUpdatedAt != null
                                ? permissionUpdatedAt.toLocalDateTime()
                                : null);
                        userRole.getPermissions().add(permission);
                    }
                }
            }
            return new ArrayList<>(usersById.values());
        }, active ? 1 : 0));
    }
    
    @Override
    public boolean existsUserById(Long userId) {
        String sql = "SELECT COUNT(*) FROM USUARIO WHERE USUARIO_ID = ?";
        Integer userCount = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return userCount != null && userCount > 0;
    }

    @Override
    public boolean existsUserByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM USUARIO WHERE CORREO = ?";
        Integer userCount = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return userCount != null && userCount > 0;
    }

    @Override
    public boolean existsUserByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM USUARIO WHERE USERNAME = ?";
        Integer userCount = jdbcTemplate.queryForObject(sql, Integer.class, username);
        return userCount != null && userCount > 0;
    }

    @Override
    public void deleteUserById(Long userId) {
        String sql = "DELETE FROM USUARIO WHERE USUARIO_ID = ?";
        jdbcTemplate.update(sql, userId);
    }
    
    @Override
    public boolean existsRoleById(Long roleId) {
        String sql = "SELECT COUNT(*) FROM ROL WHERE ROL_ID = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, roleId);
        return count != null && count > 0;
    }
    
}