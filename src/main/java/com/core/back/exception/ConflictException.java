package com.core.back.exception;

public class ConflictException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ConflictException() {
        super("Ocurrió un conflicto con el estado actual del recurso.");
    }

    public ConflictException(String message) {
        super(message);
    }
    
}