package io.vestoria.exception;

public abstract class VestoriaException extends RuntimeException {
    public VestoriaException(String message) {
        super(message);
    }
}
