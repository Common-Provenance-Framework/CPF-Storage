package org.commonprovenance.framework.store.controller.advice;

import org.commonprovenance.framework.store.controller.dto.error.ErrorDTO;
import org.commonprovenance.framework.store.controller.dto.error.UnhandledErrorDTO;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(3)
@RestControllerAdvice
public class UnhandledExceptionHandler {
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorDTO> handleRest(Exception ex) {
    System.out.println(ex.getLocalizedMessage());
    System.out.println(ex.getMessage());
    System.out.println(ex.getClass().descriptorString());
    System.out.println(ex.getClass().getCanonicalName());
    System.out.println(ex.getClass().getName());
    System.out.println(ex.getClass().getSimpleName());
    System.out.println(ex.getClass().getTypeName());
    return ResponseEntity.internalServerError().body(new UnhandledErrorDTO());
  }
}
