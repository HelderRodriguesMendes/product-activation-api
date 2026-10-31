package br.com.helder.productactivation.domain.exception;

public class ActivationConflictException extends RuntimeException {

    public ActivationConflictException(String message) {
        super(message);
    }
}