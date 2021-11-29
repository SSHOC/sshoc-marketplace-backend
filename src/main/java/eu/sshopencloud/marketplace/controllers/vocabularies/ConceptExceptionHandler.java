package eu.sshopencloud.marketplace.controllers.vocabularies;

import eu.sshopencloud.marketplace.controllers.ErrorResponse;
import eu.sshopencloud.marketplace.services.vocabularies.exception.ConceptAlreadyExistsException;
import eu.sshopencloud.marketplace.services.vocabularies.exception.PropertyTypeAlreadyExistsException;
import eu.sshopencloud.marketplace.services.vocabularies.exception.VocabularyAlreadyExistsException;
import eu.sshopencloud.marketplace.services.vocabularies.exception.VocabularyIsClosedException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@ControllerAdvice(assignableTypes = {ConceptController.class})
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class ConceptExceptionHandler {

    @ExceptionHandler(value = {
            ConceptAlreadyExistsException.class,
            VocabularyIsClosedException.class
    })
    public ResponseEntity<Object> handleBadRequestException(Exception ex, WebRequest request) {
        log.error("Exception", ex);
        ErrorResponse errorResponse = ErrorResponse.builder().timestamp(LocalDateTime.now()).status(HttpStatus.BAD_REQUEST.value()).error(ex.getMessage()).build();
        return ResponseEntity.badRequest().body(errorResponse);
    }

}
