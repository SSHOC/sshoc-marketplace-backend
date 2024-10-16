package eu.sshopencloud.marketplace.controllers;

import eu.sshopencloud.marketplace.services.items.exception.ItemIsAlreadyMergedException;
import eu.sshopencloud.marketplace.services.items.exception.VersionNotChangedException;
import eu.sshopencloud.marketplace.validators.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDateTime;


@ControllerAdvice
@Slf4j
public class MarketplaceExceptionHandler {

    @ExceptionHandler(value = { ValidationException.class })
    public ResponseEntity<Object> handleValidationException(ValidationException ex, WebRequest request) {
        log.error("Validation Exception", ex);
        ValidationResponse validationResponse = ValidationResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .errors(ex.getErrors().getFieldErrors().stream().map(error -> ValidatedError.builder().field(error.getField())
                        .code(error.getCode()).args(error.getArguments()).message(error.getDefaultMessage()).build()).toArray(ValidatedError[]::new))
                .build();
        return ResponseEntity.badRequest().body(validationResponse);
    }

    @ExceptionHandler(value = { PageTooLargeException.class, ParseException.class, IllegalArgumentException.class, ItemIsAlreadyMergedException.class })
    public ResponseEntity<Object> handleBadRequestException(Exception ex, WebRequest request) {
        log.error("Exception", ex);
        ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now()).status(HttpStatus.BAD_REQUEST.value()).error(ex.getMessage()).build();
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(value = { VersionNotChangedException.class })
    public ResponseEntity<Object> handleNotModifiedException(Exception ex, WebRequest request) {
        log.error("Exception", ex);
        ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now()).status(HttpStatus.BAD_REQUEST.value()).error(ex.getMessage()).build();
        return ResponseEntity.status(HttpStatus.NOT_MODIFIED).body(errorResponse);
    }

    @ExceptionHandler(value = { AccessDeniedException.class })
    public ResponseEntity<Object> handleInsufficientPrivilegesException(Exception ex, WebRequest request) {
        log.error("Exception", ex);
        ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now()).status(HttpStatus.FORBIDDEN.value()).error(ex.getMessage()).build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(value = EntityNotFoundException.class)
    public ResponseEntity<Object> handleEntityNotFoundException(EntityNotFoundException ex, WebRequest request) {
        log.error("No entity", ex);
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLockingException(OptimisticLockException e) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error(e.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(value = NoHandlerFoundException.class)
    public ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, WebRequest request) {
        log.error("No endpoint", ex);
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(value = { IOException.class, Exception.class })
    public ResponseEntity<Object> handleServerException(Exception ex, WebRequest request) {
        log.error("Server Exception", ex);
        ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now()).status(HttpStatus.INTERNAL_SERVER_ERROR.value()).error(ex.getMessage()).build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(value = RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleServerError(Exception ex, WebRequest request) {
        log.error("Runtime exception", ex);
        if (ex.getCause() != null && ex.getCause().getCause() != null && ex.getCause().getCause().getCause() != null && ex.getCause().getCause().getCause() instanceof ParseException) {
            ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now()).status(HttpStatus.BAD_REQUEST.value()).error(ex.getCause().getCause().getMessage()).build();
            return ResponseEntity.badRequest().body(errorResponse);
        } else{
            ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now()).status(HttpStatus.INTERNAL_SERVER_ERROR.value()).error(ex.getMessage()).build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @ExceptionHandler(value = { MethodArgumentNotValidException.class })
    public ResponseEntity<Object> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        log.error("MethodArgumentNotValidException", ex);
        ValidationResponse validationResponse = ValidationResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .errors(ex.getBindingResult().getFieldErrors().stream().map(error -> ValidatedError.builder().field(error.getField())
                        .code(error.getCode()).args(error.getArguments()).message(error.getDefaultMessage()).build()).toArray(ValidatedError[]::new))
                .build();
        return ResponseEntity.badRequest().body(validationResponse);
    }
}
