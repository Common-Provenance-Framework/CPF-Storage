package org.commonprovenance.framework.store.controller.validator;

import org.commonprovenance.framework.store.common.validation.DataValidator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IsUUIDValidator implements ConstraintValidator<IsUUID, String> {

  @Override
  public void initialize(IsUUID annotation) {
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext _context) {
    return DataValidator.isUUID(value);
  }
}
