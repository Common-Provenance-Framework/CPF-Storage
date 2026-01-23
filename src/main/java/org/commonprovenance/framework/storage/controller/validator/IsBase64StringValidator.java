package org.commonprovenance.framework.storage.controller.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Base64;
import java.util.regex.Pattern;

public class IsBase64StringValidator implements ConstraintValidator<IsBase64String, String> {
  private static final Pattern B64 = Pattern.compile("^[A-Za-z0-9+/]+={0,2}$");
  private boolean url;

  @Override
  public void initialize(IsBase64String annotation) {
    this.url = annotation.url();
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext _context) {
    if (value == null)
      return false;
    if (value.isBlank())
      return false;

    String trimmed = value.trim();
    if (trimmed.length() % 4 != 0)
      return false;
    if (!B64.matcher(trimmed).matches())
      return false;

    try {
      byte[] decoded = (url ? Base64.getUrlDecoder() : Base64.getDecoder()).decode(trimmed);
      return decoded != null;
    } catch (IllegalArgumentException ex) {
      return false;
    }
  }
}
