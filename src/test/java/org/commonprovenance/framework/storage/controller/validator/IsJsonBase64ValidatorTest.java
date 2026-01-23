package org.commonprovenance.framework.storage.controller.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.commonprovenance.framework.storage.common.utils.Base64Utils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

@DisplayName("Validator - IsJsonString")
public class IsJsonBase64ValidatorTest {

  private final String json_valid = Base64Utils
      .encodeFromString("{\"root\":{\"key_string\":\"value1\",\"key_int\":6}}");
  private final String json_invalid = Base64Utils.encodeFromString("this_is_not_json");

  private class Bean {
    @IsJsonBase64(message = "Validation failure!")
    private final String val;

    public Bean(String val) {
      this.val = val;
    }
  }

  private final IsJsonBase64Validator jsonValidator = new IsJsonBase64Validator();
  private final Validator validator = Validation
      .buildDefaultValidatorFactory()
      .getValidator();

  @Test
  @DisplayName("Validator UnitTest - HappyPath")
  public void should_validate_json_happy_path() {

    assertTrue(jsonValidator.isValid(json_valid, null),
        "should pass if string is valid json string");
  }

  @Test
  @DisplayName("Validator UnitTest - ErrorPath")
  public void should_validate_json_error_path() {
    assertFalse(jsonValidator.isValid(json_invalid, null),
        "should fail if string is not valid json string");
  }

  @Test
  @DisplayName("Validator Integration Test - HappyPath")
  public void should_pass_for_valid_value() {

    Bean beanValidUUID = new Bean(this.json_valid);

    Set<ConstraintViolation<Bean>> violations = this.validator.validate(beanValidUUID);

    assertTrue(violations.isEmpty());
  }

  @Test
  @DisplayName("Validator Integration Test - ErrorPath")
  public void should_fail_for_invalid_value() {
    Bean beanInvalidUUID = new Bean(this.json_invalid);
    Set<ConstraintViolation<Bean>> violations = validator.validate(beanInvalidUUID);

    assertEquals(1, violations.size(), "sould have exact one violation");
    ConstraintViolation<Bean> violation = violations.iterator().next();
    assertEquals("Validation failure!", violation.getMessage(),
        "should have correct violation message");
  }
}
