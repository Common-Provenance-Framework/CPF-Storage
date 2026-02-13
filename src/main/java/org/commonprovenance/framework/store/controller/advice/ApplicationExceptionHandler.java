package org.commonprovenance.framework.store.controller.advice;

import java.util.List;

import org.commonprovenance.framework.store.controller.dto.error.BadRequestDTO;
import org.commonprovenance.framework.store.controller.dto.error.ErrorDTO;
import org.commonprovenance.framework.store.controller.dto.error.InternalServerErrorDTO;
import org.commonprovenance.framework.store.exceptions.BadRequestException;
import org.commonprovenance.framework.store.exceptions.ConflictException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(1)
@RestControllerAdvice
public class ApplicationExceptionHandler {
  @ExceptionHandler(InternalApplicationException.class)
  public ResponseEntity<ErrorDTO> handleInternalApplication(InternalApplicationException internalAppException) {
    System.err.println("*** Internal Server Error ***");
    System.out.println("-----------------------------------------------");
    System.err.println(internalAppException.getMessage());
    System.err.println(internalAppException.getCause().getMessage());
    System.out.println("-----------------------------------------------");

    return ResponseEntity.internalServerError().body(new InternalServerErrorDTO());
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ErrorDTO> handleBadRequest(BadRequestException badRequest) {
    System.err.println("*** Bad Request Error ***");
    System.out.println("-----------------------------------------------");
    System.err.println(badRequest.getMessage());
    System.out.println("-----------------------------------------------");

    return ResponseEntity.badRequest().body(new BadRequestDTO(List.of(badRequest.getMessage())));
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ErrorDTO> handleBadRequest(ConflictException badRequest) {
    System.err.println("*** Conflict Error ***");
    System.out.println("-----------------------------------------------");
    System.err.println(badRequest.getMessage());
    System.out.println("-----------------------------------------------");

    return ResponseEntity.status(HttpStatus.CONFLICT).body(new BadRequestDTO(List.of(badRequest.getMessage())));
  }
}
