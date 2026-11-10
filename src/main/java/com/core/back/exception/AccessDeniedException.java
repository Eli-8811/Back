package com.core.back.exception;

public class AccessDeniedException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AccessDeniedException() {
        super("Acceso denegado. No tienes los permisos necesarios.");
    }

    public AccessDeniedException(String message) {
        super(message);
    }
    
}