package com.core.back.repository;

import java.util.List;
import java.util.Optional;

public interface IMfaRepository {

    Optional<String> findEncryptedSecret(Long userId);

    void completeEnrollment(Long userId, String encryptedSecret, long lastTotpStep, List<String> recoveryCodeHashes);

    boolean acceptTotpStep(Long userId, long step);

    boolean isRecoveryCodeAvailable(Long userId, String recoveryCodeHash);

    boolean consumeRecoveryCode(Long userId, String recoveryCodeHash);
}
