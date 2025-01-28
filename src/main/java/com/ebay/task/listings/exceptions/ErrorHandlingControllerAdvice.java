package com.ebay.task.listings.exceptions;

import com.fasterxml.jackson.core.JsonParseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.StringUtils;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentConversionNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@ControllerAdvice
public class ErrorHandlingControllerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    ResponseEntity<Object> onMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        FieldError fieldError = Optional.ofNullable(e.getBindingResult().getFieldError())
                .orElse(new FieldError("", "", HttpStatus.BAD_REQUEST.getReasonPhrase()));
        String defaultErrorMessage = Objects.requireNonNull(fieldError.getDefaultMessage()).split("\\.")[0];
        String finalErrorMessage = "";
        if (!defaultErrorMessage.toLowerCase().contains(fieldError.getField().toLowerCase())) {
            finalErrorMessage = finalErrorMessage.concat(fieldError.getField()) + " ";
        }
        finalErrorMessage = StringUtils.capitalize(finalErrorMessage
                        .concat(defaultErrorMessage)
                        .toLowerCase())
                .replace("[0].", " ")
                .replace("enum ", "")
                + ".";

        Violation violation = Violation.builder()
                .timestamp(Instant.now().getEpochSecond())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(finalErrorMessage)
                .build();
        return new ResponseEntity<>(violation, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({
            JsonParseException.class,
            HttpMessageNotReadableException.class,
            HttpMessageConversionException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    ResponseEntity<Object> onParseException(
            Exception e) {
        String errorMessage = e.getMessage()
                .substring(0, e.getMessage().indexOf(':'))
                + ".";

        Violation violation = Violation.builder()
                .timestamp(Instant.now().getEpochSecond())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(errorMessage)
                .build();
        return new ResponseEntity<>(violation, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            MethodArgumentConversionNotSupportedException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    ResponseEntity<Object> onMethodArgumentException(
            Exception e) {
        String errorMessage = "";
        if (e.getClass().equals(MethodArgumentTypeMismatchException.class)) {
            errorMessage = ((MethodArgumentTypeMismatchException) e).getName();
        }
        errorMessage = errorMessage.concat(" input is invalid.");
        errorMessage = StringUtils.capitalize(errorMessage.strip());

        Violation violation = Violation.builder()
                .timestamp(Instant.now().getEpochSecond())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(errorMessage)
                .build();
        return new ResponseEntity<>(violation, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ResponseBody
    ResponseEntity<Object> onRequestMethodNotSupported(HttpRequestMethodNotSupportedException e) {

        Violation violation = Violation.builder()
                .timestamp(Instant.now().getEpochSecond())
                .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                .error(HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase())
                .message(e.getMessage() + ".")
                .build();
        return new ResponseEntity<>(violation, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    ResponseEntity<Object> onDataIntegrityViolationException(DataIntegrityViolationException e) {

        Violation violation = Violation.builder()
                .timestamp(Instant.now().getEpochSecond())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Invalid entity data JSON.")
                .build();
        return new ResponseEntity<>(violation, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidDataAccessResourceUsageException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    ResponseEntity<Object> onInvalidDataAccessResourceUsageException(InvalidDataAccessResourceUsageException e) {

        Violation violation = Violation.builder()
                .timestamp(Instant.now().getEpochSecond())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Invalid entity data JSON.")
                .build();
        return new ResponseEntity<>(violation, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ResponseBody
    ResponseEntity<Object> onMiscException(Exception e) {

        Violation violation = Violation.builder()
                .timestamp(Instant.now().getEpochSecond())
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .error(HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase())
                .message("Unable to serve at the moment.")
                .build();
        return new ResponseEntity<>(violation, HttpStatus.SERVICE_UNAVAILABLE);
    }
}
