package org.commonprovenance.framework.storage.controller.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;

@DisplayName("Validator - ValueOfProvJsonSchemaValidatorTest")
public class IsProvBase64JsonValidatorTest {
  private final String json_valid = "{\"prefix\":{\"xsd\":\"http://www.w3.org/2001/XMLSchema#\",\"default\":\"https://www.default.com/\",\"ex\":\"https://www.example.com/\",\"prov\":\"http://www.w3.org/ns/prov#\"},\"bundle\":{\"ex:bundleA\":{\"prefix\":{\"ex\":\"https://www.example.com/\"},\"entity\":{\"entity1\":{\"prov:value\":[{\"$\":\"42\",\"type\":\"xsd:int\"}],\"prov:label\":[{\"$\":\"Entity Label\",\"lang\":\"en\"}],\"prov:location\":[\"Entity Location\"],\"prov:type\":[\"Document\"],\"ex:version\":[{\"$\":\"2\",\"type\":\"xsd:int\"}],\"ex:byteSize\":[{\"$\":\"1034\",\"type\":\"xsd:positiveInteger\"}],\"ex:compression\":[{\"$\":\"0.825\",\"type\":\"xsd:double\"}],\"ex:content\":[{\"$\":\"Y29udGVudCBoZXJl\",\"type\":\"xsd:base64Binary\"}]}},\"activity\":{\"ex:activity1\":{\"prov:startTime\":\"2025-08-16T12:00:00.000+02:00\",\"prov:endTime\":\"2025-08-16T13:00:00.000+02:00\",\"prov:type\":[{\"$\":\"ex:edit\",\"type\":\"xsd:QName\"}],\"ex:host\":[\"server.example.org\"]}},\"agent\":{\"ex:agent1\":{\"ex:employee\":[{\"$\":\"1234\",\"type\":\"xsd:int\"}],\"ex:name\":[\"Alice\"],\"prov:type\":[{\"$\":\"prov:Person\",\"type\":\"xsd:QName\"}]}},\"wasAssociatedWith\":{\"_:n1\":{\"prov:activity\":\"ex:activity1\",\"prov:agent\":\"ex:agent1\",\"prov:role\":[\"editor\"],\"prov:plan\":\"ex:rec-advance\"}},\"wasAttributedTo\":{\"_:n0\":{\"prov:entity\":\"entity1\",\"prov:agent\":\"ex:agent1\"}},\"wasGeneratedBy\":{\"_:n2\":{\"prov:entity\":\"entity1\",\"prov:activity\":\"ex:activity1\",\"prov:time\":\"2025-08-16T13:00:00.000+02:00\"}}}}}";
  private final String json_invalid = "{\"prefix\":{\"ex\":\"http://example.org\",\"w3\":\"http://www.w3.org/\",\"tr\":\"http://www.w3.org/TR/2011/\"},\"bundle\":{\"ex:bundle1\":{\"@id\":\"ex:bundle1\",\"entity\":{\"tr:WD-prov-dm-20111215\":{\"prov:type\":\"document\",\"ex:version\":\"2\"}},\"activity\":{\"ex:edit1\":{\"prov:type\":\"edit\"}},\"wasGeneratedBy\":{\"_:wGB1\":{\"prov:activity\":\"ex:edit1\",\"prov:entity\":\"tr:WD-prov-dm-20111215\"}},\"agent\":{\"ex:Paolo\":{\"prov:type\":{\"$\":\"prov:Person\",\"type\":\"xsd:QName\"}},\"ex:Simon\":{\"prov:type\":{\"$\":\"prov:Person\",\"type\":\"xsd:QName\"}}},\"wasAssociatedWith\":{\"_:wAW1\":{\"prov:activity\":\"ex:edit1\",\"prov:agent\":\"ex:Paolo\",\"prov:role\":\"editor\"},\"_:wAW2\":{\"prov:activity\":\"ex:edit1\",\"prov:agent\":\"ex:Simon\",\"prov:role\":\"contributor\"}}}}}";

  private class Bean {
    @IsProvBase64Json(message = "Validation failure!")
    private final String val;

    public Bean(String val) {
      this.val = val;
    }
  }

  private jakarta.validation.Validator validator = jakarta.validation.Validation
      .buildDefaultValidatorFactory()
      .getValidator();

  @Test
  @DisplayName("Validator UnitTest - should validate json values correctly")
  public void should_validate_json_values_correctly() {

    IsProvBase64JsonValidator jsonValidator = new IsProvBase64JsonValidator();
    ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
    ConstraintValidatorContext.ConstraintViolationBuilder builder = mock(
        ConstraintValidatorContext.ConstraintViolationBuilder.class);

    when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);

    // HappyPath
    assertTrue(jsonValidator.isValid(this.json_valid, context),
        "should pass if string is valid common provenance json");

    // Error Path
    assertFalse(jsonValidator.isValid(this.json_invalid, context),
        "should fail if string is nod valid common provenance json");
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
    assertEquals(
        "JSON validation error: string found, array expected; string found, array expected; string found, array expected; object found, array expected; object found, array expected; string found, array expected; string found, array expected; property '@id' is not defined in the schema and the schema does not allow additional properties",
        violation.getMessage(),
        "should have correct violation message");
  }
}
