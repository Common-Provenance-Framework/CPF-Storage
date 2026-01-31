package org.commonprovenance.framework.store.model.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;

import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.model.Format;
import org.commonprovenance.framework.store.persistence.neo4j.entity.DocumentEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import reactor.test.StepVerifier;

@DisplayName("Neo4j Repository - ModelFactory")
public class ModelFactoryTest {

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
  @DisplayName("ErrorPath - should return Mono with InternalApplicationException, if identifier is not valid UUID string")
  void should_fail_identifier_InternalApplicationException() {
    String testId = "identifier";
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
}
