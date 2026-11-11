package com.core.back.exception;

public class ValidationException  extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ValidationException() {
        super("Field validation failed.");
    }

    public ValidationException(String message) {
        super(message);
    }
    
}