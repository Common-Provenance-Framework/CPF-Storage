package org.commonprovenance.framework.storage.controller.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.UUID;

import org.commonprovenance.framework.storage.controller.dto.form.DocumentFormDTO;
import org.commonprovenance.framework.storage.exceptions.InternalApplicationException;
import org.commonprovenance.framework.storage.model.Format;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import reactor.test.StepVerifier;

@DisplayName("Controller - Domain Mapper (Formular to Model)")
public class DomainMapperTest {
  @Test
  @DisplayName("HappyPath - should return Mono with Document")
  void should_map_DocumentFormDTO_to_Document() {
    String base64StringGraph = "AAAAQQAAAGIAAAByAAAAYQAAAGsAAABhAAAAIAAAAEQAAABhAAAAYgAAAHIAAABhAAAALgAAAC4=";
    String format = "JSON";

    DocumentFormDTO formular = new DocumentFormDTO(base64StringGraph, format);

    StepVerifier.create(DomainMapper.toDomain(formular))
        .assertNext(doc -> {
          assertInstanceOf(UUID.class, doc.getIdentifier(),
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
    StepVerifier.create(DomainMapper.toDomain(null))
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
}
