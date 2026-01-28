package org.commonprovenance.framework.storage.controller.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;

@DisplayName("Validator - ValueOfEnum")
public class IsValueOfEnumTest {

  enum MyEnum {
    A, B
  }

  private class Bean {
    @IsValueOfEnum(enumClass = MyEnum.class, message = "Validation failure!")
    private final String val;

    public Bean(String val) {
      this.val = val;
    }
  }

  private jakarta.validation.Validator validator = jakarta.validation.Validation
      .buildDefaultValidatorFactory()
      .getValidator();

  @Test
  @DisplayName("Validator UnitTest - should validate enum values correctly")
  public void should_validate_enum_values_correctly() {

    IsValueOfEnumValidator valOfEnumValidator = new IsValueOfEnumValidator();
    valOfEnumValidator.initialize(new IsValueOfEnum() {
      @Override
      public Class<? extends Enum<?>> enumClass() {
        return MyEnum.class;
      }

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
        return IsValueOfEnum.class;
      }
    });

    assertTrue(valOfEnumValidator.isValid("A", null), "should pass for 'A' - exact name");
    assertTrue(valOfEnumValidator.isValid("a", null), "should pass for 'a' - case-insensitive");
    assertTrue(valOfEnumValidator.isValid("  b  ", null), "should pass for '  b  ' - trimmed");
    assertFalse(valOfEnumValidator.isValid(null, null), "should fail for null");
    assertFalse(valOfEnumValidator.isValid("X", null), "should fail for 'X' - invalid name");
  }

  @Test
  @DisplayName("Validator Integration Test - HappyPath")
  public void should_pass_for_valid_value() {

    Bean beanA = new Bean("A");

    Set<ConstraintViolation<Bean>> violations = this.validator.validate(beanA);
    assertTrue(violations.isEmpty());
  }

  @Test
  @DisplayName("Validator Integration Test - ErrorPath")
  public void should_fail_for_invalid_value() {
    Bean beanUnknown = new Bean("unknown");
    Set<ConstraintViolation<Bean>> violations = validator.validate(beanUnknown);

    assertEquals(1, violations.size(), "sould have exact one violation");
    ConstraintViolation<Bean> violation = violations.iterator().next();
    assertEquals("Validation failure!", violation.getMessage(), "should have correct violation message");
  }
}
