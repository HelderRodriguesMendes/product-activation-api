package br.com.helder.productactivation.application.exception;

public class ProviderBadRequestException extends RuntimeException {

    public ProviderBadRequestException(String message) {
        super(message);
    }
}