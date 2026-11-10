package com.core.back.exception;

public class UnauthorizedException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UnauthorizedException() {
        super("No autorizado. Es necesario autenticarse para acceder a este recurso.");
    }

    public UnauthorizedException(String message) {
        super(message);
    }
    
}