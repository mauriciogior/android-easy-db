package com.mauriciogiordano.easydb.exception;

public class NoContextFoundException extends RuntimeException {

    /**
     * Throws a RuntimeException with a custom message.
     */
    public NoContextFoundException() {
        super("Have you set the context to make this operation? (if you did not used a constructor with Context as parameter, you must call Model#setContext before using operations such as: save, find, etc.)");
    }
}
