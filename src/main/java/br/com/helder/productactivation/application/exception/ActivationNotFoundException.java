package br.com.helder.productactivation.application.exception;

public class ActivationNotFoundException extends RuntimeException {

    public ActivationNotFoundException(String message) {
        super(message);
    }
}