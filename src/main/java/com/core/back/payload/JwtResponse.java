package com.core.back.payload;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@JsonPropertyOrder({ "tokenType", "token" })
public class JwtResponse {

	private String tokenType;
    private String token;

}