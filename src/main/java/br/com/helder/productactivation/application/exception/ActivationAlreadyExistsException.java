
package br.com.helder.productactivation.application.exception;

public class ActivationAlreadyExistsException extends RuntimeException {

    public ActivationAlreadyExistsException(Throwable cause) {
        super("Activation already exists", cause);
    }
}
