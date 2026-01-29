package org.commonprovenance.framework.store.controller.advice;

import org.commonprovenance.framework.store.controller.dto.error.ErrorDTO;
import org.commonprovenance.framework.store.controller.dto.error.InternalServerErrorDTO;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
