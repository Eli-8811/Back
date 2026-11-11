package com.core.back.exception;

public class ConflictException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ConflictException() {
	    super("A conflict occurred with the current resource state.");
    }

    public ConflictException(String message) {
        super(message);
    }
    
}