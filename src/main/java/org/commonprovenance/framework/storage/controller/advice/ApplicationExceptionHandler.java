package org.commonprovenance.framework.storage.controller.advice;

import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.commonprovenance.framework.storage.controller.dto.error.ErrorDTO;
import org.commonprovenance.framework.storage.controller.dto.error.InternalServerErrorDTO;
import org.commonprovenance.framework.storage.exceptions.InternalApplicationException;

@Order(1)
@RestControllerAdvice
public class ApplicationExceptionHandler {
  @ExceptionHandler(InternalApplicationException.class)
  public ResponseEntity<ErrorDTO> handleInternalApplication(InternalApplicationException internalAppException) {
    System.err.println("*** Internal Server Error ***");
    System.err.println(internalAppException.getMessage());
    System.err.println(internalAppException.getCause().getMessage());

    return ResponseEntity.internalServerError().body(new InternalServerErrorDTO());
  }
}
