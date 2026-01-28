package org.commonprovenance.framework.storage.controller.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class IsValueOfEnumValidator implements ConstraintValidator<IsValueOfEnum, String> {
  private Set<String> accepted;

  @Override
  public void initialize(IsValueOfEnum annotation) {
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