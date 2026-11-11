package com.core.back.exception;

public class ResourceNotFoundException extends RuntimeException {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ResourceNotFoundException() {
        super("The requested resource was not found.");
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
    
}