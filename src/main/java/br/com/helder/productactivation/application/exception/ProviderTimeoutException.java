package br.com.helder.productactivation.application.exception;

public class ProviderTimeoutException extends ProviderUnavailableException {

    public ProviderTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}