package br.com.helder.productactivation.web.exception;

import br.com.helder.productactivation.application.exception.ActivationNotFoundException;
import br.com.helder.productactivation.domain.exception.ActionDomainException;
import br.com.helder.productactivation.domain.exception.ActivationConflictException;
import br.com.helder.productactivation.domain.exception.IllegalStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ActivationNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(
        ActivationNotFoundException exception
    ) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(ActivationConflictException.class)
    public ResponseEntity<ProblemDetail> handleConflict(
        ActivationConflictException exception
    ) {
        return buildResponse(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(ActionDomainException.class)
    public ResponseEntity<ProblemDetail> handleDomainValidation(
        ActionDomainException exception
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(IllegalStatusException.class)
    public ResponseEntity<ProblemDetail> handleIllegalStatus(
        IllegalStatusException exception
    ) {
        return buildResponse(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(
        MethodArgumentNotValidException exception
    ) {
        String message = exception.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .distinct()
            .sorted()
            .reduce((first, second) -> first + "; " + second)
            .orElse("Invalid request data");

        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleInvalidBody(
        HttpMessageNotReadableException exception
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid request body");
    }

    private ResponseEntity<ProblemDetail> buildResponse(
        HttpStatus status,
        String message
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            status,
            message
        );

        problem.setTitle(status.getReasonPhrase());

        return ResponseEntity.status(status).body(problem);
    }
}