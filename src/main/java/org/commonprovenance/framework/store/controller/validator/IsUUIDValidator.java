package org.commonprovenance.framework.store.controller.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.UUID;

public class IsUUIDValidator implements ConstraintValidator<IsUUID, String> {

  @Override
  public void initialize(IsUUID annotation) {
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext _context) {
    if (value == null)
      return false;

    try {
      UUID.fromString(value.trim());
      return true;
    } catch (IllegalArgumentException exception) {
      return false;
    }
  }
}
