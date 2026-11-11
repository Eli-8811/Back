package com.core.back.exception;

import java.io.Serial;

public class ValidationException  extends RuntimeException {

    /**
	 * 
	 */

    @Serial
	private static final long serialVersionUID = 1L;

	public ValidationException() {
        super("Field validation failed.");
    }

    public ValidationException(String message) {
        super(message);
    }
    
}