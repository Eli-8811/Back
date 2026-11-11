package com.core.back.exception;

import java.io.Serial;

public class ResourceNotFoundException extends RuntimeException {
    
    /**
	 * 
	 */
    @Serial
	private static final long serialVersionUID = 1L;

	public ResourceNotFoundException() {
        super("The requested resource was not found.");
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
    
}