package com.core.back.service;

import com.core.back.entity.UserEntity;
import com.core.back.payload.UserRequest;

import java.util.List;

public interface ITokenBlacklistService {

    void blacklistToken(String tokenId, long remainingExpirationMillis);

    boolean isTokenBlacklisted(String tokenId);

}