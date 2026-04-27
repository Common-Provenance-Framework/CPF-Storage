package org.commonprovenance.framework.store.controller.advice;

import org.commonprovenance.framework.store.controller.advice.utils.AdviceUtils;
import org.commonprovenance.framework.store.controller.dto.error.ErrorDTO;
import org.commonprovenance.framework.store.controller.dto.error.UnhandledErrorDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(3)
@RestControllerAdvice
public class UnhandledExceptionHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(UnhandledExceptionHandler.class);

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorDTO> handleRest(Exception ex) {
    LOGGER.error(AdviceUtils.buildMessage(ex, "\"*** Unhandled Exception ***\""), ex);
    return ResponseEntity.internalServerError().body(new UnhandledErrorDTO());
  }
}
