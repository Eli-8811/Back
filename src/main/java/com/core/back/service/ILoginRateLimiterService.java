package com.core.back.service;

import jakarta.servlet.http.HttpServletRequest;

public interface ILoginRateLimiterService {

    boolean isAllowed(HttpServletRequest request);

    void reset(HttpServletRequest request);
}