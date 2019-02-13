package com.github.rapgru.propertycluster;

public class WrongTypeContextException extends RuntimeException{
    public WrongTypeContextException() {
    }

    public WrongTypeContextException(String message) {
        super(message);
    }

    public WrongTypeContextException(String message, Throwable cause) {
        super(message, cause);
    }

    public WrongTypeContextException(Throwable cause) {
        super(cause);
    }
}
