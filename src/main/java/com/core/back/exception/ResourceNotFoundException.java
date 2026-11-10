package com.core.back.exception;

public class ResourceNotFoundException extends RuntimeException {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ResourceNotFoundException() {
        super("El recurso solicitado no fue encontrado.");
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
    
}