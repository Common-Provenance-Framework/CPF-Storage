package org.commonprovenance.framework.store.controller.validator;

import java.util.Base64;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

public class IsJsonBase64Validator implements ConstraintValidator<IsJsonBase64, String> {
  @Override
  public void initialize(IsJsonBase64 annotation) {
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    try {
      new ObjectMapper().readTree(Base64.getDecoder().decode(value));
      return true;
    } catch (JacksonException e) {
      return false;
    }
  }
}
