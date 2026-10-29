package br.com.helder.productactivation.domain.exception;

public class IllegalStatusException extends RuntimeException {

    public IllegalStatusException(String message) {
        super(message);
    }
}
