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
    System.err.println("*** Unhandled Exception ***");
    System.err.println("-----------------------------------------------");
    System.err.println(ex.getLocalizedMessage());
    System.err.println(ex.getMessage());
    System.err.println(ex.getClass().descriptorString());
    System.err.println(ex.getClass().getCanonicalName());
    System.err.println(ex.getClass().getName());
    System.err.println(ex.getClass().getSimpleName());
    System.err.println(ex.getClass().getTypeName());
    System.err.println("-----------------------------------------------");
    ex.printStackTrace(System.err);
    System.out.println("-----------------------------------------------");
    return ResponseEntity.internalServerError().body(new UnhandledErrorDTO());
  }
}
