package org.commonprovenance.framework.storage.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Method;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@DisplayName("Provenance JSON Utils Test")
public class ProvJsonUtilsTest {
  private final String DOCUMENT = "{\"prefix\":{\"xsd\":\"http://www.w3.org/2001/XMLSchema#\",\"ex\":\"www.example.com/\",\"blank\":\"https://openprovenance.org/blank/\",\"prov\":\"http://www.w3.org/ns/prov#\"},\"bundle\":{\"ex:bundleA\":{\"entity\":{\"blank:entity1\":{\"ex:version\":2,\"ex:byteSize\":{\"$\":1034,\"type\":\"xsd:positiveInteger\"},\"ex:compression\":{\"$\":0.825,\"type\":\"xsd:double\"},\"ex:content\":{\"$\":\"Y29udGVudCBoZXJl\",\"type\":\"xsd:base64Binary\"}}},\"activity\":{\"blank:activity1\":{\"prov:startTime\":\"2025-08-16T12:00:00.000+02:00\",\"prov:endTime\":\"2025-08-16T13:00:00.000+02:00\",\"ex:host\":\"server.example.org\",\"prov:type\":{\"$\":\"ex:edit\",\"type\":\"xsd:QName\"}}},\"agent\":{\"blank:agent1\":{\"ex:employee\":1234,\"ex:name\":\"Alice\",\"prov:type\":{\"$\":\"prov:Person\",\"type\":\"xsd:QName\"}}},\"wasAssociatedWith\":{\"blank:assoc\":{\"prov:activity\":\"blank:activity1\",\"prov:agent\":\"blank:agent1\"}},\"wasAttributedTo\":{\"blank:attr\":{\"prov:entity\":\"blank:entity1\",\"prov:agent\":\"blank:agent1\"}},\"wasGeneratedBy\":{\"blank:gen\":{\"prov:entity\":\"blank:entity1\",\"prov:activity\":\"blank:activity1\"}}}}}";
  private final String DOCUMENT_WITH_EXPLICIT_BUNDLE_ID = "{\"prefix\":{\"xsd\":\"http://www.w3.org/2001/XMLSchema#\",\"ex\":\"www.example.com/\",\"blank\":\"https://openprovenance.org/blank/\",\"prov\":\"http://www.w3.org/ns/prov#\"},\"bundle\":{\"ex:bundleA\":{\"entity\":{\"blank:entity1\":{\"ex:version\":2,\"ex:byteSize\":{\"$\":1034,\"type\":\"xsd:positiveInteger\"},\"ex:compression\":{\"$\":0.825,\"type\":\"xsd:double\"},\"ex:content\":{\"$\":\"Y29udGVudCBoZXJl\",\"type\":\"xsd:base64Binary\"}}},\"activity\":{\"blank:activity1\":{\"prov:startTime\":\"2025-08-16T12:00:00.000+02:00\",\"prov:endTime\":\"2025-08-16T13:00:00.000+02:00\",\"ex:host\":\"server.example.org\",\"prov:type\":{\"$\":\"ex:edit\",\"type\":\"xsd:QName\"}}},\"agent\":{\"blank:agent1\":{\"ex:employee\":1234,\"ex:name\":\"Alice\",\"prov:type\":{\"$\":\"prov:Person\",\"type\":\"xsd:QName\"}}},\"wasAssociatedWith\":{\"blank:assoc\":{\"prov:activity\":\"blank:activity1\",\"prov:agent\":\"blank:agent1\"}},\"wasAttributedTo\":{\"blank:attr\":{\"prov:entity\":\"blank:entity1\",\"prov:agent\":\"blank:agent1\"}},\"wasGeneratedBy\":{\"blank:gen\":{\"prov:entity\":\"blank:entity1\",\"prov:activity\":\"blank:activity1\"}},\"@id\":\"ex:bundleA\"}}}";
  private final String DOCUMENT_WITH_STRINGIFY_VALUE = "{\"prefix\":{\"xsd\":\"http://www.w3.org/2001/XMLSchema#\",\"ex\":\"www.example.com/\",\"blank\":\"https://openprovenance.org/blank/\",\"prov\":\"http://www.w3.org/ns/prov#\"},\"bundle\":{\"ex:bundleA\":{\"entity\":{\"blank:entity1\":{\"ex:version\":\"2\",\"ex:byteSize\":{\"$\":\"1034\",\"type\":\"xsd:positiveInteger\"},\"ex:compression\":{\"$\":\"0.825\",\"type\":\"xsd:double\"},\"ex:content\":{\"$\":\"Y29udGVudCBoZXJl\",\"type\":\"xsd:base64Binary\"}}},\"activity\":{\"blank:activity1\":{\"prov:startTime\":\"2025-08-16T12:00:00.000+02:00\",\"prov:endTime\":\"2025-08-16T13:00:00.000+02:00\",\"ex:host\":\"server.example.org\",\"prov:type\":{\"$\":\"ex:edit\",\"type\":\"xsd:QName\"}}},\"agent\":{\"blank:agent1\":{\"ex:employee\":\"1234\",\"ex:name\":\"Alice\",\"prov:type\":{\"$\":\"prov:Person\",\"type\":\"xsd:QName\"}}},\"wasAssociatedWith\":{\"blank:assoc\":{\"prov:activity\":\"blank:activity1\",\"prov:agent\":\"blank:agent1\"}},\"wasAttributedTo\":{\"blank:attr\":{\"prov:entity\":\"blank:entity1\",\"prov:agent\":\"blank:agent1\"}},\"wasGeneratedBy\":{\"blank:gen\":{\"prov:entity\":\"blank:entity1\",\"prov:activity\":\"blank:activity1\"}}}}}";
  private final String DOCUMENT_WITH_PREFIXES = "{\"prefix\":{\"xsd\":\"http://www.w3.org/2001/XMLSchema#\",\"ex\":\"www.example.com/\",\"blank\":\"https://openprovenance.org/blank/\",\"prov\":\"http://www.w3.org/ns/prov#\"},\"bundle\":{\"ex:bundleA\":{\"entity\":{\"blank:entity1\":{\"ex:version\":2,\"ex:byteSize\":{\"$\":1034,\"type\":\"xsd:positiveInteger\"},\"ex:compression\":{\"$\":0.825,\"type\":\"xsd:double\"},\"ex:content\":{\"$\":\"Y29udGVudCBoZXJl\",\"type\":\"xsd:base64Binary\"}}},\"activity\":{\"blank:activity1\":{\"prov:startTime\":\"2025-08-16T12:00:00.000+02:00\",\"prov:endTime\":\"2025-08-16T13:00:00.000+02:00\",\"ex:host\":\"server.example.org\",\"prov:type\":{\"$\":\"ex:edit\",\"type\":\"xsd:QName\"}}},\"agent\":{\"blank:agent1\":{\"ex:employee\":1234,\"ex:name\":\"Alice\",\"prov:type\":{\"$\":\"prov:Person\",\"type\":\"xsd:QName\"}}},\"wasAssociatedWith\":{\"blank:assoc\":{\"prov:activity\":\"blank:activity1\",\"prov:agent\":\"blank:agent1\"}},\"wasAttributedTo\":{\"blank:attr\":{\"prov:entity\":\"blank:entity1\",\"prov:agent\":\"blank:agent1\"}},\"wasGeneratedBy\":{\"blank:gen\":{\"prov:entity\":\"blank:entity1\",\"prov:activity\":\"blank:activity1\"}},\"prefix\":{\"xsd\":\"http://www.w3.org/2001/XMLSchema#\",\"ex\":\"www.example.com/\",\"blank\":\"https://openprovenance.org/blank/\",\"prov\":\"http://www.w3.org/ns/prov#\"}}}}";
  private final String DOCUMENT_WITH_ARRAY_TYPE = "{\"prefix\":{\"xsd\":\"http://www.w3.org/2001/XMLSchema#\",\"ex\":\"www.example.com/\",\"blank\":\"https://openprovenance.org/blank/\",\"prov\":\"http://www.w3.org/ns/prov#\"},\"bundle\":{\"ex:bundleA\":{\"entity\":{\"blank:entity1\":{\"ex:version\":2,\"ex:byteSize\":[{\"$\":1034,\"type\":\"xsd:positiveInteger\"}],\"ex:compression\":[{\"$\":0.825,\"type\":\"xsd:double\"}],\"ex:content\":[{\"$\":\"Y29udGVudCBoZXJl\",\"type\":\"xsd:base64Binary\"}]}},\"activity\":{\"blank:activity1\":{\"prov:startTime\":\"2025-08-16T12:00:00.000+02:00\",\"prov:endTime\":\"2025-08-16T13:00:00.000+02:00\",\"ex:host\":\"server.example.org\",\"prov:type\":[{\"$\":\"ex:edit\",\"type\":\"xsd:QName\"}]}},\"agent\":{\"blank:agent1\":{\"ex:employee\":1234,\"ex:name\":\"Alice\",\"prov:type\":[{\"$\":\"prov:Person\",\"type\":\"xsd:QName\"}]}},\"wasAssociatedWith\":{\"blank:assoc\":{\"prov:activity\":\"blank:activity1\",\"prov:agent\":\"blank:agent1\"}},\"wasAttributedTo\":{\"blank:attr\":{\"prov:entity\":\"blank:entity1\",\"prov:agent\":\"blank:agent1\"}},\"wasGeneratedBy\":{\"blank:gen\":{\"prov:entity\":\"blank:entity1\",\"prov:activity\":\"blank:activity1\"}}}}}";
  private final String DOCUMENT_WITH_ARRAY_STRING = "{\"prefix\":{\"xsd\":\"http://www.w3.org/2001/XMLSchema#\",\"ex\":\"www.example.com/\",\"blank\":\"https://openprovenance.org/blank/\",\"prov\":\"http://www.w3.org/ns/prov#\"},\"bundle\":{\"ex:bundleA\":{\"entity\":{\"blank:entity1\":{\"ex:version\":2,\"ex:byteSize\":{\"$\":1034,\"type\":\"xsd:positiveInteger\"},\"ex:compression\":{\"$\":0.825,\"type\":\"xsd:double\"},\"ex:content\":{\"$\":\"Y29udGVudCBoZXJl\",\"type\":\"xsd:base64Binary\"}}},\"activity\":{\"blank:activity1\":{\"prov:startTime\":\"2025-08-16T12:00:00.000+02:00\",\"prov:endTime\":\"2025-08-16T13:00:00.000+02:00\",\"ex:host\":[\"server.example.org\"],\"prov:type\":{\"$\":\"ex:edit\",\"type\":\"xsd:QName\"}}},\"agent\":{\"blank:agent1\":{\"ex:employee\":1234,\"ex:name\":[\"Alice\"],\"prov:type\":{\"$\":\"prov:Person\",\"type\":\"xsd:QName\"}}},\"wasAssociatedWith\":{\"blank:assoc\":{\"prov:activity\":\"blank:activity1\",\"prov:agent\":\"blank:agent1\"}},\"wasAttributedTo\":{\"blank:attr\":{\"prov:entity\":\"blank:entity1\",\"prov:agent\":\"blank:agent1\"}},\"wasGeneratedBy\":{\"blank:gen\":{\"prov:entity\":\"blank:entity1\",\"prov:activity\":\"blank:activity1\"}}}}}";
  private final String DOCUMENT_PREPROCESSED = "{\"prefix\":{\"xsd\":\"http://www.w3.org/2001/XMLSchema#\",\"ex\":\"www.example.com/\",\"blank\":\"https://openprovenance.org/blank/\",\"prov\":\"http://www.w3.org/ns/prov#\"},\"bundle\":{\"ex:bundleA\":{\"entity\":{\"blank:entity1\":{\"ex:version\":\"2\",\"ex:byteSize\":[{\"$\":\"1034\",\"type\":\"xsd:positiveInteger\"}],\"ex:compression\":[{\"$\":\"0.825\",\"type\":\"xsd:double\"}],\"ex:content\":[{\"$\":\"Y29udGVudCBoZXJl\",\"type\":\"xsd:base64Binary\"}]}},\"activity\":{\"blank:activity1\":{\"prov:startTime\":\"2025-08-16T12:00:00.000+02:00\",\"prov:endTime\":\"2025-08-16T13:00:00.000+02:00\",\"ex:host\":[\"server.example.org\"],\"prov:type\":[{\"$\":\"ex:edit\",\"type\":\"xsd:QName\"}]}},\"agent\":{\"blank:agent1\":{\"ex:employee\":\"1234\",\"ex:name\":[\"Alice\"],\"prov:type\":[{\"$\":\"prov:Person\",\"type\":\"xsd:QName\"}]}},\"wasAssociatedWith\":{\"blank:assoc\":{\"prov:activity\":\"blank:activity1\",\"prov:agent\":\"blank:agent1\"}},\"wasAttributedTo\":{\"blank:attr\":{\"prov:entity\":\"blank:entity1\",\"prov:agent\":\"blank:agent1\"}},\"wasGeneratedBy\":{\"blank:gen\":{\"prov:entity\":\"blank:entity1\",\"prov:activity\":\"blank:activity1\"}},\"@id\":\"ex:bundleA\",\"prefix\":{\"xsd\":\"http://www.w3.org/2001/XMLSchema#\",\"ex\":\"www.example.com/\",\"blank\":\"https://openprovenance.org/blank/\",\"prov\":\"http://www.w3.org/ns/prov#\"}}}}";
  private final String DOCUMENT_POSTPROCESSED = "{\"prefix\":{\"xsd\":\"http://www.w3.org/2001/XMLSchema#\",\"ex\":\"www.example.com/\",\"blank\":\"https://openprovenance.org/blank/\",\"prov\":\"http://www.w3.org/ns/prov#\"},\"bundle\":{\"ex:bundleA\":{\"entity\":{\"blank:entity1\":{\"ex:version\":\"2\",\"ex:byteSize\":[{\"$\":\"1034\",\"type\":\"xsd:positiveInteger\"}],\"ex:compression\":[{\"$\":\"0.825\",\"type\":\"xsd:double\"}],\"ex:content\":[{\"$\":\"Y29udGVudCBoZXJl\",\"type\":\"xsd:base64Binary\"}]}},\"activity\":{\"blank:activity1\":{\"prov:startTime\":\"2025-08-16T12:00:00.000+02:00\",\"prov:endTime\":\"2025-08-16T13:00:00.000+02:00\",\"ex:host\":[\"server.example.org\"],\"prov:type\":[{\"$\":\"ex:edit\",\"type\":\"xsd:QName\"}]}},\"agent\":{\"blank:agent1\":{\"ex:employee\":\"1234\",\"ex:name\":[\"Alice\"],\"prov:type\":[{\"$\":\"prov:Person\",\"type\":\"xsd:QName\"}]}},\"wasAssociatedWith\":{\"blank:assoc\":{\"prov:activity\":\"blank:activity1\",\"prov:agent\":\"blank:agent1\"}},\"wasAttributedTo\":{\"blank:attr\":{\"prov:entity\":\"blank:entity1\",\"prov:agent\":\"blank:agent1\"}},\"wasGeneratedBy\":{\"blank:gen\":{\"prov:entity\":\"blank:entity1\",\"prov:activity\":\"blank:activity1\"}},\"prefix\":{\"xsd\":\"http://www.w3.org/2001/XMLSchema#\",\"ex\":\"www.example.com/\",\"blank\":\"https://openprovenance.org/blank/\",\"prov\":\"http://www.w3.org/ns/prov#\"}}}}";

  @Test
  @DisplayName("should add explicit bundle @id into bundle, if not exists")
  public void shouldAddExplicitBundleId() {
    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode input = mapper.readTree(this.DOCUMENT);
      JsonNode output = ProvJsonUtils.addExplicitBundleId(input);

      assertEquals(
          this.DOCUMENT_WITH_EXPLICIT_BUNDLE_ID,
          mapper.writeValueAsString(output),
          "should add explicit bundle @id into bundlo");
    } catch (Exception e) {
      fail(e.getMessage(), e.getCause());
    }
  }

  @Test
  @DisplayName("should NOT add explicit bundle @id into bundle, if exists")
  public void shouldNotAddExplicitBundleIdIfExists() {
    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode input = mapper.readTree(this.DOCUMENT_WITH_EXPLICIT_BUNDLE_ID);
      JsonNode output = ProvJsonUtils.addExplicitBundleId(input);

      assertEquals(
          this.DOCUMENT_WITH_EXPLICIT_BUNDLE_ID,
          mapper.writeValueAsString(output),
          "should add explicit bundle @id into bundlo");
    } catch (Exception e) {
      fail(e.getMessage(), e.getCause());
    }
  }

  @Test
  @DisplayName("should remove explicit bundle @id from bundle")
  public void shouldRemoveExplicitBundleId() {
    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode input = mapper.readTree(this.DOCUMENT_WITH_EXPLICIT_BUNDLE_ID);
      JsonNode output = ProvJsonUtils.removeExplicitBundleId(input);
      assertEquals(
          this.DOCUMENT,
          mapper.writeValueAsString(output),
          "should remove explicit bundle @id from bundle");
    } catch (Exception e) {
      fail(e.getMessage(), e.getCause());
    }
  }

  @Test
  @DisplayName("should stringify numeric value")
  public void shouldStringifyValues() {
    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode input = mapper.readTree(this.DOCUMENT);

      Method stringifyValues = ProvJsonUtils.class.getDeclaredMethod(
          "stringifyValues",
          JsonNode.class,
          ObjectMapper.class);

      stringifyValues.setAccessible(true);
      JsonNode output = (JsonNode) stringifyValues.invoke(null, input, mapper);

      assertEquals(
          this.DOCUMENT_WITH_STRINGIFY_VALUE,
          mapper.writeValueAsString(output),
          "should stringify numeric value");
    } catch (Exception e) {
      System.err.println(e.getMessage());
      fail(e.getMessage(), e.getCause());
    }
  }

  @Test
  @DisplayName("should copy outer prefixes into bundle")
  public void shouldCopyOuterPrefixesIntoBundle() {
    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode input = mapper.readTree(this.DOCUMENT);

      Method copyOuterPrefixesIntoBundles = ProvJsonUtils.class.getDeclaredMethod(
          "copyOuterPrefixesIntoBundles",
          JsonNode.class,
          ObjectMapper.class);

      copyOuterPrefixesIntoBundles.setAccessible(true);
      JsonNode output = (JsonNode) copyOuterPrefixesIntoBundles.invoke(null, input, mapper);

      assertEquals(
          this.DOCUMENT_WITH_PREFIXES,
          mapper.writeValueAsString(output),
          "should stringify numeric value");
    } catch (Exception e) {
      System.err.println(e.getMessage());
      fail(e.getMessage(), e.getCause());
    }
  }

  @Test
  @DisplayName("should put typed object into arrays")
  public void shouldPutTypedObjectsIntoArrays() {
    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode input = mapper.readTree(this.DOCUMENT);

      Method putTypedObjectsInArrays = ProvJsonUtils.class.getDeclaredMethod(
          "putTypedObjectsInArrays",
          JsonNode.class,
          ObjectMapper.class);

      putTypedObjectsInArrays.setAccessible(true);
      JsonNode output = (JsonNode) putTypedObjectsInArrays.invoke(null, input, mapper);

      assertEquals(
          this.DOCUMENT_WITH_ARRAY_TYPE,
          mapper.writeValueAsString(output),
          "should stringify numeric value");
    } catch (Exception e) {
      System.err.println(e.getMessage());
      fail(e.getMessage(), e.getCause());
    }
  }

  @Test
  @DisplayName("should put string values into arrays")
  public void shouldPutStringValuesIntoArrays() {
    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode input = mapper.readTree(this.DOCUMENT);

      Method putStringValuesInArray = ProvJsonUtils.class.getDeclaredMethod(
          "putStringValuesInArray",
          JsonNode.class,
          ObjectMapper.class,
          boolean.class);

      putStringValuesInArray.setAccessible(true);
      JsonNode output = (JsonNode) putStringValuesInArray.invoke(null, input, mapper, false);

      assertEquals(
          this.DOCUMENT_WITH_ARRAY_STRING,
          mapper.writeValueAsString(output),
          "should stringify numeric value");
    } catch (Exception e) {
      System.err.println(e.getMessage());
      fail(e.getMessage(), e.getCause());
    }
  }

  @Test
  @DisplayName("should preprocess incompatible json before serialization")
  public void shouldPreprocessIncompatibleJsonBeforeSerialization() {
    try {
      assertEquals(
          this.DOCUMENT_PREPROCESSED,
          ProvJsonUtils.preprocessIncompatibleJsonForDeserialization(this.DOCUMENT, false),
          "should stringify numeric value");
    } catch (Exception e) {
      System.err.println(e.getMessage());
      fail(e.getMessage(), e.getCause());
    }
  }

  @Test
  @DisplayName("should preprocess json before serialization")
  public void shouldPreprocessJsonBeforeSerialization() {
    try {
      assertEquals(
          this.DOCUMENT_WITH_EXPLICIT_BUNDLE_ID,
          ProvJsonUtils.preprocessJsonForDeserialization(this.DOCUMENT, false),
          "should stringify numeric value");
    } catch (Exception e) {
      System.err.println(e.getMessage());
      fail(e.getMessage(), e.getCause());
    }
  }

  @Test
  @DisplayName("should postprocess json after serialization")
  public void shouldPostprocessJsonAfterSerialization() {
    try {
      assertEquals(
          this.DOCUMENT_POSTPROCESSED,
          ProvJsonUtils.postprocessJsonAfterSerialization(this.DOCUMENT_PREPROCESSED, false),
          "should stringify numeric value");
    } catch (Exception e) {
      System.err.println(e.getMessage());
      fail(e.getMessage(), e.getCause());
    }
  }
}
