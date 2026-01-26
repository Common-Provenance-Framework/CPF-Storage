package org.commonprovenance.framework.storage.controller.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.core.JacksonException;

import java.util.List;

import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;

public class IsProvBase64JsonValidator implements ConstraintValidator<IsProvBase64Json, String> {
  @Override
  public void initialize(IsProvBase64Json annotation) {
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode jsonNode = mapper.readTree(value);

      List<com.networknt.schema.Error> errors = SchemaRegistry
          .withDefaultDialect(SpecificationVersion.DRAFT_2020_12)
          .getSchema(getClass().getResourceAsStream("/prov-json-schema.json"))
          .validate(jsonNode);

      if (errors.isEmpty()) {
        return true;
      } else {
        String errorMessage = errors.stream()
            .map(error -> error.getMessage())
            .reduce("", (acc, msg) -> acc.isBlank()
                ? msg
                : acc + "; " + msg);

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("JSON validation error: " + errorMessage)
            .addConstraintViolation();

        return false;
      }
    } catch (JacksonException e) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate("JSON parsing error: " + e.getMessage())
          .addConstraintViolation();

      return false;
    } catch (Exception e) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate("Unknown exception in IsProvBase64JsonValidator: " + e.getMessage())
          .addConstraintViolation();

      return false;
    }
  }

}
