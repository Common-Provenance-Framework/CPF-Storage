package org.commonprovenance.framework.storage.controller.advice;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import org.commonprovenance.framework.storage.controller.dto.error.BadRequestDTO;
import org.commonprovenance.framework.storage.controller.dto.error.ErrorDTO;

@Order(2)
@RestControllerAdvice
public class ValidationExceptionHandler {
    @ExceptionHandler(WebExchangeBindException.class)
  public ResponseEntity<ErrorDTO> handleBindException(WebExchangeBindException ex) {
    List<String> errors = ex.getBindingResult().getFieldErrors().stream()
        .map(e -> e.getField() + ": " + e.getDefaultMessage())
        .collect(Collectors.toList());
    return ResponseEntity.badRequest().body(buildValedationErrorResponse(errors));
  }

  private ErrorDTO buildValedationErrorResponse(List<String> details) {
    return new BadRequestDTO(details);
  }
}
