package org.commonprovenance.framework.store.controller.validator;

import static org.commonprovenance.framework.store.common.utils.EitherUtils.EITHER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.commonprovenance.framework.store.common.utils.Base64Utils;
import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.vavr.control.Either;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

@DisplayName("Validator - IsJsonString")
public class IsJsonBase64ValidatorTest {

  private final String json_valid = "{\"root\":{\"key_string\":\"value1\",\"key_int\":6}}";
  private final String json_invalid = "this_is_not_json";

  private void handleLeftNotExpected(ApplicationException exception) {
    fail("Left side has not been expected! " + exception.getMessage(), exception);
  }

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
    Either.<ApplicationException, String>right(this.json_valid)
        .flatMap(Base64Utils::encodeFromString)
        .peek(result -> assertTrue(jsonValidator.isValid(result, null),
            "should pass if string is valid json string"))
        .peekLeft(this::handleLeftNotExpected);
  }

  @Test
  @DisplayName("Validator UnitTest - ErrorPath")
  public void should_validate_json_error_path() {
    Either.<ApplicationException, String>right(this.json_invalid)
        .flatMap(Base64Utils::encodeFromString)
        .peek(result -> assertFalse(jsonValidator.isValid(result, null),
            "should fail if string is not valid json string"))
        .peekLeft(this::handleLeftNotExpected);
  }

  @Test
  @DisplayName("Validator Integration Test - HappyPath")
  public void should_pass_for_valid_value() {
    Either.<ApplicationException, String>right(this.json_valid)
        .flatMap(Base64Utils::encodeFromString)
        .map(encoded -> new Bean(encoded))
        .flatMap(EITHER.liftEitherChecked(this.validator::validate))
        .peek(violations -> assertTrue(violations.isEmpty()))
        .peekLeft(this::handleLeftNotExpected);
  }

  @Test
  @DisplayName("Validator Integration Test - ErrorPath")
  public void should_fail_for_invalid_value() {
    Either.<ApplicationException, String>right(this.json_invalid)
        .flatMap(Base64Utils::encodeFromString)
        .map(encoded -> new Bean(encoded))
        .flatMap(EITHER.liftEitherChecked(this.validator::validate))
        .peek(violations -> assertEquals(1, violations.size(),
            "sould have exact one violation"))
        .peek(violations -> {
          ConstraintViolation<Bean> violation = violations.iterator().next();
          assertEquals("Validation failure!", violation.getMessage(),
              "should have correct violation message");
        })
        .peekLeft(this::handleLeftNotExpected);

  }
}
