package com.core.back.exception;

import java.io.Serial;

public class
ConflictException extends RuntimeException {

    /**
	 * 
	 */
    @Serial
	private static final long serialVersionUID = 1L;

	public ConflictException() {
	    super("A conflict occurred with the current resource state.");
    }

    public ConflictException(String message) {
        super(message);
    }
    
}