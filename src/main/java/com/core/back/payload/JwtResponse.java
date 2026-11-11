package com.core.back.payload;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "tokenType", "token" })
public class JwtResponse {

	private String tokenType;
    private String token;

    public JwtResponse(String tokenType, String token) {
        this.tokenType = tokenType;
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}