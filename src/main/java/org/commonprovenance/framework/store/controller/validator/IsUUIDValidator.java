package org.commonprovenance.framework.store.controller.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.commonprovenance.framework.store.common.utils.Validators;

public class IsUUIDValidator implements ConstraintValidator<IsUUID, String> {

  @Override
  public void initialize(IsUUID annotation) {
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext _context) {
    return Validators.isUUID(value);
  }
}
