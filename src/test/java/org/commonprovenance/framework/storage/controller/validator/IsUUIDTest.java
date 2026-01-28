package org.commonprovenance.framework.storage.controller.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;

@DisplayName("Validator - IsUUID")
public class IsUUIDTest {

  private final String validUUID = "69f5fbb7-cb42-45d7-8d39-5e7c274063a0";
  private final String invalidUUID = "invalid-uuid-string";

  private class Bean {
    @IsUUID(message = "Validation failure!")
    private final String val;

    public Bean(String val) {
      this.val = val;
    }
  }

  private jakarta.validation.Validator validator = jakarta.validation.Validation
      .buildDefaultValidatorFactory()
      .getValidator();

  @Test
  @DisplayName("Validator UnitTest - should validate UUID values correctly")
  public void should_validate_uuid_values_correctly() {

    IsUUIDValidator uuidValidator = new IsUUIDValidator();
    uuidValidator.initialize(new IsUUID() {

      @Override
      public String message() {
        return "invalid UUID string";
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
        return IsUUID.class;
      }
    });

    // HappyPath
    assertTrue(
        uuidValidator.isValid(this.validUUID, null),
        "should pass if string is valid UUID string");

    // ErrorPath
    assertFalse(
        uuidValidator.isValid(this.invalidUUID, null),
        "should fail if string in not UUID string");
  }

  @Test
  @DisplayName("Validator Integration Test - HappyPath")
  public void should_pass_for_valid_value() {

    Bean beanValidUUID = new Bean(this.validUUID);

    Set<ConstraintViolation<Bean>> violations = this.validator.validate(beanValidUUID);
    assertTrue(violations.isEmpty());
  }

  @Test
  @DisplayName("Validator Integration Test - ErrorPath")
  public void should_fail_for_invalid_value() {
    Bean beanInvalidUUID = new Bean(this.invalidUUID);
    Set<ConstraintViolation<Bean>> violations = validator.validate(beanInvalidUUID);
    assertEquals(1, violations.size(), "sould have exact one violation");
    ConstraintViolation<Bean> violation = violations.iterator().next();
    assertEquals("Validation failure!", violation.getMessage(), "should have correct violation message");
  }
}
