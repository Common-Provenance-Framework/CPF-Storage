package org.commonprovenance.framework.storage.controller.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;

@DisplayName("Validator - ValueOfBase64StringTest")
public class ValueOfBase64StringTest {
  private final String base64Valid = "U29tZSBiYXNlNjQgZW5jb2RlZCBzdHJpbmc=";
  private final String base64Invalid = "This is not base64!!";

  private class Bean {
    @ValueOfBase64String(message = "Validation failure!")
    private final String val;

    public Bean(String val) {
      this.val = val;
    }
  }

  private jakarta.validation.Validator validator = jakarta.validation.Validation
      .buildDefaultValidatorFactory()
      .getValidator();

  @Test
  @DisplayName("Validator Test - should validate base64 strings correctly")
  public void should_validate_base64_strings_correctly() {

    ValueOfBase64StringValidator valOfBase64StringValidator = new ValueOfBase64StringValidator();
    valOfBase64StringValidator.initialize(new ValueOfBase64String() {
      @Override
      public String message() {
        return "msg";
      }

      @Override
      public Class<?>[] groups() {
        return new Class[0]; // returns an empty array of classes (no groups)
      }

      @Override
      public Class<? extends jakarta.validation.Payload>[] payload() {
        return new Class[0]; // returns an empty array of classes (no payload)
      }

      @Override
      public Class<? extends java.lang.annotation.Annotation> annotationType() {
        return ValueOfBase64String.class;
      }

      @Override
      public boolean url() {
        return false;
      }
    });

    // HappyPath
    assertTrue(
        valOfBase64StringValidator.isValid(this.base64Valid, null),
        "should pass if string is base64 string");

    // ErrorPath
    assertFalse(
        valOfBase64StringValidator.isValid(this.base64Invalid, null),
        "should fail if string in not base64 string");
  }

  @Test
  @DisplayName("Validator Integration Test - HappyPath")
  public void should_pass_for_valid_value() {

    Bean beanValid = new Bean(this.base64Valid);

    Set<ConstraintViolation<Bean>> violations = this.validator.validate(beanValid);
    assertTrue(violations.isEmpty());
  }

  @Test
  @DisplayName("Validator Integration Test - ErrorPath")
  public void should_fail_for_invalid_value() {
    Bean beanInvalid = new Bean(this.base64Invalid);
    Set<ConstraintViolation<Bean>> violations = validator.validate(beanInvalid);

    assertEquals(1, violations.size(), "sould have exact one violation");
    ConstraintViolation<Bean> violation = violations.iterator().next();
    assertEquals("Validation failure!", violation.getMessage(), "should have correct violation message");
  }
}
