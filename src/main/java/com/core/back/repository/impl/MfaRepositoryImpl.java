package com.core.back.repository.impl;

import com.core.back.repository.IMfaRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class MfaRepositoryImpl implements IMfaRepository {

    private final JdbcTemplate jdbcTemplate;

    public MfaRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<String> findEncryptedSecret(Long userId) {
        return jdbcTemplate.query(
                "SELECT SECRET_ENCRYPTED FROM USUARIO_MFA WHERE USUARIO_ID = ?",
                resultSet -> resultSet.next()
                        ? Optional.of(resultSet.getString("SECRET_ENCRYPTED"))
                        : Optional.empty(),
                userId);
    }

    @Override
    @Transactional
    public void completeEnrollment(
            Long userId,
            String encryptedSecret,
            long lastTotpStep,
            List<String> recoveryCodeHashes) {
        int inserted = jdbcTemplate.update(
                "INSERT INTO USUARIO_MFA (USUARIO_ID, SECRET_ENCRYPTED, LAST_TOTP_STEP) VALUES (?, ?, ?) "
                        + "ON CONFLICT (USUARIO_ID) DO NOTHING",
                userId,
                encryptedSecret,
                lastTotpStep);
        if (inserted != 1) {
            throw new IllegalStateException("MFA enrollment has already been completed");
        }
        jdbcTemplate.batchUpdate(
                "INSERT INTO USUARIO_MFA_RECOVERY_CODE (USUARIO_ID, CODE_HASH) VALUES (?, ?)",
                recoveryCodeHashes,
                recoveryCodeHashes.size(),
                (statement, codeHash) -> {
                    statement.setLong(1, userId);
                    statement.setString(2, codeHash);
                });
    }

    @Override
    public boolean acceptTotpStep(Long userId, long step) {
        return jdbcTemplate.update(
                "UPDATE USUARIO_MFA SET LAST_TOTP_STEP = ? "
                        + "WHERE USUARIO_ID = ? AND LAST_TOTP_STEP < ?",
                step,
                userId,
                step) == 1;
    }

    @Override
    public boolean isRecoveryCodeAvailable(Long userId, String recoveryCodeHash) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM USUARIO_MFA_RECOVERY_CODE WHERE USUARIO_ID = ? AND CODE_HASH = ?",
                Integer.class,
                userId,
                recoveryCodeHash);
        return count != null && count > 0;
    }

    @Override
    public boolean consumeRecoveryCode(Long userId, String recoveryCodeHash) {
        return jdbcTemplate.update(
                "DELETE FROM USUARIO_MFA_RECOVERY_CODE WHERE USUARIO_ID = ? AND CODE_HASH = ?",
                userId,
                recoveryCodeHash) == 1;
    }
}
