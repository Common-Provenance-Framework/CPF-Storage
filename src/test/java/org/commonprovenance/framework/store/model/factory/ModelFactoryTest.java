package org.commonprovenance.framework.store.model.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;

import org.commonprovenance.framework.store.controller.dto.form.DocumentFormDTO;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.model.Format;
import org.commonprovenance.framework.store.persistence.entity.DocumentEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import reactor.test.StepVerifier;

@DisplayName("Domain Model - ModelFactory")
public class ModelFactoryTest {
  // persistence
  @Test
  @DisplayName("HappyPath - should return Mono with Document")
  public void should_map_DocumentEntity_to_Document() {
    UUID testId = UUID.randomUUID();
    String base64StringGraph = "AAAAQQAAAGIAAAByAAAAYQAAAGsAAABhAAAAIAAAAEQAAABhAAAAYgAAAHIAAABhAAAALgAAAC4=";
    Format format = Format.JSON;

    DocumentEntity entity = new DocumentEntity(
        testId.toString(),
        base64StringGraph,
        format.toString());

    StepVerifier.create(ModelFactory.toDomain(entity))
        .assertNext(doc -> {
          assertEquals(testId, doc.getId());
          assertEquals(Format.JSON, doc.getFormat());
          assertEquals(base64StringGraph, doc.getGraph());
        })
        .verifyComplete();
  }

  @Test
  @DisplayName("ErrorPath - should return Mono with InternalApplicationException, if Id is not valid UUID string")
  void should_fail_id_InternalApplicationException() {
    String testId = "test_uuid";
    String base64StringGraph = "AAAAQQAAAGIAAAByAAAAYQAAAGsAAABhAAAAIAAAAEQAAABhAAAAYgAAAHIAAABhAAAALgAAAC4=";
    Format format = Format.JSON;

    DocumentEntity entity = new DocumentEntity(
        testId,
        base64StringGraph,
        format.toString());

    StepVerifier.create(ModelFactory.toDomain(entity))
        .expectErrorMatches(error -> error instanceof InternalApplicationException
            && error.getCause() instanceof IllegalArgumentException
            && error.getMessage().equals("Id '" + testId + "' is not valid UUID string."))
        .verify();
  }

  @Test
  @DisplayName("ErrorPath - should return Mono with InternalApplicationException, if format is not valid Format string")
  void should_fail_format_InternalApplicationException() {
    UUID testId = UUID.randomUUID();
    String base64StringGraph = "AAAAQQAAAGIAAAByAAAAYQAAAGsAAABhAAAAIAAAAEQAAABhAAAAYgAAAHIAAABhAAAALgAAAC4=";
    String format = "unknown";

    DocumentEntity entity = new DocumentEntity(
        testId.toString(),
        base64StringGraph,
        format);

    StepVerifier.create(ModelFactory.toDomain(entity))
        .expectErrorMatches(error -> error instanceof InternalApplicationException
            && error.getCause() instanceof IllegalArgumentException
            && error.getMessage().equals("Format '" + format + "' is not valid Document format."))
        .verify();
  }

  @Test
  @DisplayName("ErrorPath - should return Mono with InternalApplicationException, if any Exception")
  void should_fail_Exception_InternalApplicationException() {
    StepVerifier.create(ModelFactory.toDomain((DocumentEntity) null))
        .expectErrorMatches(error -> error instanceof InternalApplicationException
            && error.getCause() instanceof IllegalArgumentException
            && error.getCause() instanceof Exception
            && error.getMessage().equals("Input parameter can not be null."))
        .verify();
  }

  // Controller
  @Test
  @DisplayName("HappyPath - should return Mono with Document")
  void should_map_DocumentFormDTO_to_Document() {
    String base64StringGraph = "AAAAQQAAAGIAAAByAAAAYQAAAGsAAABhAAAAIAAAAEQAAABhAAAAYgAAAHIAAABhAAAALgAAAC4=";
    String format = "JSON";

    DocumentFormDTO formular = new DocumentFormDTO(base64StringGraph, format);

    StepVerifier.create(ModelFactory.toDomain(formular))
        .assertNext(doc -> {
          assertInstanceOf(UUID.class, doc.getId(),
              "should have Id which is UUID");
          assertEquals(Format.JSON, doc.getFormat(),
              "should have exact format");
          assertEquals(base64StringGraph, doc.getGraph(),
              "should have exact graph");
        })
        .verifyComplete();
  }

  @Test
  @DisplayName("ErrorPath - should return Mono with exact Error, if formular is null")
  void should_terminate_with_error_if_form_is_null() {
    StepVerifier.create(ModelFactory.toDomain((DocumentFormDTO) null))
        .expectErrorSatisfies(error -> {
          assertInstanceOf(InternalApplicationException.class, error);
          assertEquals("Input parameter can not be null.", error.getMessage());
          assertInstanceOf(IllegalArgumentException.class, error.getCause());
          assertNull(error.getCause().getMessage());
        })
        .verify();
  }

  @Test
  @DisplayName("ErrorPath - should return Mono with exact Error, if unsupported format")
  void should_terminate_with_error_if_unsupported_format() {
    String base64StringGraph = "AAAAQQAAAGIAAAByAAAAYQAAAGsAAABhAAAAIAAAAEQAAABhAAAAYgAAAHIAAABhAAAALgAAAC4=";
    String format = "unknownFormat";

    StepVerifier.create(ModelFactory.toDomain(new DocumentFormDTO(base64StringGraph, format)))
        .expectErrorSatisfies(error -> {
          assertInstanceOf(InternalApplicationException.class, error);
          assertEquals("Format '" + format + "' is not valid Document format.", error.getMessage());
          assertInstanceOf(IllegalArgumentException.class, error.getCause());
          assertNull(error.getCause().getMessage());
        })
        .verify();
  }

  // UUID String
  @Test
  @DisplayName("HappyPath - should return Mono with UUID")
  void should_map_String_to_UUID() {
    String uuidString = "550e8400-e29b-41d4-a716-446655440000";
    UUID expectedUUID = UUID.fromString(uuidString);

    StepVerifier.create(ModelFactory.toUUID(uuidString))
        .assertNext(uuid -> {
          assertInstanceOf(UUID.class, uuid,
              "should be UUID");
          assertEquals(expectedUUID, uuid,
              "should be exact uuid");
        })
        .verifyComplete();
  }

  @Test
  @DisplayName("ErrorPath - should return Mono with exact Error, if uuid String is null")
  void should_terminate_with_error_if_uuid_is_null() {
    String uuid = null;
    StepVerifier.create(
        ModelFactory.toUUID(uuid))
        .expectErrorSatisfies(error -> {
          assertInstanceOf(InternalApplicationException.class, error);
          assertEquals("DTO 'id' can not be null.", error.getMessage());
          assertInstanceOf(IllegalArgumentException.class, error.getCause());
          assertNull(error.getCause().getMessage());
        })
        .verify();
  }

  @Test
  @DisplayName("ErrorPath - should return Mono with exact Error, if invalid uuid String")
  void should_terminate_with_error_if_invalid_uuid() {
    String id = "invalid_uuid";
    StepVerifier.create(ModelFactory.toUUID(id))
        .expectErrorSatisfies(error -> {
          assertInstanceOf(InternalApplicationException.class, error);
          assertEquals("Id '" + id + "' is not valid UUID string.", error.getMessage());
          assertInstanceOf(IllegalArgumentException.class, error.getCause());
          assertNull(error.getCause().getMessage());
        })
        .verify();
  }
}
