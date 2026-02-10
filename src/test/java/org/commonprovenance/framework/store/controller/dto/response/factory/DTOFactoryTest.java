package org.commonprovenance.framework.store.controller.dto.response.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;

import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Format;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import reactor.test.StepVerifier;

@DisplayName("Controller - DTO Mapper (Model to Response)")
public class DTOFactoryTest {
  @Test
  @DisplayName("HappyPath - should return Mono with DocumentResponseDTO")
  void should_map_Document_to_DocumentResponseDTO() {
    String testId = "6ee9d79b-0615-4cb1-b0f3-2303d10c8cff";
    String base64StringGraph = "AAAAQQAAAGIAAAByAAAAYQAAAGsAAABhAAAAIAAAAEQAAABhAAAAYgAAAHIAAABhAAAALgAAAC4=";
    String format = "JSON";
    String signature = "..";

    Document document = new Document(
        UUID.fromString(testId),
        base64StringGraph,
        Format.from(format).get(),
        signature);

    StepVerifier.create(DTOFactory.toDTO(document))
        .assertNext(response -> {
          assertEquals(testId, response.getId(),
              "response should have Id field with exact value");
          assertEquals(base64StringGraph, response.getGraph(),
              "response should have graph field with exact value");
          assertEquals(format, response.getFormat(),
              "response should have format field with exact value");
        })
        .verifyComplete();
  }

  @Test
  @DisplayName("ErrorPath - should return Mono with exact error")
  void should_return_Mono_with_error() {

    StepVerifier.create(DTOFactory.toDTO((Document) null))
        .expectErrorSatisfies(error -> {
          assertInstanceOf(InternalApplicationException.class, error);
          assertEquals("Input parameter can not be null.", error.getMessage());
          assertInstanceOf(IllegalArgumentException.class, error.getCause());
          assertNull(error.getCause().getMessage());
        })
        .verify();
  }
}
