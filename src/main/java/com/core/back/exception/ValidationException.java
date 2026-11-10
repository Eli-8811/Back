package com.core.back.exception;

public class ValidationException  extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ValidationException() {
        super("Error de validación en campos");
    }

    public ValidationException(String message) {
        super(message);
    }
    
}