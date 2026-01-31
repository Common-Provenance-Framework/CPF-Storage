package org.commonprovenance.framework.store.controller.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.UUID;

import org.commonprovenance.framework.store.controller.dto.form.DocumentFormDTO;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.model.Format;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import reactor.test.StepVerifier;

@DisplayName("Controller - Domain Mapper (Formular to Model)")
public class DomainMapperTest {
  // Document Formular
  @Test
  @DisplayName("HappyPath - should return Mono with Document")
  void should_map_DocumentFormDTO_to_Document() {
    String base64StringGraph = "AAAAQQAAAGIAAAByAAAAYQAAAGsAAABhAAAAIAAAAEQAAABhAAAAYgAAAHIAAABhAAAALgAAAC4=";
    String format = "JSON";

    DocumentFormDTO formular = new DocumentFormDTO(base64StringGraph, format);

    StepVerifier.create(DomainMapper.toDomain(formular))
        .assertNext(doc -> {
          assertInstanceOf(UUID.class, doc.getId(),
              "should have identifier which is UUID");
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
    DocumentFormDTO doc = null;
    StepVerifier.create(DomainMapper.toDomain(doc))
        .expectErrorSatisfies(error -> {
          assertInstanceOf(InternalApplicationException.class, error);
          assertEquals("Can not convert to Document", error.getMessage());
          assertInstanceOf(IllegalArgumentException.class, error.getCause());
          assertEquals("DocumentFormDTO can not be null!", error.getCause().getMessage());
        })
        .verify();
  }

  @Test
  @DisplayName("ErrorPath - should return Mono with exact Error, if unsupported format")
  void should_terminate_with_error_if_unsupported_format() {
    String base64StringGraph = "AAAAQQAAAGIAAAByAAAAYQAAAGsAAABhAAAAIAAAAEQAAABhAAAAYgAAAHIAAABhAAAALgAAAC4=";
    String format = "unknownFormat";

    StepVerifier.create(DomainMapper.toDomain(new DocumentFormDTO(base64StringGraph, format)))
        .expectErrorSatisfies(error -> {
          assertInstanceOf(InternalApplicationException.class, error);
          assertEquals("Can not convert to Document", error.getMessage());
          assertInstanceOf(IllegalArgumentException.class, error.getCause());
          assertEquals("Unsupported format: unknownFormat", error.getCause().getMessage());
        })
        .verify();
  }

  // UUID String
  @Test
  @DisplayName("HappyPath - should return Mono with UUID")
  void should_map_String_to_UUID() {
    String uuidString = "550e8400-e29b-41d4-a716-446655440000";
    UUID expectedUUID = UUID.fromString(uuidString);

    StepVerifier.create(DomainMapper.toDomain(uuidString))
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
        DomainMapper.toDomain(uuid))
        .expectErrorSatisfies(error -> {
          assertInstanceOf(InternalApplicationException.class, error);
          assertEquals("Can not convert to UUID", error.getMessage());
          assertInstanceOf(IllegalArgumentException.class, error.getCause());
          assertEquals("UUID String can not be null!", error.getCause().getMessage());
        })
        .verify();
  }

  @Test
  @DisplayName("ErrorPath - should return Mono with exact Error, if invalid uuid String")
  void should_terminate_with_error_if_invalid_uuid() {
    String invalidUUID = "invalid_uuid";
    StepVerifier.create(DomainMapper.toDomain(invalidUUID))
        .expectErrorSatisfies(error -> {
          assertInstanceOf(InternalApplicationException.class, error);
          assertEquals("Can not convert to UUID", error.getMessage());
          assertInstanceOf(IllegalArgumentException.class, error.getCause());
          assertEquals("Not valid UUID string: " + invalidUUID, error.getCause().getMessage());
        })
        .verify();
  }
}
