package org.commonprovenance.framework.store.controller.advice;

import java.util.List;

import org.commonprovenance.framework.store.controller.advice.utils.AdviceUtils;
import org.commonprovenance.framework.store.controller.dto.error.BadRequestDTO;
import org.commonprovenance.framework.store.controller.dto.error.ErrorDTO;
import org.commonprovenance.framework.store.controller.dto.error.InternalServerErrorDTO;
import org.commonprovenance.framework.store.controller.dto.error.NotFoundDTO;
import org.commonprovenance.framework.store.exceptions.BadRequestException;
import org.commonprovenance.framework.store.exceptions.ConflictException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(1)
@RestControllerAdvice
public class ApplicationExceptionHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationExceptionHandler.class);

  @ExceptionHandler(InternalApplicationException.class)
  public ResponseEntity<ErrorDTO> handleInternalApplication(InternalApplicationException internalAppException) {
    LOGGER.error(AdviceUtils.buildMessage(internalAppException, "*** Internal Server Error ***"));
    return ResponseEntity.internalServerError().body(new InternalServerErrorDTO());
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ErrorDTO> handleBadRequest(BadRequestException badRequest) {
    LOGGER.warn("Bad Request: {}", AdviceUtils.buildMessage(badRequest));
    return ResponseEntity.badRequest().body(new BadRequestDTO(List.of(badRequest.getMessage())));
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ErrorDTO> handleBadRequest(ConflictException conflict) {
    LOGGER.warn("Conflict: {}", AdviceUtils.buildMessage(conflict));
    return ResponseEntity.status(HttpStatus.CONFLICT).body(new BadRequestDTO(List.of(conflict.getMessage())));
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ErrorDTO> handleBadRequest(NotFoundException notFound) {
    LOGGER.warn("Not Found: {}", AdviceUtils.buildMessage(notFound));
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new NotFoundDTO(notFound.getMessage()));
  }
}
