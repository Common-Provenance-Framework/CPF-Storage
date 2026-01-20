package org.commonprovenance.framework.storage.controller.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class ValueOfEnumValidator implements ConstraintValidator<ValueOfEnum, String> {
  private Set<String> accepted;

  @Override
  public void initialize(ValueOfEnum annotation) {
    accepted = Arrays.stream(annotation.enumClass().getEnumConstants())
        .map(Enum::name)
        .collect(Collectors.toSet());
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext _context) {
    if (value == null)
      return false;
    return accepted.contains(value.toUpperCase().trim());
  }
}