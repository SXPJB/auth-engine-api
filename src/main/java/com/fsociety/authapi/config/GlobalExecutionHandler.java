package com.fsociety.authapi.config;

import com.fsociety.authapi.utils.*;
import com.fsociety.authapi.utils.Error;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExecutionHandler {

  private final Logger log = LoggerFactory.getLogger(GlobalExecutionHandler.class);

  @ExceptionHandler(DataIntegrityViolationException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ResponseEntity<ResponseBody<Error>> handleDataIntegrityViolationException(
      DataIntegrityViolationException e) {
    log.error("Data integrity violation exception", e);
    if (e.getRootCause() == null) {
      return new ResponseEntityBuilder<Error>()
          .withData(new Error(ErrorId.DATA_INTEGRITY_VIOLATION.getId(), null, ""))
          .withStatus(HttpStatus.CONFLICT)
          .build();
    }
    String errorMessage = "Data integrity violation exception: " + e.getRootCause().getMessage();
    String field = extractFieldFromMessage(errorMessage);
    return new ResponseEntityBuilder<Error>()
        .withData(new Error(ErrorId.DATA_INTEGRITY_VIOLATION.getId(), field, errorMessage))
        .withStatus(HttpStatus.CONFLICT)
        .build();
  }

  private String extractFieldFromMessage(String errorMessage) {
    Pattern pattern = Pattern.compile("\"(.*?)\"");
    Matcher matcher = pattern.matcher(errorMessage);
    if (matcher.find()) {
      return matcher.group(1);
    }
    return null;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ResponseBody<List<Error>>> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException e) {
    log.error("Method argument not valid exception", e);
    List<Error> errors =
        e.getBindingResult().getFieldErrors().stream()
            .map(
                error ->
                    new Error(
                        ErrorId.CONSTRAINT_VIOLATION.getId(),
                        error.getField(),
                        error.getDefaultMessage()))
            .toList();
    return new ResponseEntityBuilder<List<Error>>()
        .withData(errors)
        .withStatus(HttpStatus.BAD_REQUEST)
        .withMessage("Method argument not valid exception")
        .build();
  }

  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ResponseBody<List<Error>>> handleConstraintViolationException(
      ConstraintViolationException e) {
    log.error("Constraint violation exception", e);
    List<Error> errors = collectConstraintVaErrors(e);
    return new ResponseEntityBuilder<List<Error>>()
        .withData(errors)
        .withStatus(HttpStatus.BAD_REQUEST)
        .withMessage("Constraint violation exception")
        .build();
  }

  private List<Error> collectConstraintVaErrors(ConstraintViolationException e) {
    return e.getConstraintViolations().stream()
        .map(
            violation ->
                new Error(
                    "Constraint_violation",
                    violation.getPropertyPath().toString(),
                    violation.getMessage()))
        .toList();
  }

  @ExceptionHandler(NotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ResponseEntity<ResponseBody<Error>> handleNotFoundException(NotFoundException e) {
    log.error("Not found exception", e);
    return new ResponseEntityBuilder<Error>()
        .withData(new Error(ErrorId.NOT_FOUND.getId(), e.getResourceName(), e.getMessage()))
        .withStatus(HttpStatus.NOT_FOUND)
        .build();
  }

  @ExceptionHandler(LoginAuthenticationException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ResponseEntity<ResponseBody<Error>> handleLoginAuthenticationException(
      LoginAuthenticationException e) {
    log.error("Login authentication exception", e);
    return new ResponseEntityBuilder<Error>()
        .withData(new Error(ErrorId.LOGIN_AUTHENTICATION.getId(), null, e.getMessage()))
        .withStatus(HttpStatus.UNAUTHORIZED)
        .build();
  }

  @ExceptionHandler(RegistrationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ResponseBody<Error>> handleRegistrationException(RegistrationException e) {
    log.error("Registration exception", e);
    return new ResponseEntityBuilder<Error>()
        .withSuccess(false)
        .withData(new Error(ErrorId.REGISTRATION.getId(), null, e.getMessage()))
        .withStatus(HttpStatus.BAD_REQUEST)
        .build();
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseEntity<ResponseBody<Error>> handleException(Exception e) {
    log.error("Internal server error", e);
    return new ResponseEntityBuilder<Error>()
        .withData(new Error(ErrorId.INTERNAL_SERVER_ERROR.getId(), null, e.getMessage()))
        .withMessage("Internal server error")
        .withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        .build();
  }
}
