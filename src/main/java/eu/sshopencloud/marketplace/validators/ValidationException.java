package eu.sshopencloud.marketplace.validators;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.Errors;

@Getter
@RequiredArgsConstructor
public class ValidationException extends RuntimeException {

    private final Errors errors;

}
