package eu.sshopencloud.marketplace.controllers;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidatedError {

    private String field;

    private String code;

    private Object[] args;

    private String message;

}
