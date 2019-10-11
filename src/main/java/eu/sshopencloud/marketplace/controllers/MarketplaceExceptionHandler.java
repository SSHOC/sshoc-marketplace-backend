package eu.sshopencloud.marketplace.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
@Slf4j
public class MarketplaceExceptionHandler {

    @ExceptionHandler(value = NoHandlerFoundException.class)
    public ResponseEntity<Object> handleException(NoHandlerFoundException ex, WebRequest request) {
        log.error("No endpoint", ex);
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Object> handleException(Exception ex, WebRequest request) {
        log.error("Exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @ExceptionHandler(value = RuntimeException.class)
    public ResponseEntity handleServerError(Exception ex, WebRequest request) {
        log.error("Runtime exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

}
